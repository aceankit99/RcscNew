/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CONTRACT_VERSION")
public class ContractVersion implements Comparable<ContractVersion>, Serializable {

    private static final long serialVersionUID = -1990764230607265489L;
    private static final Logger LOG = Logger.getLogger(Contract.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTRACT_VERSION_SEQ")
    @SequenceGenerator(name = "CONTRACT_VERSION_SEQ", sequenceName = "CONTRACT_VERSION_SEQ", allocationSize = 50)
    @Column(name = "CONTRACT_VERSION_ID")
    private Long id;
    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User submittedBy;
    @Column(name = "CONTRACT_ENTRY_DATE")
    private LocalDate contractEntryDate;
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contractVersion")
    private List<ContractAttachment> contractAttachment = new ArrayList<ContractAttachment>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contractVersion")
    private List<ContractConsideration> contractConsideration = new ArrayList<ContractConsideration>();
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contractVersion")
    private List<VariableConsideration> variableConsideration = new ArrayList<VariableConsideration>();
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contractVersion")
    private List<LineItems> lineItems = new ArrayList<LineItems>();
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contractVersion")
    private List<ContractPerformanceObligation> contractPerformanceObligation = new ArrayList<ContractPerformanceObligation>();

    public ContractVersion() {
    }

    @Override
    public int compareTo(ContractVersion obj) {
        return this.id.compareTo(obj.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public LocalDate getContractEntryDate() {
        return contractEntryDate;
    }

    public void setContractEntryDate(LocalDate contractEntryDate) {
        this.contractEntryDate = contractEntryDate;
    }

    public List<ContractAttachment> getContractAttachment() {
        return contractAttachment;
    }

    public List<ContractConsideration> getContractConsideration() {
        return contractConsideration;
    }

    public void setContractConsideration(List<ContractConsideration> contractConsideration) {
        this.contractConsideration = contractConsideration;
    }

    public List<VariableConsideration> getVariableConsideration() {
        return variableConsideration;
    }

    public void setVariableConsideration(List<VariableConsideration> variableConsideration) {
        this.variableConsideration = variableConsideration;
    }

    public List<LineItems> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItems> lineItems) {
        this.lineItems = lineItems;
    }

    public List<ContractPerformanceObligation> getContractPerformanceObligation() {
        return contractPerformanceObligation;
    }

    public void setContractPerformanceObligation(List<ContractPerformanceObligation> contractPerformanceObligation) {
        this.contractPerformanceObligation = contractPerformanceObligation;
    }

}
