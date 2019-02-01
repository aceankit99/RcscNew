/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.stateful;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.service.FinancialPeriodService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.inject.Inject;

/**
 *
 * @author kgraves
 */
@Stateful
@StatefulTimeout(value = 60, unit = TimeUnit.MINUTES)
public class PeriodStateManager {

    @Inject
    private FinancialPeriodService financialPeriodService;

    private FinancialPeriod currentPeriod = null;

    @PostConstruct
    public void init() {
        Logger.getLogger(PeriodStateManager.class.getName()).log(Level.INFO, "Creating stateful period manager bean.");
        currentPeriod = financialPeriodService.getCurrentFinancialPeriod();
    }

    public FinancialPeriod getCurrentPeriod() {
        return currentPeriod;
    }

    public void setCurrentPeriod(FinancialPeriod currentPeriod) {
        this.currentPeriod = currentPeriod;
    }
}
