package com.flowserve.system606.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "WORKFLOW_ACTIONS")
public class WorkflowAction extends BaseEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1543395541215367981L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WF_ACTION_SEQ")
    @SequenceGenerator(name = "WF_ACTION_SEQ", sequenceName = "WF_ACTION_SEQ", allocationSize = 10)
    @Column(name = "WORKFLOW_ACTION_ID")
    private Long id;
    @Column(name = "WORKFLOW_ACTION_TYPE")
    private WorkflowActionType workflowActionType;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ACTION_DATE")
    private LocalDateTime actionDate;
    @Column(name = "REJECT_REASON")
    private String rejectionReason;
    @Column(name = "COMMENTS", length = 3000)
    private String comments;
    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    public WorkflowAction() {
        this.actionDate = LocalDateTime.now();
    }

    public WorkflowAction(WorkflowActionType type, User user) {
        this.workflowActionType = type;
        this.user = user;
        this.actionDate = LocalDateTime.now();
    }

    public String getName() {
        return workflowActionType.getName();
    }

    public boolean isApproveAction() {
        return WorkflowActionType.APPROVE.equals(this.workflowActionType);
    }

    public boolean isCreateAction() {
        return WorkflowActionType.CREATE.equals(this.workflowActionType);
    }

    public boolean isRequestApprovalAction() {
        return WorkflowActionType.REQUEST_APPROVAL.equals(this.workflowActionType);
    }

    public boolean isRejectAction() {
        return WorkflowActionType.REJECT.equals(this.workflowActionType);
    }

    public boolean isUpdateAction() {
        return WorkflowActionType.UPDATE.equals(this.workflowActionType);
    }

    public boolean isRemoveAction() {
        return WorkflowActionType.REMOVE.equals(this.workflowActionType);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkflowActionType getWorkflowActionType() {
        return workflowActionType;
    }

    public void setWorkflowActionType(WorkflowActionType workflowActionType) {
        this.workflowActionType = workflowActionType;
    }

    public String getComments() {
        if (isRejectAction() && rejectionReason != null) {
            return rejectionReason + ": " + comments;
        }
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
