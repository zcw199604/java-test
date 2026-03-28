package com.example.tobacco.catalog;

import com.example.tobacco.model.CategoryItem;
import com.example.tobacco.model.CategoryRequest;
import com.example.tobacco.model.ProductItem;
import com.example.tobacco.model.ProductRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogService {

    private final JdbcTemplate jdbcTemplate;

    public CatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductItem> listProducts(String keyword, String status, String category) {
        StringBuilder sql = new StringBuilder(
                "select id, code, name, category, unit, unit_price as unitPrice, warning_threshold as warningThreshold, status from products where 1=1");
        List<Object> params = new ArrayList<Object>();
        if (hasText(keyword)) {
            sql.append(" and (code like ? or name like ? or category like ?)");
            String keywordLike = likeValue(keyword);
            params.add(keywordLike);
            params.add(keywordLike);
            params.add(keywordLike);
        }
        if (hasText(status)) {
            sql.append(" and status = ?");
            params.add(status.trim());
        }
        if (hasText(category)) {
            sql.append(" and category = ?");
            params.add(category.trim());
        }
        sql.append(" order by id");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<ProductItem>(ProductItem.class));
    }

    public ProductItem productDetail(Long id) {
        return jdbcTemplate.queryForObject(
                "select id, code, name, category, unit, unit_price as unitPrice, warning_threshold as warningThreshold, status from products where id=?",
                new BeanPropertyRowMapper<ProductItem>(ProductItem.class), id);
    }

    public List<CategoryItem> listCategories() {
        return jdbcTemplate.query("select id, name, remark, status from categories order by id", new BeanPropertyRowMapper<CategoryItem>(CategoryItem.class));
    }

    public void createCategory(CategoryRequest request) {
        jdbcTemplate.update("insert into categories(name, remark, status) values(?,?,'ENABLED')", request.getName(), request.getRemark());
    }

    public void updateCategory(Long id, CategoryRequest request) {
        jdbcTemplate.update("update categories set name=?, remark=? where id=?", request.getName(), request.getRemark(), id);
    }

    public void createProduct(ProductRequest request) {
        jdbcTemplate.update("insert into products(code,name,category,unit,unit_price,warning_threshold,status) values(?,?,?,?,?,?,'ENABLED')",
                request.getCode(), request.getName(), request.getCategory(), request.getUnit(), request.getUnitPrice(), request.getWarningThreshold());
    }

    public void updateProduct(Long id, ProductRequest request) {
        jdbcTemplate.update("update products set code=?, name=?, category=?, unit=?, unit_price=?, warning_threshold=? where id=?",
                request.getCode(), request.getName(), request.getCategory(), request.getUnit(), request.getUnitPrice(), request.getWarningThreshold(), id);
    }

    public void deleteProduct(Long id) {
        jdbcTemplate.update("update products set status='DISABLED' where id=?", id);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String likeValue(String value) {
        return "%" + value.trim() + "%";
    }
}
