package com.example.tobacco.mapper.report;

import com.example.tobacco.model.TrendPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("select count(1) from purchase_orders")
    Integer countPurchaseOrders();

    @Select("select ifnull(sum(total_amount),0) from purchase_orders")
    BigDecimal sumPurchaseAmount();

    @Select("select count(1) from sales_orders")
    Integer countSalesOrders();

    @Select("select ifnull(sum(total_amount),0) from sales_orders")
    BigDecimal sumSalesAmount();

    @Select("select count(1) from inventories")
    Integer countInventories();

    @Select("select ifnull(sum(quantity),0) from inventories")
    BigDecimal sumInventoryQuantity();

    @Select("select date_format(created_date, '%Y-%m-%d') as period, ifnull(sum_purchase,0) as purchaseAmount, ifnull(sum_sales,0) as salesAmount " +
            "from (select curdate() as created_date union all select date_sub(curdate(), interval 1 day) union all select date_sub(curdate(), interval 2 day) union all select date_sub(curdate(), interval 3 day) union all select date_sub(curdate(), interval 4 day) union all select date_sub(curdate(), interval 5 day) union all select date_sub(curdate(), interval 6 day)) d " +
            "left join (select date(created_at) as pday, sum(total_amount) as sum_purchase from purchase_orders group by date(created_at)) p on d.created_date = p.pday " +
            "left join (select date(created_at) as sday, sum(total_amount) as sum_sales from sales_orders group by date(created_at)) s on d.created_date = s.sday order by created_date")
    List<TrendPoint> selectTrend();

    @Select("select ifnull(sum(total_amount - paid_amount),0) from sales_orders")
    BigDecimal sumReceivableAmount();

    @SelectProvider(type = ReportSqlProvider.class, method = "buildComplianceTraceSql")
    List<Map<String, Object>> selectComplianceTrace(@Param("keywordLike") String keywordLike,
                                                    @Param("warehouseId") Long warehouseId,
                                                    @Param("bizType") String bizType,
                                                    @Param("nodeCode") String nodeCode);

    @Select("select id, biz_type as bizType, biz_id as bizId, order_no as orderNo, abnormal_type as abnormalType, status, reported_by as reportedBy, audited_by as auditedBy, detail, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from abnormal_documents order by id desc")
    List<Map<String, Object>> selectAbnormalDocs();

    @Update("update abnormal_documents set status=#{status}, audited_by=#{operator} where id=#{id} and status='PENDING'")
    int updateAbnormalDoc(@Param("id") Long id, @Param("status") String status, @Param("operator") String operator);

    @Select("select id, biz_type as bizType, biz_id as bizId, order_no as orderNo, abnormal_type as abnormalType, status, reported_by as reportedBy, audited_by as auditedBy, detail, DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') as createdAt from abnormal_documents where id=#{id}")
    Map<String, Object> selectAbnormalDocById(@Param("id") Long id);

    @Select("select p.category, ifnull(sum(po.total_amount),0) as purchaseAmount, ifnull(sum(so.total_amount),0) as salesAmount from products p left join purchase_orders po on p.id=po.product_id left join sales_orders so on p.id=so.product_id group by p.category")
    List<Map<String, Object>> selectCategoryPurchaseSales();

    @Select("select p.name as productName, i.quantity, i.warning_threshold as warningThreshold from inventories i left join products p on i.product_id=p.id where i.quantity <= i.warning_threshold")
    List<Map<String, Object>> selectInventoryWarnings();

    @Select("select '采购汇总' as 类型, count(1) as 数量, ifnull(sum(total_amount),0) as 金额 from purchase_orders union all select '销售汇总' as 类型, count(1), ifnull(sum(total_amount),0) from sales_orders union all select '库存汇总' as 类型, count(1), ifnull(sum(quantity),0) from inventories")
    List<Map<String, Object>> selectExportRows();
}
