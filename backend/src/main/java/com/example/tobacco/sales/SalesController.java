package com.example.tobacco.sales;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService salesService;
    public SalesController(SalesService salesService) { this.salesService = salesService; }

    @GetMapping
    public ApiResponse<List<SalesOrderItem>> list(HttpServletRequest request) {
        return ApiResponse.success(salesService.list(String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesOrderItem> detail(@PathVariable Long id, HttpServletRequest request) {
        return ApiResponse.success(salesService.detail(id, String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }

    @PostMapping
    public ApiResponse<SalesOrderItem> create(@Validated @RequestBody CreateSalesRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.create(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PutMapping("/{id}")
    public ApiResponse<SalesOrderItem> update(@PathVariable Long id, @Validated @RequestBody CreateSalesRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.update(id, request, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<SalesOrderItem> audit(@PathVariable Long id, @Validated @RequestBody AuditRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.audit(id, request.getDecision(), request.getRemark(), String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesOrderItem> cancel(@PathVariable Long id, @RequestBody CancelRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.cancel(id, request.getReason(), String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/outbound")
    public ApiResponse<SalesOrderItem> outbound(@PathVariable Long id,
                                                @Validated @RequestBody WarehouseActionRequest request,
                                                HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.outbound(id, request, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/payment")
    public ApiResponse<SalesOrderItem> payment(@PathVariable Long id, @Validated @RequestBody PaymentRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.payment(id, request, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @GetMapping("/statistics")
    public ApiResponse<ReportSummaryItem> statistics(HttpServletRequest request) {
        return ApiResponse.success(salesService.statistics(String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }

    @GetMapping("/receivables")
    public ApiResponse<List<Map<String, Object>>> receivables(HttpServletRequest request) {
        return ApiResponse.success(salesService.receivables(String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }

    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.importFromExcel(file, String.valueOf(httpRequest.getAttribute("username"))));
    }
}
