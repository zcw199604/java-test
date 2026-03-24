package com.example.tobacco.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class InventoryChangeRequest {
    @NotNull private Long productId;
    @NotNull @Min(1) private Integer quantity;
    @NotBlank private String remark;
    private String fromWarehouse;
    private String toWarehouse;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getFromWarehouse() { return fromWarehouse; }
    public void setFromWarehouse(String fromWarehouse) { this.fromWarehouse = fromWarehouse; }
    public String getToWarehouse() { return toWarehouse; }
    public void setToWarehouse(String toWarehouse) { this.toWarehouse = toWarehouse; }
}
