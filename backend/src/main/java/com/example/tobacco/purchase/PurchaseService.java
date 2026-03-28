package com.example.tobacco.purchase;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.message.MessageService;
import com.example.tobacco.model.CreatePurchaseRequest;
import com.example.tobacco.model.PurchaseOrderItem;
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
public class PurchaseService {
    private static final String PURCHASE_DETAIL_SQL =
            "select p.id, p.order_no as orderNo, p.supplier_id as supplierId, s.name as supplierName, " +
            "p.product_id as productId, pr.name as productName, p.quantity, p.unit_price as unitPrice, " +
            "p.total_amount as totalAmount, p.status, p.created_by as createdBy, " +
            "DATE_FORMAT(p.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, " +
            "IFNULL(DATE_FORMAT(p.received_at,'%Y-%m-%d %H:%i:%s'),'') as receivedAt, " +
            "IFNULL(DATE_FORMAT(p.inbound_at,'%Y-%m-%d %H:%i:%s'),'') as inboundAt, " +
            "IFNULL(p.audited_by,'') as auditedBy, " +
            "IFNULL(DATE_FORMAT(p.audited_at,'%Y-%m-%d %H:%i:%s'),'') as auditedAt, " +
            "IFNULL(p.audit_remark,'') as auditRemark, " +
            "IFNULL(p.cancel_reason,'') as cancelReason " +
            "from purchase_orders p " +
            "left join suppliers s on p.supplier_id=s.id " +
            "left join products pr on p.product_id=pr.id";

    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;

