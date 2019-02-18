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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author constacloud
 */
@Entity
@Table(name = "CONSIDERATION")
public class Consideration implements Serializable{
    
    private static final long serialVersionUID = -1998867230901512809L;
    private static final Logger LOG = Logger.getLogger(Consideration.class.getName());
    
     @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONSIDERATION_SEQ")
    @SequenceGenerator(name = "CONSIDERATION_SEQ", sequenceName = "CONSIDERATION_SEQ", allocationSize = 50)
    @Column(name = "CONSIDERATION_ID")
    private Long id;
     @Column(name = "NAME")
     private String name;
     @Column(name = "DESCRIPTION")
     private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
     
    
}
