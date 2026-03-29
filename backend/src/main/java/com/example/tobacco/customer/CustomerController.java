package com.example.tobacco.customer;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CustomerItem;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    public CustomerController(CustomerService customerService) { this.customerService = customerService; }
    @GetMapping
    public ApiResponse<List<CustomerItem>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status) {
        return ApiResponse.success(customerService.list(keyword, status));
    }
    @GetMapping("/{id}")
    public ApiResponse<CustomerItem> detail(@PathVariable Long id) { return ApiResponse.success(customerService.detail(id)); }
    @PostMapping
    public ApiResponse<String> create(@RequestBody Map<String, String> request) { customerService.create(request); return ApiResponse.success("ok"); }
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Map<String, String> request) { customerService.update(id, request); return ApiResponse.success("ok"); }
    @DeleteMapping("/{id}")
    public ApiResponse<String> disable(@PathVariable Long id) { customerService.disable(id); return ApiResponse.success("ok"); }
}
