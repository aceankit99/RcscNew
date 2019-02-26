/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.ContractAttachment;
import com.flowserve.system606.model.ContractVersion;
import com.flowserve.system606.model.Customer;
import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationVersionService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author constacloud
 */
@Named(value = "contractBooking")
@ViewScoped
public class ContractBooking implements Serializable {

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    @Inject
    private AdminService adminService;
    @Inject
    private CalculationVersionService calculationVersionService;

    private Contract contract = new Contract();
    List<BusinessUnit> businessUnits = new ArrayList<BusinessUnit>();
    private User completedUser;
    private Customer completeCustomer;
    private ContractAttachment contractAttachment;
    private ContractVersion contractVersion;
    List<ContractAttachment> contractAttachmentList = new ArrayList<ContractAttachment>();

    @PostConstruct
    public void init() {

        try {
            businessUnits = adminService.allBusinessUnit();
            contract = adminService.findContractbyID(new Long(1));
            contractVersion = contract.getContractVersionMap().get("1.0");
            contractAttachmentList = contract.getContractVersionMap().get("1.0").getContractAttachment();
            contractAttachment = contractAttachmentList.get(0);
        } catch (Exception ex) {
            Logger.getLogger(ReportingUnitEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
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

    public ContractVersion getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(ContractVersion contractVersion) {
        this.contractVersion = contractVersion;
    }

    public ContractAttachment getContractAttachment() {
        return contractAttachment;
    }

    public void setContractAttachment(ContractAttachment contractAttachment) {
        this.contractAttachment = contractAttachment;
    }

    public List<ContractAttachment> getContractAttachmentList() {
        return contractAttachmentList;
    }

    public void setContractAttachmentList(List<ContractAttachment> contractAttachmentList) {
        this.contractAttachmentList = contractAttachmentList;
    }

}
