package com.example.tobacco.mapper.sales;

import com.example.tobacco.model.SalesOrderItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesMapper {

    String DETAIL_SQL = "select s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, s.created_by as createdBy, s.warehouse_id as warehouseId, s.warehouse_name as warehouseName, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt, IFNULL(s.audited_by,'') as auditedBy, IFNULL(DATE_FORMAT(s.audited_at,'%Y-%m-%d %H:%i:%s'),'') as auditedAt, IFNULL(s.audit_remark,'') as auditRemark, IFNULL(s.cancel_reason,'') as cancelReason from sales_orders s left join customers c on s.customer_id=c.id left join products p on s.product_id=p.id";

    @SelectProvider(type = SalesSqlProvider.class, method = "buildListSql")
    List<SalesOrderItem> list(@Param("username") String username, @Param("isSeller") boolean isSeller);

    @Select(DETAIL_SQL + " where s.id=#{id}")
    SalesOrderItem detail(@Param("id") Long id);


    @Select("select id, biz_type as bizType, biz_id as bizId, order_no as orderNo, node_code as nodeCode, node_name as nodeName, operator, remark, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from trace_records where biz_type='SALES' and (biz_id=#{id} or order_no=#{orderNo}) order by id desc")
    List<Map<String, Object>> trace(@Param("id") Long id, @Param("orderNo") String orderNo);

    @Insert("insert into sales_orders(order_no,customer_id,product_id,quantity,unit_price,total_amount,paid_amount,status,created_by) values(#{orderNo},#{customerId},#{productId},#{quantity},#{unitPrice},#{totalAmount},#{paidAmount},#{status},#{createdBy})")
    void insertOrder(@Param("orderNo") String orderNo,
                     @Param("customerId") Long customerId,
                     @Param("productId") Long productId,
                     @Param("quantity") Integer quantity,
                     @Param("unitPrice") BigDecimal unitPrice,
                     @Param("totalAmount") BigDecimal totalAmount,
                     @Param("paidAmount") BigDecimal paidAmount,
                     @Param("status") String status,
                     @Param("createdBy") String createdBy);

    @Select("select id from sales_orders where order_no=#{orderNo}")
    Long selectIdByOrderNo(@Param("orderNo") String orderNo);

    @Update("update sales_orders set customer_id=#{customerId}, product_id=#{productId}, quantity=#{quantity}, unit_price=#{unitPrice}, total_amount=#{totalAmount}, paid_amount=#{paidAmount}, status='CREATED', audited_by=null, audited_at=null, audit_remark=null, cancel_reason=null, warehouse_id=null, warehouse_name=null where id=#{id}")
    void updateOrder(@Param("id") Long id,
                     @Param("customerId") Long customerId,
                     @Param("productId") Long productId,
                     @Param("quantity") Integer quantity,
                     @Param("unitPrice") BigDecimal unitPrice,
                     @Param("totalAmount") BigDecimal totalAmount,
                     @Param("paidAmount") BigDecimal paidAmount);

    @Update("update sales_orders set status=#{status}, audited_by=#{operator}, audited_at=now(), audit_remark=#{remark} where id=#{id}")
    void auditOrder(@Param("id") Long id, @Param("status") String status, @Param("operator") String operator, @Param("remark") String remark);

    @Update("update sales_orders set status='CANCELLED', cancel_reason=#{reason} where id=#{id}")
    void cancelOrder(@Param("id") Long id, @Param("reason") String reason);

    @Select("select quantity from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Integer selectInventoryQuantity(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Update("update inventories set quantity=#{quantity} where product_id=#{productId} and warehouse_id=#{warehouseId}")
    void updateInventoryQuantity(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);

    @Update("update sales_orders set status='OUTBOUND', outbound_at=now(), warehouse_id=#{warehouseId}, warehouse_name=#{warehouseName} where id=#{id}")
    void outboundOrder(@Param("id") Long id, @Param("warehouseId") Long warehouseId, @Param("warehouseName") String warehouseName);

    @Insert("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,warehouse_id,warehouse_name,from_warehouse_id,from_warehouse_name,to_warehouse_id,to_warehouse_name,operator_name,remark) values(#{productId},#{bizType},#{bizId},#{changeQty},#{beforeQty},#{afterQty},#{warehouseId},#{warehouseName},#{fromWarehouseId},#{fromWarehouseName},#{toWarehouseId},#{toWarehouseName},#{operatorName},#{remark})")
    void insertInventoryRecord(@Param("productId") Long productId,
                               @Param("bizType") String bizType,
                               @Param("bizId") Long bizId,
                               @Param("changeQty") Integer changeQty,
                               @Param("beforeQty") Integer beforeQty,
                               @Param("afterQty") Integer afterQty,
                               @Param("warehouseId") Long warehouseId,
                               @Param("warehouseName") String warehouseName,
                               @Param("fromWarehouseId") Long fromWarehouseId,
                               @Param("fromWarehouseName") String fromWarehouseName,
                               @Param("toWarehouseId") Long toWarehouseId,
                               @Param("toWarehouseName") String toWarehouseName,
                               @Param("operatorName") String operatorName,
                               @Param("remark") String remark);

    @Update("update sales_orders set paid_amount=#{paidAmount}, status=#{status} where id=#{id}")
    void updatePayment(@Param("id") Long id, @Param("paidAmount") BigDecimal paidAmount, @Param("status") String status);

    @Insert("insert into payment_records(sales_order_id, amount, payer_name, remark) values(#{salesOrderId},#{amount},#{payerName},#{remark})")
    void insertPaymentRecord(@Param("salesOrderId") Long salesOrderId,
                             @Param("amount") BigDecimal amount,
                             @Param("payerName") String payerName,
                             @Param("remark") String remark);

    @SelectProvider(type = SalesSqlProvider.class, method = "buildReceivablesSql")
    List<Map<String, Object>> selectReceivables(@Param("username") String username, @Param("isSeller") boolean isSeller);

    @Select("select count(1) from sales_orders where created_by=#{username}")
    Integer countSalesByUser(@Param("username") String username);

    @Select("select ifnull(sum(total_amount),0) from sales_orders where created_by=#{username}")
    BigDecimal sumSalesAmountByUser(@Param("username") String username);

    @Select("select count(1) from sales_orders")
    Integer countSalesAll();

    @Select("select ifnull(sum(total_amount),0) from sales_orders")
    BigDecimal sumSalesAmountAll();

    @Select("select id from customers where name=#{name} and status='ENABLED'")
    Long selectCustomerIdByName(@Param("name") String name);

    @Select("select id from products where code=#{code} and status='ENABLED'")
    Long selectProductIdByCode(@Param("code") String code);

    @Select("select warning_threshold from products where id=#{productId}")
    Integer selectInventoryWarningThreshold(@Param("productId") Long productId);

    @Select("select name from products where id=#{productId}")
    String selectProductName(@Param("productId") Long productId);

    @Select("select id from users where role_code='KEEPER' and status='ENABLED'")
    List<Long> selectKeeperIds();

    @Select("select id from users where username=#{username}")
    Long selectUserIdByUsername(@Param("username") String username);

    @Select("select id from warehouses where id=#{warehouseId} and status='ENABLED'")
    Long selectWarehouseId(@Param("warehouseId") Long warehouseId);

    @Select("select name from warehouses where id=#{warehouseId}")
    String selectWarehouseName(@Param("warehouseId") Long warehouseId);
}
