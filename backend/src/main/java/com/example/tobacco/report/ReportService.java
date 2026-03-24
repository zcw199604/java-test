package com.example.tobacco.report;

import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.TrendPoint;
import com.example.tobacco.util.ExcelUtil;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final JdbcTemplate jdbcTemplate;
    private final ExcelUtil excelUtil;

    public ReportService(JdbcTemplate jdbcTemplate, ExcelUtil excelUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.excelUtil = excelUtil;
    }

    public ReportSummaryItem purchaseSummary() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("采购汇总");
        item.setCount(jdbcTemplate.queryForObject("select count(1) from purchase_orders", Integer.class));
        item.setAmount(jdbcTemplate.queryForObject("select ifnull(sum(total_amount),0) from purchase_orders", BigDecimal.class));
        return item;
    }

    public ReportSummaryItem salesSummary() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("销售汇总");
        item.setCount(jdbcTemplate.queryForObject("select count(1) from sales_orders", Integer.class));
        item.setAmount(jdbcTemplate.queryForObject("select ifnull(sum(total_amount),0) from sales_orders", BigDecimal.class));
        return item;
    }

    public ReportSummaryItem inventorySummary() {
        ReportSummaryItem item = new ReportSummaryItem();
        item.setLabel("库存汇总");
        item.setCount(jdbcTemplate.queryForObject("select count(1) from inventories", Integer.class));
        item.setAmount(jdbcTemplate.queryForObject("select ifnull(sum(quantity),0) from inventories", BigDecimal.class));
        return item;
    }

    public List<TrendPoint> trend() {
        return jdbcTemplate.query(
                "select date_format(created_date, '%Y-%m-%d') as period, ifnull(sum_purchase,0) as purchaseAmount, ifnull(sum_sales,0) as salesAmount " +
                        "from (select curdate() as created_date union all select date_sub(curdate(), interval 1 day) union all select date_sub(curdate(), interval 2 day) union all select date_sub(curdate(), interval 3 day) union all select date_sub(curdate(), interval 4 day) union all select date_sub(curdate(), interval 5 day) union all select date_sub(curdate(), interval 6 day)) d " +
                        "left join (select date(created_at) as pday, sum(total_amount) as sum_purchase from purchase_orders group by date(created_at)) p on d.created_date = p.pday " +
                        "left join (select date(created_at) as sday, sum(total_amount) as sum_sales from sales_orders group by date(created_at)) s on d.created_date = s.sday order by created_date",
                new BeanPropertyRowMapper<TrendPoint>(TrendPoint.class));
    }

    public Map<String, Object> psiSummary() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("purchase", purchaseSummary());
        result.put("sales", salesSummary());
        result.put("inventory", inventorySummary());
        result.put("receivableAmount", jdbcTemplate.queryForObject("select ifnull(sum(total_amount - paid_amount),0) from sales_orders", BigDecimal.class));
        return result;
    }

    public List<Map<String, Object>> complianceTrace() {
        return jdbcTemplate.queryForList("select id, biz_type as bizType, biz_id as bizId, order_no as orderNo, node_code as nodeCode, node_name as nodeName, operator, remark, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from trace_records order by id desc");
    }

    public List<Map<String, Object>> abnormalDocs() {
        return jdbcTemplate.queryForList("select id, biz_type as bizType, biz_id as bizId, order_no as orderNo, abnormal_type as abnormalType, status, reported_by as reportedBy, audited_by as auditedBy, detail, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from abnormal_documents order by id desc");
    }

    public Map<String, Object> linkage() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("categoryPurchaseSales", jdbcTemplate.queryForList("select p.category, ifnull(sum(po.total_amount),0) as purchaseAmount, ifnull(sum(so.total_amount),0) as salesAmount from products p left join purchase_orders po on p.id=po.product_id left join sales_orders so on p.id=so.product_id group by p.category"));
        result.put("inventoryWarnings", jdbcTemplate.queryForList("select p.name as productName, i.quantity, i.warning_threshold as warningThreshold from inventories i left join products p on i.product_id=p.id where i.quantity <= i.warning_threshold"));
        return result;
    }

    public byte[] exportExcel() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select '采购汇总' as 类型, count(1) as 数量, ifnull(sum(total_amount),0) as 金额 from purchase_orders union all select '销售汇总' as 类型, count(1), ifnull(sum(total_amount),0) from sales_orders union all select '库存汇总' as 类型, count(1), ifnull(sum(quantity),0) from inventories");
        return excelUtil.exportWorkbook("报表", new String[]{"类型", "数量", "金额"}, rows);
    }
}
