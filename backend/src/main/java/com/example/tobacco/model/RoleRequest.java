package com.example.tobacco.model;

import javax.validation.constraints.NotBlank;

public class RoleRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String remark;
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
