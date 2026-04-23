package com.example.tobacco.sales;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.mapper.sales.SalesMapper;
import com.example.tobacco.message.MessageService;
import com.example.tobacco.model.CreateSalesRequest;
import com.example.tobacco.model.PaymentRequest;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.SalesOrderItem;
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
public class SalesService {
    private final SalesMapper salesMapper;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;

    public SalesService(SalesMapper salesMapper, AuditService auditService,
                        MessageService messageService, ExcelUtil excelUtil) {
        this.salesMapper = salesMapper;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<SalesOrderItem> list(String username, String roleCode) {
        return salesMapper.list(username, isSeller(roleCode));
    }

    public SalesOrderItem detail(Long id, String username, String roleCode) {
        SalesOrderItem item = loadDetail(id);
        validateOwnership(item, username, roleCode);
        return item;
    }

    public List<Map<String, Object>> trace(Long id, String username, String roleCode) {
        SalesOrderItem item = loadDetail(id);
        validateOwnership(item, username, roleCode);
        return salesMapper.trace(id, item.getOrderNo());
    }

    @Transactional
    public SalesOrderItem create(CreateSalesRequest request, String username) {
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity())).setScale(2, java.math.RoundingMode.HALF_UP);
        String orderNo = "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        salesMapper.insertOrder(orderNo, request.getCustomerId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, BigDecimal.ZERO, "CREATED", username);
        Long id = salesMapper.selectIdByOrderNo(orderNo);
        auditService.trace("SALES", id, orderNo, "CREATE", "创建销售单", username, "创建销售单");
        return loadDetail(id);
    }

    @Transactional
    public SalesOrderItem update(Long id, CreateSalesRequest request, String username, String roleCode) {
        SalesOrderItem item = loadDetail(id);
        validateOwnership(item, username, roleCode);
        ensureEditable(item.getStatus(), roleCode);
        BigDecimal total = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity())).setScale(2, java.math.RoundingMode.HALF_UP);
        salesMapper.updateOrder(id, request.getCustomerId(), request.getProductId(), request.getQuantity(), request.getUnitPrice(), total, BigDecimal.ZERO);
        auditService.trace("SALES", id, item.getOrderNo(), "UPDATE", "更新销售单", username, "更新销售单明细");
        auditService.logOperation(findUserIdByUsername(username), username, "SALES", "UPDATE", "SALES", id, "更新销售单");
        return loadDetail(id);
    }

    @Transactional
    public SalesOrderItem audit(Long id, String decision, String remark, String operator, String roleCode) {
        requireApprovalRole(roleCode, "销售审核");
        SalesOrderItem item = loadDetail(id);
        if (!"CREATED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核状态的销售单可审核");
        }
        String newStatus;
        if ("APPROVED".equalsIgnoreCase(decision)) {
            newStatus = "APPROVED";
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            newStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("审核决定必须为 APPROVED 或 REJECTED");
        }
        salesMapper.auditOrder(id, newStatus, operator, remark);
        auditService.trace("SALES", id, item.getOrderNo(), "AUDIT", "审核销售单", operator, newStatus + ": " + (remark == null ? "" : remark));
        auditService.logOperation(findUserIdByUsername(operator), operator, "SALES", "AUDIT", "SALES", id, "审核销售单:" + newStatus);
        Long creatorId = findUserIdByUsername(item.getCreatedBy());
        if (creatorId != null) {
            String title = "APPROVED".equals(newStatus)
                    ? "销售单 " + item.getOrderNo() + " 审核通过"
                    : "销售单 " + item.getOrderNo() + " 被驳回: " + (remark == null ? "" : remark);
            messageService.createMessage(creatorId, title, title, "NOTICE", "SALES", id);
        }
        return loadDetail(id);
    }

    @Transactional
    public SalesOrderItem cancel(Long id, String reason, String username, String roleCode) {
        SalesOrderItem item = loadDetail(id);
        validateOwnership(item, username, roleCode);
        if (!"CREATED".equals(item.getStatus()) && !"REJECTED".equals(item.getStatus())) {
            throw new IllegalArgumentException("仅待审核或已驳回状态的销售单可取消");
        }
        salesMapper.cancelOrder(id, reason);
        auditService.trace("SALES", id, item.getOrderNo(), "CANCEL", "取消销售单", username, reason == null ? "" : reason);
        auditService.logOperation(findUserIdByUsername(username), username, "SALES", "CANCEL", "SALES", id, "取消销售单");
        return loadDetail(id);
    }

    @Transactional
    public SalesOrderItem outbound(Long id, WarehouseActionRequest request, String username, String roleCode) {
        requireApprovalRole(roleCode, "销售出库确认");
        SalesOrderItem item = loadDetail(id);
        if (!"APPROVED".equals(item.getStatus())) {
            if ("OUTBOUND".equals(item.getStatus()) || "PARTIAL_PAID".equals(item.getStatus()) || "PAID".equals(item.getStatus())) {
                return item;
            }
            throw new IllegalArgumentException("销售单需审核通过后才能出库");
        }
        Long warehouseId = request.getWarehouseId();
        if (warehouseId == null) {
            throw new IllegalArgumentException("销售出库必须选择仓库");
        }
        String warehouseName = requireWarehouseName(warehouseId);
        Integer beforeQty = salesMapper.selectInventoryQuantity(item.getProductId(), warehouseId);
        if (beforeQty == null || beforeQty < item.getQuantity()) {
            throw new IllegalArgumentException("指定仓库库存不足，无法出库");
        }
        int afterQty = beforeQty - item.getQuantity();
        salesMapper.updateInventoryQuantity(item.getProductId(), warehouseId, afterQty);
        salesMapper.outboundOrder(id, warehouseId, warehouseName);
        String remark = request.getRemark();
        if (remark == null || remark.trim().isEmpty()) {
            remark = "销售出库";
        }
        salesMapper.insertInventoryRecord(item.getProductId(), "SALES_OUTBOUND", id, -item.getQuantity(), beforeQty, afterQty,
                warehouseId, warehouseName, null, null, null, null, username, remark);
        auditService.trace("SALES", id, item.getOrderNo(), "OUTBOUND", "销售出库", username, "出库数量: " + item.getQuantity() + "，仓库: " + warehouseName);
        auditService.logOperation(findUserIdByUsername(username), username, "SALES", "OUTBOUND", "SALES", id,
                "销售出库，数量:" + item.getQuantity() + "，仓库:" + warehouseName);
        checkAndNotifyWarning(item.getProductId(), warehouseName, afterQty);
        return loadDetail(id);
    }

    @Transactional
    public SalesOrderItem payment(Long id, PaymentRequest request, String username, String roleCode) {
        SalesOrderItem item = loadDetail(id);
        validateOwnership(item, username, roleCode);
        BigDecimal paid = item.getPaidAmount().add(request.getAmount());
        String status = paid.compareTo(item.getTotalAmount()) >= 0 ? "PAID" : ("OUTBOUND".equals(item.getStatus()) ? "PARTIAL_PAID" : item.getStatus());
        salesMapper.updatePayment(id, paid, status);
        salesMapper.insertPaymentRecord(id, request.getAmount(), request.getPayerName(), request.getRemark());
        auditService.trace("SALES", id, item.getOrderNo(), "PAYMENT", "销售回款", username, "回款金额: " + request.getAmount());
        auditService.logOperation(findUserIdByUsername(username), username, "SALES", "PAYMENT", "SALES", id, "销售回款，金额:" + request.getAmount());
        return loadDetail(id);
    }

    public List<Map<String, Object>> receivables(String username, String roleCode) {
        return salesMapper.selectReceivables(username, isSeller(roleCode));
    }

    public ReportSummaryItem statistics(String username, String roleCode) {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("销售统计");
        if (isSeller(roleCode)) {
            item.setCount(salesMapper.countSalesByUser(username));
            item.setAmount(salesMapper.sumSalesAmountByUser(username));
            return item;
        }
        item.setCount(salesMapper.countSalesAll());
        item.setAmount(salesMapper.sumSalesAmountAll());
        return item;
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
                String customerName = excelUtil.require(row, "客户名称");
                String productCode = excelUtil.require(row, "商品编码");
                Integer quantity = excelUtil.toInteger(row, "数量");
                BigDecimal unitPrice = excelUtil.toBigDecimal(row, "单价");
                Long customerId = salesMapper.selectCustomerIdByName(customerName);
                Long productId = salesMapper.selectProductIdByCode(productCode);
                CreateSalesRequest request = new CreateSalesRequest();
                request.setCustomerId(customerId);
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
            Integer threshold = salesMapper.selectInventoryWarningThreshold(productId);
            if (threshold != null && currentQty <= threshold) {
                String productName = salesMapper.selectProductName(productId);
                String title = "库存预警: " + productName + " 在" + warehouseName + "当前库存" + currentQty + "，低于阈值" + threshold;
                List<Long> keeperIds = salesMapper.selectKeeperIds();
                for (Long keeperId : keeperIds) {
                    messageService.createMessage(keeperId, title, title, "ALERT", "INVENTORY", productId);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String requireWarehouseName(Long warehouseId) {
        Long existingId = salesMapper.selectWarehouseId(warehouseId);
        if (existingId == null) {
            throw new IllegalArgumentException("仓库不存在或已停用");
        }
        return salesMapper.selectWarehouseName(warehouseId);
    }

    private Long findUserIdByUsername(String username) {
        try {
            return salesMapper.selectUserIdByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSeller(String roleCode) {
        return "SELLER".equalsIgnoreCase(roleCode);
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
            throw new IllegalArgumentException("仅 CREATED 或 REJECTED 状态的销售单可编辑");
        }
        if (!"CREATED".equals(status) && !"REJECTED".equals(status) && !isAdminEditor(roleCode)) {
            throw new IllegalArgumentException("仅 CREATED 或 REJECTED 状态的销售单可编辑");
        }
    }

    private boolean isAdminEditor(String roleCode) {
        return "SUPER_ADMIN".equalsIgnoreCase(roleCode) || "ADMIN".equalsIgnoreCase(roleCode);
    }

    private void validateOwnership(SalesOrderItem item, String username, String roleCode) {
        if (isSeller(roleCode) && !String.valueOf(item.getCreatedBy()).equals(username)) {
            throw new IllegalArgumentException("仅允许操作本人创建的销售订单");
        }
    }

    private SalesOrderItem loadDetail(Long id) {
        return salesMapper.detail(id);
    }
}
