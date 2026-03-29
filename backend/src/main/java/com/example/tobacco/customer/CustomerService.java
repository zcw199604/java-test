package com.example.tobacco.customer;

import com.example.tobacco.model.CustomerItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private final JdbcTemplate jdbcTemplate;
    public CustomerService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<CustomerItem> list(String keyword, String status) {
        StringBuilder sql = new StringBuilder("select id, name, contact_name as contactName, contact_phone as contactPhone, address, status from customers where 1=1");
        List<Object> params = new ArrayList<Object>();
        if (hasText(keyword)) {
            sql.append(" and (name like ? or contact_name like ? or contact_phone like ?)");
            String value = "%" + keyword.trim() + "%";
            params.add(value);
            params.add(value);
            params.add(value);
        }
        if (hasText(status)) {
            sql.append(" and status=?");
            params.add(status.trim());
        }
        sql.append(" order by id");
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<CustomerItem>(CustomerItem.class), params.toArray());
    }

    public CustomerItem detail(Long id) {
        return jdbcTemplate.queryForObject(
                "select id, name, contact_name as contactName, contact_phone as contactPhone, address, status from customers where id=?",
                new BeanPropertyRowMapper<CustomerItem>(CustomerItem.class),
                id);
    }

    public void create(Map<String, String> request) {
        jdbcTemplate.update("insert into customers(name, contact_name, contact_phone, address, status) values(?,?,?,?,?)",
                normalizeText(request.get("name")), normalizeText(request.get("contactName")), request.get("contactPhone"), normalizeText(request.get("address")), request.getOrDefault("status", "ENABLED"));
    }

    public void update(Long id, Map<String, String> request) {
        CustomerItem existing = detail(id);
        jdbcTemplate.update("update customers set name=?, contact_name=?, contact_phone=?, address=?, status=? where id=?",
                normalizeText(valueOrDefault(request.get("name"), existing.getName())),
                normalizeText(valueOrDefault(request.get("contactName"), existing.getContactName())),
                valueOrDefault(request.get("contactPhone"), existing.getContactPhone()),
                normalizeText(valueOrDefault(request.get("address"), existing.getAddress())),
                valueOrDefault(request.get("status"), existing.getStatus()),
                id);
    }

    public void disable(Long id) {
        jdbcTemplate.update("update customers set status='DISABLED' where id=?", id);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private String normalizeText(String value) {
        if (!hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (!trimmed.matches(".*[\u00C0-\u00FF].*")) {
            return trimmed;
        }
        try {
            return new String(trimmed.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return trimmed;
        }
    }
}
