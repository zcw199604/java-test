package com.example.tobacco.purchase;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CreatePurchaseRequest;
import com.example.tobacco.model.PurchaseOrderItem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/api/purchases")
    public ApiResponse<List<PurchaseOrderItem>> list() { return ApiResponse.success(purchaseService.list()); }

    @PostMapping("/api/purchases")
    public ApiResponse<PurchaseOrderItem> create(@Validated @RequestBody CreatePurchaseRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(purchaseService.create(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PutMapping("/api/purchases/{id}")
    public ApiResponse<PurchaseOrderItem> update(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("username") String username) {
        return ApiResponse.success(purchaseService.update(id, request, username));
    }

    @PostMapping("/api/purchases/{id}/audit")
    public ApiResponse<PurchaseOrderItem> audit(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("username") String username) {
        return ApiResponse.success(purchaseService.audit(id, request.getOrDefault("decision", "APPROVED"), request.get("remark"), username));
    }

    @PostMapping("/api/purchases/{id}/cancel")
    public ApiResponse<PurchaseOrderItem> cancel(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("username") String username) {
        return ApiResponse.success(purchaseService.cancel(id, request.get("reason"), username));
    }

    @PostMapping("/api/purchases/{id}/inbound")
    public ApiResponse<PurchaseOrderItem> inbound(@PathVariable Long id, @RequestAttribute("username") String username) { return ApiResponse.success(purchaseService.inbound(id, username)); }

    @PostMapping("/api/purchases/{id}/receive")
    public ApiResponse<PurchaseOrderItem> receive(@PathVariable Long id, @RequestAttribute("username") String username) { return ApiResponse.success(purchaseService.receive(id, username)); }

    @GetMapping("/api/purchases/{id}/trace")
    public ApiResponse<List<Map<String, Object>>> trace(@PathVariable Long id) { return ApiResponse.success(purchaseService.tracks(id)); }

    @GetMapping("/api/purchases/analysis")
    public ApiResponse<Map<String, Object>> analysis() { return ApiResponse.success(purchaseService.analysis()); }

    @GetMapping("/api/purchases/export")
    public ResponseEntity<byte[]> export() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchases.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(purchaseService.exportExcel());
    }

    @PostMapping("/api/purchases/import")
    public ApiResponse<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, @RequestAttribute("username") String username) {
        return ApiResponse.success(purchaseService.importExcel(file, username));
    }

    @GetMapping("/api/purchase-requisitions")
    public ApiResponse<List<Map<String, Object>>> requisitions() { return ApiResponse.success(purchaseService.requisitions()); }

    @PostMapping("/api/purchase-requisitions")
    public ApiResponse<String> createRequisition(@RequestBody Map<String, String> request, @RequestAttribute("username") String username) {
        purchaseService.createRequisition(request, username);
        return ApiResponse.success("ok");
    }
}
