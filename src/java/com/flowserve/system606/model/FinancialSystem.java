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
@Table(name = "FINANCIAL_SYSTEMS")
public class FinancialSystem implements Comparable<FinancialSystem>, Serializable {

    private static final long serialVersionUID = 1495639122863099694L;
    private static final Logger LOG = Logger.getLogger(FinancialSystem.class.getName());

    @Id
    @Column(name = "FINANCIAL_SYSTEM_ID")
    private Long id;

    private String name;
    ;
    private String type;
    private String url;

    public FinancialSystem() {
    }

    @Override
    public int compareTo(FinancialSystem obj) {
        return this.name.compareTo(obj.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FinancialSystem) {
            return this.id.equals(((FinancialSystem) obj).getId());
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
