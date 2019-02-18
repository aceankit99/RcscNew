/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.Customer;
import com.flowserve.system606.model.DateMetric;
import com.flowserve.system606.model.DecimalMetric;
import com.flowserve.system606.model.Event;
import com.flowserve.system606.model.EventType;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricStore;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.StringAttribute;
import com.flowserve.system606.model.StringMetric;
import com.flowserve.system606.model.User;
import com.flowserve.system606.model.WorkflowAction;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.AttributeService;
import com.flowserve.system606.service.CalculationService;
import com.flowserve.system606.service.ContractService;
import com.flowserve.system606.service.CurrencyService;
import com.flowserve.system606.service.EventService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.MetricService;
import com.flowserve.system606.service.ReportingUnitService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author shubhamv
 */
@Named(value = "viewSupport")
@ViewScoped
public class ViewSupport implements Serializable {

    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    private BusinessUnit businessUnit = new BusinessUnit();
    private ReportingUnit reportingUnit = new ReportingUnit();
    private Company company;
    List<User> users = new ArrayList<User>();
    @Inject
    private AdminService adminService;
    @Inject
    private WebSession webSession;
    @Inject
    private MetricService metricService;
    @Inject
    private EventService eventService;
    private String searchString = "";
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private CurrencyService currencyService;
    @Inject
    private ContractService contractService;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private AttributeService attributeService;

    private List<FinancialPeriod> allPeriods = new ArrayList<FinancialPeriod>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private Set<ReportingUnit> preparableReportingUnits = new TreeSet<ReportingUnit>();

    public ViewSupport() {
    }

    @PostConstruct
    public void init() {
        preparableReportingUnits.addAll(reportingUnitService.getPreparableReportingUnits(webSession.getUser()));
        allPeriods = financialPeriodService.findValidDataPeriods();
        businessUnit = webSession.getEditBusinessUnit();
        reportingUnit = webSession.getEditReportingUnit();
        company = adminService.findCompanyById("FLS");
    }

    public List<User> completeUser(String searchString) {
        List<User> users = null;
        try {
            users = adminService.findByStartsWithLastName(searchString);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " user search error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error search users.", e);
        }

