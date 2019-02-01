/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author shubhamv
 */
@ManagedBean(name = "reportingUnitSearch")
@ViewScoped
public class ReportingUnitSearch implements Serializable {

    /**
     * Creates a new instance of ReportingUnit
     */
    public ReportingUnitSearch() {
    }
    List<ReportingUnit> reportingUnits = new ArrayList<ReportingUnit>();
    //List<Country> country = new ArrayList<Country>();
    @EJB
    private AdminService adminService;
    private String searchString = "";

    public void search() throws Exception {

        reportingUnits = adminService.searchReportingUnitsForAdmin(searchString);
        Collections.sort(reportingUnits);

    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public List<ReportingUnit> getReportingUnits() {
        return reportingUnits;
    }

    public void setReportingUnits(List<ReportingUnit> reportingUnits) {
        this.reportingUnits = reportingUnits;
    }

}
