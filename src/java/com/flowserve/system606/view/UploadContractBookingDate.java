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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author shubhamv
 */
@Named(value = "uploadContractBookingDate")
@ViewScoped
public class UploadContractBookingDate implements Serializable {

    @Inject
    private BatchProcessingService batchProcessingService;
    @Inject
    private AdminService adminService;

    public UploadContractBookingDate() {
    }

    public static final String PREFIX = "msaccess";
    public static final String SUFFIX = ".tmp";

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public void handleContractBookingDateUpload(FileUploadEvent event) {

        try {
            File accessFile = stream2file((InputStream) event.getFile().getInputstream());
            String fileName = accessFile.getAbsolutePath();

            batchProcessingService.processContractBookingDateUpdate(fileName, event.getFile().getFileName());

            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");

            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getCause().getMessage();
            }
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Contract Update File: " + errorMsg);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            Logger.getLogger(UploadStructuralDataUpdate.class.getName()).log(Level.SEVERE, "Error Contract Update File: ", e);
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
        List<DataImportFile> dataImportFiles = adminService.findDataImportFileByType(batchProcessingService.CONTRACT_BOOKINGDATE_FILE);
        //Collections.sort(dataImportFile);
        return dataImportFiles;
    }

}
