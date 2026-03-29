package com.example.tobacco.model;

import javax.validation.constraints.NotNull;

public class WarehouseActionRequest {
    @NotNull private Long warehouseId;
    private String remark;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
