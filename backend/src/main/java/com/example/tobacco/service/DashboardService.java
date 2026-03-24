package com.example.tobacco.service;

import com.example.tobacco.dto.DashboardModuleDto;
import com.example.tobacco.dto.DashboardSummaryDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardSummaryDto getSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setPurchaseCount(queryCount("purchase_orders"));
        summary.setInventoryCount(queryCount("inventories"));
        summary.setSalesCount(queryCount("sales_orders"));
        summary.setWarningCount(queryWarningCount());
        summary.setModules(Arrays.asList(
                new DashboardModuleDto("users", "用户管理", "系统用户与角色维护", "/system/users"),
                new DashboardModuleDto("roles", "角色权限", "角色与权限配置总览", "/system/roles"),
                new DashboardModuleDto("products", "商品管理", "烟草商品、价格与库存阈值", "/catalog/products"),
                new DashboardModuleDto("suppliers", "供应商管理", "供应商与联系人资料维护", "/supplier/list"),
                new DashboardModuleDto("purchase", "采购管理", "采购创建、到货登记与入库流转", "/purchase"),
                new DashboardModuleDto("inventory", "库存管理", "库存台账、预警、盘点与调拨", "/inventory"),
                new DashboardModuleDto("sales", "销售管理", "销售订单、出库与回款跟踪", "/sales"),
                new DashboardModuleDto("admin", "管理中心", "报表、基础设置与运营概览", "/admin"),
                new DashboardModuleDto("report", "报表中心", "采购、销售、库存汇总与趋势分析", "/report/center")
        ));
        return summary;
    }

    private Integer queryCount(String table) {
        return jdbcTemplate.queryForObject("select count(1) from " + table, Integer.class);
    }

    private Integer queryWarningCount() {
        return jdbcTemplate.queryForObject("select count(1) from inventories where quantity <= warning_threshold", Integer.class);
    }
}
