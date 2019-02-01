/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.LoadTestService;
import com.flowserve.system606.service.LoadTestServiceSet;
import com.flowserve.system606.service.LoadTestServiceSet2;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named(value = "calculateParallelDataset")
@RequestScoped
public class CalculateParallelDataset {

    /**
     * Creates a new instance of CalculateParallelDataset
     */
    @Inject
    private AdminService adminService;

    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private LoadTestService loadTestService;
    @Inject
    private LoadTestServiceSet loadTestServiceSet;
    @Inject
    private LoadTestServiceSet2 loadTestServiceSet2;
    private List<ReportingUnit> reportingUnitsToProcess = new ArrayList<ReportingUnit>();

    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    private Future<String> asyncResult;

    public CalculateParallelDataset() {
    }

    @PostConstruct
    public void init() {
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("1100"));
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("1200"));
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("5050"));
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("7866"));
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("8405"));
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("1205"));
        reportingUnitsToProcess.add(adminService.findReportingUnitByCode("8225"));
    }

    public String calcRelevantNov17Parallel() throws Exception {

        FinancialPeriod startPeriod = financialPeriodService.findById("NOV-17");
        for (ReportingUnit reportingUnit : reportingUnitsToProcess) {

            try {
                logger.log(Level.INFO, "Calculating RU: " + reportingUnit.getCode() + " POB Count: " + reportingUnit.getPerformanceObligations().size());
                asyncResult = loadTestService.calculateParallel(reportingUnit, startPeriod);

                logger.log(Level.INFO, "Completed RU: " + reportingUnit.getCode());
            } catch (Exception e) {
                Logger.getLogger(CalculateInitialDataSet.class.getName()).log(Level.INFO, "Error calculating", e);
            }
        }

        return "calculateParallelDataSet";

    }

    public String calcAllNov17Parallel() throws Exception {

        FinancialPeriod startPeriod = financialPeriodService.findById("NOV-17");
        for (ReportingUnit reportingUnit : adminService.findAllReportingUnits()) {

            try {
                logger.log(Level.INFO, "Calculating RU: " + reportingUnit.getCode() + " POB Count: " + reportingUnit.getPerformanceObligations().size());
                loadTestService.calculateParallel(reportingUnit, startPeriod);

                logger.log(Level.INFO, "Completed RU: " + reportingUnit.getCode());
            } catch (Exception e) {
                Logger.getLogger(CalculateInitialDataSet.class.getName()).log(Level.INFO, "Error calculating", e);
            }
        }

        return "calculateParallelDataSet";

    }

    public String calcAllNov17ParallelSet1() throws Exception {
        FinancialPeriod startPeriod = financialPeriodService.findById("NOV-17");

        loadTestServiceSet.calculateParallel(startPeriod);

        return "calculateParallelDataSet";

    }

    public String calcAllNov17ParallelSet1RU() throws Exception {
        FinancialPeriod startPeriod = financialPeriodService.findById("NOV-17");

        loadTestServiceSet.calculateParallel(adminService.findBUByReportingUnitCode("8293"), startPeriod);

        return "calculateParallelDataSet";

    }

    public String calcAllSep18ParallelSet1() throws Exception {
        FinancialPeriod startPeriod = financialPeriodService.findById("SEP-18");

        loadTestServiceSet.calculateParallel(startPeriod);

        return "calculateParallelDataSet";

    }

}
