/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.ReportingUnit;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author kgraves
 *
 * This EJB uses bean managed transactions due to high load batch processing
 */
@Stateless
public class LoadTestService {

    @Inject
    private CalculationService calculationService;

    public LoadTestService() {
    }

    @Asynchronous
    public Future<String> calculateParallel(ReportingUnit reportingUnit, FinancialPeriod period) throws Exception {
        calculationService.calculateAndSave(reportingUnit, period);
        return new AsyncResult<String>("Completed");
    }
}
