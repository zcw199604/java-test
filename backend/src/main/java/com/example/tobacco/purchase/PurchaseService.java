package com.example.tobacco.purchase;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.mapper.purchase.PurchaseMapper;
import com.example.tobacco.message.MessageService;
import com.example.tobacco.model.CreatePurchaseRequest;
import com.example.tobacco.model.PurchaseOrderItem;
import com.example.tobacco.model.WarehouseActionRequest;
import com.example.tobacco.util.ExcelUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseService {
    private final PurchaseMapper purchaseMapper;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;

    public PurchaseService(PurchaseMapper purchaseMapper, AuditService auditService,
                           MessageService messageService, ExcelUtil excelUtil) {
        this.purchaseMapper = purchaseMapper;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<PurchaseOrderItem> list() {
        return purchaseMapper.list();
    }

    public PurchaseOrderItem detail(Long id) {
        return loadDetail(id);
    }

    public List<Map<String, Object>> trace(Long id) {
        PurchaseOrderItem item = loadDetail(id);
        return purchaseMapper.trace(id, item.getOrderNo());
    }

    @Transactional
    public PurchaseOrderItem create(CreatePurchaseRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity())).setScale(2, java.math.RoundingMode.HALF_UP);
        String orderNo = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        purchaseMapper.insertOrder(orderNo, request.getSupplierId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, "CREATED", username);
        Long id = purchaseMapper.selectIdByOrderNo(orderNo);
        auditService.trace("PURCHASE", id, orderNo, "CREATE", "创建采购单", username, "创建采购单");
        auditService.logOperation(findUserIdByUsername(username), username, "PURCHASE", "CREATE", "PURCHASE", id, "创建采购单" + orderNo);
        return loadDetail(id);
    }

    @Transactional
    public PurchaseOrderItem update(Long id, CreatePurchaseRequest request, String operatorName, String roleCode) {
        PurchaseOrderItem item = loadDetail(id);
        ensureEditable(item.getStatus(), roleCode);
        if (isPurchaser(roleCode) && !String.valueOf(item.getCreatedBy()).equals(operatorName)) {
            throw new IllegalArgumentException("仅允许修改本人创建的采购订单");
        }
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity())).setScale(2, java.math.RoundingMode.HALF_UP);
        purchaseMapper.updateOrder(id, request.getSupplierId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "UPDATE", "更新采购单", operatorName, "更新采购单明细");
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "UPDATE", "PURCHASE", id, "更新采购单");
        return loadDetail(id);
    }

    @Transactional
    public PurchaseOrderItem audit(Long id, String decision, String remark, String operator, String roleCode) {
        requireApprovalRole(roleCode, "采购审核");
        PurchaseOrderItem item = loadDetail(id);
        if (!"CREATED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核状态的采购单可审核");
        }
        String newStatus;
        if ("APPROVED".equalsIgnoreCase(decision)) {
            newStatus = "APPROVED";
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            newStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("审核决定必须为 APPROVED 或 REJECTED");
        }
        purchaseMapper.auditOrder(id, newStatus, operator, remark);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "AUDIT", "审核采购单", operator, newStatus + ": " + (remark == null ? "" : remark));
        auditService.logOperation(findUserIdByUsername(operator), operator, "PURCHASE", "AUDIT", "PURCHASE", id, "审核采购单:" + newStatus);
        Long creatorId = findUserIdByUsername(item.getCreatedBy());
        if (creatorId != null) {
            String title = "APPROVED".equals(newStatus)
                    ? "采购单 " + item.getOrderNo() + " 审核通过"
                    : "采购单 " + item.getOrderNo() + " 被驳回: " + (remark == null ? "" : remark);
            messageService.createMessage(creatorId, title, title, "NOTICE", "PURCHASE", id);
        }
        return loadDetail(id);
    }

    @Transactional
    public PurchaseOrderItem cancel(Long id, String reason, String operatorName) {
        PurchaseOrderItem item = loadDetail(id);
        if (!"CREATED".equals(item.getStatus()) && !"REJECTED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核或已驳回状态的采购单可取消");
        }
        purchaseMapper.cancelOrder(id, reason);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "CANCEL", "取消采购单", operatorName, reason == null ? "" : reason);
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "CANCEL", "PURCHASE", id, "取消采购单");
        return loadDetail(id);
    }

    @Transactional
    public PurchaseOrderItem receive(Long id, String operatorName, String roleCode) {
        requireApprovalRole(roleCode, "采购到货确认");
        PurchaseOrderItem item = loadDetail(id);
        if ("INBOUND".equals(item.getStatus()) || "RECEIVED".equals(item.getStatus())) {
            return item;
        }
        if (!"APPROVED".equals(item.getStatus())) {
            throw new IllegalArgumentException("采购单需审核通过后才能登记到货");
        }
        purchaseMapper.receiveOrder(id);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "RECEIVE", "到货登记", operatorName, "到货登记");
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "RECEIVE", "PURCHASE", id, "采购到货登记");
        return loadDetail(id);
    }

    @Transactional
    public PurchaseOrderItem inbound(Long id, WarehouseActionRequest request, String operatorName, String roleCode) {
        requireApprovalRole(roleCode, "采购入库确认");
        PurchaseOrderItem item = loadDetail(id);
        if ("INBOUND".equals(item.getStatus())) {
            return item;
        }
        if (!"RECEIVED".equals(item.getStatus())) {
            throw new IllegalArgumentException("采购单需先登记到货后才能入库");
        }
        Long warehouseId = request.getWarehouseId();
        if (warehouseId == null) {
            throw new IllegalArgumentException("采购入库必须选择仓库");
        }
        String warehouseName = requireWarehouseName(warehouseId);
        Integer beforeQty = purchaseMapper.selectInventoryQuantity(item.getProductId(), warehouseId);
        if (beforeQty == null) {
            beforeQty = 0;
        }
        int afterQty = beforeQty + item.getQuantity();
        Integer count = purchaseMapper.countInventories(item.getProductId(), warehouseId);
        if (count != null && count > 0) {
            purchaseMapper.updateInventoryForInbound(item.getProductId(), warehouseId, warehouseName, afterQty);
        } else {
            purchaseMapper.insertInventoryForInbound(item.getProductId(), warehouseId, warehouseName, afterQty);
        }
        purchaseMapper.inboundOrder(id, warehouseId, warehouseName);
        String remark = request.getRemark();
        if (remark == null || remark.trim().isEmpty()) {
            remark = "采购单入库";
        }
        purchaseMapper.insertInventoryRecord(item.getProductId(), "PURCHASE_INBOUND", id, item.getQuantity(), beforeQty, afterQty,
                warehouseId, warehouseName, null, null, null, null, operatorName, remark);
        auditService.trace("PURCHASE", id, item.getOrderNo(), "INBOUND", "采购入库", operatorName, "入库数量: " + item.getQuantity() + "，仓库: " + warehouseName);
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "PURCHASE", "INBOUND", "PURCHASE", id,
                "采购入库，数量:" + item.getQuantity() + "，仓库:" + warehouseName);
        checkAndNotifyWarning(item.getProductId(), warehouseName, afterQty);
        return loadDetail(id);
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
                String supplierName = excelUtil.require(row, "供应商名称");
                String productCode = excelUtil.require(row, "商品编码");
                Integer quantity = excelUtil.toInteger(row, "数量");
                BigDecimal unitPrice = excelUtil.toBigDecimal(row, "单价");
                Long supplierId = purchaseMapper.selectSupplierIdByName(supplierName);
                Long productId = purchaseMapper.selectProductIdByCode(productCode);
                CreatePurchaseRequest request = new CreatePurchaseRequest();
                request.setSupplierId(supplierId);
                request.setProductId(productId);
                request.setQuantity(quantity);
                request.setUnitPrice(unitPrice);
                create(request, username);
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

    private void checkAndNotifyWarning(Long productId, String warehouseName, int currentQty) {
        try {
            Integer threshold = purchaseMapper.selectInventoryWarningThreshold(productId);
            if (threshold != null && currentQty <= threshold) {
                String productName = purchaseMapper.selectProductName(productId);
                String title = "库存预警: " + productName + " 在" + warehouseName + "当前库存" + currentQty + "，低于阈值" + threshold;
                List<Long> keeperIds = purchaseMapper.selectKeeperIds();
                for (Long keeperId : keeperIds) {
                    messageService.createMessage(keeperId, title, title, "ALERT", "INVENTORY", productId);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String requireWarehouseName(Long warehouseId) {
        Long existingId = purchaseMapper.selectWarehouseId(warehouseId);
        if (existingId == null) {
            throw new IllegalArgumentException("仓库不存在或已停用");
        }
        return purchaseMapper.selectWarehouseName(warehouseId);
    }

    private Long findUserIdByUsername(String username) {
        try {
            return purchaseMapper.selectUserIdByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isPurchaser(String roleCode) {
        return "PURCHASER".equalsIgnoreCase(roleCode);
    }

    private void requireApprovalRole(String roleCode, String actionName) {
        if (!isApprovalRole(roleCode)) {
            throw new IllegalArgumentException(actionName + "仅允许超级管理员、普通管理员或库管执行");
        }
    }

    private boolean isApprovalRole(String roleCode) {
        return "SUPER_ADMIN".equalsIgnoreCase(roleCode)
                || "ADMIN".equalsIgnoreCase(roleCode)
                || "KEEPER".equalsIgnoreCase(roleCode);
    }

    private void ensureEditable(String status, String roleCode) {
        if ("APPROVED".equals(status) || "CANCELLED".equals(status)) {
            throw new IllegalArgumentException("仅 CREATED 或 REJECTED 状态的采购单可编辑");
        }
        if (!"CREATED".equals(status) && !"REJECTED".equals(status) && !isAdminEditor(roleCode)) {
            throw new IllegalArgumentException("仅 CREATED 或 REJECTED 状态的采购单可编辑");
        }
    }

    private boolean isAdminEditor(String roleCode) {
        return "SUPER_ADMIN".equalsIgnoreCase(roleCode) || "ADMIN".equalsIgnoreCase(roleCode);
    }

    private PurchaseOrderItem loadDetail(Long id) {
        return purchaseMapper.detail(id);
    }
}
