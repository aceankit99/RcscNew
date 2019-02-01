/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.Holiday;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.FinancialPeriodService;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

/**
 *
 * @author shubhamv
 */
@Named
@ViewScoped
public class Calendar implements Serializable {

    private ScheduleModel eventModel;
    @Inject
    private AdminService adminService;
    @Inject
    private FinancialPeriodService financialPeriodService;
 
    public Calendar() {
    }

    @PostConstruct
    public void init() {
        try {
            List<Holiday> holidays = adminService.findHolidayList();
            Company company = adminService.findCompanyById("FLS");
            LocalDate freeze = financialPeriodService.calcInputFreezeWorkday(LocalDate.now(), holidays, company.getInputFreezeWorkday());
            LocalDate poci = financialPeriodService.calcInputFreezeWorkday(LocalDate.now(), holidays, company.getPociDueWorkday());          
            eventModel = new DefaultScheduleModel();
            for (Holiday holiday : holidays) {
                Date date = Date.from(holiday.getHolidayDate().atStartOfDay(ZoneOffset.UTC).toInstant());
                eventModel.addEvent(new DefaultScheduleEvent(holiday.getName(), date, date, true));
            }
            Date Freezeday = Date.from(freeze.atStartOfDay(ZoneOffset.UTC).toInstant());
            eventModel.addEvent(new DefaultScheduleEvent("Input Freeze Day", Freezeday, Freezeday, true));

            Date Pociworkday = Date.from(poci.atStartOfDay(ZoneOffset.UTC).toInstant());
            eventModel.addEvent(new DefaultScheduleEvent("POCI Due Workday", Pociworkday, Pociworkday, true));

        } catch (Exception ex) {
            Logger.getLogger(Calendar.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

}
