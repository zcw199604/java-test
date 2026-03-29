package com.example.tobacco.purchase;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.AuditRequest;
import com.example.tobacco.model.CancelRequest;
import com.example.tobacco.model.CreatePurchaseRequest;
import com.example.tobacco.model.PurchaseOrderItem;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/requisitions")
    public ApiResponse<List<PurchaseOrderItem>> requisitions() {
        return ApiResponse.success(purchaseService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderItem> detail(@PathVariable Long id) {
        return ApiResponse.success(purchaseService.detail(id));
    }

    @GetMapping("/{id}/trace")
    public ApiResponse<List<Map<String, Object>>> trace(@PathVariable Long id) {
        return ApiResponse.success(purchaseService.trace(id));
    }

    @PostMapping
    public ApiResponse<PurchaseOrderItem> create(@Validated @RequestBody CreatePurchaseRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.create(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrderItem> update(@PathVariable Long id, @Validated @RequestBody CreatePurchaseRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.update(id, request, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<PurchaseOrderItem> audit(@PathVariable Long id, @Validated @RequestBody AuditRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.audit(id, request.getDecision(), request.getRemark(), String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseOrderItem> cancel(@PathVariable Long id, @RequestBody CancelRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.cancel(id, request.getReason(), String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PostMapping("/{id}/inbound")
    public ApiResponse<PurchaseOrderItem> inbound(@PathVariable Long id, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.inbound(id, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<PurchaseOrderItem> receive(@PathVariable Long id, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.receive(id, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.importFromExcel(file, String.valueOf(httpRequest.getAttribute("username"))));
    }
}
