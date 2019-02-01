/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.Event;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationService;
import com.flowserve.system606.service.ContractService;
import com.flowserve.system606.service.EventService;
import com.flowserve.system606.service.PerformanceObligationService;
import com.flowserve.system606.service.ReportingUnitService;
import com.flowserve.system606.service.TemplateService;
import com.flowserve.system606.web.WebSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author kgraves
 */
@Named
@ViewScoped
public class InputOnlineEntry implements Serializable {

    private static final Logger logger = Logger.getLogger(InputOnlineEntry.class.getName());

    private StreamedContent file;
    private InputStream inputStream;
    private FileOutputStream outputStream;
    @Inject
    TemplateService templateService;
    private TreeNode rootTreeNode;
    private TreeNode billingTreeNode;
    @Inject
    private EventService eventService;
    @Inject
    private AdminService adminService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private PerformanceObligationService performanceObligationService;
    private BigDecimal eacValue;
    @Inject
    private ViewSupport viewSupport;
    @Inject
    private ContractService contractService;
    @Inject
    private WebSession webSession;
    private TreeNode selectedNode;
    private List<Contract> contracts;
    private String activeTabIndex;
    private ReportingUnit reportingUnit;
    private boolean pobsExpandable;

    List<String> salesDestination = Arrays.asList("ASIA", "CHINA", "INDIA", "JAPAN",
            "EUROPE", "RUSCIS", "LA", "CANADA", "US",
            "ANTARCTICA", "OTHER", "N AFRICA", "OTHER AFRICA",
            "S AFRICA", "IRAQ", "MIDEAST");
    List<String> oeamDisagg = Arrays.asList("OE", "AM");

    @PostConstruct
    public void init() {
        reportingUnit = adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId());
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        billingTreeNode = viewSupport.generateNodeTreeForBilling(reportingUnit, webSession.getCurrentPeriod());
        initContracts(reportingUnit);
        pobsExpandable = viewSupport.isPobsExpandable(reportingUnit);

