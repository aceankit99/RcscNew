package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "WORKFLOW_CONTEXTS")
public class WorkflowContext implements Serializable {

    @Transient
    private static final long serialVersionUID = 8112918539712898232L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WORKFLOW_CONTEXT_SEQ")
    @SequenceGenerator(name = "WORKFLOW_CONTEXT_SEQ", sequenceName = "WORKFLOW_CONTEXT_SEQ", allocationSize = 10)
    @Column(name = "APPROVAL_REQUEST_ID")
    private Long id;
    @Column(name = "WORKFLOW_STATUS")
    private WorkflowStatus workflowStatus;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REPORTING_UNIT_ID")
    private ReportingUnit reportingUnit;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_ID")
    private ApprovalGroup approvalGroup;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(name = "APPROVAL_WORKHISTORY", joinColumns = @JoinColumn(name = "WORKFLOW_CONTEXT_ID"), inverseJoinColumns = @JoinColumn(name = "WORKFLOW_ACTION_ID"))
    private List<WorkflowAction> workflowHistory = new ArrayList<WorkflowAction>();
    @JoinColumn(name = "PERIOD_ID")
    private FinancialPeriod financialPeriod;

    public WorkflowContext() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkflowStatus getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkflowStatus workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(ReportingUnit reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ApprovalGroup getApprovalGroup() {
        return approvalGroup;
    }

    public void setApprovalGroup(ApprovalGroup approvalGroup) {
        this.approvalGroup = approvalGroup;
    }

    public List<WorkflowAction> getWorkflowHistory() {
        return workflowHistory;
    }

    public void setWorkflowHistory(List<WorkflowAction> workflowHistory) {
        this.workflowHistory = workflowHistory;
    }

    public FinancialPeriod getFinancialPeriod() {
        return financialPeriod;
    }

    public void setFinancialPeriod(FinancialPeriod financialPeriod) {
        this.financialPeriod = financialPeriod;
    }

}
