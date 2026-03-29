package com.example.tobacco.interceptor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JdbcTemplate jdbcTemplate;

    public AuthInterceptor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/captcha") || uri.startsWith("/api/auth/forgot-password") || uri.startsWith("/api/auth/reset-password") || uri.startsWith("/api/health")) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(response, "未登录或令牌缺失");
        }
        String token = auth.substring(7);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(buildSessionQuery(), token);
        if (rows.isEmpty()) {
            return unauthorized(response, "登录已过期或令牌无效");
        }
        Map<String, Object> row = rows.get(0);
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (!"ACTIVE".equals(String.valueOf(row.get("status"))) || expireAt == null || expireAt.isBefore(LocalDateTime.now())) {
            return unauthorized(response, "登录已过期或令牌无效");
        }
        if (columnExists("user_sessions", "last_access_at")) {
            jdbcTemplate.update("update user_sessions set last_access_at=now() where " + userSessionTokenColumn() + "=?", token);
        }
        String username = hasText(stringValue(row.get("sessionUsername"))) ? stringValue(row.get("sessionUsername")) : stringValue(row.get("username"));
        String roleCode = hasText(stringValue(row.get("sessionRoleCode"))) ? stringValue(row.get("sessionRoleCode")) : stringValue(row.get("userRoleCode"));
        request.setAttribute("token", token);
        request.setAttribute("userId", ((Number) row.get("userId")).longValue());
        request.setAttribute("username", username);
        request.setAttribute("roleCode", roleCode);
        return true;
    }

    private String buildSessionQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("select us.user_id as userId, ");
        if (columnExists("user_sessions", "username")) {
            sql.append("us.username as sessionUsername, ");
        } else {
            sql.append("null as sessionUsername, ");
        }
        if (columnExists("user_sessions", "role_code")) {
            sql.append("us.role_code as sessionRoleCode, ");
        } else {
            sql.append("null as sessionRoleCode, ");
        }
        sql.append("u.username, u.role_code as userRoleCode, us.")
                .append(userSessionExpireColumn())
                .append(" as expireAt, us.status from user_sessions us left join users u on us.user_id = u.id where us.")
                .append(userSessionTokenColumn())
                .append("=? order by us.id desc limit 1");
        return sql.toString();
    }

    private String userSessionTokenColumn() {
        return columnExists("user_sessions", "session_token") ? "session_token" : "token";
    }

    private String userSessionExpireColumn() {
        return columnExists("user_sessions", "expire_at") ? "expire_at" : "expired_at";
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
        return false;
    }
}
