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
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;

    public SalesService(JdbcTemplate jdbcTemplate, AuditService auditService, MessageService messageService, ExcelUtil excelUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<SalesOrderItem> list() {
        return jdbcTemplate.query("select s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, s.created_by as createdBy, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt from sales_orders s left join customers c on s.customer_id=c.id left join products p on s.product_id=p.id order by s.id desc",
                new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class));
    }

    public List<Map<String, Object>> publishings() {
        return jdbcTemplate.queryForList("select id, publish_no as publishNo, title, content, product_id as productId, price, status, created_by as createdBy, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from sales_publishings order by id desc");
    }

    public void createPublishing(Map<String, String> request, String username) {
        String publishNo = "SP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update("insert into sales_publishings(publish_no,title,content,product_id,price,status,created_by) values(?,?,?,?,?,?,?)",
                publishNo, request.get("title"), request.get("content"), request.get("productId"), request.get("price"), "PUBLISHED", username);
    }

    @Transactional
    public SalesOrderItem create(CreateSalesRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity()));
        String orderNo = "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update("insert into sales_orders(order_no,customer_id,product_id,quantity,unit_price,total_amount,paid_amount,status,audit_status,receivable_status,created_by) values(?,?,?,?,?,?,?,?,?,?,?)",
                orderNo, request.getCustomerId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, BigDecimal.ZERO, "CREATED", "PENDING", "UNPAID", username);
        Long id = jdbcTemplate.queryForObject("select id from sales_orders where order_no=?", Long.class, orderNo);
        auditService.trace("SALES", id, orderNo, "CREATED", "销售单创建", username, "创建销售单");
        return detail(id);
    }

    @Transactional
    public SalesOrderItem update(Long id, Map<String, String> request, String username) {
        jdbcTemplate.update("update sales_orders set customer_id=?, product_id=?, quantity=?, unit_price=?, total_amount=? where id=? and status in ('CREATED','APPROVED')",
                request.get("customerId"), request.get("productId"), request.get("quantity"), request.get("unitPrice"), new BigDecimal(request.get("unitPrice")).multiply(new BigDecimal(request.get("quantity"))), id);
        SalesOrderItem item = detail(id);
        auditService.trace("SALES", id, item.getOrderNo(), "UPDATED", "销售单修改", username, request.get("remark"));
        return item;
    }

    @Transactional
    public SalesOrderItem audit(Long id, String decision, String remark, String username) {
        SalesOrderItem item = detail(id);
        String status = "APPROVED";
        String auditStatus = "APPROVED";
        if (!"APPROVED".equalsIgnoreCase(decision)) {
            status = "CREATED";
            auditStatus = "REJECTED";
            auditService.abnormal("SALES", id, item.getOrderNo(), "AUDIT_REJECTED", "OPEN", username, remark == null ? "销售单审核拒绝" : remark);
        }
        jdbcTemplate.update("update sales_orders set status=?, audit_status=?, audit_by=?, audit_at=now(), audit_remark=? where id=?", status, auditStatus, username, remark, id);
        auditService.trace("SALES", id, item.getOrderNo(), auditStatus, "销售审核", username, remark);
        return detail(id);
    }

    @Transactional
    public SalesOrderItem cancel(Long id, String reason, String username) {
        jdbcTemplate.update("update sales_orders set status='CANCELLED', cancel_reason=? where id=? and status not in ('PAID','OUTBOUND','PARTIAL_PAID')", reason, id);
        SalesOrderItem item = detail(id);
        auditService.trace("SALES", id, item.getOrderNo(), "CANCELLED", "销售取消", username, reason);
        return item;
    }

    @Transactional
    public SalesOrderItem outbound(Long id, String username) {
        SalesOrderItem item = detail(id);
        if (!("CREATED".equals(item.getStatus()) || "APPROVED".equals(item.getStatus()))) return item;
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=? and warehouse_id=1", Integer.class, item.getProductId());
        if (beforeQty == null || beforeQty < item.getQuantity()) {
            auditService.abnormal("SALES", id, item.getOrderNo(), "NO_STOCK", "OPEN", username, "库存不足，无法出库");
            throw new IllegalArgumentException("库存不足，无法出库");
        }
        int afterQty = beforeQty - item.getQuantity();
        jdbcTemplate.update("update inventories set quantity=? where product_id=? and warehouse_id=1", afterQty, item.getProductId());
        jdbcTemplate.update("update sales_orders set status='OUTBOUND', outbound_at=now() where id=?", id);
        jdbcTemplate.update("insert into inventory_records(product_id,warehouse_id,biz_type,biz_id,change_qty,before_qty,after_qty,remark,source_module,source_order_no) values(?,?,?,?,?,?,?,?,?,?)", item.getProductId(), 1L, "SALES_OUTBOUND", id, -item.getQuantity(), beforeQty, afterQty, "销售出库", "SALES", item.getOrderNo());
        auditService.trace("SALES", id, item.getOrderNo(), "OUTBOUND", "销售出库", username, "库存扣减");
        return detail(id);
    }

    @Transactional
    public SalesOrderItem payment(Long id, PaymentRequest request, String username) {
        SalesOrderItem item = detail(id);
        BigDecimal paid = item.getPaidAmount().add(request.getAmount());
        String status = paid.compareTo(item.getTotalAmount()) >= 0 ? "PAID" : ("OUTBOUND".equals(item.getStatus()) ? "PARTIAL_PAID" : item.getStatus());
        String receivableStatus = paid.compareTo(item.getTotalAmount()) >= 0 ? "PAID" : "PARTIAL";
        jdbcTemplate.update("update sales_orders set paid_amount=?, status=?, receivable_status=? where id=?", paid, status, receivableStatus, id);
        jdbcTemplate.update("insert into payment_records(sales_order_id, amount, payer_name, remark) values(?,?,?,?)", id, request.getAmount(), request.getPayerName(), request.getRemark());
        jdbcTemplate.update("insert into receivable_records(sales_order_id, amount, status, due_date, paid_at, remark) values(?,?,?,?,now(),?)", id, request.getAmount(), receivableStatus, LocalDateTime.now().plusDays(7), request.getRemark());
        auditService.trace("SALES", id, item.getOrderNo(), receivableStatus, "销售回款", username, request.getRemark());
        return detail(id);
    }

    public List<Map<String, Object>> receivables() {
        return jdbcTemplate.queryForList("select s.id, s.order_no as orderNo, c.name as customerName, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.receivable_status as receivableStatus, IFNULL(DATE_FORMAT(max(r.due_date),'%Y-%m-%d'),'') as dueDate from sales_orders s left join customers c on s.customer_id=c.id left join receivable_records r on s.id=r.sales_order_id group by s.id, s.order_no, c.name, s.total_amount, s.paid_amount, s.receivable_status order by s.id desc");
    }

    public ReportSummaryItem statistics() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("销售统计");
        item.setCount(jdbcTemplate.queryForObject("select count(1) from sales_orders", Integer.class));
        item.setAmount(jdbcTemplate.queryForObject("select ifnull(sum(total_amount),0) from sales_orders", BigDecimal.class));
        return item;
    }

    public byte[] exportExcel() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select order_no as 订单号, customer_id as 客户ID, product_id as 商品ID, quantity as 数量, unit_price as 单价, total_amount as 总金额, status as 状态 from sales_orders order by id desc");
        return excelUtil.exportWorkbook("销售单", new String[]{"订单号", "客户ID", "商品ID", "数量", "单价", "总金额", "状态"}, rows);
    }

    @Transactional
    public Map<String, Object> importExcel(MultipartFile file, String username) {
        List<Map<String, String>> rows = excelUtil.importWorkbook(file);
        int success = 0;
        for (Map<String, String> row : rows) {
            CreateSalesRequest request = new CreateSalesRequest();
            request.setCustomerId(excelUtil.toLong(row, "客户ID"));
            request.setProductId(excelUtil.toLong(row, "商品ID"));
            request.setQuantity(excelUtil.toInteger(row, "数量"));
            request.setUnitPrice(excelUtil.toBigDecimal(row, "单价"));
            create(request, username);
            success++;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", success);
        result.put("total", rows.size());
        return result;
    }

    private SalesOrderItem detail(Long id) {
        return jdbcTemplate.queryForObject("select s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, s.created_by as createdBy, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt from sales_orders s left join customers c on s.customer_id=c.id left join products p on s.product_id=p.id where s.id=?",
                new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class), id);
    }
}
