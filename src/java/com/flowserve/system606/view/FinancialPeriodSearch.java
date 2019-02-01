package com.flowserve.system606.view;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class FinancialPeriodSearch implements Serializable {

    List<FinancialPeriod> financialPeriods = new ArrayList<FinancialPeriod>();
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private WebSession webSession;
    private String searchString = "";

    public void search() throws Exception {
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public List<FinancialPeriod> getFinancialPeriods() throws Exception {
        financialPeriods = financialPeriodService.findFinancialPeriods();
        Collections.sort(financialPeriods);

        return financialPeriods;
    }

    public void setFinancialPeriods(List<FinancialPeriod> financialPeriods) {
        this.financialPeriods = financialPeriods;
    }

    public void createNextFinancialPeriod() throws Exception {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            financialPeriodService.createNextFinancialPeriod();
        } catch (Exception e) {
            Logger.getLogger(FinancialPeriodEdit.class.getName()).log(Level.INFO, "Error creating period: ", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error creating period: ", e.getMessage()));
        }
    }
}
