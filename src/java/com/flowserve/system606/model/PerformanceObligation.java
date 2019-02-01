/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import com.flowserve.system606.service.PerformanceObligationService;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "PERFORMANCE_OBLIGATIONS")
public class PerformanceObligation extends BaseEntity<Long> implements MetricStore, Measurable, Comparable<PerformanceObligation>, Serializable {

    private static final long serialVersionUID = 4995349370717535419L;
    private static final Logger LOG = Logger.getLogger(PerformanceObligation.class.getName());

    @Id
    @Column(name = "POB_ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "REVENUE_METHOD")
    private RevenueMethod revenueMethod;
    @ManyToOne
    @JoinColumn(name = "CONTRACT_ID")
    private Contract contract;
    @Column(name = "IS_ACTIVE")
    private boolean active = true;
    @Column(name = "IS_VALID")
    private boolean valid = true;
    @OneToOne
    @JoinColumn(name = "CREATED_BY_ID")
    private User createdBy;
    @Temporal(TIMESTAMP)
    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;
    @OneToOne
    @JoinColumn(name = "LAST_UPDATED_BY_ID")
    private User lastUpdatedBy;
    @Temporal(TIMESTAMP)
    @Column(name = "LAST_UPDATE_DATE")
    private LocalDateTime lastUpdateDate;
    // Add to DB manually: ALTER TABLE pob_metric_set ADD CONSTRAINT pob_period_unique UNIQUE (pob_id, period_id);
    // Or figure out how to add this constraint to annotations.
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "POB_METRIC_SET", joinColumns = @JoinColumn(name = "POB_ID"), inverseJoinColumns = @JoinColumn(name = "METRIC_SET_ID"))
    @MapKeyJoinColumn(name = "PERIOD_ID")
    private Map<FinancialPeriod, MetricSet> periodMetricSetMap = new HashMap<FinancialPeriod, MetricSet>();

    @OneToOne
    @JoinColumn(name = "ATTRIBUTE_SET_ID")
    private AttributeSet attributeSet;

    @ManyToOne
    @JoinColumn(name = "CONTRACT_VERSION_ID")
    private ContractVersion contractVersion;

    public PerformanceObligation() {
    }

    //
    public boolean attributeExistsForVersion(AttributeType attributeType) {
        return attributeSet.getTypeMetricMap().get(attributeType) != null;
    }

    public Attribute getVersionAttribute(AttributeType attributeType) {
        return attributeSet.getTypeMetricMap().get(attributeType);
    }

    public Attribute initializeAttributeForVersion(AttributeType attributeType) {
        Attribute attribute = null;
        try {
            if (attributeType.isContractLevel()) {
                Class<?> clazz = Class.forName(MetricType.PACKAGE_PREFIX + attributeType.getMetricClass());
                attribute = (Attribute) clazz.newInstance();
                attribute.setMetricType(attributeType);
                attribute.setCreationDate(LocalDateTime.now());
                attribute.setValid(true);
                //attribute.setContractVersion(contractVersion);
                attribute.setAttributeSet(attributeSet);
                attributeSet.getTypeMetricMap().put(attributeType, attribute);
                //contractVersionAttributeSetMap.get(contractVersion).getTypeMetricMap().put(attributeType, attribute);
            }
        } catch (Exception e) {
            Logger.getLogger(Contract.class.getName()).log(Level.SEVERE, "Severe exception initializing metricTypeId: " + attributeType.getId(), e);
            throw new IllegalStateException("Severe exception initializing metricTypeId: " + attributeType.getId(), e);
        }

        if (attribute == null) {
            Logger.getLogger(Contract.class.getName()).log(Level.FINER, "Null metric at contract level!:  " + attributeType.getCode());
        }
        return attribute;
    }

    //
    @Override
    public int compareTo(PerformanceObligation obj) {
        return this.id.compareTo(obj.getId());
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public RevenueMethod getRevenueMethod() {
        return revenueMethod;
    }

    public void setRevenueMethod(RevenueMethod revenueMethod) {
        this.revenueMethod = revenueMethod;
    }

    public Metric getPeriodMetric(FinancialPeriod period, MetricType metricType) {
        return periodMetricSetMap.get(period).getTypeMetricMap().get(metricType);
    }

    public List<Measurable> getChildMeasurables() {
        return new ArrayList<Measurable>();
    }

    public MetricSet initializeMetricSetForPeriod(FinancialPeriod period) {
        MetricSet metricSet = new MetricSet();
        periodMetricSetMap.put(period, metricSet);

        return metricSet;
    }

    public Metric initializeMetricForPeriod(FinancialPeriod period, MetricType metricType) {
        Metric metric = null;
        try {
            if (metricType.isPobLevel()) {
                Class<?> clazz = Class.forName(MetricType.PACKAGE_PREFIX + metricType.getMetricClass());
                metric = (Metric) clazz.newInstance();
                metric.setMetricType(metricType);
                metric.setValid(true);
                metric.setCreationDate(LocalDateTime.now());
                metric.setFinancialPeriod(period);
                metric.setMetricSet(periodMetricSetMap.get(period));
                periodMetricSetMap.get(period).getTypeMetricMap().put(metricType, metric);

            }
        } catch (Exception e) {
            Logger.getLogger(PerformanceObligationService.class.getName()).log(Level.SEVERE, "Severe exception initializing metricTypeId: " + metricType.getId(), e);
            throw new IllegalStateException("Severe exception initializing metricTypeId: " + metricType.getId(), e);
        }

        return metric;
    }

    public FinancialPeriod getEarliestPeriod() {
        FinancialPeriod earliestPeriod = null;

        for (FinancialPeriod financialPeriod : periodMetricSetMap.keySet()) {
            if (earliestPeriod == null) {
                earliestPeriod = financialPeriod;
            }
            if (financialPeriod.isAfter(earliestPeriod)) {
                continue;
            } else {
                earliestPeriod = financialPeriod;
            }
        }

        return earliestPeriod;
    }

    public boolean metricSetExistsForPeriod(FinancialPeriod period) {
        return periodMetricSetMap.get(period) != null;
    }

    public boolean metricExistsForPeriod(FinancialPeriod period, MetricType metricType) {
        return periodMetricSetMap.get(period).getTypeMetricMap().get(metricType) != null;
    }

    public Currency getLocalCurrency() {
        return this.getContract().getReportingUnit().getLocalCurrency();
    }

    public Currency getReportingCurrency() {
        return this.getContract().getReportingUnit().getCompany().getReportingCurrency();
    }

    public Currency getContractCurrency() {
        return this.getContract().getContractCurrency();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public AttributeSet getAttributeSet() {
        return attributeSet;
    }

    public void setAttributeSet(AttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }

    public ContractVersion getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(ContractVersion contractVersion) {
        this.contractVersion = contractVersion;
    }
}
