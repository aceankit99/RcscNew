package com.flowserve.system606.model;

public class StringMetricPriorPeriod {

    private StringMetric metric;
    private MetricType metricType;

    public StringMetricPriorPeriod(StringMetric priorPeriodMetric) {
        this.metric = priorPeriodMetric;
        this.metricType = priorPeriodMetric.getMetricType();
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public String getValue() {
        return metric.getValue();
    }

}
