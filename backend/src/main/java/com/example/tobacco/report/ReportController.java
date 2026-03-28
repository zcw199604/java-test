package com.example.tobacco.report;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.TrendPoint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/purchase-summary")
    public ApiResponse<ReportSummaryItem> purchaseSummary() { return ApiResponse.success(reportService.purchaseSummary()); }

    @GetMapping("/sales-summary")
    public ApiResponse<ReportSummaryItem> salesSummary() { return ApiResponse.success(reportService.salesSummary()); }

    @GetMapping("/inventory-summary")
    public ApiResponse<ReportSummaryItem> inventorySummary() { return ApiResponse.success(reportService.inventorySummary()); }

    @GetMapping("/trend")
    public ApiResponse<List<TrendPoint>> trend() { return ApiResponse.success(reportService.trend()); }

    @GetMapping("/psi-summary")
    public ApiResponse<Map<String, Object>> psiSummary() { return ApiResponse.success(reportService.psiSummary()); }

    @GetMapping("/compliance-trace")
    public ApiResponse<List<Map<String, Object>>> complianceTrace() { return ApiResponse.success(reportService.complianceTrace()); }

    @GetMapping("/abnormal-docs")
    public ApiResponse<List<Map<String, Object>>> abnormalDocs() { return ApiResponse.success(reportService.abnormalDocs()); }

    @PostMapping("/abnormal-docs/{id}/audit")
    public ApiResponse<Map<String, Object>> auditAbnormalDoc(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest httpRequest) {
        return ApiResponse.success(reportService.auditAbnormalDoc(id, body.get("decision"), body.get("remark"), String.valueOf(httpRequest.getAttribute("username"))));
    }

    @GetMapping("/linkage")
    public ApiResponse<Map<String, Object>> linkage() { return ApiResponse.success(reportService.linkage()); }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-summary.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(reportService.exportExcel());
    }
}
