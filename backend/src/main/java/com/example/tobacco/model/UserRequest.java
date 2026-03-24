package com.example.tobacco.model;

import javax.validation.constraints.NotBlank;

public class UserRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    @NotBlank private String roleCode;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
}
