package com.flowserve.system606.view;

import com.flowserve.system606.controller.AdminController;
import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
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
 * @author span
 */
@Named
@ViewScoped
public class BusinessUnitEdit implements Serializable {

    private BusinessUnit businessUnit = new BusinessUnit();

    @Inject
    private WebSession webSession;

    @Inject
    private AdminService adminService;

    @Inject
    private AdminController adminController;
    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public BusinessUnitEdit() {
    }

    @PostConstruct
    public void init() {
        businessUnit = webSession.getEditBusinessUnit();
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public List<BusinessUnit> completeSite(String searchString) {
        List<BusinessUnit> sites = null;

        try {
            sites = adminService.searchParentBu(searchString, this.businessUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " site location error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error siteLocations.", e);
        }
        return sites;
    }

    public String addUpdateCondition() throws Exception {
        return this.businessUnit.getId() == null ? adminController.addBusinessUnit(this.businessUnit) : adminController.updateBusinessUnit(this.businessUnit);
    }

}
