package com.example.tobacco.inventory;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.InventoryChangeRequest;
import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @GetMapping("/inventories")
    public ApiResponse<List<InventoryItem>> list() { return ApiResponse.success(inventoryService.list()); }

    @GetMapping("/inventory-records")
    public ApiResponse<List<InventoryRecordItem>> records(@RequestParam(value = "bizType", required = false) String bizType) { return ApiResponse.success(inventoryService.records(bizType)); }

    @GetMapping("/inventory-warnings")
    public ApiResponse<List<InventoryItem>> warnings() { inventoryService.generateWarnings(); return ApiResponse.success(inventoryService.warnings()); }

    @GetMapping("/inventory-warnings/history")
    public ApiResponse<List<Map<String, Object>>> warningHistory() { return ApiResponse.success(inventoryService.warningHistory()); }

    @PostMapping("/inventory-transfers")
    public ApiResponse<String> transfer(@Validated @RequestBody InventoryChangeRequest request, @RequestAttribute("username") String username) { return ApiResponse.success(inventoryService.transfer(request, username)); }

    @PostMapping("/inventory-checks")
    public ApiResponse<String> check(@Validated @RequestBody InventoryChangeRequest request, @RequestAttribute("username") String username) { return ApiResponse.success(inventoryService.check(request, username)); }

    @GetMapping("/inventories/export")
    public ResponseEntity<byte[]> exportExcel() {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventories.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(inventoryService.exportExcel());
    }

    @PostMapping("/inventories/import")
    public ApiResponse<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, @RequestAttribute("username") String username) { return ApiResponse.success(inventoryService.importExcel(file, username)); }
}
