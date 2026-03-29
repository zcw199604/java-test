package com.example.tobacco.mapper.inventory;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class InventorySqlProvider {
    public String buildListSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("i.id, i.product_id as productId, p.name as productName, i.warehouse_id as warehouseId, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt");
            FROM("inventories i left join products p on i.product_id=p.id");
            if (params.get("warehouseId") != null) WHERE("i.warehouse_id=#{warehouseId}");
            if (params.get("keywordLike") != null) WHERE("(p.name like #{keywordLike} or i.warehouse_name like #{keywordLike})");
            if (params.get("statusFilter") != null) {
                String status = String.valueOf(params.get("statusFilter"));
                if ("warning".equalsIgnoreCase(status)) {
                    WHERE("i.quantity <= i.warning_threshold");
                } else if ("normal".equalsIgnoreCase(status)) {
                    WHERE("i.quantity > i.warning_threshold");
                }
            }
            ORDER_BY("i.updated_at desc, i.id desc");
        }}.toString();
    }

    public String buildRecordsSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("r.id, r.product_id as productId, p.name as productName, r.biz_type as bizType, r.biz_id as bizId, r.warehouse_id as warehouseId, r.warehouse_name as warehouseName, r.from_warehouse_id as fromWarehouseId, r.from_warehouse_name as fromWarehouseName, r.to_warehouse_id as toWarehouseId, r.to_warehouse_name as toWarehouseName, r.change_qty as changeQty, r.before_qty as beforeQty, r.after_qty as afterQty, coalesce(u.real_name, r.operator_name) as operatorName, r.remark, DATE_FORMAT(r.created_at,'%Y-%m-%d %H:%i:%s') as createdAt");
            FROM("inventory_records r left join products p on r.product_id=p.id left join users u on r.operator_name=u.username");
            if (params.get("warehouseId") != null) {
                WHERE("(r.warehouse_id=#{warehouseId} or r.from_warehouse_id=#{warehouseId} or r.to_warehouse_id=#{warehouseId})");
            }
            if (params.get("bizType") != null) WHERE("r.biz_type=#{bizType}");
            ORDER_BY("r.id desc");
        }}.toString();
    }

    public String buildWarningsSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("i.id, i.product_id as productId, p.name as productName, i.warehouse_id as warehouseId, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt");
            FROM("inventories i left join products p on i.product_id=p.id");
            WHERE("i.quantity <= i.warning_threshold");
            if (params.get("warehouseId") != null) WHERE("i.warehouse_id=#{warehouseId}");
            ORDER_BY("i.quantity asc, i.id asc");
        }}.toString();
    }
}
