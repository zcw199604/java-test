package com.example.tobacco.model;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequest {
    @NotNull private BigDecimal amount;
    private String payerName;
    private String remark;
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
