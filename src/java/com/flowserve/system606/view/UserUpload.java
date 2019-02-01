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
 * @author shubhamc
 */
@Named
@ViewScoped
public class UserUpload implements Serializable {

    @Inject
    AdminService adminService;
    @Inject
    private ReportingUnitService reportingUnitService;

    List<DataImportFile> dataImportFile = new ArrayList<DataImportFile>();

    public void handleFileUpload(FileUploadEvent event) {

        try {
            reportingUnitService.uploadedUserList((InputStream) event.getFile().getInputstream(), event.getFile().getFileName());
            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error handleTemplateUpload: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public List<DataImportFile> getDataImportFile() throws Exception {
        dataImportFile = adminService.findDataImportFileByType("userUpload");
        return dataImportFile;
    }

    public void setDataImportFile(List<DataImportFile> dataImportFile) {
        this.dataImportFile = dataImportFile;
    }
}
