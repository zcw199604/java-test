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

    public List<InventoryItem> list() {
        return inventoryMapper.list();
    }

    public List<InventoryRecordItem> records() {
        return inventoryMapper.records();
    }

    public List<InventoryItem> warnings() {
        return inventoryMapper.warnings();
    }

    @Transactional
    public String transfer(InventoryChangeRequest request, String operatorName) {
        Integer beforeQty = inventoryMapper.selectQuantity(request.getProductId());
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int changeQty = request.getQuantity();
        if (beforeQty < changeQty) throw new IllegalArgumentException("库存不足，无法调拨");
        int afterQty = beforeQty - changeQty;
        inventoryMapper.updateQuantity(request.getProductId(), afterQty);
        inventoryMapper.insertRecord(request.getProductId(), "TRANSFER", null, -changeQty, beforeQty, afterQty, operatorName, request.getRemark());
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "INVENTORY", "TRANSFER", "INVENTORY", request.getProductId(), "库存调拨，数量:" + changeQty);
        checkAndNotifyWarning(request.getProductId(), afterQty);
        return "调拨完成，库存已扣减" + changeQty;
    }

    @Transactional
    public String check(InventoryChangeRequest request, String operatorName) {
        Integer beforeQty = inventoryMapper.selectQuantity(request.getProductId());
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int afterQty = request.getQuantity();
        inventoryMapper.updateQuantity(request.getProductId(), afterQty);
        inventoryMapper.insertRecord(request.getProductId(), "CHECK", null, afterQty - beforeQty, beforeQty, afterQty, operatorName, request.getRemark());
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "INVENTORY", "CHECK", "INVENTORY", request.getProductId(), "库存盘点，结果:" + afterQty);
        checkAndNotifyWarning(request.getProductId(), afterQty);
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
                Integer count = inventoryMapper.countByProductId(productId);
                if (count != null && count > 0) {
                    inventoryMapper.updateInventory(productId, quantity, threshold, warehouseName);
                } else {
                    inventoryMapper.insertInventory(productId, warehouseName, quantity, threshold);
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

    private void checkAndNotifyWarning(Long productId, int currentQty) {
        try {
            Integer threshold = inventoryMapper.selectWarningThreshold(productId);
            if (threshold != null && currentQty <= threshold) {
                String productName = inventoryMapper.selectProductName(productId);
                String title = "库存预警: " + productName + " 当前库存" + currentQty + "，低于阈值" + threshold;
                List<Long> keeperIds = inventoryMapper.selectKeeperIds();
                for (Long keeperId : keeperIds) {
                    messageService.createMessage(keeperId, title, title, "ALERT", "INVENTORY", productId);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private Long findUserIdByUsername(String username) {
        try {
            return inventoryMapper.selectUserIdByUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
