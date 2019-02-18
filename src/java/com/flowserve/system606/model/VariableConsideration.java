/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author constacloud
 */
@Entity
@Table(name = "VARIABLE_CONSIDERATION")
public class VariableConsideration extends Consideration implements Serializable{
     private static final long serialVersionUID = -1953260232911912809L;
    private static final Logger LOG = Logger.getLogger(ContractConsideration.class.getName());
    
     @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VARIABLE_CONSIDERATION_SEQ")
    @SequenceGenerator(name = "VARIABLE_CONSIDERATION_SEQ", sequenceName = "VARIABLE_CONSIDERATION_SEQ", allocationSize = 50)
    @Column(name = "VARIABLE_CONSIDERATION_ID")
     private Long id;
     @Column(name = "TYPE")
     private String type;
     @Column(name = "DESCRIPTION")
     private String description;
     @Column(name = "MAX_VC")
     private BigDecimal maxVC;
     @Column(name = "POB_SPECIFIC")
     private BigDecimal pobSpecific;
     @Column(name = "PROB_PERCENTAGE")
     private BigDecimal probPercentage;
     @Column(name = "LIKELY_AMOUNT")
     private BigDecimal likelyAmount;
     @Column(name = "DOCUMENT_REFERENCE")
     private String documentReference;
     @ManyToOne
    @JoinColumn(name = "CONTRACT_VERSION_ID")
    private ContractVersion contractVersion;
//    @OneToOne
//    @JoinColumn(name = "ID")
//    private Questionnaire questionnaire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getMaxVC() {
        return maxVC;
    }

    public void setMaxVC(BigDecimal maxVC) {
        this.maxVC = maxVC;
    }

    public BigDecimal getPobSpecific() {
        return pobSpecific;
    }

    public void setPobSpecific(BigDecimal pobSpecific) {
        this.pobSpecific = pobSpecific;
    }

    public BigDecimal getProbPercentage() {
        return probPercentage;
    }

    public void setProbPercentage(BigDecimal probPercentage) {
        this.probPercentage = probPercentage;
    }

    public BigDecimal getLikelyAmount() {
        return likelyAmount;
    }

    public void setLikelyAmount(BigDecimal likelyAmount) {
        this.likelyAmount = likelyAmount;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }
    
     
}
