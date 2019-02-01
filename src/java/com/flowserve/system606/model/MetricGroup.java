/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author shubhamv
 */
@Entity
@Table(name = "METRIC_GROUP")
public class MetricGroup extends BaseEntity<Long> implements Comparable<MetricGroup>, Serializable {

    private static final long serialVersionUID = -8382719960002472187L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "METRIC_GROUP_SEQ")
    @SequenceGenerator(name = "METRIC_GROUP_SEQ", sequenceName = "METRIC_GROUP_SEQ", allocationSize = 1)
    @Column(name = "METRIC_GROUP_ID")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "DIRECTION")
    private MetricDirection direction;
    @Column(name = "CODE")
    private String code;
    @Column(name = "OWNER_ENTITY_TYPE")
    private String ownerEntityType;
    @Column(name = "METRIC_CURRENCY_TYPE")
    private CurrencyType inputCurrencyType;
    @Column(name = "COLUMN_GROUP")
    private String columnGroup;
    @Column(name = "GROUP_NAME")
    private String groupName;  // for grouping tabs in calc review screens
    @Column(name = "GROUP_POSITION")
    private int groupPosition;
    @Column(name = "METRIC_POSITION")
    private int metricPosition;
    @Column(name = "IS_PRIOR")
    private boolean prior;
    @Column(name = "IS_ACTIVE")
    private boolean active;   // maybe redundant
    @OneToOne
    @JoinColumn(name = "METRIC_TYPE_ID")
    private MetricType metricType;
    @Column(name = "IS_EDITABLE")
    private boolean editable;

    public MetricGroup() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public int compareTo(MetricGroup o) {
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetricGroup) {
            return this.code.equals(((MetricGroup) obj).getCode());
        }
        return false;
    }

    public MetricDirection getDirection() {
        return direction;
    }

    public void setDirection(MetricDirection direction) {
        this.direction = direction;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOwnerEntityType() {
        return ownerEntityType;
    }

    public void setOwnerEntityType(String ownerEntityType) {
        this.ownerEntityType = ownerEntityType;
    }

    public CurrencyType getInputCurrencyType() {
        return inputCurrencyType;
    }

    public void setInputCurrencyType(CurrencyType inputCurrencyType) {
        this.inputCurrencyType = inputCurrencyType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupPosition() {
        return groupPosition;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    public int getMetricPosition() {
        return metricPosition;
    }

    public void setMetricPosition(int metricPosition) {
        this.metricPosition = metricPosition;
    }

    public boolean isPrior() {
        return prior;
    }

    public void setPrior(boolean prior) {
        this.prior = prior;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getColumnGroup() {
        return columnGroup;
    }

    public void setColumnGroup(String columnGroup) {
        this.columnGroup = columnGroup;
    }
}
