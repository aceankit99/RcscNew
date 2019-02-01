/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.controller.AdminController;
import com.flowserve.system606.model.Country;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.User;
import com.flowserve.system606.model.WorkflowAction;
import com.flowserve.system606.model.WorkflowActionType;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named
@ViewScoped
public class ReportingUnitEdit implements Serializable {

    private static Logger logger = Logger.getLogger("com.flowserve.system606");
    private ReportingUnit editReporintgUnit = new ReportingUnit();
    private Country country = new Country();
    List<Country> countries = new ArrayList<Country>();
    private User completedUser;
    private User completedPUser;
    private User completedRUser;
    private User completedVUser;
    @Inject
    private WebSession webSession;
    @Inject
    private AdminService adminService;
    @Inject
    private AdminController adminController;

    public ReportingUnitEdit() {
    }

    @PostConstruct
    public void init() {
        editReporintgUnit = webSession.getEditReportingUnit();

        try {
            countries = adminService.AllCountry();
        } catch (Exception ex) {
            Logger.getLogger(ReportingUnitEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addApprover(User approver) {
        try {
            if (approver == null || editReporintgUnit.getApprovers().contains(approver)) {
                return;
            }
            String comment = "Approver " + approver.getName() + " Added";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.APPROVER_ADD, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getApprovers().add(approver);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
            completedUser = null;
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void removeApprover(User approver) {

        try {
            String comment = "Approver " + approver.getName() + " Removed";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.APPROVER_REMOVE, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getApprovers().remove(approver);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void addPreparer(User preparer) {

        try {
            if (preparer == null || editReporintgUnit.getPreparers().contains(preparer)) {
                return;
            }
            String comment = "Preparer " + preparer.getName() + " Added";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.PREPARER_ADD, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getPreparers().add(preparer);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
            completedPUser = null;
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void removePreparer(User preparer) {

        try {
            String comment = "Preparer " + preparer.getName() + " Removed";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.PREPARER_REMOVE, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getPreparers().remove(preparer);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void addReviewer(User reviewer) {
        try {
            if (reviewer == null || editReporintgUnit.getReviewers().contains(reviewer)) {
                return;
            }
            String comment = "Reviewer " + reviewer.getName() + " Added";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.REVIEWER_ADD, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getReviewers().add(reviewer);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
            completedRUser = null;
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void removeReviewer(User reviewer) {

        try {
            String comment = "Reviewer " + reviewer.getName() + " Removed";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.REVIEWER_REMOVE, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getReviewers().remove(reviewer);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void addViewer(User viewer) {
        try {
            if (viewer == null || editReporintgUnit.getReviewers().contains(viewer)) {
                return;
            }
            String comment = "Viewer " + viewer.getName() + " Added";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.VIEWER_ADD, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getViewers().add(viewer);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
            completedVUser = null;
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public void removeViewer(User viewer) {

        try {
            String comment = "Viewer " + viewer.getName() + " Removed";
            WorkflowAction action = new WorkflowAction(WorkflowActionType.VIEWER_REMOVE, webSession.getUser());
            action.setComments(comment);
            adminService.persist(action);
            editReporintgUnit.getViewers().remove(viewer);
            editReporintgUnit.getWorkflowHistory().add(action);
            editReporintgUnit = adminService.update(editReporintgUnit);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Site save error " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.log(Level.SEVERE, "Error site save.", e);
        }
    }

    public String addUpdateCondition() throws Exception {
        return this.editReporintgUnit.getId() == null ? adminController.addReportingUnit(this.editReporintgUnit) : adminController.updateReportingUnit(this.editReporintgUnit);
    }

    public ReportingUnit getEditReporintgUnit() {
        return editReporintgUnit;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public User getCompletedUser() {
        return completedUser;
    }

    public void setCompletedUser(User completedUser) {
        this.completedUser = completedUser;
    }

    public User getCompletedPUser() {
        return completedPUser;
    }

    public void setCompletedPUser(User completedPUser) {
        this.completedPUser = completedPUser;
    }

    public User getCompletedVUser() {
        return completedVUser;
    }

    public void setCompletedVUser(User completedVUser) {
        this.completedVUser = completedVUser;
    }

    public User getCompletedRUser() {
        return completedRUser;
    }

    public void setCompletedRUser(User completedRUser) {
        this.completedRUser = completedRUser;
    }

}
