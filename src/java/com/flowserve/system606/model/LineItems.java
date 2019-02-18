/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "LINE_ITEMS")
public class LineItems implements Comparable<LineItems>,Serializable{
    
    @Id
    @Column(name = "CONTRACT_ID")
    private String id;
    @Column(name = "ITEM_NAME")
    private String itemName;
    @Column(name = "QUANTITY")
    private Long quantity;
    @Column(name = "UNIT_PRICE")
    private BigDecimal unitPrice;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "STANDALONE_SELLING_PRICE")
    private BigDecimal standaloneSellingPrice;
    @Column(name = "DELIVERY_DATE")
    private LocalDate deliveryDate;
    @Column(name = "COST")
    private BigDecimal cost;
    @Column(name = "SAP_MARGIN")
    private BigDecimal sapMargin;
    @ManyToOne
    @JoinColumn(name = "CONTRACT_VERSION_ID")
    private ContractVersion contractVersion;

    @Override
    public int compareTo(LineItems obj) {
        return this.id.compareTo(obj.getId());
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getSapMargin() {
        return sapMargin;
    }

    public void setSapMargin(BigDecimal sapMargin) {
        this.sapMargin = sapMargin;
    }
    
    
}
