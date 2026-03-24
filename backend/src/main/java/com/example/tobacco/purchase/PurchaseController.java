package com.example.tobacco.purchase;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CreatePurchaseRequest;
import com.example.tobacco.model.PurchaseOrderItem;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public ApiResponse<List<PurchaseOrderItem>> list() {
        return ApiResponse.success(purchaseService.list());
    }

    @PostMapping
    public ApiResponse<PurchaseOrderItem> create(@Validated @RequestBody CreatePurchaseRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.create(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PostMapping("/{id}/inbound")
    public ApiResponse<PurchaseOrderItem> inbound(@PathVariable Long id) {
        return ApiResponse.success(purchaseService.inbound(id));
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<PurchaseOrderItem> receive(@PathVariable Long id) {
        return ApiResponse.success(purchaseService.receive(id));
    }
}
