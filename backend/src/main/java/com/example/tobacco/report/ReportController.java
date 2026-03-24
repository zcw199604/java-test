package com.example.tobacco.report;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.TrendPoint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/purchase-summary")
    public ApiResponse<ReportSummaryItem> purchaseSummary() {
        return ApiResponse.success(reportService.purchaseSummary());
    }

    @GetMapping("/sales-summary")
    public ApiResponse<ReportSummaryItem> salesSummary() {
        return ApiResponse.success(reportService.salesSummary());
    }

    @GetMapping("/inventory-summary")
    public ApiResponse<ReportSummaryItem> inventorySummary() {
        return ApiResponse.success(reportService.inventorySummary());
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendPoint>> trend() {
        return ApiResponse.success(reportService.trend());
    }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportData() {
        byte[] body = reportService.exportCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-summary.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
