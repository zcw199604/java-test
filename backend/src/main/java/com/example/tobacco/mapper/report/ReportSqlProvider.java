package com.example.tobacco.mapper.report;

import java.util.Map;

public class ReportSqlProvider {

    public String buildComplianceTraceSql(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (")
                .append(" select concat('TRACE-', t.id) as id, 'TRACE' as sourceType, t.biz_type as bizType, t.biz_id as bizId, t.order_no as orderNo, t.node_code as nodeCode, t.node_name as nodeName, ")
                .append(" coalesce(u.real_name, t.operator) as operator, t.remark as remark, DATE_FORMAT(t.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, ")
                .append(" coalesce(pp.id, sp.id) as productId, coalesce(pp.name, sp.name) as productName, ")
                .append(" coalesce(po.warehouse_id, so.warehouse_id) as warehouseId, coalesce(po.warehouse_name, so.warehouse_name) as warehouseName, ")
                .append(" null as fromWarehouseId, null as fromWarehouseName, null as toWarehouseId, null as toWarehouseName, ")
                .append(" null as changeQty, null as beforeQty, null as afterQty ")
                .append(" from trace_records t ")
                .append(" left join users u on t.operator = u.username ")
                .append(" left join purchase_orders po on t.biz_type = 'PURCHASE' and po.id = t.biz_id ")
                .append(" left join sales_orders so on t.biz_type = 'SALES' and so.id = t.biz_id ")
                .append(" left join products pp on po.product_id = pp.id ")
                .append(" left join products sp on so.product_id = sp.id ")
                .append(" union all ")
                .append(" select concat('INV-', r.id) as id, 'INVENTORY' as sourceType, 'INVENTORY' as bizType, r.biz_id as bizId, ")
                .append(" case when r.biz_type = 'PURCHASE_INBOUND' then po.order_no when r.biz_type = 'SALES_OUTBOUND' then so.order_no else null end as orderNo, ")
                .append(" r.biz_type as nodeCode, ")
                .append(" case r.biz_type when 'PURCHASE_INBOUND' then '采购入库' when 'SALES_OUTBOUND' then '销售出库' when 'TRANSFER_OUT' then '调拨出库' when 'TRANSFER_IN' then '调拨入库' when 'CHECK' then '库存盘点' else r.biz_type end as nodeName, ")
                .append(" coalesce(u2.real_name, r.operator_name) as operator, r.remark as remark, DATE_FORMAT(r.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, ")
                .append(" r.product_id as productId, p.name as productName, r.warehouse_id as warehouseId, r.warehouse_name as warehouseName, ")
                .append(" r.from_warehouse_id as fromWarehouseId, r.from_warehouse_name as fromWarehouseName, r.to_warehouse_id as toWarehouseId, r.to_warehouse_name as toWarehouseName, ")
                .append(" r.change_qty as changeQty, r.before_qty as beforeQty, r.after_qty as afterQty ")
                .append(" from inventory_records r ")
                .append(" left join users u2 on r.operator_name = u2.username ")
                .append(" left join products p on r.product_id = p.id ")
                .append(" left join purchase_orders po on r.biz_type = 'PURCHASE_INBOUND' and po.id = r.biz_id ")
                .append(" left join sales_orders so on r.biz_type = 'SALES_OUTBOUND' and so.id = r.biz_id ")
                .append(") unified where 1=1 ");

        if (params.get("warehouseId") != null) {
            sql.append(" and (unified.warehouseId = #{warehouseId} or unified.fromWarehouseId = #{warehouseId} or unified.toWarehouseId = #{warehouseId}) ");
        }
        if (params.get("bizType") != null) {
            sql.append(" and unified.bizType = #{bizType} ");
        }
        if (params.get("nodeCode") != null) {
            sql.append(" and unified.nodeCode = #{nodeCode} ");
        }
        if (params.get("keywordLike") != null) {
            sql.append(" and (unified.orderNo like #{keywordLike} or unified.productName like #{keywordLike} or unified.warehouseName like #{keywordLike} or unified.fromWarehouseName like #{keywordLike} or unified.toWarehouseName like #{keywordLike} or unified.remark like #{keywordLike} or unified.nodeName like #{keywordLike}) ");
        }
        sql.append(" order by unified.createdAt desc, unified.id desc");
        return sql.toString();
    }
}
