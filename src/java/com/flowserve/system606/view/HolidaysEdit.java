/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.controller.AdminController;
import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.Holiday;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class HolidaysEdit implements Serializable {

    List<Company> company = new ArrayList<Company>();
    private Holiday days = new Holiday();
    @Inject
    private WebSession webSession;

    private LocalDate sampleDdate;

    private Date dt;

    @Inject
    private AdminController adminController;
    @Inject
    private AdminService adminService;

    public HolidaysEdit() {
    }

    @PostConstruct
    public void init() {
        days = webSession.getEditHolidays();
        try {
            company = adminService.findAllCompany();
        } catch (Exception ex) {
            Logger.getLogger(HolidaysEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Holiday getDays() {

        return days;
    }

    public void setDays(Holiday days) {

        this.days = days;
    }

    public String addUpdateCondition() throws Exception {

        return this.days.getId() == null ? adminController.addHoliday(this.days) : adminController.updateHoliday(this.days);
    }

    public List<Company> completeCompany(String searchString) {
        List<Company> sites = null;

        try {
            sites = adminService.searchCompany(searchString);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " company error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            Logger.getLogger(HolidaysEdit.class.getName()).log(Level.SEVERE, "Error Company", e);
        }
        return sites;
    }

    public LocalDate getSampleDdate() {
        return sampleDdate;
    }

    public void setSampleDdate(LocalDate sampleDdate) {
        this.sampleDdate = sampleDdate;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public List<Company> getCompany() {
        return company;
    }

    public void setCompany(List<Company> company) {
        this.company = company;
    }

}
