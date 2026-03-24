package com.example.tobacco.customer;

import com.example.tobacco.model.CustomerItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private final JdbcTemplate jdbcTemplate;
    public CustomerService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }
    public List<CustomerItem> list() {
        return jdbcTemplate.query("select id, name, contact_name as contactName, contact_phone as contactPhone, address, status from customers order by id", new BeanPropertyRowMapper<CustomerItem>(CustomerItem.class));
    }
    public void create(Map<String, String> request) {
        jdbcTemplate.update("insert into customers(name, contact_name, contact_phone, address, status, credit_limit) values(?,?,?,?,?,?)",
                request.get("name"), request.get("contactName"), request.get("contactPhone"), request.get("address"), request.getOrDefault("status", "ENABLED"), request.getOrDefault("creditLimit", "0"));
    }
    public void update(Long id, Map<String, String> request) {
        jdbcTemplate.update("update customers set name=?, contact_name=?, contact_phone=?, address=?, status=?, credit_limit=? where id=?",
                request.get("name"), request.get("contactName"), request.get("contactPhone"), request.get("address"), request.getOrDefault("status", "ENABLED"), request.getOrDefault("creditLimit", "0"), id);
    }
    public void disable(Long id) {
        jdbcTemplate.update("update customers set status='DISABLED' where id=?", id);
    }
}
