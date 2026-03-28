package com.example.tobacco.model;

import javax.validation.constraints.NotBlank;

public class AuditRequest {
    @NotBlank private String decision;
    private String remark;

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
