package com.example.tobacco.system;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.auth.AuthService;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.PasswordCodec;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordCodec passwordCodec;
    private final AuditService auditService;
    private final AuthService authService;

    public SystemService(JdbcTemplate jdbcTemplate, PasswordCodec passwordCodec, AuditService auditService, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordCodec = passwordCodec;
        this.auditService = auditService;
        this.authService = authService;
    }

    public List<UserProfile> listUsers() {
        return jdbcTemplate.query(
                "select u.id, u.username, u.real_name as realName, u.role_code as roleCode, r.name as roleName, u.status, " +
                        "DATE_FORMAT(u.created_at, '%Y-%m-%d %H:%i:%s') as createdAt, " +
                        "(select scope_type from user_data_scopes s where s.user_id=u.id order by s.id desc limit 1) as scopeType, " +
                        "(select scope_value from user_data_scopes s where s.user_id=u.id order by s.id desc limit 1) as scopeValue " +
                        "from users u left join roles r on u.role_code = r.code order by u.id",
                new BeanPropertyRowMapper<UserProfile>(UserProfile.class));
    }

    public Map<String, Object> userDetail(Long id) {
        Map<String, Object> detail = jdbcTemplate.queryForMap(
                "select id, username, real_name as realName, role_code as roleCode, status from users where id=?", id);
        List<Map<String, Object>> scopes = jdbcTemplate.queryForList(
                "select scope_type as scopeType, scope_value as scopeValue from user_data_scopes where user_id=? order by id", id);
        detail.put("dataScopes", scopes);
        if (!scopes.isEmpty()) {
            detail.put("scopeType", scopes.get(0).get("scopeType"));
            detail.put("scopeValue", scopes.get(0).get("scopeValue"));
        }
        return detail;
    }

    @Transactional
    public void createUser(Map<String, String> request, Long operatorId, String operatorName) {
        String username = required(request.get("username"), "账号不能为空");
        String password = required(request.get("password"), "初始密码不能为空");
        String realName = required(request.get("realName"), "姓名不能为空");
        String roleCode = required(request.get("roleCode"), "角色不能为空");
        jdbcTemplate.update("insert into users(username,password,real_name,role_code,status) values(?,?,?,?,?)",
                username,
                passwordCodec.encode(username, password),
                realName,
                roleCode,
                request.getOrDefault("status", "ENABLED"));
        Long userId = jdbcTemplate.queryForObject("select id from users where username=?", Long.class, username);
        replaceDataScopes(userId, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_USER", "USER", userId, "创建用户");
    }

    @Transactional
    public void updateUser(Long id, Map<String, String> request, Long operatorId, String operatorName) {
        Map<String, Object> existing = jdbcTemplate.queryForMap("select username, real_name as realName, role_code as roleCode, status from users where id=?", id);
        jdbcTemplate.update("update users set real_name=?, role_code=?, status=? where id=?",
                valueOrDefault(request.get("realName"), String.valueOf(existing.get("realName"))),
                valueOrDefault(request.get("roleCode"), String.valueOf(existing.get("roleCode"))),
                valueOrDefault(request.get("status"), String.valueOf(existing.get("status"))),
                id);
        if (hasText(request.get("password"))) {
            String username = String.valueOf(existing.get("username"));
            jdbcTemplate.update("update users set password=? where id=?", passwordCodec.encode(username, request.get("password")), id);
        }
        replaceDataScopes(id, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER", "USER", id, "更新用户");
    }

    public void updateUserStatus(Long id, String status, Long operatorId, String operatorName) {
        String targetStatus = hasText(status) ? status.trim() : "ENABLED";
        jdbcTemplate.update("update users set status=? where id=?", targetStatus, id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER_STATUS", "USER", id, "更新用户状态为" + targetStatus);
    }

    @Transactional
    public void deleteUser(Long id, Long operatorId, String operatorName) {
        Map<String, Object> user = jdbcTemplate.queryForMap("select username from users where id=?", id);
        String username = String.valueOf(user.get("username"));
        jdbcTemplate.update("delete from user_data_scopes where user_id=?", id);
        jdbcTemplate.update("delete from user_sessions where user_id=?", id);
        jdbcTemplate.update("delete from users where id=?", id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "DELETE_USER", "USER", id, "删除用户" + username);
    }

    public List<Map<String, Object>> listRoles() {
        List<Map<String, Object>> roles = jdbcTemplate.queryForList("select id, code, name, remark from roles order by id");
        for (Map<String, Object> role : roles) {
            String code = String.valueOf(role.get("code"));
            role.put("status", "ENABLED");
            role.put("permissions", jdbcTemplate.queryForList(
                    "select permission_code from role_permissions where role_code=? order by permission_code", String.class, code));
        }
        return roles;
    }

    public void createRole(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into roles(code,name,remark) values(?,?,?)",
                request.get("code"), request.get("name"), request.get("remark"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_ROLE", "ROLE", null, "创建角色" + request.get("code"));
    }

    @Transactional
    public void updateRole(String code, Map<String, Object> request, Long operatorId, String operatorName) {
        Map<String, Object> role = jdbcTemplate.queryForMap("select name, remark from roles where code=?", code);
        String name = request.get("name") == null ? String.valueOf(role.get("name")) : String.valueOf(request.get("name"));
        String remark = request.get("remark") == null ? stringValue(role.get("remark")) : stringValue(request.get("remark"));
        jdbcTemplate.update("update roles set name=?, remark=? where code=?", name, remark, code);
        jdbcTemplate.update("delete from role_permissions where role_code=?", code);
        Object permissions = request.get("permissions");
        if (permissions instanceof List) {
            List permissionList = (List) permissions;
            for (Object permission : permissionList) {
                if (permission == null) {
                    continue;
                }
                String permissionCode = String.valueOf(permission);
                if (!permissionCode.contains(":")) {
                    continue;
                }
                jdbcTemplate.update("insert into role_permissions(role_code, permission_code) values(?,?)", code, permissionCode);
            }
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_ROLE", "ROLE", null, "更新角色" + code);
    }

    public List<Map<String, Object>> listPermissions() {
        return jdbcTemplate.queryForList("select id, code, name, module, remark from permissions order by id");
    }

    public void createPermission(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into permissions(code,name,module,remark) values(?,?,?,?)",
                request.get("code"), request.get("name"), request.get("module"), request.get("remark"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_PERMISSION", "PERMISSION", null, request.get("code"));
    }

    public List<Map<String, Object>> listConfigs() {
        return jdbcTemplate.queryForList("select id, config_key as configKey, config_value as configValue, remark, DATE_FORMAT(updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from system_configs order by id");
    }

    public void updateConfig(String key, Map<String, String> request, Long operatorId, String operatorName) {
        int updated = jdbcTemplate.update("update system_configs set config_value=?, remark=?, updated_at=now() where config_key=?",
                request.get("configValue"), request.get("remark"), key);
        if (updated == 0) {
            jdbcTemplate.update("insert into system_configs(config_key, config_value, remark) values(?,?,?)",
                    key, request.get("configValue"), request.get("remark"));
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_CONFIG", "CONFIG", null, key);
    }

    public Map<String, Object> profile(String username) {
        Map<String, Object> profile = jdbcTemplate.queryForMap("select id, username, real_name as realName, role_code as roleCode, status from users where username=?", username);
        profile.put("permissions", authService.permissions(String.valueOf(profile.get("roleCode"))));
        profile.put("menus", authService.menus(String.valueOf(profile.get("roleCode"))));
        profile.put("dataScopes", jdbcTemplate.queryForList("select scope_type as scopeType, scope_value as scopeValue from user_data_scopes where user_id=?", ((Number) profile.get("id")).longValue()));
        return profile;
    }

    public void updateProfile(String username, Map<String, String> request, Long operatorId) {
        jdbcTemplate.update("update users set real_name=? where username=?",
                request.get("realName"), username);
        auditService.logOperation(operatorId, username, "SYSTEM", "UPDATE_PROFILE", "USER", operatorId, "更新个人中心");
    }

    public void changePassword(String username, String oldPassword, String newPassword, Long operatorId) {
        String encodedPassword = jdbcTemplate.queryForObject("select password from users where username=?", String.class, username);
        if (!passwordCodec.matches(username, oldPassword, encodedPassword)) {
            throw new IllegalArgumentException("原密码错误");
        }
        jdbcTemplate.update("update users set password=? where username=?", passwordCodec.encode(username, newPassword), username);
        auditService.logOperation(operatorId, username, "SYSTEM", "CHANGE_PASSWORD", "USER", operatorId, "修改个人密码");
    }

    public List<Map<String, Object>> warehouses() {
        List<Map<String, Object>> warehouses = jdbcTemplate.queryForList("select id, name, address, status from warehouses order by id");
        for (Map<String, Object> warehouse : warehouses) {
            warehouse.put("code", warehouse.get("id"));
        }
        return warehouses;
    }

    public void createWarehouse(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into warehouses(name,address,status) values(?,?,?)",
                request.get("name"), request.get("address"), request.getOrDefault("status", "ENABLED"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_WAREHOUSE", "WAREHOUSE", null, request.get("name"));
    }

    private void replaceDataScopes(Long userId, String scopeType, String scopeValue) {
        jdbcTemplate.update("delete from user_data_scopes where user_id=?", userId);
        if (hasText(scopeType)) {
            jdbcTemplate.update("insert into user_data_scopes(user_id, scope_type, scope_value) values(?,?,?)",
                    userId, scopeType.trim(), hasText(scopeValue) ? scopeValue.trim() : "ALL");
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String required(String value, String message) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
