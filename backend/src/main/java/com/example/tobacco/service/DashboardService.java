package com.example.tobacco.service;

import com.example.tobacco.dto.DashboardModuleDto;
import com.example.tobacco.dto.DashboardSummaryDto;
import com.example.tobacco.mapper.dashboard.DashboardMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardSummaryDto getSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setPurchaseCount(dashboardMapper.countPurchases());
        summary.setInventoryCount(dashboardMapper.countInventories());
        summary.setSalesCount(dashboardMapper.countSales());
        summary.setWarningCount(dashboardMapper.countWarnings());
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
}
