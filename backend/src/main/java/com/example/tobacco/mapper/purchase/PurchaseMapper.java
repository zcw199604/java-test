package com.example.tobacco.mapper.purchase;

import com.example.tobacco.model.PurchaseOrderItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PurchaseMapper {

    String DETAIL_SQL = "select p.id, p.order_no as orderNo, p.supplier_id as supplierId, s.name as supplierName, " +
            "p.product_id as productId, pr.name as productName, p.quantity, p.unit_price as unitPrice, " +
            "p.total_amount as totalAmount, p.status, p.created_by as createdBy, p.warehouse_id as warehouseId, p.warehouse_name as warehouseName, " +
            "DATE_FORMAT(p.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, " +
            "IFNULL(DATE_FORMAT(p.received_at,'%Y-%m-%d %H:%i:%s'),'') as receivedAt, " +
            "IFNULL(DATE_FORMAT(p.inbound_at,'%Y-%m-%d %H:%i:%s'),'') as inboundAt, " +
            "IFNULL(p.audited_by,'') as auditedBy, " +
            "IFNULL(DATE_FORMAT(p.audited_at,'%Y-%m-%d %H:%i:%s'),'') as auditedAt, " +
            "IFNULL(p.audit_remark,'') as auditRemark, " +
            "IFNULL(p.cancel_reason,'') as cancelReason " +
            "from purchase_orders p left join suppliers s on p.supplier_id=s.id left join products pr on p.product_id=pr.id";

    @Select(DETAIL_SQL + " order by p.id desc")
    List<PurchaseOrderItem> list();

    @Select(DETAIL_SQL + " where p.id=#{id}")
    PurchaseOrderItem detail(@Param("id") Long id);

    @Select("select id, biz_type as bizType, biz_id as bizId, order_no as orderNo, node_code as nodeCode, node_name as nodeName, operator, remark, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from trace_records where biz_type='PURCHASE' and (biz_id=#{id} or order_no=#{orderNo}) order by id desc")
    List<Map<String, Object>> trace(@Param("id") Long id, @Param("orderNo") String orderNo);

    @Insert("insert into purchase_orders(order_no,supplier_id,product_id,quantity,unit_price,total_amount,status,created_by) values(#{orderNo},#{supplierId},#{productId},#{quantity},#{unitPrice},#{totalAmount},#{status},#{createdBy})")
    void insertOrder(@Param("orderNo") String orderNo,
                     @Param("supplierId") Long supplierId,
                     @Param("productId") Long productId,
                     @Param("quantity") Integer quantity,
                     @Param("unitPrice") BigDecimal unitPrice,
                     @Param("totalAmount") BigDecimal totalAmount,
                     @Param("status") String status,
                     @Param("createdBy") String createdBy);

    @Select("select id from purchase_orders where order_no=#{orderNo}")
    Long selectIdByOrderNo(@Param("orderNo") String orderNo);

    @Update("update purchase_orders set supplier_id=#{supplierId}, product_id=#{productId}, quantity=#{quantity}, unit_price=#{unitPrice}, total_amount=#{totalAmount}, status='CREATED', audited_by=null, audited_at=null, audit_remark=null, cancel_reason=null, warehouse_id=null, warehouse_name=null where id=#{id}")
    void updateOrder(@Param("id") Long id,
                     @Param("supplierId") Long supplierId,
                     @Param("productId") Long productId,
                     @Param("quantity") Integer quantity,
                     @Param("unitPrice") BigDecimal unitPrice,
                     @Param("totalAmount") BigDecimal totalAmount);

    @Update("update purchase_orders set status=#{status}, audited_by=#{operator}, audited_at=now(), audit_remark=#{remark} where id=#{id}")
    void auditOrder(@Param("id") Long id, @Param("status") String status, @Param("operator") String operator, @Param("remark") String remark);

    @Update("update purchase_orders set status='CANCELLED', cancel_reason=#{reason} where id=#{id}")
    void cancelOrder(@Param("id") Long id, @Param("reason") String reason);

    @Update("update purchase_orders set status='RECEIVED', received_at=now() where id=#{id} and status='APPROVED'")
    void receiveOrder(@Param("id") Long id);

    @Select("select quantity from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Integer selectInventoryQuantity(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Select("select count(1) from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Integer countInventories(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Update("update inventories set quantity=#{quantity}, warning_threshold=(select warning_threshold from products where id=#{productId}), warehouse_name=#{warehouseName} where product_id=#{productId} and warehouse_id=#{warehouseId}")
    void updateInventoryForInbound(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("warehouseName") String warehouseName, @Param("quantity") Integer quantity);

    @Insert("insert into inventories(product_id,warehouse_id,warehouse_name,quantity,warning_threshold) values(#{productId},#{warehouseId},#{warehouseName},#{quantity},(select warning_threshold from products where id=#{productId}))")
    void insertInventoryForInbound(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("warehouseName") String warehouseName, @Param("quantity") Integer quantity);

    @Update("update purchase_orders set status='INBOUND', inbound_at=now(), warehouse_id=#{warehouseId}, warehouse_name=#{warehouseName} where id=#{id}")
    void inboundOrder(@Param("id") Long id, @Param("warehouseId") Long warehouseId, @Param("warehouseName") String warehouseName);

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


    @Select("select id from suppliers where name=#{name} and status='ENABLED'")
    Long selectSupplierIdByName(@Param("name") String name);

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
