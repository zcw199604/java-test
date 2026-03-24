package com.example.tobacco.supplier;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.SupplierItem;
import com.example.tobacco.model.SupplierRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) { this.supplierService = supplierService; }

    @GetMapping
    public ApiResponse<List<SupplierItem>> list() { return ApiResponse.success(supplierService.listSuppliers()); }

    @PostMapping
    public ApiResponse<String> create(@Validated @RequestBody SupplierRequest request) { supplierService.createSupplier(request); return ApiResponse.success("ok"); }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @Validated @RequestBody SupplierRequest request) { supplierService.updateSupplier(id, request); return ApiResponse.success("ok"); }
}
