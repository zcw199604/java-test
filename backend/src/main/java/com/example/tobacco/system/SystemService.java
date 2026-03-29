package com.example.tobacco.system;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.auth.AuthService;
import com.example.tobacco.mapper.system.SystemMapper;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.PasswordCodec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemService {

    private final PasswordCodec passwordCodec;
    private final SystemMapper systemMapper;
    private final AuditService auditService;
    private final AuthService authService;

    public SystemService(PasswordCodec passwordCodec, AuditService auditService, AuthService authService, SystemMapper systemMapper) {
        this.passwordCodec = passwordCodec;
        this.auditService = auditService;
        this.authService = authService;
        this.systemMapper = systemMapper;
    }

    public List<UserProfile> listUsers() {
        return systemMapper.selectUsers();
    }

    public Map<String, Object> userDetail(Long id) {
        Map<String, Object> detail = systemMapper.selectUserDetail(id);
        List<Map<String, Object>> scopes = systemMapper.selectDataScopes(id);
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
        systemMapper.insertUser(username, passwordCodec.encode(username, password), realName, roleCode, request.getOrDefault("status", "ENABLED"));
        Long userId = systemMapper.selectUserIdByUsername(username);
        replaceDataScopes(userId, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_USER", "USER", userId, "创建用户");
    }

    @Transactional
    public void updateUser(Long id, Map<String, String> request, Long operatorId, String operatorName, String operatorRoleCode) {
        Map<String, Object> existing = systemMapper.selectUserBaseById(id);
        ensureManageUserPermission(operatorRoleCode, String.valueOf(existing.get("roleCode")), "修改管理员账号");
        String targetRoleCode = valueOrDefault(request.get("roleCode"), String.valueOf(existing.get("roleCode")));
        ensureAssignableRole(operatorRoleCode, targetRoleCode);
        systemMapper.updateUser(
                id,
                valueOrDefault(request.get("realName"), String.valueOf(existing.get("realName"))),
                targetRoleCode,
                valueOrDefault(request.get("status"), String.valueOf(existing.get("status"))));
        if (hasText(request.get("password"))) {
            String username = String.valueOf(existing.get("username"));
            systemMapper.updateUserPasswordById(id, passwordCodec.encode(username, request.get("password")));
        }
        replaceDataScopes(id, request.get("scopeType"), request.get("scopeValue"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER", "USER", id, "更新用户");
    }

    public void updateUserStatus(Long id, String status, Long operatorId, String operatorName, String operatorRoleCode) {
        String targetRoleCode = systemMapper.selectUserRoleCodeById(id);
        ensureManageUserPermission(operatorRoleCode, targetRoleCode, "变更管理员账号状态");
        String targetStatus = hasText(status) ? status.trim() : "ENABLED";
        systemMapper.updateUserStatus(id, targetStatus);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_USER_STATUS", "USER", id, "更新用户状态为" + targetStatus);
    }

    @Transactional
    public void deleteUser(Long id, Long operatorId, String operatorName, String operatorRoleCode) {
        Map<String, Object> user = systemMapper.selectUserBaseById(id);
        ensureManageUserPermission(operatorRoleCode, String.valueOf(user.get("roleCode")), "删除管理员账号");
        String username = String.valueOf(user.get("username"));
        systemMapper.deleteUserDataScopes(id);
        systemMapper.deleteUserSessions(id);
        systemMapper.deleteUser(id);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "DELETE_USER", "USER", id, "删除用户" + username);
    }

    public List<Map<String, Object>> listRoles() {
        List<Map<String, Object>> roles = systemMapper.selectRoles();
        for (Map<String, Object> role : roles) {
            String code = String.valueOf(role.get("code"));
            role.put("status", "ENABLED");
            role.put("permissions", systemMapper.selectRolePermissions(code));
        }
        return roles;
    }

    public void createRole(Map<String, String> request, Long operatorId, String operatorName) {
        systemMapper.insertRole(request.get("code"), request.get("name"), request.get("remark"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_ROLE", "ROLE", null, "创建角色" + request.get("code"));
    }

    @Transactional
    public void updateRole(String code, Map<String, Object> request, Long operatorId, String operatorName) {
        Map<String, Object> role = systemMapper.selectRoleByCode(code);
        String name = request.get("name") == null ? String.valueOf(role.get("name")) : String.valueOf(request.get("name"));
        String remark = request.get("remark") == null ? stringValue(role.get("remark")) : stringValue(request.get("remark"));
        systemMapper.updateRole(code, name, remark);
        systemMapper.deleteRolePermissions(code);
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
                systemMapper.insertRolePermission(code, permissionCode);
            }
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_ROLE", "ROLE", null, "更新角色" + code);
    }

    public List<Map<String, Object>> listPermissions() {
        return systemMapper.selectPermissions();
    }

    public void createPermission(Map<String, String> request, Long operatorId, String operatorName) {
        systemMapper.insertPermission(request.get("code"), request.get("name"), request.get("module"), request.get("remark"));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_PERMISSION", "PERMISSION", null, request.get("code"));
    }

    public List<Map<String, Object>> listConfigs() {
        return systemMapper.selectConfigs();
    }

    public void updateConfig(String key, Map<String, String> request, Long operatorId, String operatorName) {
        int updated = systemMapper.updateConfig(key, request.get("configValue"), request.get("remark"));
        if (updated == 0) {
            systemMapper.insertConfig(key, request.get("configValue"), request.get("remark"));
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_CONFIG", "CONFIG", null, key);
    }

    public Map<String, Object> profile(String username) {
        Map<String, Object> profile = systemMapper.selectProfileByUsername(username);
        profile.put("permissions", authService.permissions(String.valueOf(profile.get("roleCode"))));
        profile.put("menus", authService.menus(String.valueOf(profile.get("roleCode"))));
        profile.put("dataScopes", systemMapper.selectDataScopes(((Number) profile.get("id")).longValue()));
        return profile;
    }

    public void updateProfile(String username, Map<String, String> request, Long operatorId) {
        systemMapper.updateUserRealName(username, request.get("realName"));
        auditService.logOperation(operatorId, username, "SYSTEM", "UPDATE_PROFILE", "USER", operatorId, "更新个人中心");
    }

    public void changePassword(String username, String oldPassword, String newPassword, Long operatorId) {
        String encodedPassword = systemMapper.selectPasswordByUsername(username);
        if (!passwordCodec.matches(username, oldPassword, encodedPassword)) {
            throw new IllegalArgumentException("原密码错误");
        }
        systemMapper.updateUserPassword(username, passwordCodec.encode(username, newPassword));
        auditService.logOperation(operatorId, username, "SYSTEM", "CHANGE_PASSWORD", "USER", operatorId, "修改个人密码");
    }

    public List<Map<String, Object>> warehouses(String keyword, String status) {
        boolean hasCode = warehouseHasCodeColumn();
        List<Map<String, Object>> warehouses = systemMapper.selectWarehouses(hasCode, trim(keyword), likeValue(keyword), trim(status));
        for (Map<String, Object> warehouse : warehouses) {
            if (warehouse.get("code") == null) {
                warehouse.put("code", warehouse.get("id"));
            }
        }
        return warehouses;
    }

    public Map<String, Object> warehouseDetail(Long id) {
        Map<String, Object> detail = systemMapper.selectWarehouseDetail(id, warehouseHasCodeColumn());
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
            systemMapper.insertWarehouseWithCode(generateWarehouseCode(), name, address, status);
        } else {
            systemMapper.insertWarehouseWithoutCode(name, address, status);
        }
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "CREATE_WAREHOUSE", "WAREHOUSE", null, name);
    }

    public void updateWarehouse(Long id, Map<String, String> request, Long operatorId, String operatorName) {
        Map<String, Object> existing = warehouseDetail(id);
        systemMapper.updateWarehouse(
                id,
                normalizeText(valueOrDefault(request.get("name"), String.valueOf(existing.get("name")))),
                normalizeText(valueOrDefault(request.get("address"), stringValue(existing.get("address")))),
                valueOrDefault(request.get("status"), String.valueOf(existing.get("status"))));
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_WAREHOUSE", "WAREHOUSE", id, "更新仓库" + id);
    }

    public void updateWarehouseStatus(Long id, String status, Long operatorId, String operatorName) {
        String targetStatus = hasText(status) ? status.trim() : "ENABLED";
        systemMapper.updateWarehouseStatus(id, targetStatus);
        auditService.logOperation(operatorId, operatorName, "SYSTEM", "UPDATE_WAREHOUSE_STATUS", "WAREHOUSE", id, "更新仓库状态为" + targetStatus);
    }

    private boolean warehouseHasCodeColumn() {
        Integer count = systemMapper.countWarehouseCodeColumn();
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
        systemMapper.deleteUserDataScopes(userId);
        if (hasText(scopeType)) {
            systemMapper.insertUserDataScope(userId, scopeType.trim(), hasText(scopeValue) ? scopeValue.trim() : "ALL");
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

    private String trim(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String likeValue(String value) {
        return hasText(value) ? "%" + value.trim() + "%" : null;
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
