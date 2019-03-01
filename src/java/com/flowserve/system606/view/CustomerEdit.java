/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.controller.AdminController;
import com.flowserve.system606.model.Customer;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author constacloud
 */
@Named(value = "customerEdit")
@ViewScoped
public class CustomerEdit implements Serializable {

    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    private Customer editCustomer = new Customer();
    @Inject
    private WebSession webSession;
    @Inject
    private AdminService adminService;
    @Inject
    private AdminController adminController;

    private boolean ruDisable;

    public CustomerEdit() {
    }

    @PostConstruct
    public void init() {
        editCustomer = webSession.getEditCustomer();
        ruDisable = editCustomer.isMaster();
    }

    public void disableIfMaster() {
        ruDisable = editCustomer.isMaster();
    }

    public String addUpdateCondition() throws Exception {
        return this.editCustomer.getId() == null ? adminController.addCustomer(this.editCustomer) : adminController.updateCustomer(this.editCustomer);
    }

    public Customer getEditCustomer() {
        return editCustomer;
    }

    public void setEditCustomer(Customer editCustomer) {
        this.editCustomer = editCustomer;
    }

    public boolean isRuDisable() {
        return ruDisable;
    }

    public void setRuDisable(boolean ruDisable) {
        this.ruDisable = ruDisable;
    }

}
