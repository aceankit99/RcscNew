/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowAction;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.JournalService;
import com.flowserve.system606.service.Report2DisclosureService;
import com.flowserve.system606.service.ReportOtherInputService;
import com.flowserve.system606.service.ReportsAssetsService;
import com.flowserve.system606.service.ReportsBacklogDisclosureService;
import com.flowserve.system606.service.ReportsDisclosureService;
import com.flowserve.system606.service.ReportsService;
import com.flowserve.system606.web.WebSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class ReportsReview implements Serializable {

    private static final Logger logger = Logger.getLogger(ReportsList.class.getName());

    private StreamedContent file;
    private InputStream inputStream;
    private FileOutputStream outputStream;

    @Inject
    ReportsService reportsService;
    @Inject
    private ReportsAssetsService reportAssetsService;
    @Inject
    private ReportsDisclosureService reportsDisclosureService;
    @Inject
    private Report2DisclosureService report2DisclosureService;
    @Inject
    private JournalService journalService;
    @Inject
    private ReportOtherInputService reportOtherInputService;
    @Inject
    private ReportsBacklogDisclosureService reportsBacklogDisclosureService;

    private TreeNode rootTreeNode;
    private List<WorkflowAction> workflowActions;
    ReportingUnit reportingUnit;
    private TreeNode selectedNode;
    private List<Contract> contracts;

    @Inject
    private ViewSupport viewSupport;
    @Inject
    private WebSession webSession;
    @Inject
    private AdminService adminService;

    @PostConstruct
    public void init() {
        reportingUnit = adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId());
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        workflowActions = reportingUnit.getWorkflowContext(webSession.getCurrentPeriod()).getWorkflowHistory();

        initContracts();
        if (webSession.getSelectedContracts() != null && webSession.getSelectedContracts().length > 0) {
            filterByContracts();
        }
        if (webSession.getFilterText() != null) {
            filterByContractText();
        }
    }

    public TreeNode getRootTreeNode() {
        return rootTreeNode;
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

    public void onReportingUnitSelect(SelectEvent event) {
        webSession.setFilterText(null);
        webSession.setCurrentReportingUnit((ReportingUnit) event.getObject());
        init();
    }

    public void switchPeriod(FinancialPeriod period) {
        webSession.switchPeriod(period);
        init();
    }

    public void filterByContracts() {
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);

        if (webSession.getSelectedContracts().length == 0) {
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        } else {
            viewSupport.filterNodeTreeContracts(rootTreeNode, Arrays.asList(webSession.getSelectedContracts()));
        }
    }

    private boolean isEmpty(String text) {
        if (text == null || "".equals(webSession.getFilterText().trim())) {
            return true;
        }

        return false;
    }

    public void initContracts() {
        contracts = reportingUnit.getUnarchivedContracts();
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public void filterByContractText() {
        if (isEmpty(webSession.getFilterText())) {
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        } else {
            viewSupport.filterNodeTree(rootTreeNode, webSession.getFilterText());
        }
    }

    public WorkflowStatus getWorkflowStatus() {
        return reportingUnit.getWorkflowStatus(webSession.getCurrentPeriod());
    }

    public void clearFilterByContractText() {
        webSession.setFilterText(null);
        webSession.setSelectedContracts(null);
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
    }

    public StreamedContent getContractSummaryReport(Contract contract) {
        try {
            String filename = "Contract_Sum_Report_" + contract.getName() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Outputs_Summary_v2.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportsService.generateContractSummaryReport(inputStream, outputStream, contract, webSession.getCurrentPeriod());
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsReview.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating contract summary report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getContractSummaryReportingUnitLevelReport(ReportingUnit ru) {
        try {
            String filename = "RU_Sum_Report_" + ru.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Outputs_Summary_v2.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportsService.generateContractSummaryReportingUnitLevelReport(inputStream, outputStream, ru, webSession.getCurrentPeriod());
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsReview.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating RU_summary report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getFinancialSummaryReport(Contract contract) {
        try {
            String filename = "FS_Report_" + contract.getName() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = ReportsReview.class.getResourceAsStream("/resources/excel_input_templates/Outputs_Summary_v2_ORIGINAL.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportsService.generateReportFinancialSummary(inputStream, outputStream, contract, webSession.getCurrentPeriod());
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsReview.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating Financial summary report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getRUFinancialSummaryReport(ReportingUnit ru) {
        try {
            String filename = "RU_FS_Report_" + ru.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = ReportsReview.class.getResourceAsStream("/resources/excel_input_templates/Outputs_Summary_v2_ORIGINAL.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            reportsService.generateRUReportFinancialSummary(inputStream, outputStream, ru, webSession.getCurrentPeriod());
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsReview.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating Financial summary report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        return file;
    }

    public StreamedContent getJournalEntryReport(ReportingUnit reportingUnit) {
        try {
            String filename = "JE_Report_" + reportingUnit.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
            inputStream = RceInput.class.getResourceAsStream("/resources/excel_input_templates/Journal_Entry_RU_Template.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            journalService.generateJournalEntryReport(inputStream, outputStream, reportingUnit, webSession.getCurrentPeriod());
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(ReportsReview.class.getName()).log(Level.INFO, "Error generating report: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating journal entry report: " + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

        return file;
    }

    public StreamedContent getReport2AssetsandLiab(ReportingUnit ru) {
        try {
            String filename = "Report_Contract_Assets&Liab_" + "RU" + ru.getCode() + "_" + webSession.getCurrentPeriod().getId() + ".xlsx";
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

    public List<WorkflowAction> getWorkflowActions() {
        return workflowActions;
    }

}
