package com.flowserve.system606.controller;

import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationService;
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
public class CalcReviewController implements Serializable {

    private static final Logger logger = Logger.getLogger("com.flowserve.system606");
    private static final long serialVersionUID = 6393550544523662986L;
    @Inject
    private WebSession webSession;
    @Inject
    private AdminService adminService;
    @Inject
    private CalculationService calculationService;

    public CalcReviewController() {
    }

    public String returnToInputs() {
        return "inputOnlineEntry";
    }

    public String reviewCalculations(ReportingUnit reportingUnit) throws Exception {
        // TODO - If problem here, then return back and show validations, etc.
        // TODO - Actally handle all that as part of upload and just enable button if everything ok.

//        if (webSession.getCurrentPeriod().isOpen() && (reportingUnit.isDraft(webSession.getCurrentPeriod()) || reportingUnit.isRejected(webSession.getCurrentPeriod()))) {
//            calculationService.calculateAndSave(reportingUnit, webSession.getCurrentPeriod());
//        }
        return "pobCalculationReview";
    }

    public void reviewRecalcRU(ReportingUnit reportingUnit) throws Exception {

        if (isRecalculable(reportingUnit)) {
            calculationService.calculateAndSave(reportingUnit, webSession.getCurrentPeriod());
        }

    }

    public String reviewReports() throws Exception {
        ReportingUnit ru = webSession.getCurrentReportingUnit();

        for (PerformanceObligation pob : ru.getUnarchivedPerformanceObligations()) {
            if (!pob.isValid()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Warning", "This reporting unit is not valid for review.  Please return to the inputs section and correct the contracts marked in red.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                break;
            }
        }

        return "reportsReview?faces-redirect=true";
        //return "reportsReview";
    }

    public boolean isRecalculable(ReportingUnit reportingUnit) {
        return webSession.getCurrentPeriod().isOpen() && (reportingUnit.isDraft(webSession.getCurrentPeriod()) || reportingUnit.isRejected(webSession.getCurrentPeriod()));
    }
}