        if (webSession.getFilterText() != null) {
            filterByContractText();
        }
        if (webSession.getSelectedContracts() != null && webSession.getSelectedContracts().length > 0) {
            filterByContracts();
        }
    }

    public String getTextStyle(Measurable measurable) {
        if (measurable instanceof ReportingUnit || measurable instanceof Contract) {
            return "color: grey; font-style: italic;";
        }

        return "";
    }

    public boolean isPobsExpandable() {
        return pobsExpandable;
    }

    public void switchPeriod(FinancialPeriod period) {
        webSession.switchPeriod(period);
        init();
    }

    public void onTabChange(TabChangeEvent event) {
        activeTabIndex = event.getTab().getId();

    }

    public void onReportingUnitSelect(SelectEvent event) {
        Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "RU Selected");
        webSession.setFilterText(null);
        init();
    }

    public WorkflowStatus getWorkflowStatus() {
        return reportingUnit.getWorkflowStatus(webSession.getCurrentPeriod());
    }

    public void initContracts(ReportingUnit reportingUnit) {
        contracts = reportingUnit.getUnarchivedContracts();
    }

    public void filterByContractText() {
        if (isEmpty(webSession.getFilterText())) {
            //rootTreeNode = viewSupport.generateNodeTree(reportingUnits);
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
            billingTreeNode = viewSupport.generateNodeTreeForBilling(reportingUnit, webSession.getCurrentPeriod());
        } else {
            viewSupport.filterNodeTree(rootTreeNode, webSession.getFilterText());
            viewSupport.filterBillingNodeTree(billingTreeNode, webSession.getFilterText());
        }
    }

    public void filterByContracts() {
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        billingTreeNode = viewSupport.generateNodeTreeForBilling(reportingUnit, webSession.getCurrentPeriod());
        if (webSession.getSelectedContracts().length == 0) {
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
            billingTreeNode = viewSupport.generateNodeTreeForBilling(reportingUnit, webSession.getCurrentPeriod());
        } else {
            viewSupport.filterNodeTreeContracts(rootTreeNode, Arrays.asList(webSession.getSelectedContracts()));
            viewSupport.filterNodeTreeContracts(billingTreeNode, Arrays.asList(webSession.getSelectedContracts()));
        }
    }

    public void addBillingEvent(Contract contract) throws Exception {
        webSession.getExpandedContractIds().add(contract.getId());
        calculationService.addBillingEvent(contract, webSession.getCurrentPeriod(), webSession.getUser());
        init();
    }

    public void removeBillingEvent(CurrencyEvent billingEvent) throws Exception {
        calculationService.removeBillingEvent(billingEvent, webSession.getCurrentPeriod(), webSession.getUser());
        init();
    }

    public void setBillingName(Event billingEvent, String billingNumber) {
        billingEvent.setName("Billing " + billingNumber);
    }

    public String getBillingTextStyle(Event billingEvent, String billingNumber) {
        List<Event> listEvent = billingEvent.getEventList().getEventList();
        for (Event e : listEvent) {
            if (e.getNumber() != null && billingNumber != null) {
                if (e.getNumber().equalsIgnoreCase(billingNumber) && e.getId() != billingEvent.getId()) {
                    return "color: red; font-style: italic;";
                }
            }
        }
        return "";
    }

    public String getBillingMessage(Event billingEvent, String billingNumber) {
        List<Event> listEvent = billingEvent.getEventList().getEventList();
        for (Event e : listEvent) {
            if (e.getNumber() != null && billingNumber != null) {
                if (e.getNumber().equalsIgnoreCase(billingNumber) && e.getId() != billingEvent.getId()) {
                    return "Duplicate Invoice Number not Allowed";
                }
            }
        }
        return "";
    }

    public void clearFilterByContractText() {
        webSession.setFilterText(null);
        webSession.setSelectedContracts(null);
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        billingTreeNode = viewSupport.generateNodeTreeForBilling(reportingUnit, webSession.getCurrentPeriod());
    }

    private boolean isEmpty(String text) {
        if (text == null || "".equals(webSession.getFilterText().trim())) {
            return true;
        }

        return false;
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed", "Old: " + oldValue + ", New:" + newValue);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public BigDecimal getEacValue() {
        return eacValue;
    }

    public void setEacValue(BigDecimal eacValue) {
        this.eacValue = eacValue;
    }

    public void calculateOutputs(PerformanceObligation pob) throws Exception {
        try {
            updateAuditInfo(pob);
            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "Executing business rules POB: " + pob.getId());
            calculationService.executeBusinessRules(pob, webSession.getCurrentPeriod());
            calculationService.executeBusinessRules(pob.getContract(), webSession.getCurrentPeriod());
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error calculateOutputs.", e);
        }
    }

    public void calculateOutputsContract(Contract contract) throws Exception {
        try {
            updateAuditInfo(contract);
            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "Executing calculateOutputsContract");
            calculationService.executeBusinessRules(contract, webSession.getCurrentPeriod());
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error calculateOutputsContract.", e);
        }
    }

    public void calculateEventOutputs(Event event) throws Exception {
        try {
            updateAuditInfo(event.getContract());
            calculationService.executeBusinessRules(event.getContract(), webSession.getCurrentPeriod());
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error calculateOutputs.", e);
        }
    }

    public void processBillingAmountCCChange(Event event) throws Exception {
        try {
            CurrencyEvent billingEvent = (CurrencyEvent) event;
            // If the billing event data changes (and it is since this is a 'change' event handler, then reset the LC value to null so that it is recomputed.
            calculationService.processBillingAmountCCChange(billingEvent, webSession.getCurrentPeriod(), webSession.getUser());
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error calculateOutputs.", e);
        }
    }

    public void processBillingAmountLCChange(Event event) throws Exception {
        Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "processBillingAmountLCChange");
        try {
            calculationService.processBillingAmountLCChange((CurrencyEvent) event, webSession.getCurrentPeriod(), webSession.getUser());
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error calculateOutputs.", e);
        }
    }

    public TreeNode getRootTreeNode() {
        return rootTreeNode;
    }

    public void saveInputs() throws Exception {
        if (viewSupport.isEditable()) {
            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.FINE, "Saving inputs.");
            if (reportingUnit.isInitialized(webSession.getCurrentPeriod())) {
                reportingUnit.setDraft(webSession.getCurrentPeriod());
            }
            adminService.update(reportingUnit);
            for (Contract contract : reportingUnit.getUnarchivedContracts()) {
                Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.FINER, "Updating Contract: " + contract.getName());
                contractService.update(contract);
            }
            // KJG 10/2/18 - Attempting to clear red contracts.
            init();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Inputs saved.", ""));
            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.FINE, "Inputs saved.");
        }
    }

    public void cancelEdits() throws Exception {
        Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.FINE, "Current edits canceled.");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current edits canceled.", ""));
        init();
    }

    public TreeNode getBillingTreeNode() {
        return billingTreeNode;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public int getProgress() {
        return 50;
    }

    public String getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(String activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    private void updateAuditInfo(PerformanceObligation pob) {
        pob.setLastUpdatedBy(webSession.getUser());
        pob.setLastUpdateDate(LocalDateTime.now());
        pob.getContract().setLastUpdatedBy(webSession.getUser());
        pob.getContract().setLastUpdateDate(LocalDateTime.now());
    }

    private void updateAuditInfo(Contract contract) {
        contract.setLastUpdatedBy(webSession.getUser());
        contract.setLastUpdateDate(LocalDateTime.now());
    }

    public StreamedContent getFile() {
        try {
            String filename = "POCI_Template_RU-" + reportingUnit.getCode() + ".xlsx";
            inputStream = PobInput.class.getResourceAsStream("/resources/excel_input_templates/POCI_Template_FINAL.xlsx");
            File tempFile = File.createTempFile(filename, ".xlsx");
            outputStream = new FileOutputStream(tempFile);
            templateService.processTemplateDownload(inputStream, outputStream, reportingUnit);
            InputStream inputStreamFromOutputStream = new FileInputStream(tempFile);
            file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
        } catch (Exception e) {
            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "Error generating file: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error generating file" + e.getMessage(), e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

        return file;
    }

    public void handleTemplateUpload(FileUploadEvent event) {

        try {
            templateService.processTemplateUpload((InputStream) event.getFile().getInputstream(), event.getFile().getFileName());

            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " uploaded successfully.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().getExternalContext().redirect("inputOnlineEntry.xhtml");
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error handleTemplateUpload: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error handleTemplateUpload.", e);
        }
    }

    public List<String> getSalesDestination() {
        return salesDestination;
    }

    public List<String> getOeamDisagg() {
        return oeamDisagg;
    }

    public void viewAllPObs() {

        ReportingUnit ru = webSession.getCurrentReportingUnit();
        List<Contract> cu = ru.getUnarchivedContracts();
        for (Contract contract : cu) {
            webSession.getExpandedContractIds().add(contract.getId());
        }
    }

    public void hideAllPObs() {

        webSession.getExpandedContractIds().clear();
    }

    public String refresh() {

        return "inputOnlineEntry";
    }

    public void overrideAllPObsValid() {
        try {
            logger.info("overrideAllPObsValid RU: " + reportingUnit.getCode());
            performanceObligationService.overrideAllPObsValid(reportingUnit.getPerformanceObligations());
        } catch (Exception e) {
            Logger.getLogger(PobCalculationReview.class.getName()).log(Level.INFO, "Error overriding: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error overriding: ", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
}
