package com.example.tobacco.mapper.catalog;

import com.example.tobacco.model.CategoryItem;
import com.example.tobacco.model.ProductItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CatalogMapper {

    @SelectProvider(type = CatalogSqlProvider.class, method = "buildProductListSql")
    List<ProductItem> listProducts(@Param("keyword") String keyword,
                                   @Param("keywordLike") String keywordLike,
                                   @Param("status") String status,
                                   @Param("category") String category);

    @Select("select id, code, name, category, unit, unit_price as unitPrice, warning_threshold as warningThreshold, status from products where id=#{id}")
    ProductItem productDetail(@Param("id") Long id);

    @Select("select id, name, remark, status from categories order by id")
    List<CategoryItem> listCategories();

    @Insert("insert into categories(name, remark, status) values(#{name}, #{remark}, 'ENABLED')")
    void insertCategory(@Param("name") String name, @Param("remark") String remark);

    @Update("update categories set name=#{name}, remark=#{remark} where id=#{id}")
    void updateCategory(@Param("id") Long id, @Param("name") String name, @Param("remark") String remark);

    @Insert("insert into products(code, name, category, unit, unit_price, warning_threshold, status) values(#{code}, #{name}, #{category}, #{unit}, #{unitPrice}, #{warningThreshold}, 'ENABLED')")
    void insertProduct(@Param("code") String code,
                       @Param("name") String name,
                       @Param("category") String category,
                       @Param("unit") String unit,
                       @Param("unitPrice") BigDecimal unitPrice,
                       @Param("warningThreshold") Integer warningThreshold);

    @Update("update products set code=#{code}, name=#{name}, category=#{category}, unit=#{unit}, unit_price=#{unitPrice}, warning_threshold=#{warningThreshold} where id=#{id}")
    void updateProduct(@Param("id") Long id,
                       @Param("code") String code,
                       @Param("name") String name,
                       @Param("category") String category,
                       @Param("unit") String unit,
                       @Param("unitPrice") BigDecimal unitPrice,
                       @Param("warningThreshold") Integer warningThreshold);

    @Update("update products set status='DISABLED' where id=#{id}")
    void disableProduct(@Param("id") Long id);
}
