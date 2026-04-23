package com.example.tobacco.mapper.inventory;

import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InventoryMapper {

    @SelectProvider(type = InventorySqlProvider.class, method = "buildListSql")
    List<InventoryItem> list(@Param("warehouseId") Long warehouseId, @Param("keywordLike") String keywordLike, @Param("statusFilter") String statusFilter);

    @SelectProvider(type = InventorySqlProvider.class, method = "buildRecordsSql")
    List<InventoryRecordItem> records(@Param("warehouseId") Long warehouseId, @Param("bizType") String bizType, @Param("keywordLike") String keywordLike);

    @SelectProvider(type = InventorySqlProvider.class, method = "buildWarningsSql")
    List<InventoryItem> warnings(@Param("warehouseId") Long warehouseId);

    @Select("select quantity from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Integer selectQuantity(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Select("select id from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Long selectInventoryId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Update("update inventories set quantity=#{quantity} where product_id=#{productId} and warehouse_id=#{warehouseId}")
    void updateQuantity(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);

    @Insert("insert into inventory_records(product_id,biz_type,biz_id,warehouse_id,warehouse_name,from_warehouse_id,from_warehouse_name,to_warehouse_id,to_warehouse_name,change_qty,before_qty,after_qty,operator_name,remark) values(#{productId},#{bizType},#{bizId},#{warehouseId},#{warehouseName},#{fromWarehouseId},#{fromWarehouseName},#{toWarehouseId},#{toWarehouseName},#{changeQty},#{beforeQty},#{afterQty},#{operatorName},#{remark})")
    void insertRecord(@Param("productId") Long productId,
                      @Param("bizType") String bizType,
                      @Param("bizId") Long bizId,
                      @Param("warehouseId") Long warehouseId,
                      @Param("warehouseName") String warehouseName,
                      @Param("fromWarehouseId") Long fromWarehouseId,
                      @Param("fromWarehouseName") String fromWarehouseName,
                      @Param("toWarehouseId") Long toWarehouseId,
                      @Param("toWarehouseName") String toWarehouseName,
                      @Param("changeQty") Integer changeQty,
                      @Param("beforeQty") Integer beforeQty,
                      @Param("afterQty") Integer afterQty,
                      @Param("operatorName") String operatorName,
                      @Param("remark") String remark);

    @Select("select id from products where code=#{code} and status='ENABLED'")
    Long selectProductIdByCode(@Param("code") String code);

    @Select("select count(1) from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Integer countByProductWarehouse(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Update("update inventories set quantity=#{quantity}, warning_threshold=#{warningThreshold}, warehouse_name=#{warehouseName} where product_id=#{productId} and warehouse_id=#{warehouseId}")
    void updateInventory(@Param("productId") Long productId,
                         @Param("warehouseId") Long warehouseId,
                         @Param("quantity") Integer quantity,
                         @Param("warningThreshold") Integer warningThreshold,
                         @Param("warehouseName") String warehouseName);

    @Insert("insert into inventories(product_id,warehouse_id,warehouse_name,quantity,warning_threshold) values(#{productId},#{warehouseId},#{warehouseName},#{quantity},#{warningThreshold})")
    void insertInventory(@Param("productId") Long productId,
                         @Param("warehouseId") Long warehouseId,
                         @Param("warehouseName") String warehouseName,
                         @Param("quantity") Integer quantity,
                         @Param("warningThreshold") Integer warningThreshold);

    @Select("select warning_threshold from inventories where product_id=#{productId} and warehouse_id=#{warehouseId}")
    Integer selectWarningThreshold(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

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

    @Select("select id from warehouses where name=#{warehouseName} limit 1")
    Long selectWarehouseIdByName(@Param("warehouseName") String warehouseName);

    @Select("select min(id) from warehouses where status='ENABLED'")
    Long selectDefaultWarehouseId();

    @Update("update inventories set quantity=quantity + #{deltaQty}, updated_at=now() where product_id=#{productId} and warehouse_id=#{warehouseId}")
    void increaseQuantity(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("deltaQty") Integer deltaQty);
}
