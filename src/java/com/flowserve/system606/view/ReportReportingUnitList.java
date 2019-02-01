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
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class ReportReportingUnitList implements Serializable {

    List<ReportingUnit> reportingUnits = new ArrayList<ReportingUnit>();
    @EJB
    private AdminService adminService;
    private String searchString = "";

    public void search() throws Exception {

        reportingUnits = adminService.searchReportingUnits(searchString);
        Collections.sort(reportingUnits);

    }

    public List<ReportingUnit> getReportingUnits() {
        return reportingUnits;
    }

    public void setReportingUnits(List<ReportingUnit> reportingUnits) {
        this.reportingUnits = reportingUnits;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

}
