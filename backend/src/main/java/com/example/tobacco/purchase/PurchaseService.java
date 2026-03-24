package com.example.tobacco.purchase;

import com.example.tobacco.model.CreatePurchaseRequest;
import com.example.tobacco.model.PurchaseOrderItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public PurchaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                orderNo,
                request.getSupplierId(),
                request.getProductId(),
                request.getQuantity(),
                request.getUnitPrice(),
                total,
                "CREATED",
                username);
        Long id = jdbcTemplate.queryForObject("select id from purchase_orders where order_no=?", Long.class, orderNo);
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem receive(Long id) {
        PurchaseOrderItem item = detail(id);
        if ("INBOUND".equals(item.getStatus()) || "RECEIVED".equals(item.getStatus())) {
            return item;
        }
        jdbcTemplate.update("update purchase_orders set status='RECEIVED', received_at=now() where id=? and status='CREATED'", id);
        return detail(id);
    }

    @Transactional
    public PurchaseOrderItem inbound(Long id, String operatorName) {
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
                    afterQty,
                    item.getProductId(),
                    item.getProductId());
        } else {
            jdbcTemplate.update(
                    "insert into inventories(product_id,warehouse_name,quantity,warning_threshold) values(?,?,?,(select warning_threshold from products where id=?))",
                    item.getProductId(),
                    "中心仓",
                    afterQty,
                    item.getProductId());
        }
        jdbcTemplate.update("update purchase_orders set status='INBOUND', inbound_at=now() where id=?", id);
        jdbcTemplate.update(
                "insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)",
                item.getProductId(),
                "PURCHASE_INBOUND",
                id,
                item.getQuantity(),
                beforeQty,
                afterQty,
                operatorName,
                "采购单入库");
        return detail(id);
    }

    private PurchaseOrderItem detail(Long id) {
        return jdbcTemplate.queryForObject(
                PURCHASE_DETAIL_SQL + " where p.id=?",
                new BeanPropertyRowMapper<PurchaseOrderItem>(PurchaseOrderItem.class),
                id);
    }
}
