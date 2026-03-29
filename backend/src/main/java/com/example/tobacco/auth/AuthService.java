package com.example.tobacco.auth;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.mapper.auth.AuthMapper;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.PasswordCodec;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
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

    private final AuthMapper authMapper;
    private final PasswordCodec passwordCodec;
    private final AuditService auditService;
    private final SecurityManager securityManager;

    public AuthService(AuthMapper authMapper, PasswordCodec passwordCodec, AuditService auditService, SecurityManager securityManager) {
        this.authMapper = authMapper;
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
        Map<String, Object> row = authMapper.selectLoginUserByUsername(request.getUsername());
        if (row == null || row.isEmpty()) {
            auditService.logLogin(null, request.getUsername(), "FAIL", "用户名不存在", clientIp(httpRequest), clientAgent(httpRequest));
            throw new IllegalArgumentException("用户名或密码错误");
        }
        Long userId = longValue(row.get("id"));
        if (!passwordCodec.matches(request.getUsername(), request.getPassword(), stringValue(row.get("password")))) {
            auditService.logLogin(userId, request.getUsername(), "FAIL", "密码错误", clientIp(httpRequest), clientAgent(httpRequest));
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!"ENABLED".equals(stringValue(row.get("status")))) {
            auditService.logLogin(userId, request.getUsername(), "FAIL", "账号已禁用", clientIp(httpRequest), clientAgent(httpRequest));
            throw new IllegalArgumentException("当前账号已禁用");
        }

        Subject subject = new Subject.Builder(securityManager).buildSubject();
        subject.login(new UsernamePasswordToken(request.getUsername(), request.getPassword()));

        String token = UUID.randomUUID().toString().replace("-", "");
        insertUserSession(token, userId, request.getUsername(), stringValue(row.get("roleCode")), LocalDateTime.now().plusHours(12));
        auditService.logLogin(userId, request.getUsername(), "SUCCESS", "登录成功", clientIp(httpRequest), clientAgent(httpRequest));
        auditService.logOperation(userId, request.getUsername(), "AUTH", "LOGIN", "USER_SESSION", userId, "用户登录");
        return buildResponse(toProfile(row), token);
    }

    public LoginResponse currentProfile(String username, String token) {
        Map<String, Object> row = authMapper.selectProfileByUsername(username);
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return buildResponse(toProfile(row), token);
    }

    public void logout(String token) {
        Map<String, Object> row = authMapper.selectLatestSessionByToken(
                userSessionHasUsernameColumn(),
                userSessionHasRoleCodeColumn(),
                userSessionTokenColumn(),
                userSessionExpireColumn(),
                token);
        authMapper.updateSessionStatus(userSessionTokenColumn(), "LOGOUT", token);
        if (row != null && !row.isEmpty()) {
            Long userId = longValue(row.get("userId"));
            String username = findUsernameByUserId(userId);
            auditService.logOperation(userId, username, "AUTH", "LOGOUT", "USER_SESSION", userId, "用户登出");
        }
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        Long userId = authMapper.selectUserIdByUsername(request.getUsername());
        if (userId == null) {
            throw new IllegalArgumentException("用户不存在");
        }
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
        Map<String, Object> row = authMapper.selectLatestPasswordReset(
                passwordResetTokenColumn(),
                passwordResetExpireColumn(),
                request.getUsername(),
                request.getResetToken());
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("重置凭证无效");
        }
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (expireAt == null || expireAt.isBefore(LocalDateTime.now()) || !"NEW".equals(stringValue(row.get("status")))) {
            throw new IllegalArgumentException("重置凭证已失效");
        }
        String encoded = passwordCodec.encode(request.getUsername(), request.getNewPassword());
        authMapper.updateUserPasswordByUsername(request.getUsername(), encoded);
        authMapper.updatePasswordResetUsed(passwordResetHasUsedAtColumn(), longValue(row.get("id")));
        auditService.logOperation(longValue(row.get("userId")), request.getUsername(), "AUTH", "RESET_PASSWORD", "PASSWORD_RESET", longValue(row.get("id")), "完成密码重置");
    }

    public List<String> permissions(String roleCode) {
        return authMapper.selectPermissionsByRoleCode(roleCode);
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
        profile.setId(longValue(row.get("id")));
        profile.setUsername(stringValue(row.get("username")));
        profile.setRealName(stringValue(row.get("realName")));
        profile.setRoleCode(stringValue(row.get("roleCode")));
        profile.setRoleName(stringValue(row.get("roleName")));
        profile.setStatus(stringValue(row.get("status")));
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
        Map<String, Object> row = authMapper.selectLatestCaptchaRecord(
                captchaKeyColumn(),
                captchaCodeColumn(),
                captchaExpireColumn(),
                captchaKey);
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("验证码无效");
        }
        LocalDateTime expireAt = toLocalDateTime(row.get("expireAt"));
        if (!"NEW".equals(stringValue(row.get("status"))) || expireAt == null || expireAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("验证码已失效");
        }
        if (!stringValue(row.get("captchaCode")).equals(captchaCode)) {
            throw new IllegalArgumentException("验证码错误");
        }
        authMapper.markCaptchaUsed(longValue(row.get("id")));
    }

    private void insertCaptchaRecord(String captchaKey, String captchaCode, LocalDateTime expireAt) {
        authMapper.insertCaptchaRecord(captchaKeyColumn(), captchaCodeColumn(), captchaExpireColumn(), captchaKey, captchaCode, expireAt);
    }

    private void insertUserSession(String token, Long userId, String username, String roleCode, LocalDateTime expireAt) {
        authMapper.insertUserSession(
                userSessionHasUsernameColumn(),
                userSessionHasRoleCodeColumn(),
                userSessionTokenColumn(),
                userSessionExpireColumn(),
                token,
                userId,
                username,
                roleCode,
                expireAt);
    }

    private String findUsernameByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return authMapper.selectUsernameByUserId(userId);
        } catch (Exception e) {
            return null;
        }
    }

    private void insertPasswordResetRecord(Long userId, String username, String resetToken, LocalDateTime expireAt) {
        authMapper.insertPasswordResetRecord(
                passwordResetHasUsernameColumn(),
                passwordResetTokenColumn(),
                passwordResetExpireColumn(),
                userId,
                username,
                resetToken,
                expireAt);
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

    private boolean userSessionHasUsernameColumn() {
        return columnExists("user_sessions", "username");
    }

    private boolean userSessionHasRoleCodeColumn() {
        return columnExists("user_sessions", "role_code");
    }

    private String passwordResetTokenColumn() {
        return tableColumn("password_reset_records", "reset_token", "token");
    }

    private String passwordResetExpireColumn() {
        return tableColumn("password_reset_records", "expire_at", "expired_at");
    }

    private boolean passwordResetHasUsernameColumn() {
        return columnExists("password_reset_records", "username");
    }

    private boolean passwordResetHasUsedAtColumn() {
        return columnExists("password_reset_records", "used_at");
    }

    private String tableColumn(String tableName, String primary, String fallback) {
        return columnExists(tableName, primary) ? primary : fallback;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = authMapper.countTableColumn(tableName, columnName);
        return count != null && count > 0;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private Long longValue(Object value) {
        return value == null ? null : ((Number) value).longValue();
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

    private String clientIp(HttpServletRequest request) {
        return request == null ? "" : request.getRemoteAddr();
    }

    private String clientAgent(HttpServletRequest request) {
        return request == null ? "" : String.valueOf(request.getHeader("User-Agent"));
    }
}
