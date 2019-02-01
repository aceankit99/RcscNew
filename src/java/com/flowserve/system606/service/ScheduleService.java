/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Holiday;
import com.flowserve.system606.model.PeriodStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author shubhamc
 */
@Stateless
public class ScheduleService {

    @Inject
    private AdminService adminService;
    @Inject
    private FinancialPeriodService financialPeriodService;

    private static final Logger logger = Logger.getLogger(ScheduleService.class.getName());

    @PostConstruct
    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "12", dayOfMonth = "*", year = "*", minute = "0", second = "0", persistent = false)

    public void userFreezeCheckerCron() {
        logger.info("cron started");
        Company com = adminService.findCompanyById("FLS");
        int workday = com.getInputFreezeWorkday();
        List<Holiday> holidays = null;
        try {
            holidays = adminService.findHolidayList();
        } catch (Exception ex) {
            Logger.getLogger(ScheduleService.class.getName()).log(Level.SEVERE, null, ex);
        }
        LocalDate date = LocalDate.now();
        try {
            if (financialPeriodService.isXWorkday(date, workday, holidays)) {
                logger.info("freeze day");
                FinancialPeriod period = financialPeriodService.getCurrentFinancialPeriod();
                period.setStatus(PeriodStatus.USER_FREEZE);
                financialPeriodService.update(period);

            }
        } catch (Exception ex) {
            Logger.getLogger(ScheduleService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
