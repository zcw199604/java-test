package com.example.tobacco.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSalesSeriesDto {

    private Long productId;
    private String productName;
    private List<BigDecimal> values;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<BigDecimal> getValues() {
        return values;
    }

    public void setValues(List<BigDecimal> values) {
        this.values = values;
    }
}
