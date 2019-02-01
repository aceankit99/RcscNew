/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.MetricService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author user
 */
@Named(value = "metricControlTotals")
@ViewScoped
public class MetricControlTotals implements Serializable {

    /**
     * Creates a new instance of MetricControlTotals
     */
    List<FinancialPeriod> financialPeriods = new ArrayList<FinancialPeriod>();
    List<String> metricCodeList = new ArrayList();

    List<Object[]> metricControlTotalsList = new ArrayList<Object[]>();

    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private MetricService metricService;

    private FinancialPeriod selectedPeriod;
    private MetricType metricType;
    @Inject
    private WebSession webSession;

    public MetricControlTotals() {
    }

    @PostConstruct
    public void init() {
        metricCodeList.add("REVENUE_TO_RECOGNIZE_PERIOD_CC");
        metricCodeList.add("OPERATING_INCOME_PERIOD_LC");
        metricCodeList.add("TPC_TO_ACCEL_CTD_LC");
        metricCodeList.add("PARTIAL_SHIPMENT_COSTS_LC");
        metricCodeList.add("REVENUE_TO_RECOGNIZE_CTD_CC");
        metricCodeList.add("COST_OF_GOODS_SOLD_CTD_LC");
        metricCodeList.add("CONTRACT_BILLINGS_PERIOD_CC");

        financialPeriods = financialPeriodService.findValidDataPeriods();
        selectedPeriod = webSession.getCurrentPeriod();
    }

    public List<Object[]> getMetricControlTotalsList() throws Exception {
        metricControlTotalsList.clear();
        for (String metricCode : metricCodeList) {
            metricType = metricService.findMetricTypeByCode(metricCode);
            Object[] sumOfMetricType = metricService.findSumOfMetricTypeForAllRU(metricType, selectedPeriod);
            if (sumOfMetricType != null) {
                metricControlTotalsList.add(sumOfMetricType);
            }
        }

        return metricControlTotalsList;
    }

    public List<FinancialPeriod> getFinancialPeriods() {
        return financialPeriods;
    }

    public void setFinancialPeriods(List<FinancialPeriod> financialPeriods) {
        this.financialPeriods = financialPeriods;
    }

    public FinancialPeriod getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setSelectedPeriod(FinancialPeriod selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    public void setMetricControlTotalsList(List<Object[]> metricControlTotalsList) {
        this.metricControlTotalsList = metricControlTotalsList;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

}
