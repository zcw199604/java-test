package com.example.tobacco.auth;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.PasswordCodec;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordCodec passwordCodec;
    private final AuditService auditService;
    private final SecurityManager securityManager;

    public AuthService(JdbcTemplate jdbcTemplate, PasswordCodec passwordCodec, AuditService auditService, SecurityManager securityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordCodec = passwordCodec;
        this.auditService = auditService;
        this.securityManager = securityManager;
    }

    public Map<String, String> createCaptcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        String captchaCode = String.valueOf(1000 + (int) (Math.random() * 9000));
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10);
        insertCaptchaRecord(captchaKey, captchaCode, expireAt);
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("captchaKey", captchaKey);
        result.put("captchaCode", captchaCode);
        result.put("uuid", captchaKey);
        result.put("code", captchaCode);
        result.put("expiredAt", expireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        validateCaptchaIfPresent(request.getCaptchaKey(), request.getCaptchaCode());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.username, u.password, u.real_name, u.role_code, u.status, r.name as role_name from users u left join roles r on u.role_code = r.code where u.username = ?",
                request.getUsername());
        if (rows.isEmpty()) {
            auditService.logLogin(null, request.getUsername(), "FAIL", "用户名不存在", clientIp(httpRequest), clientAgent(httpRequest));
            throw new IllegalArgumentException("用户名或密码错误");
        }
        Map<String, Object> row = rows.get(0);
        if (!passwordCodec.matches(request.getUsername(), request.getPassword(), String.valueOf(row.get("password")))) {
            auditService.logLogin(((Number) row.get("id")).longValue(), request.getUsername(), "FAIL", "密码错误", clientIp(httpRequest), clientAgent(httpRequest));
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!"ENABLED".equals(String.valueOf(row.get("status")))) {
            auditService.logLogin(((Number) row.get("id")).longValue(), request.getUsername(), "FAIL", "账号已禁用", clientIp(httpRequest), clientAgent(httpRequest));
            throw new IllegalArgumentException("当前账号已禁用");
        }

        Subject subject = new Subject.Builder(securityManager).buildSubject();
        subject.login(new UsernamePasswordToken(request.getUsername(), request.getPassword()));

        String token = UUID.randomUUID().toString().replace("-", "");
        Long userId = ((Number) row.get("id")).longValue();
        insertUserSession(token, userId, request.getUsername(), String.valueOf(row.get("role_code")), LocalDateTime.now().plusHours(12));
        auditService.logLogin(userId, request.getUsername(), "SUCCESS", "登录成功", clientIp(httpRequest), clientAgent(httpRequest));
        auditService.logOperation(userId, request.getUsername(), "AUTH", "LOGIN", "USER_SESSION", userId, "用户登录");
        return buildResponse(toProfile(row), token);
    }

    public LoginResponse currentProfile(String username, String token) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.username, u.real_name, u.role_code, u.status, r.name as role_name from users u left join roles r on u.role_code = r.code where u.username = ?",
                username);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return buildResponse(toProfile(rows.get(0)), token);
    }

    public void logout(String token) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select user_id as userId from user_sessions where " + userSessionTokenColumn() + "=? order by id desc limit 1",
                token);
        jdbcTemplate.update("update user_sessions set status='LOGOUT' where " + userSessionTokenColumn() + "=?", token);
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            Long userId = row.get("userId") == null ? null : ((Number) row.get("userId")).longValue();
            String username = findUsernameByUserId(userId);
            auditService.logOperation(userId, username, "AUTH", "LOGOUT", "USER_SESSION", userId, "用户登出");
        }
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id from users where username=?", request.getUsername());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Long userId = ((Number) rows.get(0).get("id")).longValue();
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        insertPasswordResetRecord(userId, request.getUsername(), resetToken, LocalDateTime.now().plusMinutes(30));
        auditService.logOperation(userId, request.getUsername(), "AUTH", "FORGOT_PASSWORD", "PASSWORD_RESET", userId, "发起密码重置");
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("resetToken", resetToken);
        result.put("token", resetToken);
        result.put("message", "reset token generated");
        return result;
    }

    public void resetPassword(ResetPasswordRequest request) {
        String tokenColumn = passwordResetTokenColumn();
        String expireColumn = passwordResetExpireColumn();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select pr.id, pr.user_id as userId, pr.status, pr." + expireColumn + " as expireAt from password_reset_records pr " +
                        "left join users u on pr.user_id = u.id where u.username=? and pr." + tokenColumn + "=? order by pr.id desc limit 1",
                request.getUsername(), request.getResetToken());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("重置凭证无效");
        }
        Map<String, Object> row = rows.get(0);
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (expireAt == null || expireAt.isBefore(LocalDateTime.now()) || !"NEW".equals(String.valueOf(row.get("status")))) {
            throw new IllegalArgumentException("重置凭证已失效");
        }
        String encoded = passwordCodec.encode(request.getUsername(), request.getNewPassword());
        jdbcTemplate.update("update users set password=? where username=?", encoded, request.getUsername());
        if (columnExists("password_reset_records", "used_at")) {
            jdbcTemplate.update("update password_reset_records set status='USED', used_at=now() where id=?", ((Number) row.get("id")).longValue());
        } else {
            jdbcTemplate.update("update password_reset_records set status='USED' where id=?", ((Number) row.get("id")).longValue());
        }
        auditService.logOperation(((Number) row.get("userId")).longValue(), request.getUsername(), "AUTH", "RESET_PASSWORD", "PASSWORD_RESET", ((Number) row.get("id")).longValue(), "完成密码重置");
    }

    public List<String> permissions(String roleCode) {
        return jdbcTemplate.queryForList("select permission_code from role_permissions where role_code=? order by permission_code", String.class, roleCode);
    }

    public List<String> menus(String roleCode) {
        List<String> menus = new ArrayList<String>();
        if ("SUPER_ADMIN".equals(roleCode) || "ADMIN".equals(roleCode)) {
            menus.addAll(Arrays.asList("dashboard", "system-users", "system-roles", "permissions", "configs", "catalog-products", "supplier-list", "customer-list", "warehouse", "purchase", "inventory", "sales", "report", "logs", "messages"));
        } else if ("PURCHASER".equals(roleCode)) {
            menus.addAll(Arrays.asList("dashboard", "purchase", "catalog-products", "supplier-list", "messages"));
        } else if ("SELLER".equals(roleCode)) {
            menus.addAll(Arrays.asList("dashboard", "sales", "customer-list", "catalog-products", "messages"));
        } else {
            menus.addAll(Arrays.asList("dashboard", "inventory", "warehouse", "messages"));
        }
        return menus;
    }

    private LoginResponse buildResponse(UserProfile profile, String token) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(profile.getId());
        response.setUsername(profile.getUsername());
        response.setRealName(profile.getRealName());
        response.setRoleCode(profile.getRoleCode());
        response.setRoleName(profile.getRoleName());
        response.setMenus(menus(profile.getRoleCode()));
        response.setPermissions(permissions(profile.getRoleCode()));
        return response;
    }

    private UserProfile toProfile(Map<String, Object> row) {
        UserProfile profile = new UserProfile();
        profile.setId(((Number) row.get("id")).longValue());
        profile.setUsername(String.valueOf(row.get("username")));
        profile.setRealName(String.valueOf(row.get("real_name")));
        profile.setRoleCode(String.valueOf(row.get("role_code")));
        profile.setRoleName(String.valueOf(row.get("role_name")));
        profile.setStatus(String.valueOf(row.get("status")));
        return profile;
    }

    private void validateCaptchaIfPresent(String captchaKey, String captchaCode) {
        if (!hasText(captchaKey) && !hasText(captchaCode)) {
            return;
        }
        validateCaptcha(captchaKey, captchaCode);
    }

    private void validateCaptcha(String captchaKey, String captchaCode) {
        if (!hasText(captchaKey) || !hasText(captchaCode)) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        String keyColumn = captchaKeyColumn();
        String codeColumn = captchaCodeColumn();
        String expireColumn = captchaExpireColumn();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, " + codeColumn + " as captchaCode, " + expireColumn + " as expireAt, status from captcha_records where " + keyColumn + "=? order by id desc limit 1",
                captchaKey);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("验证码无效");
        }
        Map<String, Object> row = rows.get(0);
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (!"NEW".equals(String.valueOf(row.get("status"))) || expireAt == null || expireAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码已失效");
        }
        if (!String.valueOf(row.get("captchaCode")).equals(captchaCode)) {
            throw new IllegalArgumentException("验证码错误");
        }
        jdbcTemplate.update("update captcha_records set status='USED' where id=?", ((Number) row.get("id")).longValue());
    }

    private void insertCaptchaRecord(String captchaKey, String captchaCode, LocalDateTime expireAt) {
        List<String> columns = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        columns.add(captchaKeyColumn());
        values.add(captchaKey);
        columns.add(captchaCodeColumn());
        values.add(captchaCode);
        columns.add(captchaExpireColumn());
        values.add(Timestamp.valueOf(expireAt));
        columns.add("status");
        values.add("NEW");
        jdbcTemplate.update("insert into captcha_records(" + join(columns) + ") values(" + placeholders(columns.size()) + ")", values.toArray());
    }

    private void insertUserSession(String token, Long userId, String username, String roleCode, LocalDateTime expireAt) {
        List<String> columns = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        columns.add(userSessionTokenColumn());
        values.add(token);
        columns.add("user_id");
        values.add(userId);
        if (columnExists("user_sessions", "username")) {
            columns.add("username");
            values.add(username);
        }
        if (columnExists("user_sessions", "role_code")) {
            columns.add("role_code");
            values.add(roleCode);
        }
        columns.add(userSessionExpireColumn());
        values.add(Timestamp.valueOf(expireAt));
        columns.add("status");
        values.add("ACTIVE");
        jdbcTemplate.update("insert into user_sessions(" + join(columns) + ") values(" + placeholders(columns.size()) + ")", values.toArray());
    }

    private String findUsernameByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject("select username from users where id=?", String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    private void insertPasswordResetRecord(Long userId, String username, String resetToken, LocalDateTime expireAt) {
        List<String> columns = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        columns.add("user_id");
        values.add(userId);
        if (columnExists("password_reset_records", "username")) {
            columns.add("username");
            values.add(username);
        }
        columns.add(passwordResetTokenColumn());
        values.add(resetToken);
        columns.add(passwordResetExpireColumn());
        values.add(Timestamp.valueOf(expireAt));
        columns.add("status");
        values.add("NEW");
        jdbcTemplate.update("insert into password_reset_records(" + join(columns) + ") values(" + placeholders(columns.size()) + ")", values.toArray());
    }

    private String captchaKeyColumn() {
        return tableColumn("captcha_records", "captcha_key", "uuid");
    }

    private String captchaCodeColumn() {
        return tableColumn("captcha_records", "captcha_code", "code");
    }

    private String captchaExpireColumn() {
        return tableColumn("captcha_records", "expire_at", "expired_at");
    }

    private String userSessionTokenColumn() {
        return tableColumn("user_sessions", "session_token", "token");
    }

    private String userSessionExpireColumn() {
        return tableColumn("user_sessions", "expire_at", "expired_at");
    }

    private String passwordResetTokenColumn() {
        return tableColumn("password_reset_records", "reset_token", "token");
    }

    private String passwordResetExpireColumn() {
        return tableColumn("password_reset_records", "expire_at", "expired_at");
    }

    private String tableColumn(String tableName, String primary, String fallback) {
        return columnExists(tableName, primary) ? primary : fallback;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private String join(List<String> columns) {
        return String.join(",", columns);
    }

    private String placeholders(int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('?');
        }
        return builder.toString();
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
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

    private String clientIp(HttpServletRequest request) {
        return request == null ? "" : request.getRemoteAddr();
    }

    private String clientAgent(HttpServletRequest request) {
        return request == null ? "" : String.valueOf(request.getHeader("User-Agent"));
    }
}
