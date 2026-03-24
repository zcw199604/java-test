package com.example.tobacco.system;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.auth.AuthService;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.PasswordCodec;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                "select u.id, u.username, u.real_name as realName, u.role_code as roleCode, r.name as roleName, u.status, DATE_FORMAT(u.created_at, '%Y-%m-%d %H:%i:%s') as createdAt from users u left join roles r on u.role_code = r.code order by u.id",
                new BeanPropertyRowMapper<UserProfile>(UserProfile.class));
    }

    public Map<String, Object> userDetail(Long id) {
        return jdbcTemplate.queryForMap("select id, username, real_name as realName, role_code as roleCode, status, email, phone from users where id=?", id);
    }

    @Transactional
    public void createUser(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into users(username,password,real_name,role_code,status,email,phone) values(?,?,?,?,?,?,?)",
                request.get("username"),
                passwordCodec.encode(request.get("username"), request.get("password")),
                request.get("realName"),
                request.get("roleCode"),
                request.getOrDefault("status", "ENABLED"),
                request.get("email"),
                request.get("phone"));
        Long userId = jdbcTemplate.queryForObject("select id from users where username=?", Long.class, request.get("username"));
        replaceDataScopes(userId, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_USER", "USER", userId, "创建用户");
    }

    @Transactional
    public void updateUser(Long id, Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("update users set real_name=?, role_code=?, status=?, email=?, phone=? where id=?",
                request.get("realName"), request.get("roleCode"), request.getOrDefault("status", "ENABLED"), request.get("email"), request.get("phone"), id);
        if (request.get("password") != null && request.get("password").trim().length() > 0) {
            String username = jdbcTemplate.queryForObject("select username from users where id=?", String.class, id);
            jdbcTemplate.update("update users set password=? where id=?", passwordCodec.encode(username, request.get("password")), id);
        }
        replaceDataScopes(id, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER", "USER", id, "更新用户");
    }

    public void updateUserStatus(Long id, String status, Long operatorId, String operatorName) {
        jdbcTemplate.update("update users set status=? where id=?", status, id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER_STATUS", "USER", id, "更新用户状态为" + status);
    }

    public List<Map<String, Object>> listRoles() {
        return jdbcTemplate.queryForList("select id, code, name, remark, status from roles order by id");
    }

    public void createRole(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into roles(code,name,remark,status) values(?,?,?,?)", request.get("code"), request.get("name"), request.get("remark"), request.getOrDefault("status", "ENABLED"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_ROLE", "ROLE", null, "创建角色" + request.get("code"));
    }

    @Transactional
    public void updateRole(String code, Map<String, Object> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("update roles set name=?, remark=?, status=? where code=?", request.get("name"), request.get("remark"), request.getOrDefault("status", "ENABLED"), code);
        jdbcTemplate.update("delete from role_permissions where role_code=?", code);
        Object permissions = request.get("permissions");
        if (permissions instanceof List) {
            List list = (List) permissions;
            for (Object permission : list) {
                jdbcTemplate.update("insert into role_permissions(role_code, permission_code) values(?,?)", code, String.valueOf(permission));
            }
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_ROLE", "ROLE", null, "更新角色" + code);
    }

    public List<Map<String, Object>> listPermissions() {
        return jdbcTemplate.queryForList("select id, code, name, module, action, remark from permissions order by id");
    }

    public void createPermission(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into permissions(code,name,module,action,remark) values(?,?,?,?,?)",
                request.get("code"), request.get("name"), request.get("module"), request.get("action"), request.get("remark"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_PERMISSION", "PERMISSION", null, request.get("code"));
    }

    public List<Map<String, Object>> listConfigs() {
        return jdbcTemplate.queryForList("select id, config_key as configKey, config_value as configValue, config_group as configGroup, remark, updated_by as updatedBy, DATE_FORMAT(updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from system_configs order by id");
    }

    public void updateConfig(String key, Map<String, String> request, Long operatorId, String operatorName) {
        int updated = jdbcTemplate.update("update system_configs set config_value=?, remark=?, updated_by=?, updated_at=now() where config_key=?",
                request.get("configValue"), request.get("remark"), operatorName, key);
        if (updated == 0) {
            jdbcTemplate.update("insert into system_configs(config_key, config_value, config_group, remark, updated_by) values(?,?,?,?,?)",
                    key, request.get("configValue"), request.getOrDefault("configGroup", "CUSTOM"), request.get("remark"), operatorName);
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_CONFIG", "CONFIG", null, key);
    }

    public Map<String, Object> profile(String username) {
        Map<String, Object> profile = jdbcTemplate.queryForMap("select id, username, real_name as realName, role_code as roleCode, email, phone, status from users where username=?", username);
        profile.put("permissions", authService.permissions(String.valueOf(profile.get("roleCode"))));
        profile.put("menus", authService.menus(String.valueOf(profile.get("roleCode"))));
        profile.put("dataScopes", jdbcTemplate.queryForList("select scope_type as scopeType, scope_value as scopeValue from user_data_scopes where user_id=?", ((Number) profile.get("id")).longValue()));
        return profile;
    }

    public void updateProfile(String username, Map<String, String> request, Long operatorId) {
        jdbcTemplate.update("update users set real_name=?, email=?, phone=? where username=?",
                request.get("realName"), request.get("email"), request.get("phone"), username);
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
        return jdbcTemplate.queryForList("select id, code, name, address, status from warehouses order by id");
    }

    public void createWarehouse(Map<String, String> request, Long operatorId, String operatorName) {
        jdbcTemplate.update("insert into warehouses(code,name,address,status) values(?,?,?,?)", request.get("code"), request.get("name"), request.get("address"), request.getOrDefault("status", "ENABLED"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_WAREHOUSE", "WAREHOUSE", null, request.get("code"));
    }

    private void replaceDataScopes(Long userId, String scopeType, String scopeValue) {
        jdbcTemplate.update("delete from user_data_scopes where user_id=?", userId);
        if (scopeType != null && scopeType.trim().length() > 0) {
            jdbcTemplate.update("insert into user_data_scopes(user_id, scope_type, scope_value) values(?,?,?)", userId, scopeType, scopeValue == null ? "ALL" : scopeValue);
        }
    }
}
