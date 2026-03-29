package com.example.tobacco.service;

import com.example.tobacco.dto.DashboardModuleDto;
import com.example.tobacco.dto.DashboardSalesHistoryDto;
import com.example.tobacco.dto.DashboardSalesSeriesDto;
import com.example.tobacco.dto.DashboardSummaryDto;
import com.example.tobacco.mapper.dashboard.DashboardMapper;
import com.example.tobacco.model.DashboardSalesHistoryRow;
import com.example.tobacco.model.DashboardSalesTopItem;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final List<Integer> SUPPORTED_DAYS = Arrays.asList(7, 30);
    private static final int DEFAULT_LIMIT = 6;
    private static final int MAX_LIMIT = 10;

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

    public DashboardSalesHistoryDto getSalesHistory(String metric, Integer days, Integer limit) {
        String normalizedMetric = normalizeMetric(metric);
        int normalizedDays = normalizeDays(days);
        int normalizedLimit = normalizeLimit(limit);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(normalizedDays - 1L);
        List<String> periods = buildPeriods(startDate, endDate);
        List<DashboardSalesTopItem> topItems = dashboardMapper.selectTopSalesProducts(startDate.toString(), endDate.toString(), normalizedMetric, normalizedLimit);

        DashboardSalesHistoryDto result = new DashboardSalesHistoryDto();
        result.setMetric(normalizedMetric);
        result.setPeriods(periods);
        if (topItems == null || topItems.isEmpty()) {
            result.setSeries(new ArrayList<>());
            return result;
        }

        List<Long> productIds = topItems.stream().map(DashboardSalesTopItem::getProductId).collect(Collectors.toList());
        List<DashboardSalesHistoryRow> rows = dashboardMapper.selectSalesHistory(startDate.toString(), endDate.toString(), normalizedMetric, productIds);
        Map<Long, Map<String, BigDecimal>> valueMap = new LinkedHashMap<>();
        for (DashboardSalesHistoryRow row : rows) {
            valueMap.computeIfAbsent(row.getProductId(), key -> new LinkedHashMap<>()).put(row.getPeriod(), defaultDecimal(row.getMetricValue()));
        }

        List<DashboardSalesSeriesDto> series = new ArrayList<>();
        for (DashboardSalesTopItem item : topItems) {
            DashboardSalesSeriesDto seriesDto = new DashboardSalesSeriesDto();
            seriesDto.setProductId(item.getProductId());
            seriesDto.setProductName(item.getProductName());
            Map<String, BigDecimal> productValues = valueMap.getOrDefault(item.getProductId(), new LinkedHashMap<>());
            List<BigDecimal> values = periods.stream()
                    .map(period -> defaultDecimal(productValues.get(period)))
                    .collect(Collectors.toList());
            seriesDto.setValues(values);
            series.add(seriesDto);
        }
        result.setSeries(series);
        return result;
    }

    private String normalizeMetric(String metric) {
        if (metric == null || StringUtil.isBlank(metric) || "quantity".equalsIgnoreCase(metric)) {
            return "quantity";
        }
        if ("amount".equalsIgnoreCase(metric)) {
            return "amount";
        }
        throw new IllegalArgumentException("metric 仅支持 quantity 或 amount");
    }

    private int normalizeDays(Integer days) {
        if (days == null) {
            return 7;
        }
        if (!SUPPORTED_DAYS.contains(days)) {
            throw new IllegalArgumentException("days 仅支持 7 或 30");
        }
        return days;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit 仅支持 1 到 10");
        }
        return limit;
    }

    private List<String> buildPeriods(LocalDate startDate, LocalDate endDate) {
        List<String> periods = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            periods.add(date.toString());
        }
        return periods;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
