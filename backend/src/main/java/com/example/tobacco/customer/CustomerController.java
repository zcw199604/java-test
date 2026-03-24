package com.example.tobacco.customer;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CustomerItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    public CustomerController(CustomerService customerService) { this.customerService = customerService; }
    @GetMapping
    public ApiResponse<List<CustomerItem>> list() { return ApiResponse.success(customerService.list()); }
}
