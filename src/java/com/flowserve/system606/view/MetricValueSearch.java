/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.MetricService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named(value = "metricValueSearch")
@ViewScoped
public class MetricValueSearch implements Serializable {

    List<MetricType> metricTypes = new ArrayList<MetricType>();
    List<ReportingUnit> reportingUnits = new ArrayList<ReportingUnit>();
    List<FinancialPeriod> financialPeriods = new ArrayList<FinancialPeriod>();
    List<Object[]> metricList = new ArrayList<Object[]>();
    @Inject
    private MetricService metricService;
    @Inject
    private AdminService adminService;
    @Inject
    private FinancialPeriodService financialPeriodService;
    private MetricType metricCode;
    private FinancialPeriod selectedPeriod;
    private ReportingUnit selectedRU;

    /**
     * Creates a new instance of MetricValueSearch
     */
    public MetricValueSearch() {
    }

    @PostConstruct
    public void init() {

        try {
            metricTypes = metricService.findCurrencyMetric();
            reportingUnits = adminService.findAllReportingUnits();
            financialPeriods = financialPeriodService.findValidDataPeriods();
        } catch (Exception ex) {
            Logger.getLogger(MetricValueSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void search() throws Exception {

        metricList = metricService.searchMetric(metricCode, selectedRU, selectedPeriod);
    }

    public List<MetricType> getMetricTypes() {
        return metricTypes;
    }

    public void setMetricTypes(List<MetricType> metricTypes) {
        this.metricTypes = metricTypes;
    }

    public FinancialPeriod getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setSelectedPeriod(FinancialPeriod selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    public ReportingUnit getSelectedRU() {
        return selectedRU;
    }

    public void setSelectedRU(ReportingUnit selectedRU) {
        this.selectedRU = selectedRU;
    }

    public List<ReportingUnit> getReportingUnits() {
        return reportingUnits;
    }

    public void setReportingUnits(List<ReportingUnit> reportingUnits) {
        this.reportingUnits = reportingUnits;
    }

    public MetricType getMetricCode() {
        return metricCode;
    }

    public void setMetricCode(MetricType metricCode) {
        this.metricCode = metricCode;
    }

    public List<FinancialPeriod> getFinancialPeriods() {
        return financialPeriods;
    }

    public void setFinancialPeriods(List<FinancialPeriod> financialPeriods) {
        this.financialPeriods = financialPeriods;
    }

    public List<Object[]> getMetricList() {
        return metricList;
    }

    public void setMetricList(List<Object[]> metricList) {
        this.metricList = metricList;
    }

}
