package com.example.tobacco.sales;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.message.MessageService;
import com.example.tobacco.model.CreateSalesRequest;
import com.example.tobacco.model.PaymentRequest;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.SalesOrderItem;
import com.example.tobacco.util.ExcelUtil;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesService {
    private static final String SALES_DETAIL_SQL =
            "select s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, " +
            "s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, " +
            "s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, " +
            "s.created_by as createdBy, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, " +
            "IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt, " +
            "IFNULL(s.audited_by,'') as auditedBy, " +
            "IFNULL(DATE_FORMAT(s.audited_at,'%Y-%m-%d %H:%i:%s'),'') as auditedAt, " +
            "IFNULL(s.audit_remark,'') as auditRemark, " +
            "IFNULL(s.cancel_reason,'') as cancelReason " +
            "from sales_orders s " +
            "left join customers c on s.customer_id=c.id " +
            "left join products p on s.product_id=p.id";

    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;

    public SalesService(JdbcTemplate jdbcTemplate, AuditService auditService,
                        MessageService messageService, ExcelUtil excelUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<SalesOrderItem> list(String username, String roleCode) {
        if (isSeller(roleCode)) {
            return jdbcTemplate.query(SALES_DETAIL_SQL + " where s.created_by=? order by s.id desc",
                    new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class), username);
        }
        return jdbcTemplate.query(SALES_DETAIL_SQL + " order by s.id desc",
                new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class));
    }

    @Transactional
    public SalesOrderItem create(CreateSalesRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity()));
        String orderNo = "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update("insert into sales_orders(order_no,customer_id,product_id,quantity,unit_price,total_amount,paid_amount,status,created_by) values(?,?,?,?,?,?,?,?,?)",
                orderNo, request.getCustomerId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, BigDecimal.ZERO, "CREATED", username);
        Long id = jdbcTemplate.queryForObject("select id from sales_orders where order_no=?", Long.class, orderNo);
        auditService.trace("SALES", id, orderNo, "CREATE", "创建销售单", username, "创建销售单");
        return detail(id);
    }

    @Transactional
    public SalesOrderItem audit(Long id, String decision, String remark, String operator) {
        SalesOrderItem item = detail(id);
        if (!"CREATED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核状态的销售单可审核");
        }
        String newStatus;
        if ("APPROVED".equalsIgnoreCase(decision)) {
            newStatus = "APPROVED";
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            newStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("审核决定必须为 APPROVED 或 REJECTED");
        }
        jdbcTemplate.update("update sales_orders set status=?, audited_by=?, audited_at=now(), audit_remark=? where id=?",
                newStatus, operator, remark, id);
        auditService.trace("SALES", id, item.getOrderNo(), "AUDIT", "审核销售单", operator, newStatus + ": " + (remark == null ? "" : remark));
        Long creatorId = findUserIdByUsername(item.getCreatedBy());
        if (creatorId != null) {
            String title = "APPROVED".equals(newStatus)
                    ? "销售单 " + item.getOrderNo() + " 审核通过"
                    : "销售单 " + item.getOrderNo() + " 被驳回: " + (remark == null ? "" : remark);
            messageService.createMessage(creatorId, title, title, "NOTICE", "SALES", id);
        }
        return detail(id);
    }

    @Transactional
    public SalesOrderItem cancel(Long id, String reason, String username, String roleCode) {
        SalesOrderItem item = detail(id);
        validateOwnership(item, username, roleCode);
        if (!"CREATED".equals(item.getStatus()) && !"REJECTED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核或已驳回状态的销售单可取消");
        }
        jdbcTemplate.update("update sales_orders set status='CANCELLED', cancel_reason=? where id=?", reason, id);
        auditService.trace("SALES", id, item.getOrderNo(), "CANCEL", "取消销售单", username, reason == null ? "" : reason);
        return detail(id);
    }

    @Transactional
    public SalesOrderItem outbound(Long id, String username, String roleCode) {
        SalesOrderItem item = detail(id);
        validateOwnership(item, username, roleCode);
        if (!"APPROVED".equals(item.getStatus())) {
            if ("OUTBOUND".equals(item.getStatus()) || "PARTIAL_PAID".equals(item.getStatus()) || "PAID".equals(item.getStatus())) {
                return item;
            }
            throw new IllegalArgumentException("销售单需审核通过后才能出库");
        }
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=?", Integer.class, item.getProductId());
        if (beforeQty == null || beforeQty < item.getQuantity()) throw new IllegalArgumentException("库存不足，无法出库");
        int afterQty = beforeQty - item.getQuantity();
        jdbcTemplate.update("update inventories set quantity=? where product_id=?", afterQty, item.getProductId());
        jdbcTemplate.update("update sales_orders set status='OUTBOUND', outbound_at=now() where id=?", id);
        jdbcTemplate.update("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)",
                item.getProductId(), "SALES_OUTBOUND", id, -item.getQuantity(), beforeQty, afterQty, username, "销售出库");
        auditService.trace("SALES", id, item.getOrderNo(), "OUTBOUND", "销售出库", username, "出库数量: " + item.getQuantity());
        checkAndNotifyWarning(item.getProductId(), afterQty);
        return detail(id);
    }

    @Transactional
    public SalesOrderItem payment(Long id, PaymentRequest request, String username, String roleCode) {
        SalesOrderItem item = detail(id);
        validateOwnership(item, username, roleCode);
        BigDecimal paid = item.getPaidAmount().add(request.getAmount());
        String status = paid.compareTo(item.getTotalAmount()) >= 0 ? "PAID" : ("OUTBOUND".equals(item.getStatus()) ? "PARTIAL_PAID" : item.getStatus());
        jdbcTemplate.update("update sales_orders set paid_amount=?, status=? where id=?", paid, status, id);
        jdbcTemplate.update("insert into payment_records(sales_order_id, amount, payer_name, remark) values(?,?,?,?)", id, request.getAmount(), request.getPayerName(), request.getRemark());
        auditService.trace("SALES", id, item.getOrderNo(), "PAYMENT", "销售回款", username, "回款金额: " + request.getAmount());
        return detail(id);
    }

    public ReportSummaryItem statistics(String username, String roleCode) {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("销售统计");
        if (isSeller(roleCode)) {
            item.setCount(jdbcTemplate.queryForObject("select count(1) from sales_orders where created_by=?", Integer.class, username));
            item.setAmount(jdbcTemplate.queryForObject("select ifnull(sum(total_amount),0) from sales_orders where created_by=?", BigDecimal.class, username));
            return item;
        }
        item.setCount(jdbcTemplate.queryForObject("select count(1) from sales_orders", Integer.class));
        item.setAmount(jdbcTemplate.queryForObject("select ifnull(sum(total_amount),0) from sales_orders", BigDecimal.class));
        return item;
    }

    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file, String username) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        List<Map<String, String>> rows = excelUtil.importWorkbook(file);
        if (rows.size() > 1000) {
            throw new IllegalArgumentException("单次导入不能超过1000行");
        }
        int success = 0;
        int failed = 0;
        StringBuilder errors = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            try {
                Map<String, String> row = rows.get(i);
                String customerName = excelUtil.require(row, "客户名称");
                String productCode = excelUtil.require(row, "商品编码");
                Integer quantity = excelUtil.toInteger(row, "数量");
                BigDecimal unitPrice = excelUtil.toBigDecimal(row, "单价");
                Long customerId = jdbcTemplate.queryForObject("select id from customers where name=? and status='ENABLED'", Long.class, customerName);
                Long productId = jdbcTemplate.queryForObject("select id from products where code=? and status='ENABLED'", Long.class, productCode);
                CreateSalesRequest request = new CreateSalesRequest();
                request.setCustomerId(customerId);
                request.setProductId(productId);
                request.setQuantity(quantity);
                request.setUnitPrice(unitPrice);
                create(request, username);
                success++;
            } catch (Exception e) {
                failed++;
                errors.append("第").append(i + 2).append("行: ").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors.toString());
        return result;
    }

    private void checkAndNotifyWarning(Long productId, int currentQty) {
        try {
            Integer threshold = jdbcTemplate.queryForObject("select warning_threshold from inventories where product_id=?", Integer.class, productId);
            if (threshold != null && currentQty <= threshold) {
                String productName = jdbcTemplate.queryForObject("select name from products where id=?", String.class, productId);
                String title = "库存预警: " + productName + " 当前库存" + currentQty + "，低于阈值" + threshold;
                List<Long> keeperIds = jdbcTemplate.queryForList("select id from users where role_code='KEEPER' and status='ENABLED'", Long.class);
                for (Long keeperId : keeperIds) {
                    messageService.createMessage(keeperId, title, title, "ALERT", "INVENTORY", productId);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private Long findUserIdByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject("select id from users where username=?", Long.class, username);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSeller(String roleCode) {
        return "SELLER".equalsIgnoreCase(roleCode);
    }

    private void validateOwnership(SalesOrderItem item, String username, String roleCode) {
        if (isSeller(roleCode) && !String.valueOf(item.getCreatedBy()).equals(username)) {
            throw new IllegalArgumentException("仅允许操作本人创建的销售订单");
        }
    }

    private SalesOrderItem detail(Long id) {
        return jdbcTemplate.queryForObject(SALES_DETAIL_SQL + " where s.id=?",
                new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class), id);
    }
}
