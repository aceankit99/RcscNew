/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricGroup;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.MetricGroupService;
import com.flowserve.system606.service.MetricService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.math.BigDecimal;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;

/**
 * @author kgraves
 */
@Named
@ViewScoped
public class PobCalcReview implements Serializable {

    private static final Logger logger = Logger.getLogger(PobCalculationReview.class.getName());

    private TreeNode rootTreeNode;
    @Inject
    private CalculationService calculationService;
    @Inject
    private AdminService adminService;
    private BigDecimal eacValue;
    @Inject
    private ViewSupport viewSupport;
    @Inject
    private WebSession webSession;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private MetricGroupService metricGroupService;
    @Inject
    private MetricService metricService;
    private List<Contract> contracts;
    ReportingUnit reportingUnit;
    private TreeNode selectedNode;
    private boolean pobsExpandable;

    private List<MetricGroup> dynamicColumns;
    private List<MetricGroup> dynamicTabs;
    private static Map<String, List<MetricGroup>> metricGroupsCache = new HashMap<String, List<MetricGroup>>();

    @PostConstruct
    public void init() {
        try {
            metricGroupsCache.clear();
            reportingUnit = adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId());
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
            initContracts();
            dynamicColumns = metricGroupService.findOutputMetricGroups();
            dynamicTabs = metricGroupService.findDistinctMetricGroups();

            if (webSession.getFilterText() != null) {
                filterByContractText();
            }
            if (webSession.getSelectedContracts() != null && webSession.getSelectedContracts().length > 0) {
                filterByContracts();
            }

            pobsExpandable = viewSupport.isPobsExpandable(reportingUnit);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error init pobs", e);
        }
    }

    //// START New code for dynamic columns /////
    public List<MetricGroup> getContractMetric(String group) {
        if (!metricGroupsCache.containsKey(group + "CC")) {
            List<MetricGroup> contractMetricGroup = new ArrayList<MetricGroup>();
            for (MetricGroup contractMetric : dynamicColumns) {
                if (contractMetric.getInputCurrencyType() != null && contractMetric.getInputCurrencyType().getShortName().equals("CC") && contractMetric.getGroupName().equals(group)) {
                    contractMetricGroup.add(contractMetric);
                }
            }
            metricGroupsCache.put(group + "CC", contractMetricGroup);
        }
        return metricGroupsCache.get(group + "CC");
    }

    public List<MetricGroup> getLocalMetric(String group) {
        if (!metricGroupsCache.containsKey(group + "LC")) {
            List<MetricGroup> localMetricGroup = new ArrayList<MetricGroup>();
            for (MetricGroup localMetric : dynamicColumns) {
                if (localMetric.getInputCurrencyType() != null && localMetric.getInputCurrencyType().getShortName().equals("LC") && localMetric.getGroupName().equals(group)) {
                    localMetricGroup.add(localMetric);
                }
            }
            metricGroupsCache.put(group + "LC", localMetricGroup);
        }
        return metricGroupsCache.get(group + "LC");
    }

    public List<MetricGroup> getNonCurrencyMetric(String group) {
        if (!metricGroupsCache.containsKey(group + "None")) {
            List<MetricGroup> nonCurrencyMetricGroup = new ArrayList<MetricGroup>();
            for (MetricGroup nonCurrencyMetric : dynamicColumns) {
                if (nonCurrencyMetric.getInputCurrencyType() == null && nonCurrencyMetric.getGroupName().equals(group)) {
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

    public boolean isCurrencyCode(Measurable measurable) {
        return measurable instanceof PerformanceObligation || measurable instanceof Contract;
    }

    public boolean isContractCurrencyValue(MetricGroup metricGroup, Measurable measurable) {
        return !(measurable instanceof ReportingUnit);
    }

    public boolean isDecimalValue(Measurable measurable) {
        return !(measurable instanceof ReportingUnit);
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
        return Integer.toString(getContractMetric(group).size() + 1); // +1 for contract currency code
    }

    public String getColSpanLCMetric(String group) {
        return Integer.toString(getLocalMetric(group).size() + 1); // +1 for local currency code
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

    public List<MetricGroup> getDynamicTabs() {
        return dynamicTabs;
    }

    public void setDynamicTabs(List<MetricGroup> dynamicTabs) {
        this.dynamicTabs = dynamicTabs;
    }
    //// END New code for dynamic columns /////

    public void initContracts() {
        contracts = reportingUnit.getUnarchivedContracts();
    }

    public boolean isPobsExpandable() {
        return pobsExpandable;
    }

    public void filterByContractText() {
        if (isEmpty(webSession.getFilterText())) {
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        } else {
            viewSupport.filterNodeTree(rootTreeNode, webSession.getFilterText());
        }
    }

    private boolean isEmpty(String text) {
        if (text == null || "".equals(webSession.getFilterText().trim())) {
            return true;
        }

        return false;
    }

    public WorkflowStatus getWorkflowStatus() {
        return reportingUnit.getWorkflowStatus(webSession.getCurrentPeriod());
    }

    public void onReportingUnitSelect(SelectEvent event) {
        webSession.setFilterText(null);
        webSession.setCurrentReportingUnit((ReportingUnit) event.getObject());
        init();
    }

    public void clearFilterByContractText() {
        webSession.setFilterText(null);
        webSession.setSelectedContracts(null);
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
    }

    public void filterByContracts() {
        rootTreeNode = viewSupport.generateNodeTree(reportingUnit);

        if (webSession.getSelectedContracts().length == 0) {
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
        } else {
            viewSupport.filterNodeTreeContracts(rootTreeNode, Arrays.asList(webSession.getSelectedContracts()));
        }
    }

    public void switchPeriod(FinancialPeriod period) {
        webSession.switchPeriod(period);
        init();
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

    public boolean isNov17Calculable() {
        if ("kgraves".equals(webSession.getUser().getFlsId())) {
            return true;
        }

        if ("rcs_admin".equals(webSession.getUser().getFlsId())) {
            return true;
        }

        return false;
    }

    public void calculateAndSave() {
        try {
            calculationService.calculateAndSave(reportingUnit, webSession.getCurrentPeriod());
        } catch (Exception e) {
            Logger.getLogger(PobCalculationReview.class.getName()).log(Level.INFO, "Error recalculating: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error recalculating: ", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void calculateAndSaveSinceNov17() {
        try {
            calculationService.calculateAndSave(reportingUnit, financialPeriodService.findById("NOV-17"));
        } catch (Exception e) {
            Logger.getLogger(PobCalculationReview.class.getName()).log(Level.INFO, "Error recalculating: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error recalculating: ", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void calculateAndSaveCurrentPeriod() {
        try {
            calculationService.calculateAndSave(reportingUnit, webSession.getCurrentPeriod());
        } catch (Exception e) {
            Logger.getLogger(PobCalculationReview.class.getName()).log(Level.INFO, "Error recalculating: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error recalculating: ", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void convertRCValueAndSaveCurrentPeriod() {
        try {
            calculationService.convertRCAndSave(reportingUnit, webSession.getCurrentPeriod());
        } catch (Exception e) {
            Logger.getLogger(PobCalculationReview.class.getName()).log(Level.INFO, "Error recalculating: ", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error recalculating: ", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void printInputs(PerformanceObligation pob) {
//        Map<String, Input> inputs = pob.getInputs();
//        for (String inputTypeId : inputs.keySet()) {
//            logger.info("execute BR.  inputTypeId: " + inputTypeId + "\tvalue: " + inputs.get(inputTypeId).getValue());
//        }
    }

    public void calculateOutputs(PerformanceObligation pob) throws Exception {
        printInputs(pob);
        calculationService.executeBusinessRules(pob, webSession.getCurrentPeriod());
    }

    public TreeNode getRootTreeNode() {
        return rootTreeNode;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public boolean isDraft() {
        return reportingUnit.isDraft(webSession.getCurrentPeriod());
    }

    public boolean isPrepared() {
        return reportingUnit.isPrepared(webSession.getCurrentPeriod());
    }

    public boolean isReviewed() {
        return reportingUnit.isReviewed(webSession.getCurrentPeriod());
    }

    public boolean isSubmittableForReview() throws Exception {
//        if (!calculationService.isCalculationDataValid(reportingUnit, webSession.getCurrentPeriod())) {
//            return false;
//        }
        for (PerformanceObligation pob : reportingUnit.getUnarchivedPerformanceObligations()) {
            if (!pob.isValid()) {
                return false;
            }
        }

        return reportingUnit.isPreparable(webSession.getCurrentPeriod(), webSession.getUser());
    }

    public boolean isReviewable() throws Exception {
        return reportingUnit.isReviewable(webSession.getCurrentPeriod(), webSession.getUser());
    }

    public boolean isSubmittableForApproval() throws Exception {
        return reportingUnit.isReviewable(webSession.getCurrentPeriod(), webSession.getUser());
    }

    public boolean isApprovable() throws Exception {
        return reportingUnit.isApprovable(webSession.getCurrentPeriod(), webSession.getUser());
    }

    public boolean isRejectable() throws Exception {
        return reportingUnit.isRejectable(webSession.getCurrentPeriod(), webSession.getUser());
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
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

        return "pobCalculationReview";
    }

}