        return users;

    }

    public List<Customer> completeCustomer(String searchString) {
        List<Customer> customers = null;
        try {
            customers = adminService.findCustomerByStartsWithName(searchString);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " customer search error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error search users.", e);
        }

        return customers;

    }

    public String getRole(ReportingUnit reportingUnit) {
        return reportingUnit.getRole(webSession.getCurrentPeriod(), webSession.getUser());
    }

    public List<Company> completeCompany(String searchString) {
        List<Company> sites = null;

        try {
            sites = adminService.searchCompany(searchString);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " company error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            Logger.getLogger(HolidaysEdit.class.getName()).log(Level.SEVERE, "Error Company", e);
        }
        return sites;
    }

    public List<ReportingUnit> completeReportingUnit(String searchString) {
        List<ReportingUnit> rUnit = null;
        List<Long> userUnits = new ArrayList<Long>();
        User user = webSession.getUser();

        try {
            rUnit = adminService.searchReportingUnits(searchString);
            if (user.isAdmin() || user.isGlobalViewer()) {
                return rUnit;
            } else {
                userUnits = webSession.getRuCodeList();
                //rUnit = adminService.searchReportingUnits(searchString);
                List<ReportingUnit> fin = new ArrayList<ReportingUnit>();
                for (ReportingUnit ch : rUnit) {
                    if (userUnits.contains(Long.valueOf(ch.getCode()))) {
                        fin.add(ch);
                    }
                }
                return fin;
            }

        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " RU search error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error ru search.", e);
        }
        return rUnit;
    }

    public List<ReportingUnit> parentReportingUnit(String searchString) {
        List<ReportingUnit> rUnit = null;

        try {
            rUnit = adminService.parentReportingUnits(searchString, this.reportingUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " site location error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error siteLocations.", e);
        }
        return rUnit;
    }

    public List<ReportingUnit> getCompleteReportingUnit(String searchString) {
        List<ReportingUnit> rUnit = null;

        try {
            rUnit = adminService.searchReportingUnits(searchString);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " site location error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error siteLocations.", e);
        }
        return rUnit;
    }

    public List<BusinessUnit> CompleteParentBusinessUnit(String searchString) {
        List<BusinessUnit> sites = null;

        try {
            sites = adminService.searchParentBu(searchString, this.businessUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " site location error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error siteLocations.", e);
        }
        return sites;
    }

    public List<BusinessUnit> completeBusinessUnit(String searchString) {
        List<BusinessUnit> sites = null;

        try {
            sites = adminService.searchSites(searchString);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " site location error  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error siteLocations.", e);
        }
        return sites;
    }

    public List<Currency> getAllCurrencies() {
        List<Currency> currencyList = new ArrayList<Currency>();
        currencyList.addAll(Currency.getAvailableCurrencies());
        Collections.sort(currencyList, (c1, c2) -> c1.getCurrencyCode().compareTo(c2.getCurrencyCode()));

        return currencyList;
    }

    public TreeNode generateNodeTree(ReportingUnit reportingUnit) {
        List<ReportingUnit> rus = new ArrayList<ReportingUnit>();
        rus.add(reportingUnit);
        return generateNodeTree(rus);
    }

    public String getLastWorkflowActionName(ReportingUnit reportingUnit) {
        String name = "";
        WorkflowAction lastAction = reportingUnit.getLastWorkflowAction(webSession.getCurrentPeriod());
        if (lastAction != null) {
            name = lastAction.getWorkflowActionType().getName();
        }

        return name;
    }

    public String getLastWorkflowActionUsername(ReportingUnit reportingUnit) {
        String name = "";
        WorkflowAction lastAction = reportingUnit.getLastWorkflowAction(webSession.getCurrentPeriod());
        if (lastAction != null && lastAction.getUser() != null) {
            name = lastAction.getUser().getName();
        }

        return name;
    }

    public LocalDateTime getLastWorkflowActionDate(ReportingUnit reportingUnit) {

        WorkflowAction lastAction = reportingUnit.getLastWorkflowAction(webSession.getCurrentPeriod());
        if (lastAction != null) {
            return lastAction.getActionDate();
        }

        return null;
    }

    public String getEnvName() {
        return System.getProperty("rcs_environment") == null ? "" : System.getProperty("rcs_environment");
    }

    private boolean userCurrenPeriodEqualsCompanyPeriod() {
        return webSession.getCurrentPeriod().equals(this.company.getCurrentPeriod());
    }

    public String getStyle(Measurable measurable) {
        String style = "";

        if (measurable instanceof MetricStore) {
            // KJG 11/2/2018 - Release 1.11.5.1 - Only show red if cureent period.
            if (!((MetricStore) measurable).isValid() && userCurrenPeriodEqualsCompanyPeriod()) {
                return "color: red;";
            }
        }

        return style;
    }

    public TreeNode generateNodeTree(List<ReportingUnit> reportingUnits) {

        TreeNode root = new DefaultTreeNode(new BusinessUnit(), null);

        long start = System.currentTimeMillis();

        for (ReportingUnit reportingUnit : reportingUnits) {
            TreeNode reportingUnitNode = new DefaultTreeNode(reportingUnit, root);
            reportingUnitNode.setExpanded(true);
            for (Contract contract : reportingUnit.getUnarchivedContracts()) {
                TreeNode contractNode = new DefaultTreeNode(contract, reportingUnitNode);
                contractNode.setExpanded(webSession.getExpandedContractIds().contains(contract.getId()));
                for (PerformanceObligation pob : contract.getPerformanceObligations()) {
                    new DefaultTreeNode(pob, contractNode);
                    if (!pob.isValid()) {
                        pob.getContract().setValid(false);
                    }
                }
            }
        }

        long end = System.currentTimeMillis();
        long interval = end - start;

        //Logger.getLogger(ViewSupport.class.getName()).log(Level.INFO, "generateNodeTree ms: " + interval);
        return root;
    }

    public TreeNode generateNodeTreeWorkflow(ReportingUnit reportingUnit, FinancialPeriod period) {
        TreeNode root = new DefaultTreeNode(new BusinessUnit(), null);

        TreeNode reportingUnitNode = new DefaultTreeNode(reportingUnit, root);
        reportingUnitNode.setExpanded(true);
        for (WorkflowAction workflowAction : reportingUnit.getWorkflowContext(period).getWorkflowHistory()) {
            TreeNode actionNode = new DefaultTreeNode(workflowAction, reportingUnitNode);
            actionNode.setExpanded(true);
        }

        return root;
    }

    public TreeNode generateNodeTreeForBilling(ReportingUnit reportingUnit, FinancialPeriod period) {
        TreeNode root = new DefaultTreeNode(new BusinessUnit(), null);
        EventType billingEventType = eventService.getEventTypeByCode("BILLING_EVENT_CC");

        TreeNode reportingUnitNode = new DefaultTreeNode(reportingUnit, root);
        reportingUnitNode.setExpanded(true);
        for (Contract contract : reportingUnit.getUnarchivedContracts()) {
            TreeNode contractNode = new DefaultTreeNode(contract, reportingUnitNode);
            contractNode.setExpanded(webSession.getExpandedContractIds().contains(contract.getId()));
            for (Event billingEvent : calculationService.getAllEventsByPeriodAndEventType(contract, billingEventType, period)) {
                new DefaultTreeNode(billingEvent, contractNode);
                if (!billingEvent.isValid()) {
                    billingEvent.getContract().setValid(false);
                }
            }
        }

        return root;
    }

    public void onNodeExpand(NodeExpandEvent event) {
        if (event.getTreeNode().getData() instanceof Contract) {
            webSession.getExpandedContractIds().add(((Contract) event.getTreeNode().getData()).getId());
        }
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
        if (event.getTreeNode().getData() instanceof Contract) {
            webSession.getExpandedContractIds().remove(((Contract) event.getTreeNode().getData()).getId());
        }
    }

    public LocalDate getCurrentPeriodMinDate() {
        return webSession.getCurrentPeriod().getStartDate();
    }

    public LocalDate getCurrentPeriodMaxDate() {
        return webSession.getCurrentPeriod().getEndDate();
    }

    public List<Event> getAllBillingEvents(Contract contract) {
        EventType billingEventType = eventService.getEventTypeByCode("BILLING_EVENT_CC");
        List<Event> list = calculationService.getAllEventsByEventType(contract, billingEventType);

//        Collections.sort(list, (Event m1, Event m2) -> m1.getEventDate().compareTo(m2.getEventDate()));
//        Collections.reverse(list);
        return list;
    }

    public void filterNodeTree(TreeNode root, String contractFilterText) {

        //List<TreeNode> contractsToRemove = new ArrayList<TreeNode>();
        List<TreeNode> pobsToRemove = new ArrayList<TreeNode>();

        for (TreeNode reportingUnit : root.getChildren()) {
            String ruName = ((ReportingUnit) reportingUnit.getData()).getName();
            if (Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(ruName).find()
                    || Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(ruName).find()) {
                continue;
            }

            for (TreeNode contract : reportingUnit.getChildren()) {
                String contractName = ((Contract) contract.getData()).getName();
                String contractId = ((Contract) contract.getData()).getId().toString();

                if (Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(contractName).find()
                        || Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(contractId).find()) {
                    continue;
                }

                for (TreeNode pob : contract.getChildren()) {
                    String pobName = ((PerformanceObligation) pob.getData()).getName();
                    String pobId = ((PerformanceObligation) pob.getData()).getId().toString();

                    if (pobName == null) {
                        Logger.getLogger(ViewSupport.class.getName()).log(Level.INFO, "Encountered null POB name POB ID: " + pobId);
                        pobsToRemove.add(pob);
                    } else {
                        if (!Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(pobName).find()
                                && !Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(pobId).find()) {
                            pobsToRemove.add(pob);
                        }
                    }
                }
            }
        }

        // Ugly but necessary to prevent ConcurrentModificationException
        for (TreeNode pob : pobsToRemove) {
            pob.getParent().getChildren().remove(pob);
        }

        List<TreeNode> reportingUnitsToRemove = new ArrayList<TreeNode>();
        List<TreeNode> contractsToRemove = new ArrayList<TreeNode>();

        for (TreeNode reportingUnit : root.getChildren()) {
            if (reportingUnit.getChildCount() == 0) {
                reportingUnitsToRemove.add(reportingUnit);
            }
            for (TreeNode contract : reportingUnit.getChildren()) {
                if (contract.getChildCount() == 0) {
                    contractsToRemove.add(contract);
                }
            }
        }

        for (TreeNode ru : reportingUnitsToRemove) {
            ru.getParent().getChildren().remove(ru);
        }
        for (TreeNode contract : contractsToRemove) {
            contract.getParent().getChildren().remove(contract);
        }
    }

    public String getTextStyle(Measurable measurable) {
        if (measurable instanceof ReportingUnit || measurable instanceof Contract) {
            return "font-style: italic;";
        }

        return "";
    }

    public void filterBillingNodeTree(TreeNode root, String contractFilterText) {

        //List<TreeNode> contractsToRemove = new ArrayList<TreeNode>();
        List<TreeNode> billsToRemove = new ArrayList<TreeNode>();

        for (TreeNode reportingUnit : root.getChildren()) {
            String ruName = ((ReportingUnit) reportingUnit.getData()).getName();
            if (Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(ruName).find()
                    || Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(ruName).find()) {
                continue;
            }

            for (TreeNode contract : reportingUnit.getChildren()) {
                String contractName = ((Contract) contract.getData()).getName();
                String contractId = ((Contract) contract.getData()).getId().toString();

                if (Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(contractName).find()
                        || Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(contractId).find()) {
                    continue;
                }

                for (TreeNode billingEvent : contract.getChildren()) {
                    String billNumber = ((CurrencyEvent) billingEvent.getData()).getNumber();
                    String billId = ((CurrencyEvent) billingEvent.getData()).getId().toString();

                    if (billNumber == null) {
                        Logger.getLogger(ViewSupport.class.getName()).log(Level.INFO, "Encountered null Bill name Bill ID: " + billId);
                        billsToRemove.add(billingEvent);
                    } else {
                        if (!Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(billNumber).find()
                                && !Pattern.compile(Pattern.quote(contractFilterText), Pattern.CASE_INSENSITIVE).matcher(billId).find()) {
                            billsToRemove.add(billingEvent);
                        }
                    }
                }
            }
        }

        // Ugly but necessary to prevent ConcurrentModificationException
        for (TreeNode bill : billsToRemove) {
            bill.getParent().getChildren().remove(bill);
        }

        List<TreeNode> reportingUnitsToRemove = new ArrayList<TreeNode>();
        List<TreeNode> contractsToRemove = new ArrayList<TreeNode>();

        for (TreeNode reportingUnit : root.getChildren()) {
            if (reportingUnit.getChildCount() == 0) {
                reportingUnitsToRemove.add(reportingUnit);
            }
            for (TreeNode contract : reportingUnit.getChildren()) {
                if (contract.getChildCount() == 0) {
                    contractsToRemove.add(contract);
                }
            }
        }

        for (TreeNode ru : reportingUnitsToRemove) {
            ru.getParent().getChildren().remove(ru);
        }
        for (TreeNode contract : contractsToRemove) {
            contract.getParent().getChildren().remove(contract);
        }
    }

    public void filterNodeTreeContracts(TreeNode root, List<Contract> contracts) {
        List<TreeNode> contractsToRemove = new ArrayList<TreeNode>();

        for (TreeNode reportingUnit : root.getChildren()) {
            for (TreeNode contract : reportingUnit.getChildren()) {
                if (!contracts.contains(((Contract) contract.getData()))) {
                    contractsToRemove.add(contract);
                }
            }
        }

        // Ugly but necessary to prevent ConcurrentModificationException
        for (TreeNode contract : contractsToRemove) {
            contract.getParent().getChildren().remove(contract);
        }

        List<TreeNode> reportingUnitsToRemove = new ArrayList<TreeNode>();

        for (TreeNode reportingUnit : root.getChildren()) {
            if (reportingUnit.getChildCount() == 0) {
                reportingUnitsToRemove.add(reportingUnit);
            }
        }

        for (TreeNode ru : reportingUnitsToRemove) {
            ru.getParent().getChildren().remove(ru);
        }

    }

    public FinancialPeriod getCurrentPeriod() {
        return webSession.getCurrentPeriod();
    }

    public String getMessage(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getCurrencyMetric(metricCode, measurable, webSession.getCurrentPeriod()).getMessage();
    }

    public String getMessageDate(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getDateMetric(metricCode, measurable, webSession.getCurrentPeriod()).getMessage();
    }

    public boolean getValidation(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getCurrencyMetric(metricCode, measurable, webSession.getCurrentPeriod()).isValid();
    }

    public String getStyle(String metricCode, Measurable measurable) throws Exception {
        if (calculationService.getCurrencyMetric(metricCode, measurable, webSession.getCurrentPeriod()).getMessage() != null) {
            return "color: red;";
        }
        return "";
    }

    public String getBillingStyle(Event billingEvent) throws Exception {
        if (billingEvent.getMessage() != null) {
            return "color: red;";
        }
        return "";
    }

    public String getStyleDate(String metricCode, Measurable measurable) throws Exception {
        if (calculationService.getDateMetric(metricCode, measurable, webSession.getCurrentPeriod()).getMessage() != null) {
            return "color: red;";
        }
        return "";
    }

    public String getMetricTypeDescription(String metricCode) {
        return metricService.getMetricTypeByCode(metricCode).getDescription();
    }

    public String getMetricTypeName(String metricCode) {
        return metricService.getMetricTypeByCode(metricCode).getName();
    }

    public CurrencyMetric getCurrencyMetric(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getCurrencyMetric(metricCode, measurable, webSession.getCurrentPeriod());
    }

    public DateMetric getDateMetric(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getDateMetric(metricCode, measurable, webSession.getCurrentPeriod());
    }

    public DateMetric getDateMetricPriorPeriod(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getDateMetric(metricCode, measurable, webSession.getPriorPeriod());
    }

    public DecimalMetric getDecimalMetric(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getDecimalMetric(metricCode, measurable, webSession.getCurrentPeriod());
    }

    public StringMetric getStringMetric(String metricCode, PerformanceObligation pob) throws Exception {
        return calculationService.getStringMetric(metricCode, pob, webSession.getCurrentPeriod());
    }

    public StringMetric getStringMetricPriorPeriod(String metricCode, PerformanceObligation pob) throws Exception {
        return calculationService.getStringMetric(metricCode, pob, webSession.getPriorPeriod());
    }

    public CurrencyMetric getCurrencyMetricPriorPeriod(String metricCode, Measurable measurable) throws Exception {
        return calculationService.getCurrencyMetric(metricCode, measurable, webSession.getPriorPeriod());
    }

    public List<FinancialPeriod> getAllPeriods() {
        return allPeriods;
    }

    public BigDecimal getCCtoLCExchangeRate(Contract contract) throws Exception {
        return currencyService.getCCtoLCExchangeRate(contract, webSession.getCurrentPeriod());
    }

    public BigDecimal getLCtoRCExchangeRate(Contract contract) throws Exception {
        return currencyService.getLCtoRCExchangeRate(contract, webSession.getCurrentPeriod());
    }

