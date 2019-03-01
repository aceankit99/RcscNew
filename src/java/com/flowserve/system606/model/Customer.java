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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CUSTOMERS")
public class Customer implements Comparable<Customer>, Serializable {

    private static final long serialVersionUID = -6168365395602277865L;
    private static final Logger LOG = Logger.getLogger(Customer.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTOMER_SEQ")
    @SequenceGenerator(name = "CUSTOMER_SEQ", sequenceName = "CUSTOMER_SEQ", allocationSize = 50)
    @Column(name = "CUSTOMER_ID")
    private Long id;

    @Column(name = "CUSTOMER_NAME")
    private String name;

    @Column(name = "LEGAL_CUSTOMER_NAME")
    private String legalName;

    @Column(name = "CUSTOMER_NUMBER")
    private String number;

    @ManyToOne
    @JoinColumn(name = "REPORTING_UNIT_ID")
    private ReportingUnit reportingUnit;
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Customer parentCustomer;

    @Column(name = "LEGACY_ID")
    private Long legacyId;

    @Column(name = "MASTER")
    private boolean master;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCustomer")
    private List<Customer> childCustomers = new ArrayList<Customer>();

    public Customer() {
    }

    @Override
    public int compareTo(Customer obj) {
        return this.name.compareTo(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Customer) {
            return this.id.equals(((Customer) obj).getId());
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

//    public FinancialSystem getFinancialSystem() {
//        return financialSystem;
//    }
//
//    public void setFinancialSystem(FinancialSystem financialSystem) {
//        this.financialSystem = financialSystem;
//    }
    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(ReportingUnit reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public Customer getParentCustomer() {
        return parentCustomer;
    }

    public void setParentCustomer(Customer parentCustomer) {
        this.parentCustomer = parentCustomer;
    }

    public List<Customer> getChildCustomers() {
        return childCustomers;
    }

    public void setChildCustomers(List<Customer> childCustomers) {
        this.childCustomers = childCustomers;
    }

    public Long getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Long legacyId) {
        this.legacyId = legacyId;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }
}
