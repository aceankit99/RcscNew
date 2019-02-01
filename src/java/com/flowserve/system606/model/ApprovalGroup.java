/** *****************************************************************************
 * COPYRIGHT (C) 2012 Kelly Graves. All Rights Reserved.
 ***************************************************************************** */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.Table;

@Entity
@Table(name = "APPROVAL_GROUPS")
public class ApprovalGroup implements Comparable<ApprovalGroup>, Serializable {

    private static final long serialVersionUID = 8992803456112898428L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVAL_GROUP_ID")
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "APPROVAL_GROUP_DESC")
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID")
    private BusinessUnit businessUnit;

    @Column(name = "AMOUNT_LOWER_LIMIT")
    private Long amountLowerLimit;
    @Column(name = "AMOUNT_UPPER_LIMIT")
    private Long amountUpperLimit;

    private String approvalGroupType;
    @Column(name = "IS_ACTIVE")
    private boolean active;
    @Column(name = "IS_FORCE_INCLUDE")
    private boolean forceInclude = false;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "APPROVAL_GROUP_USERS", joinColumns = @JoinColumn(name = "GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> users = new ArrayList<User>();
    @JoinColumn(name = "RESOURCE_TYPE_ID")
    private ApprovalRole approvalRole;
    @Column(name = "PRIORITY", nullable = false)
    private Long priority = new Long(0);

    ;

	public ApprovalGroup() {
    }

    public ApprovalGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApprovalGroup) {
            return ((ApprovalGroup) obj).getId().equals(this.id);
        }
        return false;
    }

    @Override
    public int compareTo(ApprovalGroup o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getAmountRangeDescription() {
        StringBuilder range = new StringBuilder();

        if (amountLowerLimit == null) {
            range.append("0");
        } else {
            range.append(amountLowerLimit);
        }

        range.append(" - ");

        if (amountUpperLimit == null) {
            range.append("");
        } else {
            range.append(amountUpperLimit);
        }

        return range.toString();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getApprovalGroupType() {
        return approvalGroupType;
    }

    public void setApprovalGroupType(String approvalGroupType) {
        this.approvalGroupType = approvalGroupType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApprovalRole getApprovalRole() {
        return approvalRole;
    }

    public void setApprovalRole(ApprovalRole approvalRole) {
        this.approvalRole = approvalRole;
    }

    public boolean isRoleBased() {
        return this.approvalRole != null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isForceInclude() {
        return forceInclude;
    }

    public void setForceInclude(boolean forceInclude) {
        this.forceInclude = forceInclude;
    }
}
