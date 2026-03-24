package com.example.tobacco.supplier;

import com.example.tobacco.model.SupplierItem;
import com.example.tobacco.model.SupplierRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final JdbcTemplate jdbcTemplate;

    public SupplierService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<SupplierItem> listSuppliers() {
        return jdbcTemplate.query(
                "select id, name, contact_name as contactName, contact_phone as contactPhone, address, status from suppliers order by id",
                new BeanPropertyRowMapper<SupplierItem>(SupplierItem.class));
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
}
