/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.DataImportFile;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.BatchProcessingService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;

@Named
@RequestScoped
public class UploadStructuralDataUpdate implements Serializable {

    @Inject
    private BatchProcessingService batchProcessingService;
    @Inject
    private AdminService adminService;

    public static final String PREFIX = "msaccess";
    public static final String SUFFIX = ".tmp";

    public void handleFileUpload(FileUploadEvent event) {

        try {
            File accessFile = stream2file((InputStream) event.getFile().getInputstream());
            String fileName = accessFile.getAbsolutePath();
            batchProcessingService.processStructuralUpdateFile(fileName, event.getFile().getFileName());
            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getCause().getMessage();
            }
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error processStructuralUpdateFile: " + errorMsg);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            Logger.getLogger(UploadStructuralDataUpdate.class.getName()).log(Level.SEVERE, "Error processStructuralUpdateFile", e);
        }
    }

    public File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

    public List<DataImportFile> getDataImportFiles() throws Exception {
        // Shyam TODO - Please create a constant for the string below and other s like it in other file processors.  STRUCTURAL_UPDATE_FILE.  Define each final String in the EJB that processes.
        List<DataImportFile> dataImportFiles = adminService.findDataImportFileByType(batchProcessingService.STRUCTURAL_UPDATE_FILE);
        //Collections.sort(dataImportFile);
        return dataImportFiles;
    }
}
