/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.Event;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricGroup;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationService;
import com.flowserve.system606.service.ContractService;
import com.flowserve.system606.service.EventService;
import com.flowserve.system606.service.MetricGroupService;
import com.flowserve.system606.service.MetricService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class InputEntry implements Serializable {

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
    @Inject
    private MetricGroupService metricGroupService;
    @Inject
    private MetricService metricService;
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

    private List<MetricGroup> dynamicColumns;
    private List<MetricGroup> dynamicTabs;
    private static Map<String, List<MetricGroup>> metricGroupsCache = new HashMap<String, List<MetricGroup>>();

    @PostConstruct
    public void init() {
        reportingUnit = adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId());
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        billingTreeNode = viewSupport.generateNodeTreeForBilling(reportingUnit, webSession.getCurrentPeriod());
        initContracts(reportingUnit);
        pobsExpandable = viewSupport.isPobsExpandable(reportingUnit);

        dynamicColumns = metricGroupService.findInputMetricGroups();
        dynamicTabs = metricGroupService.findInputDistinctMetricGroups();

        if (webSession.getFilterText() != null) {
            filterByContractText();
        }
        if (webSession.getSelectedContracts() != null && webSession.getSelectedContracts().length > 0) {
            filterByContracts();
        }
    }

    //// START New code for dynamic columns /////
    public List<MetricGroup> getContractMetric(String group) {
        if (!metricGroupsCache.containsKey(group + "Contract")) {
            List<MetricGroup> contractMetricGroup = new ArrayList<MetricGroup>();
            for (MetricGroup contractMetric : dynamicColumns) {
                if (contractMetric.getColumnGroup() != null && contractMetric.getColumnGroup().equals("Contract") && contractMetric.getGroupName().equals(group)) {
                    contractMetricGroup.add(contractMetric);
                }
            }
            metricGroupsCache.put(group + "Contract", contractMetricGroup);
        }
        return metricGroupsCache.get(group + "Contract");
    }

    public List<MetricGroup> getLocalMetric(String group) {
        if (!metricGroupsCache.containsKey(group + "Local")) {
            List<MetricGroup> localMetricGroup = new ArrayList<MetricGroup>();
            for (MetricGroup localMetric : dynamicColumns) {
                if (localMetric.getColumnGroup() != null && localMetric.getColumnGroup().equals("Local") && localMetric.getGroupName().equals(group)) {
                    localMetricGroup.add(localMetric);
                }
            }
            metricGroupsCache.put(group + "Local", localMetricGroup);
        }
        return metricGroupsCache.get(group + "Local");
    }

    public List<MetricGroup> getNonCurrencyMetric(String group) {
        if (!metricGroupsCache.containsKey(group + "None")) {
            List<MetricGroup> nonCurrencyMetricGroup = new ArrayList<MetricGroup>();
            for (MetricGroup nonCurrencyMetric : dynamicColumns) {
                if (nonCurrencyMetric.getColumnGroup() != null && nonCurrencyMetric.getColumnGroup().equals("None") && nonCurrencyMetric.getGroupName().equals(group)) {
                    nonCurrencyMetricGroup.add(nonCurrencyMetric);
                }
                metricGroupsCache.put(group + "None", nonCurrencyMetricGroup);
            }
        }
        return metricGroupsCache.get(group + "None");
    }

    public CurrencyMetric getCurrencyMetric(MetricGroup metricGroup, Measurable measurable) throws Exception {
        if (metricGroup.isPrior()) {
            return calculationService.getCurrencyMetric(metricGroup.getCode(), measurable, webSession.getPriorPeriod());
        } else {
            return calculationService.getCurrencyMetric(metricGroup.getCode(), measurable, webSession.getCurrentPeriod());
        }
    }

    public List<String> getSelectItemsValue(MetricGroup metricGroup) {
        if (metricGroup.getCode().equals("SALES_DESTINATION")) {
            return salesDestination;
        } else if (metricGroup.getCode().equals("OEAM_DISAGG")) {
            return oeamDisagg;
        } else {
            return null;
        }
    }

    public boolean isCurrencyCode(Measurable measurable) {
        return measurable instanceof PerformanceObligation || measurable instanceof Contract;
    }

    public boolean isLocalCurrencyValue(MetricGroup metricGroup, Measurable measurable) {
        if (metricGroup.getInputCurrencyType() != null && metricGroup.getInputCurrencyType().getShortName().equals("LC")) {
            return !(measurable instanceof PerformanceObligation && metricGroup.getOwnerEntityType().equals("Contract"));
        } else {
            return false;
        }

    }

    public boolean isContractCurrencyValue(MetricGroup metricGroup, Measurable measurable) {
        if (measurable instanceof ReportingUnit) {
            return false;
        } else {
            if (metricGroup.getInputCurrencyType() != null && metricGroup.getInputCurrencyType().getShortName().equals("CC")) {
                return !(measurable instanceof PerformanceObligation && metricGroup.getOwnerEntityType().equals("Contract"));
            } else {
                return false;
            }

        }
    }

    public boolean isDateValue(MetricGroup metricGroup, Measurable measurable) {
        if (metricGroup.getInputCurrencyType() == null && metricService.getMetricTypeByCode(metricGroup.getCode()).getMetricClass().equals("DateMetric")) {
            if (measurable instanceof PerformanceObligation && metricGroup.getOwnerEntityType().equals("POB")) {
                return true;
            } else {
                return measurable instanceof Contract && metricGroup.getOwnerEntityType().equals("Contract");
            }
        } else {
            return false;
        }
    }

    public boolean isDecimalValue(MetricGroup metricGroup, Measurable measurable) {
        if (metricGroup.getInputCurrencyType() == null && metricService.getMetricTypeByCode(metricGroup.getCode()).getMetricClass().equals("DecimalMetric")) {
            return !(measurable instanceof ReportingUnit);
        } else {
            return false;
        }

    }

    public boolean isStringValue(MetricGroup metricGroup, Measurable measurable) {
        if (metricGroup.getInputCurrencyType() == null && metricService.getMetricTypeByCode(metricGroup.getCode()).getMetricClass().equals("StringMetric")) {
            if (measurable instanceof PerformanceObligation && metricGroup.getOwnerEntityType().equals("POB")) {
                return true;
            } else {
                return measurable instanceof Contract && metricGroup.getOwnerEntityType().equals("Contract");
            }

        } else {
            return false;
        }

    }

    public boolean isEditable(MetricGroup metricGroup) {
        return metricGroup.isEditable();
    }

    public boolean isDisable(MetricGroup metricGroup, Measurable measurable) {
        if (measurable instanceof PerformanceObligation && metricGroup.getOwnerEntityType().equals("POB")) {
            return true;
        } else {
            return measurable instanceof Contract && metricGroup.getOwnerEntityType().equals("Contract");
        }
    }

    public boolean isFXRate(Measurable measurable) {
        return measurable instanceof PerformanceObligation;
    }

    public String getHeaderText(MetricGroup metricGroup) {
        if (metricGroup.isPrior()) {
            return metricService.getMetricTypeByCode(metricGroup.getCode()).getName() + " Prior Period ";
        } else {
            return metricService.getMetricTypeByCode(metricGroup.getCode()).getName();
        }
    }

    public String getHeaderDescription(MetricGroup metricGroup) {
        if (metricGroup.isPrior()) {
            return metricService.getMetricTypeByCode(metricGroup.getCode()).getDescription() + " Prior Period ";
        } else {
            return metricService.getMetricTypeByCode(metricGroup.getCode()).getDescription();
        }
    }

    public String getColSpanCCMetric(String group) {
        return Integer.toString(getContractMetric(group).size() + 1); // +1 for contract currency
    }

    public String getColSpanLCMetric(String group) {
        return Integer.toString(getLocalMetric(group).size() + 1); // +1 for local currency
    }

    public String getColSpanNonCurrencyMetric(String group) {
        return Integer.toString(getNonCurrencyMetric(group).size());
    }

    public boolean isCCMetricAvail(String group) {
        return getContractMetric(group).size() > 0;
    }

    public boolean isLCMetricAvail(String group) {
        return getLocalMetric(group).size() > 0;
    }

    public boolean isNonCurrencyMetricAvail(String group) {
        return getNonCurrencyMetric(group).size() > 0;
    }

    public List<MetricGroup> getDynamicColumns() {
        return dynamicColumns;
    }

    public void setDynamicColumns(List<MetricGroup> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    public void calculateOutputs(Measurable measurable) throws Exception {
        if (measurable instanceof PerformanceObligation) {
            PerformanceObligation pob = (PerformanceObligation) measurable;
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
        } else {
            Contract contract = (Contract) measurable;
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

    }
    //// END New code for dynamic columns /////

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

//    public void calculateOutputs(PerformanceObligation pob) throws Exception {
//        try {
//            updateAuditInfo(pob);
//            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "Executing business rules POB: " + pob.getId());
//            calculationService.executeBusinessRules(pob, webSession.getCurrentPeriod());
//            calculationService.executeBusinessRules(pob.getContract(), webSession.getCurrentPeriod());
//        } catch (Exception e) {
//            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
//            FacesContext.getCurrentInstance().addMessage(null, msg);
//            logger.log(Level.SEVERE, "Error calculateOutputs.", e);
//        }
//    }
//
//    public void calculateOutputsContract(Contract contract) throws Exception {
//        try {
//            updateAuditInfo(contract);
//            Logger.getLogger(InputOnlineEntry.class.getName()).log(Level.INFO, "Executing calculateOutputsContract");
//            calculationService.executeBusinessRules(contract, webSession.getCurrentPeriod());
//        } catch (Exception e) {
//            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getCause().getCause().getMessage());
//            FacesContext.getCurrentInstance().addMessage(null, msg);
//            logger.log(Level.SEVERE, "Error calculateOutputsContract.", e);
//        }
//    }
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

    public List<MetricGroup> getDynamicTabs() {
        return dynamicTabs;
    }

    public void setDynamicTabs(List<MetricGroup> dynamicTabs) {
        this.dynamicTabs = dynamicTabs;
    }

}
