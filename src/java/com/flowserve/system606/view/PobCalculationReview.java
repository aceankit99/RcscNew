/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.math.BigDecimal;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;

/**
 * @author kgraves
 */
@Named
@ViewScoped
public class PobCalculationReview implements Serializable {

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
    private List<Contract> contracts;
    ReportingUnit reportingUnit;
    private TreeNode selectedNode;
    private boolean pobsExpandable;

    @PostConstruct
    public void init() {
        try {
            reportingUnit = adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId());
            rootTreeNode = viewSupport.generateNodeTree(reportingUnit);
            initContracts();

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

//    public void initContracts(List<ReportingUnit> reportingUnits) {
//        contracts = new ArrayList<Contract>();
//        reportingUnits.forEach(reportingUnit -> contracts.addAll(reportingUnit.getContracts()));
//    }
//
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
