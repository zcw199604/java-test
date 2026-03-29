package com.example.tobacco.mapper.inventory;

import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface InventoryMapper {

    @Select("select i.id, i.product_id as productId, p.name as productName, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from inventories i left join products p on i.product_id=p.id order by i.id")
    List<InventoryItem> list();

    @Select("select r.id, r.product_id as productId, p.name as productName, r.biz_type as bizType, r.biz_id as bizId, r.change_qty as changeQty, r.before_qty as beforeQty, r.after_qty as afterQty, coalesce(u.real_name, r.operator_name) as operatorName, r.remark, DATE_FORMAT(r.created_at,'%Y-%m-%d %H:%i:%s') as createdAt from inventory_records r left join products p on r.product_id=p.id left join users u on r.operator_name=u.username order by r.id desc")
    List<InventoryRecordItem> records();

    @Select("select i.id, i.product_id as productId, p.name as productName, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from inventories i left join products p on i.product_id=p.id where i.quantity <= i.warning_threshold order by i.quantity asc")
    List<InventoryItem> warnings();

    @Select("select quantity from inventories where product_id=#{productId}")
    Integer selectQuantity(@Param("productId") Long productId);

    @Update("update inventories set quantity=#{quantity} where product_id=#{productId}")
    void updateQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Insert("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(#{productId},#{bizType},#{bizId},#{changeQty},#{beforeQty},#{afterQty},#{operatorName},#{remark})")
    void insertRecord(@Param("productId") Long productId,
                      @Param("bizType") String bizType,
                      @Param("bizId") Long bizId,
                      @Param("changeQty") Integer changeQty,
                      @Param("beforeQty") Integer beforeQty,
                      @Param("afterQty") Integer afterQty,
                      @Param("operatorName") String operatorName,
                      @Param("remark") String remark);

    @Select("select id from products where code=#{code} and status='ENABLED'")
    Long selectProductIdByCode(@Param("code") String code);

    @Select("select count(1) from inventories where product_id=#{productId}")
    Integer countByProductId(@Param("productId") Long productId);

    @Update("update inventories set quantity=#{quantity}, warning_threshold=#{warningThreshold}, warehouse_name=#{warehouseName} where product_id=#{productId}")
    void updateInventory(@Param("productId") Long productId,
                         @Param("quantity") Integer quantity,
                         @Param("warningThreshold") Integer warningThreshold,
                         @Param("warehouseName") String warehouseName);

    @Insert("insert into inventories(product_id,warehouse_name,quantity,warning_threshold) values(#{productId},#{warehouseName},#{quantity},#{warningThreshold})")
    void insertInventory(@Param("productId") Long productId,
                         @Param("warehouseName") String warehouseName,
                         @Param("quantity") Integer quantity,
                         @Param("warningThreshold") Integer warningThreshold);

    @Select("select warning_threshold from inventories where product_id=#{productId}")
    Integer selectWarningThreshold(@Param("productId") Long productId);

    @Select("select name from products where id=#{productId}")
    String selectProductName(@Param("productId") Long productId);

    @Select("select id from users where role_code='KEEPER' and status='ENABLED'")
    List<Long> selectKeeperIds();

    @Select("select id from users where username=#{username}")
    Long selectUserIdByUsername(@Param("username") String username);
}
