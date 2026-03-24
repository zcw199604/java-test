package com.example.tobacco.controller;

import com.example.tobacco.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("status", "UP");
        result.put("service", "tobacco-platform-backend");
        return ApiResponse.success(result);
    }
}
