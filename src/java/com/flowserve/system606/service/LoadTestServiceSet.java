/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.view.CalculateInitialDataSet;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

/**
 * @author kgraves
 *
 * This EJB uses bean managed transactions due to high load batch processing
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class LoadTestServiceSet {

    @Inject
    private CalculationService calculationService;
    @Inject
    private AdminService adminService;
    @Resource
    private UserTransaction ut;

    public LoadTestServiceSet() {
    }

    @Asynchronous
    public void calculateParallel(FinancialPeriod period) throws Exception {
        Instant start = Instant.now();

        for (ReportingUnit ru : adminService.findAllReportingUnits()) {
            try {
                ut.begin();
                calculationService.calculateAndSave(ru, period);
                ut.commit();
            } catch (Exception exception) {
                Logger.getLogger(CalculateInitialDataSet.class.getName()).log(Level.INFO, "Error calculating RU: " + ru.getCode(), exception);
                ut.rollback();
            }
        }
        Logger.getLogger(LoadTestServiceSet.class.getName()).log(Level.INFO, "Completed parallel calc.");

        Instant end = Instant.now();
        Duration interval = Duration.between(start, end);
        int min = (int) (interval.getSeconds() / 60);
        int sec = (int) (interval.getSeconds() - (min * 60));
        Logger.getLogger(LoadTestServiceSet.class.getName()).log(Level.INFO, "calculateParallel() completed in MIN : " + min + " SEC : " + sec);
    }

    @Asynchronous
    public void calculateParallel(ReportingUnit ru, FinancialPeriod period) throws Exception {
        Instant start = Instant.now();

        try {
            ut.begin();
            calculationService.calculateAndSave(ru, period);
            ut.commit();
        } catch (Exception exception) {
            Logger.getLogger(CalculateInitialDataSet.class.getName()).log(Level.INFO, "Error calculating RU: " + ru.getCode(), exception);
            ut.rollback();
        }
        Logger.getLogger(LoadTestServiceSet.class.getName()).log(Level.INFO, "Completed parallel calc.");

        Instant end = Instant.now();
        Duration interval = Duration.between(start, end);
        int min = (int) (interval.getSeconds() / 60);
        int sec = (int) (interval.getSeconds() - (min * 60));
        Logger.getLogger(LoadTestServiceSet.class.getName()).log(Level.INFO, "calculateParallel() completed in MIN : " + min + " SEC : " + sec);
    }
}
