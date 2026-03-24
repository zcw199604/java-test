package com.example.tobacco.inventory;

import com.example.tobacco.model.InventoryChangeRequest;
import com.example.tobacco.model.InventoryItem;
import com.example.tobacco.model.InventoryRecordItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {
    private final JdbcTemplate jdbcTemplate;
    public InventoryService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

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
        int afterQty = beforeQty;
        jdbcTemplate.update("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)", request.getProductId(), "TRANSFER", null, 0, beforeQty, afterQty, operatorName, request.getRemark());
        return "调拨记录已登记";
    }

    @Transactional
    public String check(InventoryChangeRequest request, String operatorName) {
        Integer beforeQty = jdbcTemplate.queryForObject("select quantity from inventories where product_id=?", Integer.class, request.getProductId());
        if (beforeQty == null) throw new IllegalArgumentException("库存不存在");
        int afterQty = request.getQuantity();
        jdbcTemplate.update("update inventories set quantity=? where product_id=?", afterQty, request.getProductId());
        jdbcTemplate.update("insert into inventory_records(product_id,biz_type,biz_id,change_qty,before_qty,after_qty,operator_name,remark) values(?,?,?,?,?,?,?,?)", request.getProductId(), "CHECK", null, afterQty - beforeQty, beforeQty, afterQty, operatorName, request.getRemark());
        return "盘点结果已更新";
    }
}
