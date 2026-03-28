package com.example.tobacco.sales;

import com.example.tobacco.common.result.ApiResponse;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulletins")
public class BulletinController {
    private final BulletinService bulletinService;

    public BulletinController(BulletinService bulletinService) {
        this.bulletinService = bulletinService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.success(bulletinService.list());
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody Map<String, String> body, HttpServletRequest httpRequest) {
        bulletinService.create(
                body.get("title"),
                body.get("content"),
                body.getOrDefault("category", "NOTICE"),
                body.get("expiredAt"),
                String.valueOf(httpRequest.getAttribute("username")));
        return ApiResponse.success("发布成功");
    }
}
