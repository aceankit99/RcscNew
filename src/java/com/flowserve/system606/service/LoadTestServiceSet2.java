/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.view.CalculateInitialDataSet;
import java.util.Collection;
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
public class LoadTestServiceSet2 {

    @Inject
    private CalculationService calculationService;
    @Resource
    private UserTransaction ut;

    public LoadTestServiceSet2() {
    }

    @Asynchronous
    public void calculateParallel(Collection<ReportingUnit> reportingUnits, FinancialPeriod period) throws Exception {
        for (ReportingUnit ru : reportingUnits) {
            try {
                ut.begin();
                calculationService.calculateAndSave(ru, period);
                ut.commit();
            } catch (Exception exception) {
                Logger.getLogger(CalculateInitialDataSet.class.getName()).log(Level.INFO, "Error calculating RU: " + ru.getCode(), exception);
                ut.rollback();
            }
//
//            em.flush();
//            em.clear();

        }
        Logger.getLogger(LoadTestServiceSet2.class.getName()).log(Level.INFO, "Completed parallel calc.");
    }
}
