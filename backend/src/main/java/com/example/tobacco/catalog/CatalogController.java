package com.example.tobacco.catalog;

import com.example.tobacco.common.result.ApiResponse;
import com.example.tobacco.model.CategoryItem;
import com.example.tobacco.model.CategoryRequest;
import com.example.tobacco.model.ProductItem;
import com.example.tobacco.model.ProductRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/products")
    public ApiResponse<List<ProductItem>> listProducts() { return ApiResponse.success(catalogService.listProducts()); }

    @PostMapping("/api/products")
    public ApiResponse<String> createProduct(@Validated @RequestBody ProductRequest request) { catalogService.createProduct(request); return ApiResponse.success("ok"); }

    @PutMapping("/api/products/{id}")
    public ApiResponse<String> updateProduct(@PathVariable Long id, @Validated @RequestBody ProductRequest request) { catalogService.updateProduct(id, request); return ApiResponse.success("ok"); }

    @GetMapping("/api/categories")
    public ApiResponse<List<CategoryItem>> listCategories() { return ApiResponse.success(catalogService.listCategories()); }

    @PostMapping("/api/categories")
    public ApiResponse<String> createCategory(@Validated @RequestBody CategoryRequest request) { catalogService.createCategory(request); return ApiResponse.success("ok"); }
}
