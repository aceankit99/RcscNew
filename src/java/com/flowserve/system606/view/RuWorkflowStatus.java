/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.ReportingUnitService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named(value = "ruWorkflowStatus")
@ViewScoped
public class RuWorkflowStatus implements Serializable {

    /**
     * Creates a new instance of RuWorkflowStatus
     */
    @Inject
    private WebSession webSession;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private AdminService adminService;

    private List<ReportingUnit> listAllRUs = new ArrayList<ReportingUnit>();

    public RuWorkflowStatus() {
    }

    @PostConstruct
    public void init() {
        // KJG 11/6/18 - Release 1.11.5.1 - Add active filter.
        listAllRUs = adminService.findAllActiveReportingUnits();
    }

    public List<ReportingUnit> getListAllRUs() {
        return listAllRUs;
    }

    public WorkflowStatus getWorkflowStatus(ReportingUnit reportingUnit) {
        return reportingUnit.getWorkflowStatus(webSession.getCurrentPeriod());
    }

}
