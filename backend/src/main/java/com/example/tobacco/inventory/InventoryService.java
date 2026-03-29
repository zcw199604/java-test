package com.example.tobacco.inventory;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.mapper.inventory.InventoryMapper;
import com.example.tobacco.message.MessageService;
import com.example.tobacco.model.InventoryChangeRequest;
import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import com.example.tobacco.util.ExcelUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {
    private final InventoryMapper inventoryMapper;
    private final ExcelUtil excelUtil;
    private final AuditService auditService;
    private final MessageService messageService;

    public InventoryService(InventoryMapper inventoryMapper, ExcelUtil excelUtil, AuditService auditService, MessageService messageService) {
        this.inventoryMapper = inventoryMapper;
        this.excelUtil = excelUtil;
        this.auditService = auditService;
        this.messageService = messageService;
    }

    public List<InventoryItem> list(Long warehouseId, String keyword, String status) {
        return inventoryMapper.list(warehouseId, likeValue(keyword), trim(status));
    }

    public List<InventoryRecordItem> records(Long warehouseId, String bizType) {
        return inventoryMapper.records(warehouseId, trim(bizType));
    }

    public List<InventoryItem> warnings(Long warehouseId) {
        return inventoryMapper.warnings(warehouseId);
    }

    @Transactional
    public String transfer(InventoryChangeRequest request, String operatorName) {
        Long fromWarehouseId = request.getFromWarehouseId();
        Long toWarehouseId = request.getToWarehouseId();
        if (fromWarehouseId == null || toWarehouseId == null) {
            throw new IllegalArgumentException("调拨必须选择来源仓库和目标仓库");
        }
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new IllegalArgumentException("来源仓库和目标仓库不能相同");
        }
        String fromWarehouseName = requireWarehouseName(fromWarehouseId);
        String toWarehouseName = requireWarehouseName(toWarehouseId);
        Integer beforeQty = inventoryMapper.selectQuantity(request.getProductId(), fromWarehouseId);
        if (beforeQty == null) throw new IllegalArgumentException("来源仓库库存不存在");
        int changeQty = request.getQuantity();
        if (beforeQty < changeQty) throw new IllegalArgumentException("来源仓库库存不足，无法调拨");
        int afterQty = beforeQty - changeQty;
        inventoryMapper.updateQuantity(request.getProductId(), fromWarehouseId, afterQty);

        Integer toBeforeQty = inventoryMapper.selectQuantity(request.getProductId(), toWarehouseId);
        int toAfterQty = (toBeforeQty == null ? 0 : toBeforeQty) + changeQty;
        Integer threshold = inventoryMapper.selectWarningThreshold(request.getProductId(), fromWarehouseId);
        if (toBeforeQty == null) {
            inventoryMapper.insertInventory(request.getProductId(), toWarehouseId, toWarehouseName, toAfterQty, threshold == null ? 0 : threshold);
        } else {
            inventoryMapper.updateInventory(request.getProductId(), toWarehouseId, toAfterQty, threshold == null ? 0 : threshold, toWarehouseName);
        }

        inventoryMapper.insertRecord(request.getProductId(), "TRANSFER_OUT", null, fromWarehouseId, fromWarehouseName, fromWarehouseId, fromWarehouseName, toWarehouseId, toWarehouseName, -changeQty, beforeQty, afterQty, operatorName, request.getRemark());
        inventoryMapper.insertRecord(request.getProductId(), "TRANSFER_IN", null, toWarehouseId, toWarehouseName, fromWarehouseId, fromWarehouseName, toWarehouseId, toWarehouseName, changeQty, toBeforeQty == null ? 0 : toBeforeQty, toAfterQty, operatorName, request.getRemark());
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "INVENTORY", "TRANSFER", "INVENTORY", request.getProductId(),
                "库存调拨，数量:" + changeQty + "，" + fromWarehouseName + " -> " + toWarehouseName);
        checkAndNotifyWarning(request.getProductId(), fromWarehouseName, afterQty, threshold == null ? 0 : threshold);
        return "调拨完成，已从" + fromWarehouseName + "调至" + toWarehouseName;
    }

    @Transactional
    public String check(InventoryChangeRequest request, String operatorName) {
        Long warehouseId = request.getWarehouseId();
        if (warehouseId == null) {
            throw new IllegalArgumentException("盘点必须选择仓库");
        }
        String warehouseName = requireWarehouseName(warehouseId);
        Integer beforeQty = inventoryMapper.selectQuantity(request.getProductId(), warehouseId);
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int afterQty = request.getQuantity();
        inventoryMapper.updateQuantity(request.getProductId(), warehouseId, afterQty);
        inventoryMapper.insertRecord(request.getProductId(), "CHECK", null, warehouseId, warehouseName, null, null, null, null, afterQty - beforeQty, beforeQty, afterQty, operatorName, request.getRemark());
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "INVENTORY", "CHECK", "INVENTORY", request.getProductId(), "库存盘点，结果:" + afterQty + "，仓库:" + warehouseName);
        Integer threshold = inventoryMapper.selectWarningThreshold(request.getProductId(), warehouseId);
        checkAndNotifyWarning(request.getProductId(), warehouseName, afterQty, threshold == null ? 0 : threshold);
        return "盘点结果已更新";
    }

    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file, String username) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        List<Map<String, String>> rows = excelUtil.importWorkbook(file);
        if (rows.size() > 1000) {
            throw new IllegalArgumentException("单次导入不能超过1000行");
        }
        int success = 0;
        int failed = 0;
        StringBuilder errors = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            try {
                Map<String, String> row = rows.get(i);
                String productCode = excelUtil.require(row, "商品编码");
                String warehouseName = row.get("仓库名称") != null && row.get("仓库名称").trim().length() > 0 ? row.get("仓库名称").trim() : "中心仓";
                Integer quantity = excelUtil.toInteger(row, "数量");
                Integer threshold = row.get("预警阈值") != null && row.get("预警阈值").trim().length() > 0 ? excelUtil.toInteger(row, "预警阈值") : 0;
                Long productId = inventoryMapper.selectProductIdByCode(productCode);
                Long warehouseId = inventoryMapper.selectWarehouseIdByName(warehouseName);
                if (productId == null) throw new IllegalArgumentException("商品不存在: " + productCode);
                if (warehouseId == null) throw new IllegalArgumentException("仓库不存在: " + warehouseName);
                Integer count = inventoryMapper.countByProductWarehouse(productId, warehouseId);
                if (count != null && count > 0) {
                    inventoryMapper.updateInventory(productId, warehouseId, quantity, threshold, warehouseName);
                } else {
                    inventoryMapper.insertInventory(productId, warehouseId, warehouseName, quantity, threshold);
                }
                success++;
            } catch (Exception e) {
                failed++;
                errors.append("第").append(i + 2).append("行: ").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors.toString());
        return result;
    }

    private void checkAndNotifyWarning(Long productId, String warehouseName, int currentQty, int threshold) {
        try {
            if (currentQty <= threshold) {
                String productName = inventoryMapper.selectProductName(productId);
                String title = "库存预警: " + productName + " 在" + warehouseName + "当前库存" + currentQty + "，低于阈值" + threshold;
                List<Long> keeperIds = inventoryMapper.selectKeeperIds();
                for (Long keeperId : keeperIds) {
                    messageService.createMessage(keeperId, title, title, "ALERT", "INVENTORY", productId);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String requireWarehouseName(Long warehouseId) {
        Long existingId = inventoryMapper.selectWarehouseId(warehouseId);
        if (existingId == null) {
            throw new IllegalArgumentException("仓库不存在或已停用");
        }
        return inventoryMapper.selectWarehouseName(warehouseId);
    }

    private Long findUserIdByUsername(String username) {
        try {
            return inventoryMapper.selectUserIdByUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String likeValue(String value) {
        return value != null && value.trim().length() > 0 ? "%" + value.trim() + "%" : null;
    }
}
