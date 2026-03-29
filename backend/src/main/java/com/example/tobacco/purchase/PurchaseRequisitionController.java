package com.example.tobacco.purchase;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.PurchaseOrderItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requisitions")
public class PurchaseRequisitionController {
    private final PurchaseService purchaseService;

    public PurchaseRequisitionController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public ApiResponse<List<PurchaseOrderItem>> list() {
        return ApiResponse.success(purchaseService.list());
    }
}
