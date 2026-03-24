package com.example.tobacco.sales;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CreateSalesRequest;
import com.example.tobacco.model.PaymentRequest;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.SalesOrderItem;
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
public class SalesController {
    private final SalesService salesService;
    public SalesController(SalesService salesService) { this.salesService = salesService; }

    @GetMapping("/api/sales")
    public ApiResponse<List<SalesOrderItem>> list() { return ApiResponse.success(salesService.list()); }

    @PostMapping("/api/sales")
    public ApiResponse<SalesOrderItem> create(@Validated @RequestBody CreateSalesRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.create(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PutMapping("/api/sales/{id}")
    public ApiResponse<SalesOrderItem> update(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("username") String username) { return ApiResponse.success(salesService.update(id, request, username)); }

    @PostMapping("/api/sales/{id}/audit")
    public ApiResponse<SalesOrderItem> audit(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("username") String username) { return ApiResponse.success(salesService.audit(id, request.getOrDefault("decision", "APPROVED"), request.get("remark"), username)); }

    @PostMapping("/api/sales/{id}/cancel")
    public ApiResponse<SalesOrderItem> cancel(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("username") String username) { return ApiResponse.success(salesService.cancel(id, request.get("reason"), username)); }

    @PostMapping("/api/sales/{id}/outbound")
    public ApiResponse<SalesOrderItem> outbound(@PathVariable Long id, @RequestAttribute("username") String username) { return ApiResponse.success(salesService.outbound(id, username)); }

    @PostMapping("/api/sales/{id}/payment")
    public ApiResponse<SalesOrderItem> payment(@PathVariable Long id, @Validated @RequestBody PaymentRequest request, @RequestAttribute("username") String username) { return ApiResponse.success(salesService.payment(id, request, username)); }

    @GetMapping("/api/sales/statistics")
    public ApiResponse<ReportSummaryItem> statistics() { return ApiResponse.success(salesService.statistics()); }

    @GetMapping("/api/sales/receivables")
    public ApiResponse<List<Map<String, Object>>> receivables() { return ApiResponse.success(salesService.receivables()); }

    @GetMapping("/api/sales/export")
    public ResponseEntity<byte[]> export() {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(salesService.exportExcel());
    }

    @PostMapping("/api/sales/import")
    public ApiResponse<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, @RequestAttribute("username") String username) { return ApiResponse.success(salesService.importExcel(file, username)); }

    @GetMapping("/api/sales-publishes")
    public ApiResponse<List<Map<String, Object>>> publishings() { return ApiResponse.success(salesService.publishings()); }

    @PostMapping("/api/sales-publishes")
    public ApiResponse<String> createPublishing(@RequestBody Map<String, String> request, @RequestAttribute("username") String username) {
        salesService.createPublishing(request, username);
        return ApiResponse.success("ok");
    }
}
