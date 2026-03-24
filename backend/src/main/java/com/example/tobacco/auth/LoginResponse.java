package com.example.tobacco.auth;

import java.util.List;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleName;
    private List<String> menus;
    private List<String> permissions;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public List<String> getMenus() { return menus; }
    public void setMenus(List<String> menus) { this.menus = menus; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
