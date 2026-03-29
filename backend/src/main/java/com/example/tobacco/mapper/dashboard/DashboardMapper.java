package com.example.tobacco.mapper.dashboard;

import com.example.tobacco.model.DashboardSalesHistoryRow;
import com.example.tobacco.model.DashboardSalesTopItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    @Select("select s.product_id as productId, p.name as productName, ifnull(sum(case when #{metric} = 'amount' then s.total_amount else s.quantity end), 0) as totalValue " +
            "from sales_orders s left join products p on s.product_id = p.id " +
            "where date(ifnull(s.outbound_at, s.created_at)) between #{startDate} and #{endDate} " +
            "and s.status in ('OUTBOUND', 'PARTIAL_PAID', 'PAID') " +
            "group by s.product_id, p.name order by totalValue desc, s.product_id asc limit #{limit}")
    List<DashboardSalesTopItem> selectTopSalesProducts(@Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("metric") String metric,
                                                       @Param("limit") Integer limit);

    @Select({"<script>",
            "select s.product_id as productId, p.name as productName, min(date_format(ifnull(s.outbound_at, s.created_at), '%Y-%m-%d')) as period, ",
            "ifnull(sum(case when #{metric} = 'amount' then s.total_amount else s.quantity end), 0) as metricValue ",
            "from sales_orders s left join products p on s.product_id = p.id ",
            "where date(ifnull(s.outbound_at, s.created_at)) between #{startDate} and #{endDate} ",
            "and s.status in ('OUTBOUND', 'PARTIAL_PAID', 'PAID') ",
            "and s.product_id in ",
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> ",
            "group by s.product_id, p.name, date(ifnull(s.outbound_at, s.created_at)) ",
            "order by period asc, s.product_id asc",
            "</script>"})
    List<DashboardSalesHistoryRow> selectSalesHistory(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("metric") String metric,
                                                      @Param("productIds") List<Long> productIds);
}
