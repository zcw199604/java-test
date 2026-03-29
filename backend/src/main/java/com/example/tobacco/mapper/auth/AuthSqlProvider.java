package com.example.tobacco.mapper.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AuthSqlProvider {

    public String buildSelectCaptchaSql(Map<String, Object> params) {
        String keyColumn = safeColumn((String) params.get("keyColumn"), "captcha_key", "uuid");
        String codeColumn = safeColumn((String) params.get("codeColumn"), "captcha_code", "code");
        String expireColumn = safeColumn((String) params.get("expireColumn"), "expire_at", "expired_at");
        return "select id, " + codeColumn + " as captchaCode, " + expireColumn + " as expireAt, status from captcha_records where " + keyColumn + "=#{captchaKey} order by id desc limit 1";
    }

    public String buildInsertCaptchaSql(Map<String, Object> params) {
        String keyColumn = safeColumn((String) params.get("keyColumn"), "captcha_key", "uuid");
        String codeColumn = safeColumn((String) params.get("codeColumn"), "captcha_code", "code");
        String expireColumn = safeColumn((String) params.get("expireColumn"), "expire_at", "expired_at");
        return "insert into captcha_records(" + join(Arrays.asList(keyColumn, codeColumn, expireColumn, "status")) + ") values(#{captchaKey}, #{captchaCode}, #{expireAt}, 'NEW')";
    }

    public String buildSelectSessionSql(Map<String, Object> params) {
        boolean hasUsername = Boolean.TRUE.equals(params.get("hasUsername"));
        boolean hasRoleCode = Boolean.TRUE.equals(params.get("hasRoleCode"));
        String tokenColumn = safeColumn((String) params.get("tokenColumn"), "session_token", "token");
        String expireColumn = safeColumn((String) params.get("expireColumn"), "expire_at", "expired_at");
        StringBuilder sql = new StringBuilder();
        sql.append("select us.user_id as userId, ");
        sql.append(hasUsername ? "us.username as sessionUsername, " : "null as sessionUsername, ");
        sql.append(hasRoleCode ? "us.role_code as sessionRoleCode, " : "null as sessionRoleCode, ");
        sql.append("u.username, u.role_code as userRoleCode, us.")
                .append(expireColumn)
                .append(" as expireAt, us.status from user_sessions us left join users u on us.user_id = u.id where us.")
                .append(tokenColumn)
                .append("=#{token} order by us.id desc limit 1");
        return sql.toString();
    }

    public String buildUpdateSessionStatusSql(Map<String, Object> params) {
        String tokenColumn = safeColumn((String) params.get("tokenColumn"), "session_token", "token");
        return "update user_sessions set status=#{status} where " + tokenColumn + "=#{token}";
    }

    public String buildTouchSessionSql(Map<String, Object> params) {
        String tokenColumn = safeColumn((String) params.get("tokenColumn"), "session_token", "token");
        return "update user_sessions set last_access_at=now() where " + tokenColumn + "=#{token}";
    }

    public String buildInsertUserSessionSql(Map<String, Object> params) {
        boolean hasUsername = Boolean.TRUE.equals(params.get("hasUsername"));
        boolean hasRoleCode = Boolean.TRUE.equals(params.get("hasRoleCode"));
        String tokenColumn = safeColumn((String) params.get("tokenColumn"), "session_token", "token");
        String expireColumn = safeColumn((String) params.get("expireColumn"), "expire_at", "expired_at");
        List<String> columns = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        columns.add(tokenColumn);
        values.add("#{token}");
        columns.add("user_id");
        values.add("#{userId}");
        if (hasUsername) {
            columns.add("username");
            values.add("#{username}");
        }
        if (hasRoleCode) {
            columns.add("role_code");
            values.add("#{roleCode}");
        }
        columns.add(expireColumn);
        values.add("#{expireAt}");
        columns.add("status");
        values.add("'ACTIVE'");
        return "insert into user_sessions(" + join(columns) + ") values(" + join(values) + ")";
    }

    public String buildSelectPasswordResetSql(Map<String, Object> params) {
        String tokenColumn = safeColumn((String) params.get("tokenColumn"), "reset_token", "token");
        String expireColumn = safeColumn((String) params.get("expireColumn"), "expire_at", "expired_at");
        return "select pr.id, pr.user_id as userId, pr.status, pr." + expireColumn + " as expireAt from password_reset_records pr " +
                "left join users u on pr.user_id = u.id where u.username=#{username} and pr." + tokenColumn + "=#{resetToken} order by pr.id desc limit 1";
    }

    public String buildUpdatePasswordResetUsedSql(Map<String, Object> params) {
        boolean hasUsedAt = Boolean.TRUE.equals(params.get("hasUsedAt"));
        return hasUsedAt
                ? "update password_reset_records set status='USED', used_at=now() where id=#{id}"
                : "update password_reset_records set status='USED' where id=#{id}";
    }

    public String buildInsertPasswordResetSql(Map<String, Object> params) {
        boolean hasUsername = Boolean.TRUE.equals(params.get("hasUsername"));
        String tokenColumn = safeColumn((String) params.get("tokenColumn"), "reset_token", "token");
        String expireColumn = safeColumn((String) params.get("expireColumn"), "expire_at", "expired_at");
        List<String> columns = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        columns.add("user_id");
        values.add("#{userId}");
        if (hasUsername) {
            columns.add("username");
            values.add("#{username}");
        }
        columns.add(tokenColumn);
        values.add("#{resetToken}");
        columns.add(expireColumn);
        values.add("#{expireAt}");
        columns.add("status");
        values.add("'NEW'");
        return "insert into password_reset_records(" + join(columns) + ") values(" + join(values) + ")";
    }

    private String safeColumn(String column, String... allowed) {
        for (String item : allowed) {
            if (item.equals(column)) {
                return column;
            }
        }
        throw new IllegalArgumentException("非法字段: " + column);
    }

    private String join(List<String> items) {
        return String.join(",", items);
    }
}
