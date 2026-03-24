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
            "IFNULL(DATE_FORMAT(p.inbound_at,'%Y-%m-%d %H:%i:%s'),'') as inboundAt " +
            "from purchase_orders p " +
            "left join suppliers s on p.supplier_id=s.id " +
            "left join products pr on p.product_id=pr.id";

    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;

    public PurchaseService(JdbcTemplate jdbcTemplate, AuditService auditService, MessageService messageService, ExcelUtil excelUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<PurchaseOrderItem> list() {
        return jdbcTemplate.query(PURCHASE_DETAIL_SQL + " order by p.id desc", new BeanPropertyRowMapper<PurchaseOrderItem>(PurchaseOrderItem.class));
    }

    public List<Map<String, Object>> requisitions() {
        return jdbcTemplate.queryForList("select id, req_no as reqNo, product_id as productId, quantity, reason, status, created_by as createdBy, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from purchase_requisitions order by id desc");
    }

    @Transactional
    public void createRequisition(Map<String, String> request, String username) {
        String reqNo = "PR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update("insert into purchase_requisitions(req_no, product_id, quantity, reason, status, created_by) values(?,?,?,?,?,?)",
                reqNo, request.get("productId"), request.get("quantity"), request.get("reason"), "CREATED", username);
    }

    @Transactional
    public PurchaseOrderItem create(CreatePurchaseRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity()));
        String orderNo = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update(
                "insert into purchase_orders(order_no,supplier_id,product_id,quantity,unit_price,total_amount,status,audit_status,created_by) values(?,?,?,?,?,?,?,?,?)",
                orderNo, request.getSupplierId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, "CREATED", "PENDING", username);
        Long id = jdbcTemplate.queryForObject("select id from purchase_orders where order_no=?", Long.class, orderNo);
        track(id, orderNo, "CREATED", "采购单创建", username, "创建采购单");
        auditService.logOperation(null, username, "PURCHASE", "CREATE_PURCHASE", "PURCHASE", id, orderNo);
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem update(Long id, Map<String, String> request, String username) {
        jdbcTemplate.update("update purchase_orders set supplier_id=?, product_id=?, quantity=?, unit_price=?, total_amount=? where id=? and status in ('CREATED','APPROVED')",
                request.get("supplierId"), request.get("productId"), request.get("quantity"), request.get("unitPrice"), new BigDecimal(request.get("unitPrice")).multiply(new BigDecimal(request.get("quantity"))), id);
        PurchaseOrderItem item = detail(id);
        track(id, item.getOrderNo(), "UPDATED", "采购单修改", username, request.get("remark"));
        return item;
    }

    @Transactional
    public PurchaseOrderItem audit(Long id, String decision, String remark, String username) {
        PurchaseOrderItem item = detail(id);
        String status = "APPROVED";
        String auditStatus = "APPROVED";
        if (!"APPROVED".equalsIgnoreCase(decision)) {
            status = "CREATED";
            auditStatus = "REJECTED";
            auditService.abnormal("PURCHASE", id, item.getOrderNo(), "AUDIT_REJECTED", "OPEN", username, remark == null ? "采购单审核拒绝" : remark);
        }
        jdbcTemplate.update("update purchase_orders set status=?, audit_status=?, audit_by=?, audit_at=now(), audit_remark=? where id=?", status, auditStatus, username, remark, id);
        track(id, item.getOrderNo(), auditStatus, "采购审核", username, remark);
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem cancel(Long id, String reason, String username) {
        jdbcTemplate.update("update purchase_orders set status='CANCELLED', cancel_reason=? where id=? and status<>'INBOUND'", reason, id);
        PurchaseOrderItem item = detail(id);
        track(id, item.getOrderNo(), "CANCELLED", "采购取消", username, reason);
        return item;
    }

    @Transactional
    public PurchaseOrderItem receive(Long id, String username) {
        PurchaseOrderItem item = detail(id);
        if ("INBOUND".equals(item.getStatus()) || "RECEIVED".equals(item.getStatus())) {
            return item;
        }
        jdbcTemplate.update("update purchase_orders set status='RECEIVED', received_at=now() where id=? and status in ('APPROVED','CREATED')", id);
        item = detail(id);
        track(id, item.getOrderNo(), "RECEIVED", "采购到货", username, "登记到货");
        return item;
    }

    @Transactional
    public PurchaseOrderItem inbound(Long id, String username) {
        PurchaseOrderItem item = detail(id);
        if ("INBOUND".equals(item.getStatus())) {
            return item;
        }
        if (!"RECEIVED".equals(item.getStatus())) {
            throw new IllegalArgumentException("采购单需先登记到货后才能入库");
        }
        Map<String, Object> inventory = ensureInventory(item.getProductId(), 1L);
        int beforeQty = ((Number) inventory.get("quantity")).intValue();
        int afterQty = beforeQty + item.getQuantity();
        jdbcTemplate.update("update inventories set quantity=?, warning_threshold=(select warning_threshold from products where id=?), warehouse_name='中心仓', warehouse_id=1 where product_id=? and warehouse_id=1",
                afterQty, item.getProductId(), item.getProductId());
        jdbcTemplate.update("update purchase_orders set status='INBOUND', inbound_at=now() where id=?", id);
        jdbcTemplate.update(
                "insert into inventory_records(product_id,warehouse_id,biz_type,biz_id,change_qty,before_qty,after_qty,remark,source_module,source_order_no) values(?,?,?,?,?,?,?,?,?,?)",
                item.getProductId(), 1L, "PURCHASE_INBOUND", id, item.getQuantity(), beforeQty, afterQty, "采购单入库", "PURCHASE", item.getOrderNo());
        track(id, item.getOrderNo(), "INBOUND", "采购入库", username, "完成入库");
        auditService.trace("PURCHASE", id, item.getOrderNo(), "INBOUND", "采购入库", username, "库存增加");
        return detail(id);
    }

    public List<Map<String, Object>> tracks(Long id) {
        return jdbcTemplate.queryForList("select id, purchase_order_id as purchaseOrderId, node_code as nodeCode, node_name as nodeName, operator, remark, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from purchase_order_tracks where purchase_order_id=? order by id", id);
    }

    public Map<String, Object> analysis() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("summary", jdbcTemplate.queryForList("select s.name as supplierName, count(1) as orderCount, ifnull(sum(p.total_amount),0) as totalAmount from purchase_orders p left join suppliers s on p.supplier_id=s.id group by s.name order by totalAmount desc"));
        result.put("categorySummary", jdbcTemplate.queryForList("select pr.category, count(1) as orderCount, ifnull(sum(p.total_amount),0) as totalAmount from purchase_orders p left join products pr on p.product_id=pr.id group by pr.category order by totalAmount desc"));
        return result;
    }

    public byte[] exportExcel() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select order_no as 订单号, supplier_id as 供应商ID, product_id as 商品ID, quantity as 数量, unit_price as 单价, total_amount as 总金额, status as 状态 from purchase_orders order by id desc");
        return excelUtil.exportWorkbook("采购单", new String[]{"订单号", "供应商ID", "商品ID", "数量", "单价", "总金额", "状态"}, rows);
    }

    @Transactional
    public Map<String, Object> importExcel(MultipartFile file, String username) {
        List<Map<String, String>> rows = excelUtil.importWorkbook(file);
        int success = 0;
        for (Map<String, String> row : rows) {
            CreatePurchaseRequest request = new CreatePurchaseRequest();
            request.setSupplierId(excelUtil.toLong(row, "供应商ID"));
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

    private Map<String, Object> ensureInventory(Long productId, Long warehouseId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id, quantity from inventories where product_id=? and warehouse_id=?", productId, warehouseId);
        if (rows.isEmpty()) {
            jdbcTemplate.update("insert into inventories(product_id, warehouse_id, warehouse_name, quantity, warning_threshold, locked_qty) values(?,?,?,?,(select warning_threshold from products where id=?),0)", productId, warehouseId, "中心仓", 0, productId);
            return jdbcTemplate.queryForMap("select id, quantity from inventories where product_id=? and warehouse_id=?", productId, warehouseId);
        }
        return rows.get(0);
    }

    private void track(Long id, String orderNo, String nodeCode, String nodeName, String operator, String remark) {
        jdbcTemplate.update("insert into purchase_order_tracks(purchase_order_id, node_code, node_name, operator, remark) values(?,?,?,?,?)", id, nodeCode, nodeName, operator, remark);
        auditService.trace("PURCHASE", id, orderNo, nodeCode, nodeName, operator, remark);
    }

    private PurchaseOrderItem detail(Long id) {
        return jdbcTemplate.queryForObject(PURCHASE_DETAIL_SQL + " where p.id=?", new BeanPropertyRowMapper<PurchaseOrderItem>(PurchaseOrderItem.class), id);
    }
}
