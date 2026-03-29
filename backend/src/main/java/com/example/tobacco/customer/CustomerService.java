package com.example.tobacco.customer;

import com.example.tobacco.mapper.customer.CustomerMapper;
import com.example.tobacco.model.CustomerItem;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public List<CustomerItem> list(String keyword, String status) {
        return customerMapper.list(trim(keyword), likeValue(keyword), trim(status));
    }

    public CustomerItem detail(Long id) {
        return customerMapper.detail(id);
    }

    public void create(Map<String, String> request) {
        customerMapper.insert(
                normalizeText(request.get("name")),
                normalizeText(request.get("contactName")),
                request.get("contactPhone"),
                normalizeText(request.get("address")),
                request.getOrDefault("status", "ENABLED"));
    }

    public void update(Long id, Map<String, String> request) {
        CustomerItem existing = detail(id);
        customerMapper.update(
                id,
                normalizeText(valueOrDefault(request.get("name"), existing.getName())),
                normalizeText(valueOrDefault(request.get("contactName"), existing.getContactName())),
                valueOrDefault(request.get("contactPhone"), existing.getContactPhone()),
                normalizeText(valueOrDefault(request.get("address"), existing.getAddress())),
                valueOrDefault(request.get("status"), existing.getStatus()));
    }

    public void disable(Long id) {
        customerMapper.disable(id);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String trim(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String likeValue(String value) {
        return hasText(value) ? "%" + value.trim() + "%" : null;
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
