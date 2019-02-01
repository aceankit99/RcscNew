/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;
import javax.persistence.Transient;

@Entity
@Table(name = "CONTRACT_VERSION")
public class ContractVersion extends BaseEntity<Long> implements MetricStore, Measurable, Comparable<ContractVersion>, Serializable {

    private static final long serialVersionUID = -1990764230607265489L;
    private static final Logger LOG = Logger.getLogger(Contract.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTRACT_VERSION_SEQ")
    @SequenceGenerator(name = "CONTRACT_VERSION_SEQ", sequenceName = "CONTRACT_VERSION_SEQ", allocationSize = 50)
    @Column(name = "CONTRACT_VERSION_ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "LONG_DESC")
    private String longDescription;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CONTRACT_TYPE_ID")
    private ContractType contractType;
    @ManyToOne
    @JoinColumn(name = "REPORTING_UNIT_ID")
    private ReportingUnit reportingUnit;
    @OneToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;
    @OneToOne
    @JoinColumn(name = "BUSINESS_UNIT_ID")
    private BusinessUnit BusinessUnit;
    @Column(name = "CUSOTMER_PURCHASE_ORDER_NUM")
    private String customerPurchaseOrderNumber;
    @Column(name = "SALES_ORDER_NUM")
    private String salesOrderNumber;
    @Column(name = "TOTAL_TRANSACTION_PRICE")
    private BigDecimal totalTransactionPrice;
    @Column(name = "CONTRACT_CURRENCY")
    private Currency contractCurrency;
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
    @Column(name = "IS_ACTIVE")
    private boolean active;
    @OneToOne
    @JoinColumn(name = "SALES_DESTINATION_COUNTRY_ID")
    private Country salesDestinationCountry;
    @Column(name = "BOOKING_DATE")
    private LocalDate bookingDate;
    @Transient
    private boolean valid = true;
    @Column(name = "WORKFLOW_STATUS")
    private WorkflowStatus workflowStatus;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contractVersion")
    @OrderBy("id ASC")
    private List<PerformanceObligation> performanceObligations = new ArrayList<PerformanceObligation>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "CONTRACT_VERSION_METRIC_SET", joinColumns = @JoinColumn(name = "CONTRACT_VERSION_ID"), inverseJoinColumns = @JoinColumn(name = "METRIC_SET_ID"))
    @MapKeyJoinColumn(name = "PERIOD_ID")
    private Map<FinancialPeriod, MetricSet> periodMetricSetMap = new HashMap<FinancialPeriod, MetricSet>();

//    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "attributeSet")
//    @MapKeyJoinColumn(name = "MAP_METRIC_TYPE_ID")
//    private Map<AttributeType, Attribute> attributeMap = new HashMap<AttributeType, Attribute>();
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ATTRIBUTE_SET_ID")
    private AttributeSet attributeSet;

    @Column(name = "PROJECT_NAME")
    private String projectName;
    @Column(name = "SAP_NUM")
    private String sapNumber;
    @Column(name = "PURCHASE_ORDER_DATE")
    private LocalDate purchaseOrderDate;
    @Column(name = "FLS_SENT_DATE")
    private LocalDate flsSentDate;

    public ContractVersion() {
    }

    //
    public boolean attributeExistsForVersion(AttributeType attributeType) {
        Logger.getLogger(Contract.class.getName()).log(Level.INFO, "attributeType : " + attributeType.getCode());
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
    public int compareTo(ContractVersion obj) {
        return this.id.compareTo(obj.getId());
    }

    public List<PerformanceObligation> getPobsByRevenueMethod(RevenueMethod revenueMethod) {
        List<PerformanceObligation> pobs = new ArrayList<PerformanceObligation>();

        for (PerformanceObligation pob : performanceObligations) {
            if (revenueMethod.equals(pob.getRevenueMethod())) {
                pobs.add(pob);
            }
        }

        return pobs;
    }

    public boolean metricSetExistsForPeriod(FinancialPeriod period) {
        return periodMetricSetMap.get(period) != null;
    }

    public boolean metricExistsForPeriod(FinancialPeriod period, MetricType metricType) {
        return periodMetricSetMap.get(period).getTypeMetricMap().get(metricType) != null;
    }

    public MetricSet initializeMetricSetForPeriod(FinancialPeriod period) {
        MetricSet metricSet = new MetricSet();
        periodMetricSetMap.put(period, metricSet);
        return metricSet;
    }

    public Metric getPeriodMetric(FinancialPeriod period, MetricType metricType) {
        return periodMetricSetMap.get(period).getTypeMetricMap().get(metricType);
    }

    public Metric initializeMetricForPeriod(FinancialPeriod period, MetricType metricType) {
        Metric metric = null;
        try {
            if (metricType.isContractLevel()) {
                Class<?> clazz = Class.forName(MetricType.PACKAGE_PREFIX + metricType.getMetricClass());
                metric = (Metric) clazz.newInstance();
                metric.setMetricType(metricType);
                metric.setCreationDate(LocalDateTime.now());
                metric.setValid(true);
                metric.setFinancialPeriod(period);
                metric.setMetricSet(periodMetricSetMap.get(period));
                periodMetricSetMap.get(period).getTypeMetricMap().put(metricType, metric);
            }
        } catch (Exception e) {
            Logger.getLogger(Contract.class.getName()).log(Level.SEVERE, "Severe exception initializing metricTypeId: " + metricType.getId(), e);
            throw new IllegalStateException("Severe exception initializing metricTypeId: " + metricType.getId(), e);
        }

        if (metric == null) {
            Logger.getLogger(Contract.class.getName()).log(Level.FINER, "Null metric at contract level!:  " + metricType.getCode());
        }
        return metric;
    }

    public Currency getLocalCurrency() {
        return this.getReportingUnit().getLocalCurrency();
    }

    public Currency getReportingCurrency() {
        return this.getReportingUnit().getCompany().getReportingCurrency();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(ReportingUnit reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomerPurchaseOrderNumber() {
        return customerPurchaseOrderNumber;
    }

    public void setCustomerPurchaseOrderNumber(String customerPurchaseOrderNumber) {
        this.customerPurchaseOrderNumber = customerPurchaseOrderNumber;
    }

    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }

    public void setSalesOrderNumber(String salesOrderNumber) {
        this.salesOrderNumber = salesOrderNumber;
    }

    public BigDecimal getTotalTransactionPrice() {
        return totalTransactionPrice;
    }

    public void setTotalTransactionPrice(BigDecimal totalTransactionPrice) {
        this.totalTransactionPrice = totalTransactionPrice;
    }

    public List<PerformanceObligation> getPerformanceObligations() {
        return performanceObligations;
    }

    public List<Measurable> getChildMeasurables() {
        return new ArrayList<Measurable>(performanceObligations);
    }

    public Currency getContractCurrency() {
        return contractCurrency;
    }

    public void setContractCurrency(Currency contractCurrency) {
        this.contractCurrency = contractCurrency;
    }

//    public List<BillingEvent> getBillingEvents() {
//        return billingEvents;
//    }
    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public Country getSalesDestinationCountry() {
        return salesDestinationCountry;
    }

    public void setSalesDestinationCountry(Country salesDestinationCountry) {
        this.salesDestinationCountry = salesDestinationCountry;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isForeign() {
        return !this.getContractCurrency().equals(this.getLocalCurrency());
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public WorkflowStatus getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkflowStatus workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Map<FinancialPeriod, MetricSet> getPeriodMetricSetMap() {
        return periodMetricSetMap;
    }

    public void setPeriodMetricSetMap(Map<FinancialPeriod, MetricSet> periodMetricSetMap) {
        this.periodMetricSetMap = periodMetricSetMap;
    }

    public AttributeSet getAttributeSet() {
        return attributeSet;
    }

    public void setAttributeSet(AttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }

    public BusinessUnit getBusinessUnit() {
        return BusinessUnit;
    }

    public void setBusinessUnit(BusinessUnit BusinessUnit) {
        this.BusinessUnit = BusinessUnit;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSapNumber() {
        return sapNumber;
    }

    public void setSapNumber(String sapNumber) {
        this.sapNumber = sapNumber;
    }

    public LocalDate getPurchaseOrderDate() {
        return purchaseOrderDate;
    }

    public void setPurchaseOrderDate(LocalDate purchaseOrderDate) {
        this.purchaseOrderDate = purchaseOrderDate;
    }

    public LocalDate getFlsSentDate() {
        return flsSentDate;
    }

    public void setFlsSentDate(LocalDate flsSentDate) {
        this.flsSentDate = flsSentDate;
    }

}
