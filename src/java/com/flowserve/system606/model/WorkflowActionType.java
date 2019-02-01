package com.flowserve.system606.model;

public enum WorkflowActionType {
    CREATE("Create"),
    INITIALIZE("Initialize"),
    UPDATE("Update"),
    REQUEST_REVIEW("Request Review"),
    REQUEST_APPROVAL("Request Approval"),
    REVIEW("Review"),
    APPROVE("Approve"),
    REJECT("Reject"),
    REMOVE("Remove"),
    CANCEL("Cancel"),
    CLOSE("Close"),
    PREPARER_ADD("Preparer Added"),
    REVIEWER_ADD("Reviewer Added"),
    APPROVER_ADD("Approver Added"),
    VIEWER_ADD("Viewer Added"),
    PREPARER_REMOVE("Preparer Removed"),
    REVIEWER_REMOVE("Reviewer Removed"),
    APPROVER_REMOVE("Approver Removed"),
    VIEWER_REMOVE("Viewer Removed");

    private String name;

    private WorkflowActionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
