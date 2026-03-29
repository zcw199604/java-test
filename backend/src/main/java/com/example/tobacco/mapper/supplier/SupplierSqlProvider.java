package com.example.tobacco.mapper.supplier;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class SupplierSqlProvider {

    public String buildListSql(Map<String, Object> params) {
        String keyword = trim((String) params.get("keyword"));
        String status = trim((String) params.get("status"));
        return new SQL() {{
            SELECT("id, name, contact_name as contactName, contact_phone as contactPhone, address, status");
            FROM("suppliers");
            if (hasText(keyword)) {
                WHERE("(name like #{keywordLike} or contact_name like #{keywordLike} or contact_phone like #{keywordLike} or address like #{keywordLike})");
            }
            if (hasText(status)) {
                WHERE("status = #{status}");
            }
            ORDER_BY("id");
        }}.toString();
    }

    private boolean hasText(String value) { return value != null && value.length() > 0; }
    private String trim(String value) { return value == null ? null : value.trim(); }
}
