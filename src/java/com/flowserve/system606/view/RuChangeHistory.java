/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowAction;
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
@Named(value = "ruChangeHistory")
@ViewScoped
public class RuChangeHistory implements Serializable {

    /**
     * Creates a new instance of RuChangeHistory
     */
    @Inject
    private WebSession webSession;
    List<WorkflowAction> reportingUnitHistory = new ArrayList<>();
    private ReportingUnit ru = new ReportingUnit();

    public RuChangeHistory() {
    }

    @PostConstruct
    public void init() {
        ru = webSession.getEditReportingUnit();
        reportingUnitHistory = ru.getWorkflowHistory();
    }

    public List<WorkflowAction> getReportingUnitHistory() {
        return reportingUnitHistory;
    }

    public void setReportingUnitHistory(List<WorkflowAction> reportingUnitHistory) {
        this.reportingUnitHistory = reportingUnitHistory;
    }

    public ReportingUnit getRu() {
        return ru;
    }

    public void setRu(ReportingUnit ru) {
        this.ru = ru;
    }

}
