/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "REPORTING_GROUPS")
public class ReportingGroup implements Comparable<ReportingGroup>, Serializable {

    private static final long serialVersionUID = 4627681309882791710L;
    private static final Logger LOG = Logger.getLogger(ReportingGroup.class.getName());

    @Id
    @Column(name = "REPORTING_GROUP_ID")
    private Long id;

    private String name;

    @OneToMany  // TODO - Need to decide whether to use OneToMany or ManyToOne on the child side
    @JoinColumn(name = "REPORTING_GROUP_ID")
    private List<ReportingUnit> reportingUnits = new ArrayList<ReportingUnit>();

    public ReportingGroup() {
    }

    @Override
    public int compareTo(ReportingGroup obj) {
        return this.name.compareTo(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReportingGroup) {
            return this.id.equals(((ReportingGroup) obj).getId());
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReportingUnit> getReportingUnits() {
        return reportingUnits;
    }
}
