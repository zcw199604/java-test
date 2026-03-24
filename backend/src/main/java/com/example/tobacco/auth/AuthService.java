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
import java.util.*;

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
        String captchaCode = String.valueOf(1000 + new Random().nextInt(9000));
        jdbcTemplate.update("insert into captcha_records(captcha_key, captcha_code, expire_at, status) values(?,?,?,?)",
                captchaKey, captchaCode, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)), "NEW");
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("captchaKey", captchaKey);
        result.put("captchaCode", captchaCode);
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
        jdbcTemplate.update("insert into user_sessions(session_token, user_id, username, role_code, expire_at, status) values(?,?,?,?,?,?)",
                token, userId, request.getUsername(), String.valueOf(row.get("role_code")), Timestamp.valueOf(LocalDateTime.now().plusHours(12)), "ACTIVE");
        auditService.logLogin(userId, request.getUsername(), "SUCCESS", "登录成功", clientIp(httpRequest), clientAgent(httpRequest));
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
        jdbcTemplate.update("update user_sessions set status='LOGOUT' where session_token=?", token);
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id from users where username=?", request.getUsername());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Long userId = ((Number) rows.get(0).get("id")).longValue();
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("insert into password_reset_records(user_id, username, reset_token, expire_at, status) values(?,?,?,?,?)",
                userId, request.getUsername(), resetToken, Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)), "NEW");
        auditService.logOperation(userId, request.getUsername(), "AUTH", "FORGOT_PASSWORD", "PASSWORD_RESET", userId, "发起密码重置");
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("resetToken", resetToken);
        result.put("message", "reset token generated");
        return result;
    }

    public void resetPassword(ResetPasswordRequest request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, user_id as userId, status, expire_at as expireAt from password_reset_records where username=? and reset_token=? order by id desc limit 1",
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
        jdbcTemplate.update("update password_reset_records set status='USED', used_at=now() where id=?", ((Number) row.get("id")).longValue());
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
        if (captchaKey == null || captchaKey.trim().length() == 0) {
            return;
        }
        validateCaptcha(captchaKey, captchaCode);
    }

    private void validateCaptcha(String captchaKey, String captchaCode) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, captcha_code, expire_at as expireAt, status from captcha_records where captcha_key=? order by id desc limit 1",
                captchaKey);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("验证码无效");
        }
        Map<String, Object> row = rows.get(0);
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (!"NEW".equals(String.valueOf(row.get("status"))) || expireAt == null || expireAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码已失效");
        }
        if (!String.valueOf(row.get("captcha_code")).equals(captchaCode)) {
            throw new IllegalArgumentException("验证码错误");
        }
        jdbcTemplate.update("update captcha_records set status='USED' where id=?", ((Number) row.get("id")).longValue());
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
