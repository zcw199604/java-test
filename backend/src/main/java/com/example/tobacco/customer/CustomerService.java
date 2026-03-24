package com.example.tobacco.customer;

import com.example.tobacco.model.CustomerItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final JdbcTemplate jdbcTemplate;
    public CustomerService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }
    public List<CustomerItem> list() {
        return jdbcTemplate.query("select id,name,contact_name as contactName,contact_phone as contactPhone,address,status from customers order by id", new BeanPropertyRowMapper<CustomerItem>(CustomerItem.class));
    }
}
