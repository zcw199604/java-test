package com.example.tobacco.mapper.catalog;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class CatalogSqlProvider {

    public String buildProductListSql(Map<String, Object> params) {
        String keyword = trim((String) params.get("keyword"));
        String status = trim((String) params.get("status"));
        String category = trim((String) params.get("category"));
        return new SQL() {{
            SELECT("id, code, name, category, unit, unit_price as unitPrice, warning_threshold as warningThreshold, status");
            FROM("products");
            if (hasText(keyword)) {
                WHERE("(code like #{keywordLike} or name like #{keywordLike} or category like #{keywordLike})");
            }
            if (hasText(status)) {
                WHERE("status = #{status}");
            }
            if (hasText(category)) {
                WHERE("category = #{category}");
            }
            ORDER_BY("id");
        }}.toString();
    }

    private boolean hasText(String value) { return value != null && value.length() > 0; }
    private String trim(String value) { return value == null ? null : value.trim(); }
}
