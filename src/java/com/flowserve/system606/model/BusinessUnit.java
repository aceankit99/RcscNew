/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "BUSINESS_UNITS")

public class BusinessUnit extends TransientMeasurable<String> implements Comparable<BusinessUnit>, Serializable, Measurable {

    private static final long serialVersionUID = -5428359272300395184L;
    private static final Logger LOG = Logger.getLogger(BusinessUnit.class.getName());

    @Id
    @Column(name = "BUSINESS_UNIT_ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "DESCRIPTION")
    private String description;

    @JoinColumn(name = "PARENT_ID")
    private BusinessUnit parent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "businessUnit")
    private List<ReportingUnit> reportingUnit = new ArrayList<ReportingUnit>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    private List<BusinessUnit> childBusinessUnit = new ArrayList<BusinessUnit>();

    @ManyToOne
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    public BusinessUnit() {
    }

    @Override
    public int compareTo(BusinessUnit obj) {
        return this.name.compareTo(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BusinessUnit) {
            return this.name.equals(((BusinessUnit) obj).getName());
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BusinessUnit getParent() {
        return parent;
    }

    public void setParent(BusinessUnit parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ReportingUnit> getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(List<ReportingUnit> reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public List<BusinessUnit> getChildBusinessUnit() {
        return childBusinessUnit;
    }

    public void setChildBusinessUnit(List<BusinessUnit> childBusinessUnit) {
        this.childBusinessUnit = childBusinessUnit;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public List<Measurable> getChildMeasurables() {
        return new ArrayList<Measurable>(reportingUnit);
    }

    @Override
    public Currency getLocalCurrency() {
        return this.company.getLocalCurrency();
    }

    @Override
    public Currency getContractCurrency() {
        return null;
    }

    @Override
    public Currency getReportingCurrency() {
        return this.company.getReportingCurrency();
    }

}
