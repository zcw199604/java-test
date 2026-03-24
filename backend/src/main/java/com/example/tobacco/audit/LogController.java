package com.example.tobacco.audit;

import com.example.tobacco.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final AuditService auditService;

    public LogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/login")
    public ApiResponse<List<Map<String, Object>>> loginLogs() {
        return ApiResponse.success(auditService.loginLogs());
    }

    @GetMapping("/operation")
    public ApiResponse<List<Map<String, Object>>> operationLogs() {
        return ApiResponse.success(auditService.operationLogs());
    }
}
