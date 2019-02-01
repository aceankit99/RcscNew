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
@Table(name = "BUSINESS_PLATFORMS")
public class BusinessPlatform implements Comparable<BusinessPlatform>, Serializable {

    private static final long serialVersionUID = 6624363756253629479L;
    private static final Logger LOG = Logger.getLogger(BusinessPlatform.class.getName());

    @Id
    @Column(name = "BUSINESS_PLATFORM_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CODE")
    private String code;

//    @OneToOne
//    @JoinColumn(name = "NAME")
//    private BusinessUnit businessUnit;
    public BusinessPlatform() {
    }

    @Override
    public int compareTo(BusinessPlatform obj) {
        return this.name.compareTo(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BusinessPlatform) {
            return this.id.equals(((BusinessPlatform) obj).getId());
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

//    public BusinessUnit getBusinessUnit() {
//        return businessUnit;
//    }
//
//    public void setBusinessUnit(BusinessUnit businessUnit) {
//        this.businessUnit = businessUnit;
//    }
}
