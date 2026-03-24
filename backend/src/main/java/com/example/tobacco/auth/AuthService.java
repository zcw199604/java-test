package com.example.tobacco.auth;

import com.example.tobacco.model.UserProfile;
import com.example.tobacco.util.JwtTokenUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(JdbcTemplate jdbcTemplate, JwtTokenUtil jwtTokenUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public LoginResponse login(LoginRequest request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.username, u.password, u.real_name, u.role_code, u.status, r.name as role_name from users u left join roles r on u.role_code = r.code where u.username = ?",
                request.getUsername());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        Map<String, Object> row = rows.get(0);
        if (!String.valueOf(row.get("password")).equals(request.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!"ENABLED".equals(String.valueOf(row.get("status")))) {
            throw new IllegalArgumentException("当前账号已禁用");
        }
        UserProfile profile = toProfile(row);
        return buildResponse(profile, true);
    }

    public LoginResponse currentProfile(String username) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.username, u.real_name, u.role_code, u.status, r.name as role_name from users u left join roles r on u.role_code = r.code where u.username = ?",
                username);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return buildResponse(toProfile(rows.get(0)), false);
    }

    private LoginResponse buildResponse(UserProfile profile, boolean includeToken) {
        LoginResponse response = new LoginResponse();
        response.setUserId(profile.getId());
        response.setUsername(profile.getUsername());
        response.setRealName(profile.getRealName());
        response.setRoleCode(profile.getRoleCode());
        response.setRoleName(profile.getRoleName());
        response.setMenus(resolveMenus(profile.getRoleCode()));
        if (includeToken) {
            response.setToken(jwtTokenUtil.generateToken(profile.getId(), profile.getUsername(), profile.getRoleCode()));
        }
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

    private List<String> resolveMenus(String roleCode) {
        if ("ADMIN".equals(roleCode)) {
            return Arrays.asList("dashboard", "system-users", "system-roles", "catalog-products", "supplier-list", "purchase", "inventory", "sales", "admin");
        }
        if ("PURCHASER".equals(roleCode)) {
            return Arrays.asList("dashboard", "catalog-products", "supplier-list", "purchase");
        }
        if ("SELLER".equals(roleCode)) {
            return Arrays.asList("dashboard", "catalog-products", "sales");
        }
        return Arrays.asList("dashboard", "catalog-products", "inventory");
    }
}
