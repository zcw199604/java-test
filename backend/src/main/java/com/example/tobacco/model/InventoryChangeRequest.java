package com.example.tobacco.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class InventoryChangeRequest {
    @NotNull private Long productId;
    @NotNull @Min(1) private Integer quantity;
    @NotBlank private String remark;
    private Long warehouseId;
    private Long fromWarehouseId;
    private Long toWarehouseId;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(Long fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
    public Long getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }
}
