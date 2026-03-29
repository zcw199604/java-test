package com.example.tobacco.catalog;

import com.example.tobacco.mapper.catalog.CatalogMapper;
import com.example.tobacco.model.CategoryItem;
import com.example.tobacco.model.CategoryRequest;
import com.example.tobacco.model.ProductItem;
import com.example.tobacco.model.ProductRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final CatalogMapper catalogMapper;

    public CatalogService(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public List<ProductItem> listProducts(String keyword, String status, String category) {
        return catalogMapper.listProducts(trim(keyword), likeValue(keyword), trim(status), trim(category));
    }

    public ProductItem productDetail(Long id) {
        return catalogMapper.productDetail(id);
    }

    public List<CategoryItem> listCategories() {
        return catalogMapper.listCategories();
    }

    public void createCategory(CategoryRequest request) {
        catalogMapper.insertCategory(request.getName(), request.getRemark());
    }

    public void updateCategory(Long id, CategoryRequest request) {
        catalogMapper.updateCategory(id, request.getName(), request.getRemark());
    }

    public void createProduct(ProductRequest request) {
        catalogMapper.insertProduct(request.getCode(), request.getName(), request.getCategory(), request.getUnit(), request.getUnitPrice(), request.getWarningThreshold());
    }

    public void updateProduct(Long id, ProductRequest request) {
        catalogMapper.updateProduct(id, request.getCode(), request.getName(), request.getCategory(), request.getUnit(), request.getUnitPrice(), request.getWarningThreshold());
    }

    public void deleteProduct(Long id) {
        catalogMapper.disableProduct(id);
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
}
