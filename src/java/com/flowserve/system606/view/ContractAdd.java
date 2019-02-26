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
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationVersionService;
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
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

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
    private Contract contract = new Contract("1.0");
    private ContractVersion contractVersion = new ContractVersion();
    private ContractAttachment purcahseAttachment;
    private ContractAttachment tncAttachment;
    private ContractAttachment acknowledgementAttachment;
    private ContractAttachment sspAttachment;
    private ContractAttachment contractLineAttachment;
    private ContractAttachment otherAttachment;

    private boolean purcahseCheck;
    private boolean purcahseDisable = true;

    private UploadedFile attachmentFile;

    List<BusinessUnit> businessUnits = new ArrayList<BusinessUnit>();
    private Customer completeCustomer;

    @Inject
    private CalculationVersionService calculationVersionService;

    public ContractAdd() {
    }

    @PostConstruct
    public void init() {

        try {
            businessUnits = adminService.allBusinessUnit();
            purcahseAttachment = new ContractAttachment("Executed Purchase Order/Sales Order/Contract");
            tncAttachment = new ContractAttachment("Terms and Conditions/Master Sales Agreement");
            acknowledgementAttachment = new ContractAttachment("Order Acknowledgement");
            sspAttachment = new ContractAttachment("Standalone Selling Price Basis");
            contractLineAttachment = new ContractAttachment("Contract Line Detail Margin Analysis (SAP Form)");
            otherAttachment = new ContractAttachment();
        } catch (Exception ex) {
            Logger.getLogger(ReportingUnitEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void uploadEnableDisable(String type) {
        String summary = purcahseCheck ? "Checked" : "Unchecked";
        if (type.equalsIgnoreCase("purcahse")) {
            this.purcahseDisable = !summary.equalsIgnoreCase("Checked");
        }

    }

    public String saveNewContractVersion(Contract cv) throws Exception {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (purcahseAttachment.getAttachment() != null) {
                contractVersion.getContractAttachment().add(purcahseAttachment);
            }
            if (tncAttachment.getAttachment() != null) {
                contractVersion.getContractAttachment().add(tncAttachment);
            }
            if (acknowledgementAttachment.getAttachment() != null) {
                contractVersion.getContractAttachment().add(acknowledgementAttachment);
            }
            if (sspAttachment.getAttachment() != null) {
                contractVersion.getContractAttachment().add(sspAttachment);
            }
            if (contractLineAttachment.getAttachment() != null) {
                contractVersion.getContractAttachment().add(contractLineAttachment);
            }
            if (otherAttachment.getAttachment() != null) {
                contractVersion.getContractAttachment().add(otherAttachment);
            }
            cv.putContractVersionMap(contract.getCurrentVersion(), contractVersion);
            adminService.persist(cv);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error saving", e.getMessage()));
            return null;
        }

        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Contract Version saved", ""));

        return "contractAdd";
    }

    public void handleFileUpload(FileUploadEvent event) {
        String type = (String) event.getComponent().getAttributes().get("fileAttachment");

        try {
            byte[] bytes = IOUtils.toByteArray(event.getFile().getInputstream());
            switch (type) {
                case "purchase":
                    purcahseAttachment.setAttachment(bytes);
                    purcahseAttachment.setContentType(event.getFile().getContentType());
                    purcahseAttachment.setContractVersion(contractVersion);
                    break;
                case "tnc":
                    tncAttachment.setAttachment(bytes);
                    tncAttachment.setContentType(event.getFile().getContentType());
                    tncAttachment.setContractVersion(contractVersion);
                    break;
                case "acknowledgement":
                    acknowledgementAttachment.setAttachment(bytes);
                    acknowledgementAttachment.setContentType(event.getFile().getContentType());
                    acknowledgementAttachment.setAttachment(bytes);
                    break;
                case "ssp":
                    sspAttachment.setAttachment(bytes);
                    sspAttachment.setContentType(event.getFile().getContentType());
                    sspAttachment.setAttachment(bytes);
                    break;
                case "contractLine":
                    contractLineAttachment.setAttachment(bytes);
                    contractLineAttachment.setContentType(event.getFile().getContentType());
                    contractLineAttachment.setAttachment(bytes);
                    break;
                case "other":
                    otherAttachment.setAttachment(bytes);
                    otherAttachment.setContentType(event.getFile().getContentType());
                    otherAttachment.setAttachment(bytes);
                    break;
                default:
                    break;
            }

            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error handleInitialContractUpload: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error handleInitialContractUpload.", e);
        }
    }

    public UploadedFile getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(UploadedFile attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ContractVersion getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(ContractVersion contractVersion) {
        this.contractVersion = contractVersion;
    }

    public List<BusinessUnit> getBusinessUnits() {
        return businessUnits;
    }

    public void setBusinessUnits(List<BusinessUnit> businessUnits) {
        this.businessUnits = businessUnits;
    }

    public Customer getCompleteCustomer() {
        return completeCustomer;
    }

    public void setCompleteCustomer(Customer completeCustomer) {
        this.completeCustomer = completeCustomer;
    }

    public ContractAttachment getPurcahseAttachment() {
        return purcahseAttachment;
    }

    public void setPurcahseAttachment(ContractAttachment purcahseAttachment) {
        this.purcahseAttachment = purcahseAttachment;
    }

    public ContractAttachment getTncAttachment() {
        return tncAttachment;
    }

    public void setTncAttachment(ContractAttachment tncAttachment) {
        this.tncAttachment = tncAttachment;
    }

    public ContractAttachment getAcknowledgementAttachment() {
        return acknowledgementAttachment;
    }

    public void setAcknowledgementAttachment(ContractAttachment acknowledgementAttachment) {
        this.acknowledgementAttachment = acknowledgementAttachment;
    }

    public ContractAttachment getSspAttachment() {
        return sspAttachment;
    }

    public void setSspAttachment(ContractAttachment sspAttachment) {
        this.sspAttachment = sspAttachment;
    }

    public ContractAttachment getContractLineAttachment() {
        return contractLineAttachment;
    }

    public void setContractLineAttachment(ContractAttachment contractLineAttachment) {
        this.contractLineAttachment = contractLineAttachment;
    }

    public ContractAttachment getOtherAttachment() {
        return otherAttachment;
    }

    public void setOtherAttachment(ContractAttachment otherAttachment) {
        this.otherAttachment = otherAttachment;
    }

    public boolean isPurcahseCheck() {
        return purcahseCheck;
    }

    public void setPurcahseCheck(boolean purcahseCheck) {
        this.purcahseCheck = purcahseCheck;
    }

    public boolean isPurcahseDisable() {
        return purcahseDisable;
    }

    public void setPurcahseDisable(boolean purcahseDisable) {
        this.purcahseDisable = purcahseDisable;
    }

}
