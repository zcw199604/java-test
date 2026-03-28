package com.example.tobacco.supplier;

import com.example.tobacco.model.SupplierItem;
import com.example.tobacco.model.SupplierRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierService {

    private final JdbcTemplate jdbcTemplate;

    public SupplierService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<SupplierItem> listSuppliers(String keyword, String status) {
        StringBuilder sql = new StringBuilder(
                "select id, name, contact_name as contactName, contact_phone as contactPhone, address, status from suppliers where 1=1");
        List<Object> params = new ArrayList<Object>();
        if (hasText(keyword)) {
            sql.append(" and (name like ? or contact_name like ? or contact_phone like ? or address like ?)");
            String keywordLike = likeValue(keyword);
            params.add(keywordLike);
            params.add(keywordLike);
            params.add(keywordLike);
            params.add(keywordLike);
        }
        if (hasText(status)) {
            sql.append(" and status = ?");
            params.add(status.trim());
        }
        sql.append(" order by id");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<SupplierItem>(SupplierItem.class));
    }

    public void createSupplier(SupplierRequest request) {
        jdbcTemplate.update("insert into suppliers(name,contact_name,contact_phone,address,status) values(?,?,?,?, 'ENABLED')", request.getName(), request.getContactName(), request.getContactPhone(), request.getAddress());
    }

    public void updateSupplier(Long id, SupplierRequest request) {
        jdbcTemplate.update("update suppliers set name=?, contact_name=?, contact_phone=?, address=? where id=?", request.getName(), request.getContactName(), request.getContactPhone(), request.getAddress(), id);
    }

    public void disableSupplier(Long id) {
        jdbcTemplate.update("update suppliers set status='DISABLED' where id=?", id);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String likeValue(String value) {
        return "%" + value.trim() + "%";
    }
}
