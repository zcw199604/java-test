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
    public ApiResponse<List<CustomerItem>> list() { return ApiResponse.success(customerService.list()); }
    @PostMapping
    public ApiResponse<String> create(@RequestBody Map<String, String> request) { customerService.create(request); return ApiResponse.success("ok"); }
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Map<String, String> request) { customerService.update(id, request); return ApiResponse.success("ok"); }
    @DeleteMapping("/{id}")
    public ApiResponse<String> disable(@PathVariable Long id) { customerService.disable(id); return ApiResponse.success("ok"); }
}
