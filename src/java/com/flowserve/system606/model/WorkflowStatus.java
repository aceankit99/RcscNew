package com.flowserve.system606.model;

/**
 *
 * @author kgraves
 */
public enum WorkflowStatus {
    INITIALIZED("Not Started", "Not Started", "images/black_rounded.png", ""),
    DRAFT("Draft", "In Progress", "images/grey_rounded.png", ""),
    PREPARED("Prepared", "Pending Review", "images/yellow_rounded.png", ""),
    REVIEWED("Reviewed", "Pending Approval", "images/orange_rounded.png", ""),
    REJECTED("Rejected", "Rejected", "images/red_rounded.png", ""),
    APPROVED("Approved", "Approved", "images/green_rounded.png", ""),
    ACTIVE("Active", "Active", "images/green_rounded.png", ""),
    CLOSED("Closed", "Closed via contract closure date", "images/yellow_rounded.png", ""),
    COMPLETED("Completed", "Completed via contract completion date", "images/grey_rounded.png", ""),
    ARCHIVED("Archived", "Archived no longer visible in RCS", "images/black_rounded.png", "");

    private String name;
    private String description;
    private String icon;
    private String style;

    private WorkflowStatus(String name, String description, String icon, String style) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.style = style;
    }

    private WorkflowStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getStyle() {
        return style;
    }
}
