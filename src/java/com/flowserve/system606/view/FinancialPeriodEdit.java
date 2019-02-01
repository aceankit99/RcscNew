package com.flowserve.system606.view;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.ReportingUnitService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class FinancialPeriodEdit implements Serializable {

    @Inject
    private WebSession webSession;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private AdminService adminService;
    private FinancialPeriod financialPeriod;

    public FinancialPeriodEdit() {
    }

    @PostConstruct
    public void init() {
        financialPeriod = financialPeriodService.findById(webSession.getEditFinancialPeriodId());
    }

    public FinancialPeriod getFinancialPeriod() {
        return financialPeriod;
    }

    public String openPeriod() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            String actionStatus = financialPeriodService.openPeriod(financialPeriod, webSession.getUser());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, financialPeriod.getId() + actionStatus, ""));
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error opening period: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error opening period: ", e.getMessage()));
        }

        return "financialPeriodEdit";
    }

    public String closePeriod() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            String actionStatus = financialPeriodService.closePeriod(financialPeriod, webSession.getUser());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, financialPeriod.getId() + actionStatus, ""));
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error closing period: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error closing period: ", e.getMessage()));
        }

        return "financialPeriodEdit";
    }

    public String freezePeriod() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            financialPeriodService.freezePeriod(financialPeriod, webSession.getUser());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, financialPeriod.getId() + " period frozen for user input.", ""));
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error freezing period: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error freezing period: ", e.getMessage()));
        }

        return "financialPeriodEdit";
    }

    public String approveEmptyRU(FinancialPeriod period) throws Exception {

        FacesContext context = FacesContext.getCurrentInstance();
        try {
            reportingUnitService.approveIfEmpty(period, webSession.getUser());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, financialPeriod.getId() + " All Empty Reporting Units are approved", ""));
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error approving RUs: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error approving RUs: ", e.getMessage()));
        }

        return "financialPeriodEdit";

    }

    public String approveAllRUs(FinancialPeriod period) throws Exception {

        FacesContext context = FacesContext.getCurrentInstance();
        try {
            reportingUnitService.approveAllRUsIfNotApproved(period, webSession.getUser());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, financialPeriod.getId() + " All Reporting Units are approved", ""));
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error opening period: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error opening period: ", e.getMessage()));
        }

        return "financialPeriodEdit";

    }

    public String executeBusinessRules(FinancialPeriod period) throws Exception {

        FacesContext context = FacesContext.getCurrentInstance();
        try {
            financialPeriodService.executeBusinessRules(period);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, financialPeriod.getId() + " Execution request submitted", ""));
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error running rules: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error running rules: ", e.getMessage()));
        }

        return "financialPeriodEdit";

    }

}
