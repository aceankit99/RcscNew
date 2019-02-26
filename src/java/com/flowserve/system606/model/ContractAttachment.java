/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author constacloud
 */
@Entity
@Table(name = "CONTRACT_ATTACHMENT")
public class ContractAttachment implements Comparable<ContractAttachment>, Serializable {

    private static final long serialVersionUID = -1998864230907265809L;
    private static final Logger LOG = Logger.getLogger(ContractAttachment.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTRACT_ATTACHMENT_SEQ")
    @SequenceGenerator(name = "CONTRACT_ATTACHMENT_SEQ", sequenceName = "CONTRACT_ATTACHMENT_SEQ", allocationSize = 50)
    @Column(name = "CONTRACT_ATTACHMENT_ID")
    private Long id;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "URL")
    private String URL;
    @Column(name = "CONTENT_TYPE")
    private String contentType;
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "ATTACHMENT")
    private byte[] attachment;
    @ManyToOne
    @JoinColumn(name = "CONTRACT_VERSION_ID")
    private ContractVersion contractVersion;

    public ContractAttachment() {

    }

    public ContractAttachment(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(ContractAttachment obj) {
        return this.id.compareTo(obj.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContractAttachment) {
            return this.id.equals(((ContractAttachment) obj).getId());
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ContractVersion getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(ContractVersion contractVersion) {
        this.contractVersion = contractVersion;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

}
