/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.util.Currency;
import java.util.List;

/**
 *
 * @author kgraves
 */
public interface Measurable {

    public List<Measurable> getChildMeasurables();

    public Currency getLocalCurrency();

    public Currency getContractCurrency();

    public Currency getReportingCurrency();

    public Metric getPeriodMetric(FinancialPeriod period, MetricType metricType);

    public MetricSet initializeMetricSetForPeriod(FinancialPeriod period);

    public Metric initializeMetricForPeriod(FinancialPeriod period, MetricType metricType);

    public boolean metricSetExistsForPeriod(FinancialPeriod period);

    public boolean metricExistsForPeriod(FinancialPeriod period, MetricType metricType);


}
