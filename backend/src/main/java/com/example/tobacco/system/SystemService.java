package com.example.tobacco.system;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.auth.AuthService;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.PasswordCodec;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
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
    public void createUser(Map<String, String> request, Long operatorId, String operatorName, String operatorRoleCode) {
        String username = required(request.get("username"), "账号不能为空");
        String password = required(request.get("password"), "初始密码不能为空");
        String realName = required(request.get("realName"), "姓名不能为空");
        String roleCode = required(request.get("roleCode"), "角色不能为空");
        ensureAssignableRole(operatorRoleCode, roleCode);
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
    public void updateUser(Long id, Map<String, String> request, Long operatorId, String operatorName, String operatorRoleCode) {
        Map<String, Object> existing = jdbcTemplate.queryForMap("select username, real_name as realName, role_code as roleCode, status from users where id=?", id);
        ensureManageUserPermission(operatorRoleCode, String.valueOf(existing.get("roleCode")), "修改管理员账号");
        String targetRoleCode = valueOrDefault(request.get("roleCode"), String.valueOf(existing.get("roleCode")));
        ensureAssignableRole(operatorRoleCode, targetRoleCode);
        jdbcTemplate.update("update users set real_name=?, role_code=?, status=? where id=?",
                valueOrDefault(request.get("realName"), String.valueOf(existing.get("realName"))),
                targetRoleCode,
                valueOrDefault(request.get("status"), String.valueOf(existing.get("status"))),
                id);
        if (hasText(request.get("password"))) {
            String username = String.valueOf(existing.get("username"));
            jdbcTemplate.update("update users set password=? where id=?", passwordCodec.encode(username, request.get("password")), id);
        }
        replaceDataScopes(id, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER", "USER", id, "更新用户");
    }

    public void updateUserStatus(Long id, String status, Long operatorId, String operatorName, String operatorRoleCode) {
        String targetRoleCode = jdbcTemplate.queryForObject("select role_code from users where id=?", String.class, id);
        ensureManageUserPermission(operatorRoleCode, targetRoleCode, "变更管理员账号状态");
        String targetStatus = hasText(status) ? status.trim() : "ENABLED";
        jdbcTemplate.update("update users set status=? where id=?", targetStatus, id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER_STATUS", "USER", id, "更新用户状态为" + targetStatus);
    }

    @Transactional
    public void deleteUser(Long id, Long operatorId, String operatorName, String operatorRoleCode) {
        Map<String, Object> user = jdbcTemplate.queryForMap("select username, role_code as roleCode from users where id=?", id);
        ensureManageUserPermission(operatorRoleCode, String.valueOf(user.get("roleCode")), "删除管理员账号");
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

    public List<Map<String, Object>> warehouses(String keyword, String status) {
        String codeSelect = warehouseHasCodeColumn() ? "code, " : "";
        StringBuilder sql = new StringBuilder("select id, " + codeSelect + "name, address, status from warehouses where 1=1");
        List<Object> params = new ArrayList<Object>();
        if (hasText(keyword)) {
            sql.append(" and (name like ? or address like ?)");
            String keywordValue = "%" + keyword.trim() + "%";
            params.add(keywordValue);
            params.add(keywordValue);
        }
        if (hasText(status)) {
            sql.append(" and status=?");
            params.add(status.trim());
        }
        sql.append(" order by id");
        List<Map<String, Object>> warehouses = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> warehouse : warehouses) {
            if (warehouse.get("code") == null) {
                warehouse.put("code", warehouse.get("id"));
            }
        }
        return warehouses;
    }

    public Map<String, Object> warehouseDetail(Long id) {
        String codeSelect = warehouseHasCodeColumn() ? "code, " : "";
        Map<String, Object> detail = jdbcTemplate.queryForMap("select id, " + codeSelect + "name, address, status from warehouses where id=?", id);
        if (detail.get("code") == null) {
            detail.put("code", detail.get("id"));
        }
        return detail;
    }

    public void createWarehouse(Map<String, String> request, Long operatorId, String operatorName) {
        String name = normalizeText(required(request.get("name"), "仓库名称不能为空"));
        String status = valueOrDefault(request.get("status"), "ENABLED");
        String address = normalizeText(request.get("address"));
        if (warehouseHasCodeColumn()) {
            jdbcTemplate.update("insert into warehouses(code,name,address,status) values(?,?,?,?)",
                    generateWarehouseCode(), name, address, status);
        } else {
            jdbcTemplate.update("insert into warehouses(name,address,status) values(?,?,?)",
                    name, address, status);
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_WAREHOUSE", "WAREHOUSE", null, name);
    }

    public void updateWarehouse(Long id, Map<String, String> request, Long operatorId, String operatorName) {
        Map<String, Object> existing = warehouseDetail(id);
        jdbcTemplate.update("update warehouses set name=?, address=?, status=? where id=?",
                normalizeText(valueOrDefault(request.get("name"), String.valueOf(existing.get("name")))),
                normalizeText(valueOrDefault(request.get("address"), stringValue(existing.get("address")))),
                valueOrDefault(request.get("status"), String.valueOf(existing.get("status"))),
                id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_WAREHOUSE", "WAREHOUSE", id, "更新仓库" + id);
    }

    public void updateWarehouseStatus(Long id, String status, Long operatorId, String operatorName) {
        String targetStatus = hasText(status) ? status.trim() : "ENABLED";
        jdbcTemplate.update("update warehouses set status=? where id=?", targetStatus, id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_WAREHOUSE_STATUS", "WAREHOUSE", id, "更新仓库状态为" + targetStatus);
    }

    private boolean warehouseHasCodeColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = database() and table_name = 'warehouses' and column_name = 'code'",
                Integer.class);
        return count != null && count > 0;
    }

    private String generateWarehouseCode() {
        return "WH" + System.currentTimeMillis();
    }

    private void ensureManageUserPermission(String operatorRoleCode, String existingRoleCode, String actionName) {
        if (isAdminRole(existingRoleCode) && !"SUPER_ADMIN".equalsIgnoreCase(operatorRoleCode)) {
            throw new IllegalArgumentException("仅超级管理员可" + actionName);
        }
    }

    private void ensureAssignableRole(String operatorRoleCode, String targetRoleCode) {
        if (isAdminRole(targetRoleCode) && !"SUPER_ADMIN".equalsIgnoreCase(operatorRoleCode)) {
            throw new IllegalArgumentException("仅超级管理员可将用户设置为超级管理员或普通管理员");
        }
    }

    private boolean isAdminRole(String roleCode) {
        return "SUPER_ADMIN".equalsIgnoreCase(roleCode) || "ADMIN".equalsIgnoreCase(roleCode);
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

    private String normalizeText(String value) {
        if (!hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (!trimmed.matches(".*[\u00C0-\u00FF].*")) {
            return trimmed;
        }
        try {
            return new String(trimmed.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return trimmed;
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
