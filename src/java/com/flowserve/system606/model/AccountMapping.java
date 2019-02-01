/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "ACCOUNT_MAPPINGS")
public class AccountMapping implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCT_MAPPING_SEQ")
    @SequenceGenerator(name = "ACCT_MAPPING_SEQ", sequenceName = "ACCT_MAPPING_SEQ", allocationSize = 1)
    @Column(name = "ACCOUNT_MAPPING_ID")
    private Long id;
    @OneToOne
    @JoinColumn(name = "METRIC_TYPE_ID")
    private MetricType metricType;
    @Column(name = "OWNER_ENTITY_TYPE")
    private String ownerEntityType;
    @Column(name = "REVENUE_METHOD")
    private RevenueMethod revenueMethod;
    @OneToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;
    @Column(name = "IS_INFORMATIONAL")
    private boolean informational;

    public AccountMapping() {
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public String getOwnerEntityType() {
        return ownerEntityType;
    }

    public void setOwnerEntityType(String ownerEntityType) {
        this.ownerEntityType = ownerEntityType;
    }

    public RevenueMethod getRevenueMethod() {
        return revenueMethod;
    }

    public void setRevenueMethod(RevenueMethod revenueMethod) {
        this.revenueMethod = revenueMethod;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isInformational() {
        return informational;
    }

    public void setInformational(boolean informational) {
        this.informational = informational;
    }

}
