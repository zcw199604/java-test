package com.example.tobacco.controller;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.dto.DashboardSalesHistoryDto;
import com.example.tobacco.dto.DashboardSummaryDto;
import com.example.tobacco.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryDto> summary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @GetMapping("/sales-history")
    public ApiResponse<DashboardSalesHistoryDto> salesHistory(@RequestParam(required = false) String metric,
                                                              @RequestParam(required = false) Integer days,
                                                              @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(dashboardService.getSalesHistory(metric, days, limit));
    }
}
