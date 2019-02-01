/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CUSTOMERS")
public class Customer implements Comparable<Customer>, Serializable {

    private static final long serialVersionUID = -6168365395602277865L;
    private static final Logger LOG = Logger.getLogger(Customer.class.getName());

    @Id
    @Column(name = "CUSTOMER_ID")
    private Long id;

    @Column(name = "CUSTOMER_NAME")
    private String name;

    @Column(name = "LEGAL_CUSTOMER_NAME")
    private String legalName;

    @Column(name = "CUSTOMER_NUMBER")
    private String number;

    //@Column(name = "FINANCIAL_SYSTEM_ID")
    //private FinancialSystem financialSystem;
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
}
