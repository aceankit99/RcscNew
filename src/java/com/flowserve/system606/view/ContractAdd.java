/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.ContractVersion;
import com.flowserve.system606.model.Customer;
import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
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
 * @author shubhamv
 */
@Named(value = "contractAdd")
@ViewScoped
public class ContractAdd implements Serializable {

    @Inject
    private AdminService adminService;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    private ContractVersion addContractVersion = new ContractVersion();
    List<BusinessUnit> businessUnits = new ArrayList<BusinessUnit>();
    private User completedUser;
    private Customer completeCustomer;

    public ContractAdd() {
    }

    @PostConstruct
    public void init() {

        try {
            businessUnits = adminService.allBusinessUnit();
        } catch (Exception ex) {
            Logger.getLogger(ReportingUnitEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String saveNewContractVersion(ContractVersion cv) throws Exception {
        System.out.println("addContractVersion()");
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            adminService.persist(cv);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error saving", e.getMessage()));
            return null;
        }

        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Contract Version saved", ""));

        return "contractAdd";
    }

    public ContractVersion getAddContractVersion() {
        return addContractVersion;
    }

    public void setAddContractVersion(ContractVersion addContractVersion) {
        this.addContractVersion = addContractVersion;
    }

    public List<BusinessUnit> getBusinessUnits() {
        return businessUnits;
    }

    public void setBusinessUnits(List<BusinessUnit> businessUnits) {
        this.businessUnits = businessUnits;
    }

    public User getCompletedUser() {
        return completedUser;
    }

    public void setCompletedUser(User completedUser) {
        this.completedUser = completedUser;
    }

    public Customer getCompleteCustomer() {
        return completeCustomer;
    }

    public void setCompleteCustomer(Customer completeCustomer) {
        this.completeCustomer = completeCustomer;
    }

}
