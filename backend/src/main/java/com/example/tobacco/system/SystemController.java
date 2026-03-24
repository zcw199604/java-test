package com.example.tobacco.system;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.RoleItem;
import com.example.tobacco.model.RoleRequest;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.model.UserRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) { this.systemService = systemService; }

    @GetMapping("/users")
    public ApiResponse<List<UserProfile>> users() { return ApiResponse.success(systemService.listUsers()); }

    @PostMapping("/users")
    public ApiResponse<String> createUser(@Validated @RequestBody UserRequest request) { systemService.createUser(request); return ApiResponse.success("ok"); }

    @PutMapping("/users/{id}/status")
    public ApiResponse<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        systemService.updateUserStatus(id, body.getOrDefault("status", "ENABLED"));
        return ApiResponse.success("ok");
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleItem>> roles() { return ApiResponse.success(systemService.listRoles()); }

    @PostMapping("/roles")
    public ApiResponse<String> createRole(@Validated @RequestBody RoleRequest request) { systemService.createRole(request); return ApiResponse.success("ok"); }
}
