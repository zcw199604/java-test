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
    private final ExcelUtil excelUtil;
    private final AuditService auditService;
    private final MessageService messageService;

    public InventoryService(JdbcTemplate jdbcTemplate, ExcelUtil excelUtil, AuditService auditService, MessageService messageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.excelUtil = excelUtil;
        this.auditService = auditService;
        this.messageService = messageService;
    }

    public List<InventoryItem> list() {
        return jdbcTemplate.query("select i.id, i.product_id as productId, p.name as productName, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from inventories i left join products p on i.product_id=p.id order by i.id",
                new BeanPropertyRowMapper<InventoryItem>(InventoryItem.class));
    }

    public List<InventoryRecordItem> records() {
        return jdbcTemplate.query("select r.id, r.product_id as productId, p.name as productName, r.biz_type as bizType, r.biz_id as bizId, r.change_qty as changeQty, r.before_qty as beforeQty, r.after_qty as afterQty, coalesce(u.real_name, r.operator_name) as operatorName, r.remark, DATE_FORMAT(r.created_at,'%Y-%m-%d %H:%i:%s') as createdAt from inventory_records r left join products p on r.product_id=p.id left join users u on r.operator_name=u.username order by r.id desc",
                new BeanPropertyRowMapper<InventoryRecordItem>(InventoryRecordItem.class));
    }

    public List<InventoryItem> warnings() {
        return jdbcTemplate.query("select i.id, i.product_id as productId, p.name as productName, i.warehouse_name as warehouseName, i.quantity, i.warning_threshold as warningThreshold, DATE_FORMAT(i.updated_at,'%Y-%m-%d %H:%i:%s') as updatedAt from inventories i left join products p on i.product_id=p.id where i.quantity <= i.warning_threshold order by i.quantity asc",
                new BeanPropertyRowMapper<InventoryItem>(InventoryItem.class));
    }

    @Transactional
    public String transfer(InventoryChangeRequest request, String operatorName) {
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=?", Integer.class, request.getProductId());
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int changeQty = request.getQuantity();
        if (beforeQty < changeQty) throw new IllegalArgumentException("库存不足，无法调拨");
        int afterQty = beforeQty - changeQty;
        jdbcTemplate.update("update inventories set quantity=? where product_id=?", afterQty, request.getProductId());
        jdbcTemplate.update("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)",
                request.getProductId(), "TRANSFER", null, -changeQty, beforeQty, afterQty, operatorName, request.getRemark());
        auditService.logOperation(findUserIdByUsername(operatorName), operatorName, "INVENTORY", "TRANSFER", "INVENTORY", request.getProductId(), "库存调拨，数量:" + changeQty);
        checkAndNotifyWarning(request.getProductId(), afterQty);
        return "调拨完成，库存已扣减" + changeQty;
    }

    @Transactional
    public String check(InventoryChangeRequest request, String operatorName) {
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=?", Integer.class, request.getProductId());
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int afterQty = request.getQuantity();
        jdbcTemplate.update("update inventories set quantity=? where product_id=?", afterQty, request.getProductId());
        jdbcTemplate.update("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)",
                request.getProductId(), "CHECK", null, afterQty - beforeQty, beforeQty, afterQty, operatorName, request.getRemark());
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
                Long productId = jdbcTemplate.queryForObject("select id from products where code=? and status='ENABLED'", Long.class, productCode);
                int count = jdbcTemplate.queryForObject("select count(1) from inventories where product_id=?", Integer.class, productId);
                if (count > 0) {
                    jdbcTemplate.update("update inventories set quantity=?, warning_threshold=?, warehouse_name=? where product_id=?",
                            quantity, threshold, warehouseName, productId);
                } else {
                    jdbcTemplate.update("insert into inventories(product_id,warehouse_name,quantity,warning_threshold) values(?,?,?,?)",
                            productId, warehouseName, quantity, threshold);
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
            Integer threshold = jdbcTemplate.queryForObject("select warning_threshold from inventories where product_id=?", Integer.class, productId);
            if (threshold != null && currentQty <= threshold) {
                String productName = jdbcTemplate.queryForObject("select name from products where id=?", String.class, productId);
                String title = "库存预警: " + productName + " 当前库存" + currentQty + "，低于阈值" + threshold;
                List<Long> keeperIds = jdbcTemplate.queryForList("select id from users where role_code='KEEPER' and status='ENABLED'", Long.class);
                for (Long keeperId : keeperIds) {
                    messageService.createMessage(keeperId, title, title, "ALERT", "INVENTORY", productId);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private Long findUserIdByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject("select id from users where username=?", Long.class, username);
        } catch (Exception ex) {
            return null;
        }
    }
}
