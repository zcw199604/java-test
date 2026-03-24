package com.example.tobacco.report;

import com.example.tobacco.model.ReportSummaryItem;
import com.example.tobacco.model.TrendPoint;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {
    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public String exportCsv() {
        ReportSummaryItem purchase = purchaseSummary();
        ReportSummaryItem sales = salesSummary();
        ReportSummaryItem inventory = inventorySummary();
        List<TrendPoint> trendPoints = trend();

        StringBuilder builder = new StringBuilder();
        builder.append("报表类型,单据数/品项数,金额/库存数量\n");
        builder.append(csvLine(purchase.getLabel(), purchase.getCount(), purchase.getAmount()));
        builder.append(csvLine(sales.getLabel(), sales.getCount(), sales.getAmount()));
        builder.append(csvLine(inventory.getLabel(), inventory.getCount(), inventory.getAmount()));
        builder.append("\n趋势日期,采购金额,销售金额\n");
        for (TrendPoint point : trendPoints) {
            builder.append(csv(point.getPeriod())).append(',')
                    .append(csv(point.getPurchaseAmount())).append(',')
                    .append(csv(point.getSalesAmount())).append('\n');
        }
        return builder.toString();
    }

    private String csvLine(String label, Object count, Object amount) {
        return csv(label) + ',' + csv(count) + ',' + csv(amount) + '\n';
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        String escaped = text.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
