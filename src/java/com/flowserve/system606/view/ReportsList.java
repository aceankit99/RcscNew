/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminChangeLog;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.Report2DisclosureService;
import com.flowserve.system606.service.ReportOtherInputService;
import com.flowserve.system606.service.ReportsAssetsService;
import com.flowserve.system606.service.ReportsBacklogDisclosureService;
import com.flowserve.system606.service.ReportsDisclosureService;
import com.flowserve.system606.web.WebSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class ReportsList implements Serializable {

    private static final Logger logger = Logger.getLogger(ReportsList.class.getName());

    private StreamedContent file;
    private InputStream inputStream;
    private FileOutputStream outputStream;

    private TreeNode rootTreeNode;
    ReportingUnit reportingUnit;
    private TreeNode selectedNode;
    @Inject
    private ViewSupport viewSupport;
    @Inject
    private WebSession webSession;
    @Inject
    private AdminService adminService;
    @Inject
    private ReportsDisclosureService reportsDisclosureService;
    @Inject
    private ReportsAssetsService reportAssetsService;
    @Inject
    private Report2DisclosureService report2DisclosureService;
    @Inject
    private ReportOtherInputService reportOtherInputService;
    @Inject
    private ReportsBacklogDisclosureService reportsBacklogDisclosureService;
    @Inject
    private AdminChangeLog adminChangeLog;

    @PostConstruct
    public void init() {
        reportingUnit = adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId());
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
    }

    public StreamedContent getReport1InputAndCalc() {
        try {
            String filename = "InputCalc_Report_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Report1_input_and_calcs.xlsx");
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

    public StreamedContent getReport1InputAndCalcRULevel(ReportingUnit ru) {
        try {
            String filename = "InputCalc_Report_RU" + ru.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Report1_input_and_calcs.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
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

    public TreeNode getRootTreeNode() {
        return rootTreeNode;
    }

    public void setRootTreeNode(TreeNode rootTreeNode) {
        this.rootTreeNode = rootTreeNode;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(ReportingUnit reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public StreamedContent getReport2AssetsandLiab(ReportingUnit ru) {
        try {
            String filename = "Report_Contract-Assets&Liab_" + "RU" + ru.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Cont_asset_and_liab_report.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportAssetsService.generateReport2AssetsandLiab(inputStream, outputStream, webSession.getCurrentPeriod(), ru);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating Contract & Assets  report: " + e.getMessage(), e.getMessage());
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

    public StreamedContent getReport1Tab2PocOutputsRULevel(ReportingUnit ru) {
        try {
            String filename = "POC_Outputs_Report_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/report1_poc_output.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
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

    public StreamedContent getReport4DisAggRevDisclosure(ReportingUnit ru) {
        try {
            String filename = "Disagg_Revenue_Disclosures_Report_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Report4_disclosures_tabs.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportOtherInputService.generateReport4DisAggregatedRev(inputStream, outputStream, webSession.getCurrentPeriod(), ru);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating disclosures  report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getReportBacklogRollDisclosure(ReportingUnit ru) {
        try {
            String filename = "Backlog_Rollforward_Disclosures_Report_" + ru.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/report_disclosure_backlog_rollforward.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportsBacklogDisclosureService.generateReport4BacklogRollfoward(inputStream, outputStream, webSession.getCurrentPeriod(), ru);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsList.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating disclosures  report: " + e.getMessage(), e.getMessage());
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
