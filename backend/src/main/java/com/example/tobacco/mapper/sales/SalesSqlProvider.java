package com.example.tobacco.mapper.sales;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class SalesSqlProvider {

    public String buildListSql(Map<String, Object> params) {
        boolean isSeller = Boolean.TRUE.equals(params.get("isSeller"));
        return new SQL() {{
            SELECT("s.id, s.order_no as orderNo, s.customer_id as customerId, c.name as customerName, s.product_id as productId, p.name as productName, s.quantity, s.unit_price as unitPrice, s.total_amount as totalAmount, s.paid_amount as paidAmount, s.status, s.created_by as createdBy, s.warehouse_id as warehouseId, s.warehouse_name as warehouseName, DATE_FORMAT(s.created_at,'%Y-%m-%d %H:%i:%s') as createdAt, IFNULL(DATE_FORMAT(s.outbound_at,'%Y-%m-%d %H:%i:%s'),'') as outboundAt, IFNULL(s.audited_by,'') as auditedBy, IFNULL(DATE_FORMAT(s.audited_at,'%Y-%m-%d %H:%i:%s'),'') as auditedAt, IFNULL(s.audit_remark,'') as auditRemark, IFNULL(s.cancel_reason,'') as cancelReason");
            FROM("sales_orders s left join customers c on s.customer_id=c.id left join products p on s.product_id=p.id");
            if (isSeller) {
                WHERE("s.created_by=#{username}");
            }
            ORDER_BY("s.id desc");
        }}.toString();
    }

    public String buildReceivablesSql(Map<String, Object> params) {
        boolean isSeller = Boolean.TRUE.equals(params.get("isSeller"));
        return new SQL() {{
            SELECT("id, order_no as orderNo, customer_id as customerId, product_id as productId, warehouse_id as warehouseId, warehouse_name as warehouseName, total_amount as totalAmount, paid_amount as paidAmount, (total_amount - paid_amount) as receivableAmount, status, created_by as createdBy, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt");
            FROM("sales_orders");
            WHERE("total_amount > paid_amount");
            if (isSeller) {
                WHERE("created_by=#{username}");
            }
            ORDER_BY("id desc");
        }}.toString();
    }
}
