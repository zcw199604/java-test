package com.example.tobacco.auth;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class ShiroUserPrincipal implements Serializable {
    private Long userId;
    private String username;
    private String roleCode;
    private Set<String> permissions = new LinkedHashSet<String>();

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
}
