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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author constacloud
 */
@Entity
@Table(name = "CONTRACT_CONSIDERATION")
public class ContractConsideration extends Consideration implements Serializable{
    
     private static final long serialVersionUID = -1927267230901912809L;
    private static final Logger LOG = Logger.getLogger(ContractConsideration.class.getName());
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTRACT_CONSIDERATION_SEQ")
    @SequenceGenerator(name = "CONTRACT_CONSIDERATION_SEQ", sequenceName = "CONTRACT_CONSIDERATION_SEQ", allocationSize = 50)
    @Column(name = "CONTRACT_CONSIDERATION_ID")
    private Long id;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "STANDALONE_SELLING_PRICE")
    private BigDecimal standaloneSellingPrice;
    @Column(name = "DOCUMENT_REFERENCE")
    private String documentReference;
    @ManyToOne
    @JoinColumn(name = "CONTRACT_VERSION_ID")
    private ContractVersion contractVersion;
//     @OneToOne
//    @JoinColumn(name = "ID")
//    private Questionnaire questionnaire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getStandaloneSellingPrice() {
        return standaloneSellingPrice;
    }

    public void setStandaloneSellingPrice(BigDecimal standaloneSellingPrice) {
        this.standaloneSellingPrice = standaloneSellingPrice;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }
    
}
