package com.example.tobacco.inventory;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.InventoryChangeRequest;
import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @GetMapping("/inventories")
    public ApiResponse<List<InventoryItem>> list() { return ApiResponse.success(inventoryService.list()); }

    @GetMapping("/inventory-records")
    public ApiResponse<List<InventoryRecordItem>> records() { return ApiResponse.success(inventoryService.records()); }

    @GetMapping("/inventory-warnings")
    public ApiResponse<List<InventoryItem>> warnings() { return ApiResponse.success(inventoryService.warnings()); }

    @PostMapping("/inventory-transfers")
    public ApiResponse<String> transfer(@Validated @RequestBody InventoryChangeRequest request) { return ApiResponse.success(inventoryService.transfer(request)); }

    @PostMapping("/inventory-checks")
    public ApiResponse<String> check(@Validated @RequestBody InventoryChangeRequest request) { return ApiResponse.success(inventoryService.check(request)); }
}
