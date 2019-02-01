/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.DataImportFile;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.BatchProcessingService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.ReportingUnitService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.poi.util.IOUtils;
import org.primefaces.model.UploadedFile;

@Named
@ViewScoped
public class InputPOCIUpload implements Serializable {

    @Inject
    private AdminService adminService;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private BatchProcessingService calculationService;
    @Inject
    private FinancialPeriodService financialPeriodService;

    List<DataImportFile> dataImportFile = new ArrayList<DataImportFile>();

    public static final String PREFIX = "msaccess";
    public static final String SUFFIX = ".tmp";

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    private UploadedFile file;

    public void handleInputPOCIUpload() {

        try {
            File accessFile = stream2file((InputStream) file.getInputstream());
            String fileName = accessFile.getAbsolutePath();
            calculationService.processUploadedCalculationData(fileName, file.getFileName());
            FacesMessage msg = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (EJBException e) {
            String errorMsg = e.getCausedByException().getMessage();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "POCI Data Upload: " + errorMsg);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error handleInputPOCIUpload.", e);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "POCI Data Upload: " + errorMsg);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error handleInputPOCIUpload.", e);
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

    public List<DataImportFile> getDataImportFile() throws Exception {
        dataImportFile = adminService.findDataImportFileByType("POCI DATA");
        //Collections.sort(dataImportFile);
        return dataImportFile;
    }

    public void setDataImportFile(List<DataImportFile> dataImportFile) {
        this.dataImportFile = dataImportFile;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

}
