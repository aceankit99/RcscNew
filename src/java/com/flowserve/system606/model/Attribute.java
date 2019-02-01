/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;

/**
 *
 * @author shubhamv
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "VALUE_TYPE")
@Table(name = "ATTRIBUTES",
        indexes = {
            @Index(name = "IDX_ATTRIBUTE_SET", columnList = "ATTRIBUTE_SET_ID", unique = false)})
public abstract class Attribute<T> extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ATTRIBUTE_SEQ")
    @SequenceGenerator(name = "ATTRIBUTE_SEQ", sequenceName = "ATTRIBUTE_SEQ", allocationSize = 100)
    @Column(name = "ATTRIBUTE_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "ATTRIBUTE_TYPE_ID")
    private AttributeType attributeType;
    @OneToOne
    @JoinColumn(name = "PERIOD_ID")
    private FinancialPeriod financialPeriod;
    @OneToOne
    @JoinColumn(name = "CONTRACT_VERSION_ID")
    private ContractVersion contractVersion;
    @OneToOne
    @JoinColumn(name = "ATTRIBUTE_SET_ID")
    private AttributeSet attributeSet;
    @OneToOne
    @JoinColumn(name = "CREATED_BY_ID")
    private User createdBy;
    @Temporal(TIMESTAMP)
    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;
    @Column(name = "IS_VALID")
    private boolean valid;
    @Column(name = "MESSAGE", length = 2048)
    private String message;

    public abstract T getValue();

    public abstract void setValue(T value);

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttributeType getMetricType() {
        return attributeType;
    }

    public void setMetricType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public ContractVersion getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(ContractVersion contractVersion) {
        this.contractVersion = contractVersion;
    }

    public AttributeSet getAttributeSet() {
        return attributeSet;
    }

    public void setAttributeSet(AttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FinancialPeriod getFinancialPeriod() {
        return financialPeriod;
    }

    public void setFinancialPeriod(FinancialPeriod financialPeriod) {
        this.financialPeriod = financialPeriod;
    }
}
