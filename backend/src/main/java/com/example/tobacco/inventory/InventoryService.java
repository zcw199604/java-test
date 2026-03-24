package com.example.tobacco.inventory;

import com.example.tobacco.audit.AuditService;
import com.example.tobacco.message.MessageService;
import com.example.tobacco.model.InventoryChangeRequest;
import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import com.example.tobacco.util.ExcelUtil;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final MessageService messageService;
    private final ExcelUtil excelUtil;
    public InventoryService(JdbcTemplate jdbcTemplate, AuditService auditService, MessageService messageService, ExcelUtil excelUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.messageService = messageService;
        this.excelUtil = excelUtil;
    }

    public List<InventoryItem> list() {
        return jdbcTemplate.query("select i.id, i.product_id as productId, p.name as productName, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from inventories i left join products p on i.product_id=p.id order by i.id",
                new BeanPropertyRowMapper<InventoryItem>(InventoryItem.class));
    }

    public List<InventoryRecordItem> records(String bizType) {
        String sql = "select r.id, r.product_id as productId, p.name as productName, r.biz_type as bizType, r.biz_id as bizId, r.change_qty as changeQty, r.before_qty as beforeQty, r.after_qty as afterQty, r.remark, DATE_FORMAT(r.created_at,'%Y-%m-%d %H:%i:%s') as createdAt from inventory_records r left join products p on r.product_id=p.id";
        if (bizType != null && bizType.trim().length() > 0) {
            sql += " where r.biz_type='" + bizType + "'";
        }
        sql += " order by r.id desc";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<InventoryRecordItem>(InventoryRecordItem.class));
    }

    public List<InventoryItem> warnings() {
        return jdbcTemplate.query("select i.id, i.product_id as productId, p.name as productName, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from inventories i left join products p on i.product_id=p.id where i.quantity <= i.warning_threshold order by i.quantity asc",
                new BeanPropertyRowMapper<InventoryItem>(InventoryItem.class));
    }

    public List<Map<String, Object>> warningHistory() {
        return jdbcTemplate.queryForList("select id, warning_type as warningType, product_id as productId, warehouse_id as warehouseId, threshold, current_value as currentValue, status, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from warning_records order by id desc");
    }

    @Transactional
    public String transfer(InventoryChangeRequest request, String username) {
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=? and warehouse_id=1", Integer.class, request.getProductId());
        if (beforeQty == null || beforeQty < request.getQuantity()) throw new IllegalArgumentException("源仓库存不足");
        int afterQty = beforeQty - request.getQuantity();
        jdbcTemplate.update("update inventories set quantity=? where product_id=? and warehouse_id=1", afterQty, request.getProductId());
        ensureTargetInventory(request.getProductId(), 2L, request.getToWarehouse() == null ? "调拨仓" : request.getToWarehouse());
        Integer targetBefore = jdbcTemplate.queryForObject("select quantity from inventories where product_id=? and warehouse_id=2", Integer.class, request.getProductId());
        jdbcTemplate.update("update inventories set quantity=? where product_id=? and warehouse_id=2", targetBefore + request.getQuantity(), request.getProductId());
        jdbcTemplate.update("insert into inventory_records(product_id,warehouse_id,biz_type,biz_id,change_qty,before_qty,after_qty,remark,source_module,source_order_no) values(?,?,?,?,?,?,?,?,?,?)", request.getProductId(), 1L, "TRANSFER", null, -request.getQuantity(), beforeQty, afterQty, request.getRemark(), "INVENTORY", "TRANSFER-OUT");
        jdbcTemplate.update("insert into inventory_records(product_id,warehouse_id,biz_type,biz_id,change_qty,before_qty,after_qty,remark,source_module,source_order_no) values(?,?,?,?,?,?,?,?,?,?)", request.getProductId(), 2L, "TRANSFER_IN", null, request.getQuantity(), targetBefore, targetBefore + request.getQuantity(), request.getRemark(), "INVENTORY", "TRANSFER-IN");
        auditService.trace("INVENTORY", request.getProductId(), "TRANSFER", "TRANSFER", "库存调拨", username, request.getRemark());
        return "调拨完成";
    }

    @Transactional
    public String check(InventoryChangeRequest request, String username) {
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=? and warehouse_id=1", Integer.class, request.getProductId());
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int afterQty = request.getQuantity();
        int profitLoss = afterQty - beforeQty;
        jdbcTemplate.update("update inventories set quantity=? where product_id=? and warehouse_id=1", afterQty, request.getProductId());
        jdbcTemplate.update("insert into inventory_records(product_id,warehouse_id,biz_type,biz_id,change_qty,before_qty,after_qty,remark,source_module,source_order_no) values(?,?,?,?,?,?,?,?,?,?)", request.getProductId(), 1L, "CHECK", null, profitLoss, beforeQty, afterQty, request.getRemark(), "INVENTORY", "CHECK");
        jdbcTemplate.update("insert into inventory_check_reports(report_no, warehouse_id, product_id, system_qty, actual_qty, profit_loss_qty, status, created_by) values(?,?,?,?,?,?,?,?)", "IC" + System.currentTimeMillis(), 1L, request.getProductId(), beforeQty, afterQty, profitLoss, "REPORTED", username);
        auditService.trace("INVENTORY", request.getProductId(), "CHECK", "CHECK", "库存盘点", username, request.getRemark());
        return "盘点结果已更新";
    }

    public byte[] exportExcel() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select product_id as 商品ID, warehouse_name as 仓库, quantity as 数量, warning_threshold as 预警阈值 from inventories order by id");
        return excelUtil.exportWorkbook("库存台账", new String[]{"商品ID", "仓库", "数量", "预警阈值"}, rows);
    }

    @Transactional
    public Map<String, Object> importExcel(MultipartFile file, String username) {
        List<Map<String, String>> rows = excelUtil.importWorkbook(file);
        int success = 0;
        for (Map<String, String> row : rows) {
            Long productId = excelUtil.toLong(row, "商品ID");
            Integer quantity = excelUtil.toInteger(row, "数量");
            String warehouse = excelUtil.require(row, "仓库");
            ensureTargetInventory(productId, 1L, warehouse);
            jdbcTemplate.update("update inventories set quantity=?, warning_threshold=? where product_id=? and warehouse_id=1",
                    quantity, excelUtil.toInteger(row, "预警阈值"), productId);
            success++;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", success);
        result.put("total", rows.size());
        auditService.logOperation(null, username, "INVENTORY", "IMPORT_EXCEL", "INVENTORY", null, "库存批量导入");
        return result;
    }

    public void generateWarnings() {
        List<Map<String, Object>> warnings = jdbcTemplate.queryForList("select product_id as productId, warehouse_id as warehouseId, warning_threshold as threshold, quantity as currentValue from inventories where quantity <= warning_threshold");
        for (Map<String, Object> warning : warnings) {
            jdbcTemplate.update("insert into warning_records(warning_type, product_id, warehouse_id, threshold, current_value, status) values(?,?,?,?,?,?)",
                    "LOW_STOCK", warning.get("productId"), warning.get("warehouseId"), warning.get("threshold"), warning.get("currentValue"), "OPEN");
            messageService.createMessage(null, "库存预警", "商品ID " + warning.get("productId") + " 低于阈值", "WARNING", "INVENTORY", ((Number) warning.get("productId")).longValue());
        }
    }

    private void ensureTargetInventory(Long productId, Long warehouseId, String warehouseName) {
        Integer count = jdbcTemplate.queryForObject("select count(1) from inventories where product_id=? and warehouse_id=?", Integer.class, productId, warehouseId);
        if (count == null || count == 0) {
            jdbcTemplate.update("insert into inventories(product_id,warehouse_id,warehouse_name,quantity,warning_threshold,locked_qty) values(?,?,?,?,(select warning_threshold from products where id=?),0)", productId, warehouseId, warehouseName, 0, productId);
        }
    }
}
