package com.example.tobacco.mapper.system;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class SystemSqlProvider {

    public String buildWarehouseListSql(Map<String, Object> params) {
        boolean hasCode = Boolean.TRUE.equals(params.get("hasCode"));
        String keyword = trim((String) params.get("keyword"));
        String status = trim((String) params.get("status"));
        return new SQL() {{
            SELECT(hasCode ? "id, code, name, address, status" : "id, name, address, status");
            FROM("warehouses");
            if (hasText(keyword)) {
                WHERE("(name like #{keywordLike} or address like #{keywordLike})");
            }
            if (hasText(status)) {
                WHERE("status = #{status}");
            }
            ORDER_BY("id");
        }}.toString();
    }

    private boolean hasText(String value) {
        return value != null && value.length() > 0;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
