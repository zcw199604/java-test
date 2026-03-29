package com.example.tobacco.mapper.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DashboardMapper {

    @Select("select count(1) from purchase_orders")
    Integer countPurchases();

    @Select("select count(1) from inventories")
    Integer countInventories();

    @Select("select count(1) from sales_orders")
    Integer countSales();

    @Select("select count(1) from inventories where quantity <= warning_threshold")
    Integer countWarnings();
}