//    public WorkflowStatus getPeriodWorkflowStatus(Contract contract) {
//        return contract.getReportingUnit().getPeriodApprovalRequest(webSession.getCurrentPeriod()).getWorkflowStatus();
//    }
    public WorkflowStatus getPeriodWorkflowStatus(ReportingUnit reportingUnit) {
        return reportingUnit.getWorkflowContext(webSession.getCurrentPeriod()).getWorkflowStatus();
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getSearchString() {
        return searchString;
    }

    public boolean isRecalcable() {
        return isEditable() && webSession.isAdmin();
    }

    public boolean isEditable() {
        FinancialPeriod period = webSession.getCurrentPeriod();
        ReportingUnit ru = webSession.getCurrentReportingUnit();
        User user = webSession.getUser();

        if (ru == null) {
            Logger.getLogger(ViewSupport.class.getName()).log(Level.INFO, "Null RU. User: " + webSession.getUser().getName());

            return false;
        }

        if (ru.isPreparable(period, user)) {
            return true;
        } else if (period.isUserFreeze() && user.isAdmin()) {
            return true;
        }

        return false;
    }

    public boolean isCurrentPeriodEvent(Event event) {
        if (event.getNumber() == null) {
            return true;
        }

        return event != null && event.getFinancialPeriod() != null && event.getFinancialPeriod().equals(webSession.getCurrentPeriod());
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(ReportingUnit reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public void initWorkflowContext(Contract contract) throws Exception {
        financialPeriodService.initWorkflowContext(webSession.getCurrentPeriod(), contract);
    }

    public void submitForReview(Contract contract) {
        try {
            contractService.submitForReview(webSession.getCurrentPeriod(), contract, webSession.getUser());
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", " submit for revew:  " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error submit for revew:", e);
        }
    }

    public boolean isBillingUpdateable() {
        FinancialPeriod period = webSession.getCurrentPeriod();
        ReportingUnit ru = webSession.getCurrentReportingUnit();
        User user = webSession.getUser();
        if (period.isOpen() && ru.isDraft(period)) {
            return true;
        } else if (period.isUserFreeze() && user.isAdmin()) {
            return true;
        }

        return false;
    }

    public boolean isMenuClickable() {
        ReportingUnit ru = webSession.getCurrentReportingUnit();
        if (ru == null) {
            return true;
        }
        return false;
    }

    public boolean renderAdminMenu() {
        User user = webSession.getUser();
        if (user != null && user.isAdmin()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPobsExpandable(ReportingUnit ru) {

        Company comp = adminService.findCompanyById("FLS");
        int pobThreshold = comp.getExpandPobThreshold();
        int totalPOB = ru.getPobCount();

        if (pobThreshold == 0) {
            return true;
        }
        if (pobThreshold > totalPOB) {
            return true;
        } else {
            return false;
        }
    }
}
