package com.example.tobacco.sales;

import com.example.tobacco.model.CreateSalesRequest;
import com.example.tobacco.model.PaymentRequest;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.SalesOrderItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalesService {
    private final JdbcTemplate jdbcTemplate;
    public SalesService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<SalesOrderItem> list(String username, String roleCode) {
        String baseSql = "select s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, s.created_by as createdBy, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt from sales_orders s left join customers c on s.customer_id=c.id left join products p on s.product_id=p.id";
        if (isSeller(roleCode)) {
            return jdbcTemplate.query(baseSql + " where s.created_by=? order by s.id desc",
                    new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class), username);
        }
        return jdbcTemplate.query(baseSql + " order by s.id desc",
                new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class));
    }

    @Transactional
    public SalesOrderItem create(CreateSalesRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity()));
        String orderNo = "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update("insert into sales_orders(order_no,customer_id,product_id,quantity,unit_price,total_amount,paid_amount,status,created_by) values(?,?,?,?,?,?,?,?,?)",
                orderNo, request.getCustomerId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, BigDecimal.ZERO, "CREATED", username);
        Long id = jdbcTemplate.queryForObject("select id from sales_orders where order_no=?", Long.class, orderNo);
        return detail(id);
    }

    @Transactional
    public SalesOrderItem outbound(Long id, String username, String roleCode) {
        SalesOrderItem item = detail(id);
        validateOwnership(item, username, roleCode);
        if (!"CREATED".equals(item.getStatus())) return item;
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=?", Integer.class, item.getProductId());
        if (beforeQty == null || beforeQty < item.getQuantity()) throw new IllegalArgumentException("库存不足，无法出库");
        int afterQty = beforeQty - item.getQuantity();
        jdbcTemplate.update("update inventories set quantity=? where product_id=?", afterQty, item.getProductId());
        jdbcTemplate.update("update sales_orders set status='OUTBOUND', outbound_at=now() where id=?", id);
        jdbcTemplate.update("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)", item.getProductId(), "SALES_OUTBOUND", id, -item.getQuantity(), beforeQty, afterQty, username, "销售出库");
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


    private boolean isSeller(String roleCode) {
        return "SELLER".equalsIgnoreCase(roleCode);
    }

    private void validateOwnership(SalesOrderItem item, String username, String roleCode) {
        if (isSeller(roleCode) && !String.valueOf(item.getCreatedBy()).equals(username)) {
            throw new IllegalArgumentException("仅允许操作本人创建的销售订单");
        }
    }

    private SalesOrderItem detail(Long id) {
        return jdbcTemplate.queryForObject("select s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, s.created_by as createdBy, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt from sales_orders s left join customers c on s.customer_id=c.id left join products p on s.product_id=p.id where s.id=?",
            new BeanPropertyRowMapper<SalesOrderItem>(SalesOrderItem.class), id);
    }
}
