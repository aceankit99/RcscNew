/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "REPORTING_UNITS")
public class ReportingUnit extends TransientMeasurable<Long> implements Measurable, Comparable<ReportingUnit>, Serializable {

    private static final long serialVersionUID = 8757812203684986897L;
    private static final Logger LOG = Logger.getLogger(ReportingUnit.class.getName());
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORTING_UNIT_SEQ")
    @SequenceGenerator(name = "REPORTING_UNIT_SEQ", sequenceName = "REPORTING_UNIT_SEQ", allocationSize = 50)
    @Column(name = "REPORTING_UNIT_ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CODE")
    private String code;
    @ManyToOne
    @JoinColumn(name = "BUSINESS_UNIT_ID")
    private BusinessUnit businessUnit;
    @ManyToOne
    @JoinColumn(name = "COUNTRY_ID")
    private Country country;
    @Column(name = "LOCAL_CURRENCY")
    private Currency localCurrency;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "REPORTING_UNIT_SUBMITTER", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> submitters = new ArrayList<User>();
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "REPORTING_UNIT_PREPARERS", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> preparers = new ArrayList<User>();
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "REPORTING_UNIT_REVIEWERS", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> reviewers = new ArrayList<User>();
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "REPORTING_UNIT_APPROVERS", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> approvers = new ArrayList<User>();
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "REPORTING_UNIT_VIEWERS", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> viewers = new ArrayList<User>();
    @Column(name = "IS_ACTIVE")
    private boolean active;
    @Column(name = "REGION")
    private String region;
    @Column(name = "COE_ROLE")
    private String coeRole;
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private ReportingUnit parentReportingUnit;
    @ManyToOne
    @JoinColumn(name = "COMPANY_ID")
    private Company company;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reportingUnit")
    @OrderBy("name ASC")
    private List<Contract> contracts = new ArrayList<Contract>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reportingUnit")
    @OrderBy("name ASC")
    private List<Customer> customers = new ArrayList<Customer>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentReportingUnit")
    private List<ReportingUnit> childReportingUnits = new ArrayList<ReportingUnit>();
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "RU_APPROVAL_REQUEST", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "APPROVAL_REQUEST_ID"))
    @MapKeyJoinColumn(name = "PERIOD_ID")
    private Map<FinancialPeriod, WorkflowContext> periodWorkflowContextMap = new HashMap<FinancialPeriod, WorkflowContext>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(name = "RU_CHANGE_HISTORY", joinColumns = @JoinColumn(name = "REPORTING_UNIT_ID"), inverseJoinColumns = @JoinColumn(name = "WORKFLOW_ACTION_ID"))
    private List<WorkflowAction> workflowHistory = new ArrayList<WorkflowAction>();

    public ReportingUnit() {
    }

    @Override
    public int compareTo(ReportingUnit obj) {
        return this.code.compareTo(obj.getCode());
    }

    public String getRole(FinancialPeriod period, User user) {
        if (this.preparers.contains(user) && this.reviewers.contains(user)) {
            if (this.isInitialized(period) || this.isDraft(period)) {
                return "Preparer";
            } else if (this.isPrepared(period)) {
                return "Reviewer";
            }
        }

        if (this.approvers.contains(user)) {
            return "Approver";
        }

        if (this.reviewers.contains(user)) {
            return "Reviewer";
        }

        if (this.preparers.contains(user)) {
            return "Preparer";
        }

        if (this.viewers.contains(user)) {
            return "Viewer";
        }

        return "";
    }

    public boolean isDraft(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }
        return WorkflowStatus.DRAFT.equals(periodWorkflowContextMap.get(period).getWorkflowStatus());
    }

    public boolean isInitialized(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }
        return WorkflowStatus.INITIALIZED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus());
    }

    public boolean isPrepared(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }
        return WorkflowStatus.PREPARED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus());
    }

    public boolean isReviewed(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }
        return WorkflowStatus.REVIEWED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus());
    }

    public boolean isRejected(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }
        return WorkflowStatus.REJECTED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus());
    }

    public boolean isApproved(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }
        return WorkflowStatus.APPROVED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus());
    }

    public boolean isPreparable(FinancialPeriod period, User user) {
        if (!period.isOpen()) {
            return false;
        }

        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }

        if (WorkflowStatus.INITIALIZED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.preparers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        if (WorkflowStatus.DRAFT.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.preparers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        if (WorkflowStatus.REJECTED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.preparers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        return false;
    }

    public boolean isReviewable(FinancialPeriod period, User user) {
        if (!period.isOpen()) {
            return false;
        }

        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }

        if (WorkflowStatus.PREPARED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.reviewers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        return false;
    }

    public boolean isApprovable(FinancialPeriod period, User user) {
        if (!period.isOpen()) {
            return false;
        }

        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }

        if (WorkflowStatus.REVIEWED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.approvers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        return false;
    }

    public boolean isRejectable(FinancialPeriod period, User user) {
        if (!period.isOpen()) {
            return false;
        }

        if (periodWorkflowContextMap.get(period) == null) {
            return false;
        }

        if (WorkflowStatus.PREPARED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.reviewers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        if (WorkflowStatus.REVIEWED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.approvers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        if (WorkflowStatus.APPROVED.equals(periodWorkflowContextMap.get(period).getWorkflowStatus())) {
            if (this.approvers.contains(user) || user.isAdmin()) {
                return true;
            }
        }

        return false;
    }

    public WorkflowAction getLastWorkflowAction(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) != null) {
            List<WorkflowAction> actionHistory = periodWorkflowContextMap.get(period).getWorkflowHistory();
            if (actionHistory != null) {
                if (actionHistory.size() >= 1) {
                    return actionHistory.get(actionHistory.size() - 1);
                }
            }
        }

        return null;
    }

    public WorkflowStatus getWorkflowStatus(FinancialPeriod period) {
        if (periodWorkflowContextMap.get(period) != null) {
            return periodWorkflowContextMap.get(period).getWorkflowStatus();
        }

        return null;
    }

    public List<PerformanceObligation> getPobsByRevenueMethod(RevenueMethod revenueMethod) {
        List<PerformanceObligation> pobs = new ArrayList<PerformanceObligation>();

        for (PerformanceObligation pob : this.getUnarchivedPerformanceObligations()) {
            if (revenueMethod.equals(pob.getRevenueMethod())) {
                pobs.add(pob);
            }
        }

        return pobs;
    }

    public void setPrepared(FinancialPeriod period) {
        periodWorkflowContextMap.get(period).setWorkflowStatus(WorkflowStatus.PREPARED);
    }

    public void setReviewed(FinancialPeriod period) {
        periodWorkflowContextMap.get(period).setWorkflowStatus(WorkflowStatus.REVIEWED);
    }

    public void setRejected(FinancialPeriod period) {
        periodWorkflowContextMap.get(period).setWorkflowStatus(WorkflowStatus.REJECTED);
    }

    public void setApproved(FinancialPeriod period) {
        periodWorkflowContextMap.get(period).setWorkflowStatus(WorkflowStatus.APPROVED);
    }

    public void setInitialized(FinancialPeriod period) {
        periodWorkflowContextMap.get(period).setWorkflowStatus(WorkflowStatus.INITIALIZED);
    }

    public void setDraft(FinancialPeriod period) {
        periodWorkflowContextMap.get(period).setWorkflowStatus(WorkflowStatus.DRAFT);
    }

    public void addWorkflowAction(FinancialPeriod period, WorkflowAction action) {
        periodWorkflowContextMap.get(period).getWorkflowHistory().add(action);
    }

    public WorkflowContext getWorkflowContext(FinancialPeriod period) {
        return periodWorkflowContextMap.get(period);
    }

    public void putPeriodWorkflowContext(FinancialPeriod period, WorkflowContext workflowContext) {
        periodWorkflowContextMap.put(period, workflowContext);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getLocalCurrency() {
        return localCurrency;
    }

    public Currency getContractCurrency() {
        return null;
    }

    public Currency getReportingCurrency() {
        return company.getReportingCurrency();
    }

    public void setLocalCurrency(Currency localCurrency) {
        this.localCurrency = localCurrency;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<User> getApprovers() {
        return approvers;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<User> getPreparers() {
        return preparers;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public List<Measurable> getChildMeasurables() {
        return new ArrayList<Measurable>(getUnarchivedContracts());
    }

    public BigDecimal getPobCountRejected() {  // TODO - Remove. Temp code for UI.
        return new BigDecimal("10.0");
    }

    public List<PerformanceObligation> getPerformanceObligations() {
        List<PerformanceObligation> pobs = new ArrayList<PerformanceObligation>();
        contracts.forEach(contract -> pobs.addAll(contract.getPerformanceObligations()));

        return pobs;
    }

    public List<PerformanceObligation> getUnarchivedPerformanceObligations() {
        List<PerformanceObligation> pobs = new ArrayList<PerformanceObligation>();
        getUnarchivedContracts().forEach(contract -> pobs.addAll(contract.getPerformanceObligations()));

        return pobs;
    }

    public List<Contract> getUnarchivedContracts() {
        List<Contract> activeContracts = new ArrayList<Contract>();
        contracts.forEach(contract -> {
            if (contract.getWorkflowStatus() != WorkflowStatus.ARCHIVED) {
                activeContracts.add(contract);
            }
        });

        return activeContracts;
    }

    public int getPobCount() {
        return getUnarchivedPerformanceObligations().size();
    }

    public int getContractCount() {
        return getUnarchivedContracts().size();
    }

    public long getPobInputRequiredCount() {
        long count = 0L;
        for (PerformanceObligation pob : getUnarchivedPerformanceObligations()) {
            if (!pob.isValid()) {
                count++;
            }
        }
        return count;
        //return getPerformanceObligations().stream().filter(PerformanceObligation::isInputRequired).count();
    }

    public long getPobInvalidCount() {
        return 0L;
    }

    public ReportingUnit getParentReportingUnit() {
        return parentReportingUnit;
    }

    public void setParentReportingUnit(ReportingUnit parentReportingUnit) {
        this.parentReportingUnit = parentReportingUnit;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<ReportingUnit> getChildReportingUnits() {
        return childReportingUnits;
    }

    public void setChildReportingUnits(List<ReportingUnit> childReportingUnits) {
        this.childReportingUnits = childReportingUnits;
    }

    public boolean isParent() {
        return childReportingUnits.size() > 0;
    }

    public List<User> getSubmitters() {
        return submitters;
    }

    public void setSubmitters(List<User> submitters) {
        this.submitters = submitters;
    }

    public List<User> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<User> reviewers) {
        this.reviewers = reviewers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCoeRole() {
        return coeRole;
    }

    public void setCoeRole(String coeRole) {
        this.coeRole = coeRole;
    }

    public List<User> getViewers() {
        return viewers;
    }

    public void setViewers(List<User> viewers) {
        this.viewers = viewers;
    }

    public List<WorkflowAction> getWorkflowHistory() {
        return workflowHistory;
    }

    public void setWorkflowHistory(List<WorkflowAction> workflowHistory) {
        this.workflowHistory = workflowHistory;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