    public PurchaseService(JdbcTemplate jdbcTemplate, AuditService auditService,
                           MessageService messageService, ExcelUtil excelUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<PurchaseOrderItem> list() {
        return jdbcTemplate.query(
                PURCHASE_DETAIL_SQL + " order by p.id desc",
                new BeanPropertyRowMapper<PurchaseOrderItem>(PurchaseOrderItem.class));
    }

    @Transactional
    public PurchaseOrderItem create(CreatePurchaseRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity()));
        String orderNo = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update(
                "insert into purchase_orders(order_no,supplier_id,product_id,quantity,unit_price,total_amount,status,created_by) values(?,?,?,?,?,?,?,?)",
                orderNo, request.getSupplierId(), request.getProductId(),
                request.getQuantity(), request.getUnitPrice(), total, "CREATED", username);
        Long id = jdbcTemplate.queryForObject("select id from purchase_orders where order_no=?", Long.class, orderNo);
        auditService.trace("PURCHASE", id, orderNo, "CREATE", "创建采购单", username, "创建采购单");
        auditService.logOperation(findUserIdByUsername(username), username, "PURCHASE", "CREATE", "PURCHASE", id, "创建采购单" + orderNo);
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem audit(Long id, String decision, String remark, String operator, String roleCode) {
        requireApprovalRole(roleCode, "采购审核");
        PurchaseOrderItem item = detail(id);
        if (!"CREATED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核状态的采购单可审核");
        }
        String newStatus;
        if ("APPROVED".equalsIgnoreCase(decision)) {
            newStatus = "APPROVED";
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            newStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("审核决定必须为 APPROVED 或 REJECTED");
        }
        jdbcTemplate.update("update purchase_orders set status=?, audited_by=?, audited_at=now(), audit_remark=? where id=?",
                newStatus, operator, remark, id);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "AUDIT", "审核采购单", operator, newStatus + ": " + (remark == null ? "" : remark));
        auditService.logOperation(findUserIdByUsername(operator), operator, "PURCHASE", "AUDIT", "PURCHASE", id, "审核采购单:" + newStatus);
        // 推送审核结果消息给创建者
        Long creatorId = findUserIdByUsername(item.getCreatedBy());
        if (creatorId != null) {
            String title = "APPROVED".equals(newStatus)
                    ? "采购单 " + item.getOrderNo() + " 审核通过"
                    : "采购单 " + item.getOrderNo() + " 被驳回: " + (remark == null ? "" : remark);
            messageService.createMessage(creatorId, title, title, "NOTICE", "PURCHASE", id);
        }
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem cancel(Long id, String reason, String operatorName) {
        PurchaseOrderItem item = detail(id);
        if (!"CREATED".equals(item.getStatus()) && !"REJECTED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核或已驳回状态的采购单可取消");
        }
        jdbcTemplate.update("update purchase_orders set status='CANCELLED', cancel_reason=? where id=?", reason, id);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "CANCEL", "取消采购单", operatorName, reason == null ? "" : reason);
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "CANCEL", "PURCHASE", id, "取消采购单");
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem receive(Long id, String operatorName, String roleCode) {
        requireApprovalRole(roleCode, "采购到货确认");
        PurchaseOrderItem item = detail(id);
        if ("INBOUND".equals(item.getStatus()) || "RECEIVED".equals(item.getStatus())) {
            return item;
        }
        if (!"APPROVED".equals(item.getStatus())) {
            throw new IllegalArgumentException("采购单需审核通过后才能登记到货");
        }
        jdbcTemplate.update("update purchase_orders set status='RECEIVED', received_at=now() where id=? and status='APPROVED'", id);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "RECEIVE", "到货登记", operatorName, "到货登记");
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "RECEIVE", "PURCHASE", id, "采购到货登记");
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem inbound(Long id, String operatorName, String roleCode) {
        requireApprovalRole(roleCode, "采购入库确认");
        PurchaseOrderItem item = detail(id);
        if ("INBOUND".equals(item.getStatus())) {
            return item;
        }
        if (!"RECEIVED".equals(item.getStatus())) {
            throw new IllegalArgumentException("采购单需先登记到货后才能入库");
        }
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=?", Integer.class, item.getProductId());
        if (beforeQty == null) {
            beforeQty = 0;
        }
        int afterQty = beforeQty + item.getQuantity();
        int count = jdbcTemplate.queryForObject("select count(1) from inventories where product_id=?", Integer.class, item.getProductId());
        if (count > 0) {
            jdbcTemplate.update(
                    "update inventories set quantity=?, warning_threshold=(select warning_threshold from products where id=?), warehouse_name='中心仓' where product_id=?",
                    afterQty, item.getProductId(), item.getProductId());
        } else {
            jdbcTemplate.update(
                    "insert into inventories(product_id,warehouse_name,quantity,warning_threshold) values(?,?,?,(select warning_threshold from products where id=?))",
                    item.getProductId(), "中心仓", afterQty, item.getProductId());
        }
        jdbcTemplate.update("update purchase_orders set status='INBOUND', inbound_at=now() where id=?", id);
        jdbcTemplate.update(
                "insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)",
                item.getProductId(), "PURCHASE_INBOUND", id, item.getQuantity(), beforeQty, afterQty, operatorName, "采购单入库");
        auditService.trace("PURCHASE", id, item.getOrderNo(), "INBOUND", "采购入库", operatorName, "入库数量: " + item.getQuantity());
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "INBOUND", "PURCHASE", id, "采购入库，数量:" + item.getQuantity());
        // 库存预警检查
        checkAndNotifyWarning(item.getProductId(), afterQty);
        return detail(id);
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
                String supplierName = excelUtil.require(row, "供应商名称");
                String productCode = excelUtil.require(row, "商品编码");
                Integer quantity = excelUtil.toInteger(row, "数量");
                BigDecimal unitPrice = excelUtil.toBigDecimal(row, "单价");
                Long supplierId = jdbcTemplate.queryForObject("select id from suppliers where name=? and status='ENABLED'", Long.class, supplierName);
                Long productId = jdbcTemplate.queryForObject("select id from products where code=? and status='ENABLED'", Long.class, productCode);
                CreatePurchaseRequest request = new CreatePurchaseRequest();
                request.setSupplierId(supplierId);
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

    private void requireApprovalRole(String roleCode, String actionName) {
        if (!isApprovalRole(roleCode)) {
            throw new IllegalArgumentException(actionName + "仅允许超级管理员、普通管理员或库管执行");
        }
    }

    private boolean isApprovalRole(String roleCode) {
        return "SUPER_ADMIN".equalsIgnoreCase(roleCode)
                || "ADMIN".equalsIgnoreCase(roleCode)
                || "KEEPER".equalsIgnoreCase(roleCode);
    }

    private PurchaseOrderItem detail(Long id) {
        return jdbcTemplate.queryForObject(
                PURCHASE_DETAIL_SQL + " where p.id=?",
                new BeanPropertyRowMapper<PurchaseOrderItem>(PurchaseOrderItem.class), id);
    }
}
