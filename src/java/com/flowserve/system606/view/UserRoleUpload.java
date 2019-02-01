/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.DataImportFile;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.ReportingUnitService;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author shubhamv
 */
@Named(value = "userRoleUpload")
@ViewScoped
public class UserRoleUpload implements Serializable {

    @Inject
    private ReportingUnitService reportingUnitService;

    List<DataImportFile> dataImportFileUserRole = new ArrayList<DataImportFile>();
    @Inject
    private AdminService adminService;

    /**
     * Creates a new instance of UserRoleUpload
     */
    public UserRoleUpload() {
    }

    public void handleTemplateUpload(FileUploadEvent event) {

        try {
            reportingUnitService.reportingPreparersList((InputStream) event.getFile().getInputstream());
            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error handleTemplateUpload: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public List<DataImportFile> getDataImportFileUserRole() throws Exception {
        dataImportFileUserRole = adminService.findDataImportFileByType("User Role");
        return dataImportFileUserRole;
    }

    public void setDataImportFileUserRole(List<DataImportFile> dataImportFileUserRole) {
        this.dataImportFileUserRole = dataImportFileUserRole;
    }
}
