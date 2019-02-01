package com.flowserve.system606.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "APPROVAL_ROLES")
public class ApprovalRole implements Comparable<ApprovalRole>, Serializable {

    @Transient
    private static final long serialVersionUID = 1819876545112898428L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVAL_ROLE_ID")
    private Long id;
    @Column(name = "APPROVAL_ROLE_NAME", nullable = false, length = 2048)
    private String name;
    @Column(name = "APPROVAL_LEVEL")
    private int hierarchyLevel;

    public ApprovalRole() {
    }

    public ApprovalRole(String name, int hierarchyLevel) {
        super();
        this.name = name;
        this.hierarchyLevel = hierarchyLevel;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApprovalRole) {
            return this.id.equals(((ApprovalRole) obj).getId());
        }
        return false;
    }

    @Override
    public int compareTo(ApprovalRole obj) {
        //return Double.compare(this.hierarchyLevel, obj.hierarchyLevel);
        return this.id.compareTo(obj.getId());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
