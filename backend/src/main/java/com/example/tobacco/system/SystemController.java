package com.example.tobacco.system;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.UserProfile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserProfile>> users() { return ApiResponse.success(systemService.listUsers()); }

    @GetMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> userDetail(@PathVariable Long id) { return ApiResponse.success(systemService.userDetail(id)); }

    @PostMapping("/users")
    public ApiResponse<String> createUser(@RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username, @RequestAttribute("roleCode") String roleCode) {
        systemService.createUser(request, userId, username, roleCode);
        return ApiResponse.success("ok");
    }

    @PutMapping("/users/{id}")
    public ApiResponse<String> updateUser(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username, @RequestAttribute("roleCode") String roleCode) {
        systemService.updateUser(id, request, userId, username, roleCode);
        return ApiResponse.success("ok");
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username, @RequestAttribute("roleCode") String roleCode) {
        systemService.updateUserStatus(id, body.getOrDefault("status", "ENABLED"), userId, username, roleCode);
        return ApiResponse.success("ok");
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username, @RequestAttribute("roleCode") String roleCode) {
        systemService.deleteUser(id, userId, username, roleCode);
        return ApiResponse.success("ok");
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roles() { return ApiResponse.success(systemService.listRoles()); }

    @PostMapping("/roles")
    public ApiResponse<String> createRole(@RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.createRole(request, userId, username);
        return ApiResponse.success("ok");
    }

    @PutMapping("/roles/{code}")
    public ApiResponse<String> updateRole(@PathVariable String code, @RequestBody Map<String, Object> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.updateRole(code, request, userId, username);
        return ApiResponse.success("ok");
    }

    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> permissions() { return ApiResponse.success(systemService.listPermissions()); }

    @PostMapping("/permissions")
    public ApiResponse<String> createPermission(@RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.createPermission(request, userId, username);
        return ApiResponse.success("ok");
    }

    @GetMapping("/configs")
    public ApiResponse<List<Map<String, Object>>> configs() { return ApiResponse.success(systemService.listConfigs()); }

    @PutMapping("/configs/{key}")
    public ApiResponse<String> updateConfig(@PathVariable String key, @RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.updateConfig(key, request, userId, username);
        return ApiResponse.success("ok");
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile(@RequestAttribute("username") String username) { return ApiResponse.success(systemService.profile(username)); }

    @PutMapping("/profile")
    public ApiResponse<String> updateProfile(@RequestBody Map<String, String> request, @RequestAttribute("username") String username, @RequestAttribute("userId") Long userId) {
        systemService.updateProfile(username, request, userId);
        return ApiResponse.success("ok");
    }

    @PostMapping("/profile/password")
    public ApiResponse<String> changePassword(@RequestBody Map<String, String> request, @RequestAttribute("username") String username, @RequestAttribute("userId") Long userId) {
        systemService.changePassword(username, request.get("oldPassword"), request.get("newPassword"), userId);
        return ApiResponse.success("ok");
    }

    @GetMapping("/warehouses")
    public ApiResponse<List<Map<String, Object>>> warehouses(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status) {
        return ApiResponse.success(systemService.warehouses(keyword, status));
    }

    @GetMapping("/warehouses/{id}")
    public ApiResponse<Map<String, Object>> warehouseDetail(@PathVariable Long id) {
        return ApiResponse.success(systemService.warehouseDetail(id));
    }

    @PostMapping("/warehouses")
    public ApiResponse<String> createWarehouse(@RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.createWarehouse(request, userId, username);
        return ApiResponse.success("ok");
    }

    @PutMapping("/warehouses/{id}")
    public ApiResponse<String> updateWarehouse(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.updateWarehouse(id, request, userId, username);
        return ApiResponse.success("ok");
    }

    @PutMapping("/warehouses/{id}/status")
    public ApiResponse<String> updateWarehouseStatus(@PathVariable Long id, @RequestBody Map<String, String> request, @RequestAttribute("userId") Long userId, @RequestAttribute("username") String username) {
        systemService.updateWarehouseStatus(id, request.getOrDefault("status", "ENABLED"), userId, username);
        return ApiResponse.success("ok");
    }
}
