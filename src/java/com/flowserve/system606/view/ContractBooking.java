/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.ContractAttachment;
import com.flowserve.system606.model.Customer;
import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;



/**
 *
 * @author user
 */
@Named(value = "contractBooking")
@SessionScoped
public class ContractBooking implements Serializable{
    
    @Inject
    private AdminService adminService;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    private Contract Contract = new Contract();
    List<BusinessUnit> businessUnits = new ArrayList<BusinessUnit>();
    private User completedUser;
    private Customer completeCustomer;
    List<String> attachmentName =new ArrayList<String>();
    String fileName;
    StreamedContent content;
    byte[] tempFile;
    
    @PostConstruct
    public void init() {

        try {
            businessUnits = adminService.allBusinessUnit();
            attachmentName = adminService.allAttachment();
            content=getPDF(attachmentName.get(0));
           //just for initilizing the pdf viewer on page load
        } catch (Exception ex) {
            Logger.getLogger(ReportingUnitEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public Contract getContract() {
        return Contract;
    }

    public void setContract(Contract Contract) {
        this.Contract = Contract;
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

    public List<String> getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(List<String> attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
   
    public StreamedContent getPDF(String name) {
        
        tempFile = adminService.getAttachmentByDesc(name);
        ByteArrayInputStream bytearrayContent = new ByteArrayInputStream(tempFile);
        content = new DefaultStreamedContent(bytearrayContent, "application/pdf");
        
        return content;

    }

    public StreamedContent getContent() {
        return content;
    }

    public void setContent(StreamedContent content) {
        this.content = content;
    }
  
  
}
