package com.example.tobacco.model;

import java.math.BigDecimal;

public class ReportSummaryItem {
    private String label;
    private Integer count;
    private BigDecimal amount;
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
