package com.example.tobacco.catalog;

import com.example.tobacco.model.CategoryItem;
import com.example.tobacco.model.CategoryRequest;
import com.example.tobacco.model.ProductItem;
import com.example.tobacco.model.ProductRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final JdbcTemplate jdbcTemplate;

    public CatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductItem> listProducts() {
        return jdbcTemplate.query(
                "select id, code, name, category, unit, unit_price as unitPrice, warning_threshold as warningThreshold, status from products order by id",
                new BeanPropertyRowMapper<ProductItem>(ProductItem.class));
    }

    public List<CategoryItem> listCategories() {
        return jdbcTemplate.query("select id, name, remark, status from categories order by id", new BeanPropertyRowMapper<CategoryItem>(CategoryItem.class));
    }

    public void createCategory(CategoryRequest request) {
        jdbcTemplate.update("insert into categories(name, remark, status) values(?,?,'ENABLED')", request.getName(), request.getRemark());
    }

    public void createProduct(ProductRequest request) {
        jdbcTemplate.update("insert into products(code,name,category,unit,unit_price,warning_threshold,status) values(?,?,?,?,?,?,'ENABLED')",
                request.getCode(), request.getName(), request.getCategory(), request.getUnit(), request.getUnitPrice(), request.getWarningThreshold());
    }

    public void updateProduct(Long id, ProductRequest request) {
        jdbcTemplate.update("update products set code=?, name=?, category=?, unit=?, unit_price=?, warning_threshold=? where id=?",
                request.getCode(), request.getName(), request.getCategory(), request.getUnit(), request.getUnitPrice(), request.getWarningThreshold(), id);
    }
}
