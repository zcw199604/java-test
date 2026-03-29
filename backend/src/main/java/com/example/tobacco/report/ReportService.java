package com.example.tobacco.report;

import com.example.tobacco.mapper.report.ReportMapper;
import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.TrendPoint;
import com.example.tobacco.util.ExcelUtil;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final ReportMapper reportMapper;
    private final ExcelUtil excelUtil;

    public ReportService(ReportMapper reportMapper, ExcelUtil excelUtil) {
        this.reportMapper = reportMapper;
        this.excelUtil = excelUtil;
    }

    public ReportSummaryItem purchaseSummary() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("采购汇总");
        item.setCount(reportMapper.countPurchaseOrders());
        item.setAmount(reportMapper.sumPurchaseAmount());
        return item;
    }

    public ReportSummaryItem salesSummary() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("销售汇总");
        item.setCount(reportMapper.countSalesOrders());
        item.setAmount(reportMapper.sumSalesAmount());
        return item;
    }

    public ReportSummaryItem inventorySummary() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("库存汇总");
        item.setCount(reportMapper.countInventories());
        item.setAmount(reportMapper.sumInventoryQuantity());
        return item;
    }

    public List<TrendPoint> trend() {
        return reportMapper.selectTrend();
    }

    public Map<String, Object> psiSummary() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("purchase", purchaseSummary());
        result.put("sales", salesSummary());
        result.put("inventory", inventorySummary());
        result.put("receivableAmount", reportMapper.sumReceivableAmount());
        return result;
    }

    public List<Map<String, Object>> complianceTrace() {
        return reportMapper.selectComplianceTrace();
    }

    public List<Map<String, Object>> abnormalDocs() {
        return reportMapper.selectAbnormalDocs();
    }

    public Map<String, Object> auditAbnormalDoc(Long id, String decision, String remark, String operator) {
        String newStatus;
        if ("APPROVED".equalsIgnoreCase(decision)) {
            newStatus = "APPROVED";
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            newStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("审核决定必须为 APPROVED 或 REJECTED");
        }
        reportMapper.updateAbnormalDoc(id, newStatus, operator);
        return reportMapper.selectAbnormalDocById(id);
    }

    public Map<String, Object> linkage() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("categoryPurchaseSales", reportMapper.selectCategoryPurchaseSales());
        result.put("inventoryWarnings", reportMapper.selectInventoryWarnings());
        return result;
    }

    public byte[] exportExcel() {
        return excelUtil.exportWorkbook("报表", new String[]{"类型", "数量", "金额"}, reportMapper.selectExportRows());
    }
}
