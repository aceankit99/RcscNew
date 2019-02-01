/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminChangeLog;
import com.flowserve.system606.service.Report2DisclosureService;
import com.flowserve.system606.service.ReportsDisclosureService;
import com.flowserve.system606.service.USDreportsService;
import com.flowserve.system606.web.WebSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class CompanyReports implements Serializable {

    private StreamedContent file;
    private InputStream inputStream;
    private FileOutputStream outputStream;
    @Inject
    private WebSession webSession;
    @Inject
    private ReportsDisclosureService reportsDisclosureService;
    @Inject
    private Report2DisclosureService report2DisclosureService;
    @Inject
    private USDreportsService usdReportsService;
    @Inject
    private AdminChangeLog adminChangeLog;

    public StreamedContent getReportInputAndCalc() {
        try {
            String filename = "Input_and_calcs_Report" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = CompanyReports.class.getResourceAsStream("/resources/excel_input_templates/Report1_input_and_calcs.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            ReportingUnit ru = null;
            reportsDisclosureService.generateReport1InputAndCalc(inputStream, outputStream, webSession.getCurrentPeriod(), ru);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating disclosures  report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getReport1Tab2PocOutputs() {
        try {
            String filename = "POC_Outputs_Report_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/report1_poc_output.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            ReportingUnit ru = null;
            report2DisclosureService.generateReport1Tab2PocOutputs(inputStream, outputStream, webSession.getCurrentPeriod(), ru);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating disclosures  report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getReport5USDreportsTab1() {
        try {
            ReportingUnit ru = null;
            String filename = "USD_Reports_Tab1_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/report5_usd_reports.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            usdReportsService.generateReport5USDreportsTab1(inputStream, outputStream, webSession.getCurrentPeriod(), ru);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating USD report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getReport5USDreportsTab2() {
        try {
            String filename = "USD_Reports_Tab2_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/report5_usd_reports.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            usdReportsService.generateReport5USDreportsTab2(inputStream, outputStream, webSession.getCurrentPeriod());
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating USD report tab 2: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getAdminChangeLog() {
        try {
            String filename = "Admin_Change_Log.xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/admin_change_log.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            adminChangeLog.generateAdminChangeLog(inputStream, outputStream);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating disclosures  report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

}
