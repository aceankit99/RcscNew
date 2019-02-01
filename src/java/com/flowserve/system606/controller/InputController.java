package com.flowserve.system606.controller;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.EmailService;
import com.flowserve.system606.service.ReportingUnitService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class InputController implements Serializable {

    private static final Logger logger = Logger.getLogger("com.flowserve.system606");
    private static final long serialVersionUID = 6393550044523662986L;
    @Inject
    private AdminService adminService;
    @Inject
    private WebSession webSession;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private EmailService emailService;

    public InputController() {
    }

    public String inputDashboard() {
        return "dashboard";
    }

    public String proceedToInputDownload() {
        return "inputTemplateDownload";
    }

    public String proceedToInputUpload() {
        return "inputTemplateUpload";
    }

    public String proceedToOnlineEntry() {
        //webSession.setCurrentReportingUnit(ru);
        return "inputOnlineEntry";
    }

    public String returnToOnlineEntry() {
        return "inputOnlineEntry";
    }

    public String submitForReview(ReportingUnit reportingUnit) throws Exception {
        reportingUnitService.submitForReview(reportingUnit, webSession.getCurrentPeriod(), webSession.getUser());
        emailService.emailReviewers(reportingUnit);
        FacesMessage msg = new FacesMessage("Succesful", "Reporting unit submitted for review.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        return "dashboard";
    }

    public String submitForApproval(ReportingUnit reportingUnit) throws Exception {
        reportingUnitService.submitForApproval(reportingUnit, webSession.getCurrentPeriod(), webSession.getUser());
        emailService.emailApprovers(reportingUnit);
        FacesMessage msg = new FacesMessage("Succesful", "Reporting unit submitted for approval.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        return "dashboard";
    }

    public String approve(ReportingUnit reportingUnit) throws Exception {
        reportingUnitService.approve(reportingUnit, webSession.getCurrentPeriod(), webSession.getUser());

        FacesMessage msg = new FacesMessage("Succesful", "Reporting unit appoved.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        return "dashboard";
    }

    public String reject(ReportingUnit reportingUnit) throws Exception {
        reportingUnitService.reject(reportingUnit, webSession.getCurrentPeriod(), webSession.getUser());
        emailService.emailPreparers(reportingUnit);
        FacesMessage msg = new FacesMessage("Succesful", "Reporting unit rejected.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        return "dashboard";
    }

}
