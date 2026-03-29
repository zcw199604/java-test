package com.example.tobacco.dto;

import java.util.List;

public class DashboardSalesHistoryDto {

    private String metric;
    private List<String> periods;
    private List<DashboardSalesSeriesDto> series;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public List<String> getPeriods() {
        return periods;
    }

    public void setPeriods(List<String> periods) {
        this.periods = periods;
    }

    public List<DashboardSalesSeriesDto> getSeries() {
        return series;
    }

    public void setSeries(List<DashboardSalesSeriesDto> series) {
        this.series = series;
    }
}
