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
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select session_token as token, user_id as userId, username, role_code as roleCode, expire_at as expireAt, status from user_sessions where session_token=? order by id desc limit 1",
                token);
        if (rows.isEmpty()) {
            return unauthorized(response, "登录已过期或令牌无效");
        }
        Map<String, Object> row = rows.get(0);
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (!"ACTIVE".equals(String.valueOf(row.get("status"))) || expireAt == null || expireAt.isBefore(LocalDateTime.now())) {
            return unauthorized(response, "登录已过期或令牌无效");
        }
        jdbcTemplate.update("update user_sessions set last_access_at=now() where session_token=?", token);
        request.setAttribute("token", token);
        request.setAttribute("userId", ((Number) row.get("userId")).longValue());
        request.setAttribute("username", String.valueOf(row.get("username")));
        request.setAttribute("roleCode", String.valueOf(row.get("roleCode")));
        return true;
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
