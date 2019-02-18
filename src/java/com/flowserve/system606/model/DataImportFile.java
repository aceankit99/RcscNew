/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "DATA_IMPORT_FILES")
public class DataImportFile implements Comparable<DataImportFile>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IMPORT_FILE_SEQ")
    @SequenceGenerator(name = "IMPORT_FILE_SEQ", sequenceName = "IMPORT_FILE_SEQ", allocationSize = 50)
    @Column(name = "DATA_IMPORT_ID")
    private Long id;
    @Column(name = "FILE_NAME")
    private String filename;
    @OneToOne
    @JoinColumn(name = "COMPANY_ID")
    private Company company;
    @Temporal(TIMESTAMP)
    @Column(name = "UPLOAD_DATE")
    private LocalDateTime uploadDate;
    @Column(name = "UPLOADED_BY")
    private String uploadedBy;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "IMPORT_TYPE")
    private String type;

    @ElementCollection
    @CollectionTable(name = "DATA_IMPORT_MESSAGES")
    private List<String> dataImportMessages = new ArrayList<String>();

    public DataImportFile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getDataImportMessages() {
        return dataImportMessages;
    }

    public void setDataImportMessages(List<String> dataImportMessages) {
        this.dataImportMessages = dataImportMessages;
    }

    @Override
    public int compareTo(DataImportFile o) {
        return this.filename.compareTo(o.getFilename());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

}
