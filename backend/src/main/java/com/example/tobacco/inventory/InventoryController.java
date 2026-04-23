package com.example.tobacco.inventory;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.InventoryChangeRequest;
import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @GetMapping("/inventories")
    public ApiResponse<List<InventoryItem>> list(@RequestParam(required = false) Long warehouseId,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status) {
        return ApiResponse.success(inventoryService.list(warehouseId, keyword, status));
    }

    @GetMapping("/inventory-records")
    public ApiResponse<List<InventoryRecordItem>> records(@RequestParam(required = false) Long warehouseId,
                                                          @RequestParam(required = false) String bizType,
                                                          @RequestParam(required = false) String keyword) {
        return ApiResponse.success(inventoryService.records(warehouseId, bizType, keyword));
    }

    @GetMapping("/inventory-warnings")
    public ApiResponse<List<InventoryItem>> warnings(@RequestParam(required = false) Long warehouseId) {
        return ApiResponse.success(inventoryService.warnings(warehouseId));
    }

    @PostMapping("/inventory-transfers")
    public ApiResponse<String> transfer(@Validated @RequestBody InventoryChangeRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(inventoryService.transfer(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PostMapping("/inventory-checks")
    public ApiResponse<String> check(@Validated @RequestBody InventoryChangeRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(inventoryService.check(request, String.valueOf(httpRequest.getAttribute("username"))));
    }

    @PostMapping("/inventories/import")
    public ApiResponse<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        return ApiResponse.success(inventoryService.importFromExcel(file, String.valueOf(httpRequest.getAttribute("username"))));
    }
}
