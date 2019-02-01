package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "APPROVER_SETS")
public class ApproverSet implements Comparable<ApproverSet>, Serializable {

    @Transient
    private static final long serialVersionUID = 1819239349712898428L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVER_SET_ID")
    private Long id;
    @OneToMany
    @JoinTable(name = "APPROVER_SET_USER", joinColumns = @JoinColumn(name = "APPROVER_SET_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    private List<User> users = new ArrayList<User>();
    @JoinColumn(name = "APPROVAL_ROLE_ID")
    private ApprovalRole approvalRole;

    public ApproverSet() {
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApproverSet) {
            return this.id.equals(((ApproverSet) obj).getId());
        }
        return false;
    }

    @Override
    public int compareTo(ApproverSet obj) {
        return this.id.compareTo(obj.getId());
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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
