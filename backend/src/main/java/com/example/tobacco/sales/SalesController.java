package com.example.tobacco.sales;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CreateSalesRequest;
import com.example.tobacco.model.PaymentRequest;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.SalesOrderItem;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService salesService;
    public SalesController(SalesService salesService) { this.salesService = salesService; }

    @GetMapping
    public ApiResponse<List<SalesOrderItem>> list(HttpServletRequest request) {
        return ApiResponse.success(salesService.list(String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }

    @PostMapping
    public ApiResponse<SalesOrderItem> create(@Validated @RequestBody CreateSalesRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.create(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PostMapping("/{id}/outbound")
    public ApiResponse<SalesOrderItem> outbound(@PathVariable Long id, HttpServletRequest request) {
        return ApiResponse.success(salesService.outbound(id, String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }

    @PostMapping("/{id}/payment")
    public ApiResponse<SalesOrderItem> payment(@PathVariable Long id, @Validated @RequestBody PaymentRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(salesService.payment(id, request, String.valueOf(httpRequest.getAttribute("username")), String.valueOf(httpRequest.getAttribute("roleCode"))));
    }

    @GetMapping("/statistics")
    public ApiResponse<ReportSummaryItem> statistics(HttpServletRequest request) {
        return ApiResponse.success(salesService.statistics(String.valueOf(request.getAttribute("username")), String.valueOf(request.getAttribute("roleCode"))));
    }
}
