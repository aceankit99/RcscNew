/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.time.LocalDate;

/**
 *
 * @author shubhamv
 */
public class DateMetricPriorPeriod {

    private DateMetric metric;
    private MetricType metricType;

    public DateMetricPriorPeriod(DateMetric priorPeriodMetric) {
        this.metric = priorPeriodMetric;
        this.metricType = priorPeriodMetric.getMetricType();
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public LocalDate getValue() {
        return metric.getValue();
    }
}
