package com.example.tobacco.dto;

import java.util.List;

public class DashboardSummaryDto {

    private Integer purchaseCount;
    private Integer inventoryCount;
    private Integer salesCount;
    private Integer warningCount;
    private List<DashboardModuleDto> modules;

    public Integer getPurchaseCount() { return purchaseCount; }
    public void setPurchaseCount(Integer purchaseCount) { this.purchaseCount = purchaseCount; }
    public Integer getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(Integer inventoryCount) { this.inventoryCount = inventoryCount; }
    public Integer getSalesCount() { return salesCount; }
    public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }
    public List<DashboardModuleDto> getModules() { return modules; }
    public void setModules(List<DashboardModuleDto> modules) { this.modules = modules; }
}
