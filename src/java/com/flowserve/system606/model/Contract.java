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
@Table(name = "CONTRACTS")
public class Contract extends BaseEntity<Long> implements MetricStore, EventStore, Measurable, Comparable<Contract>, Serializable {

    private static final long serialVersionUID = -1990764230607265489L;
    private static final Logger LOG = Logger.getLogger(Contract.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTRACT_SEQ")
    @SequenceGenerator(name = "CONTRACT_SEQ", sequenceName = "CONTRACT_SEQ", allocationSize = 50)
    @Column(name = "CONTRACT_ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "LONG_DESC")
    private String longDescription;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CONTRACT_TYPE_ID")
    private ContractType contractType;
    @Column(name = "CURRENT_VERSION")
    private String currentVersion;
    @ManyToOne
    @JoinColumn(name = "REPORTING_UNIT_ID")
    private ReportingUnit reportingUnit;
    @OneToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;
    @OneToOne
    @JoinColumn(name = "BUSINESS_UNIT_ID")
    private BusinessUnit businessUnit;
    @Column(name = "CUSOTMER_PURCHASE_ORDER_NUM")
    private String customerPurchaseOrderNumber;
    @Column(name = "SALES_ORDER_NUM")
    private String salesOrderNumber;
    @Column(name = "FLS_SALES_ORDER_NUM")
    private String flsSalesOrderNumber;
    @Column(name = "FLS_SAP_NUMBER")
    private String flsSAPNumber;
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

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "contract")
    @OrderBy("id ASC")
    private List<PerformanceObligation> performanceObligations = new ArrayList<PerformanceObligation>();

    // Add to DB manually: ALTER TABLE contract_metric_set ADD CONSTRAINT contract_period_unique UNIQUE (contract_id, period_id);
    // Or figure out how to add this constraint to annotations.
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "CONTRACT_METRIC_SET", joinColumns = @JoinColumn(name = "CONTRACT_ID"), inverseJoinColumns = @JoinColumn(name = "METRIC_SET_ID"))
    @MapKeyJoinColumn(name = "PERIOD_ID")
    private Map<FinancialPeriod, MetricSet> periodMetricSetMap = new HashMap<FinancialPeriod, MetricSet>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "CONTRACT_EVENT_LIST", joinColumns = @JoinColumn(name = "CONTRACT_ID"), inverseJoinColumns = @JoinColumn(name = "EVENT_LIST_ID"))
    @MapKeyJoinColumn(name = "PERIOD_ID")
    private Map<FinancialPeriod, EventList> periodEventListMap = new HashMap<FinancialPeriod, EventList>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "CONTRACT_APPROVAL_REQUEST", joinColumns = @JoinColumn(name = "CONTRACT_ID"), inverseJoinColumns = @JoinColumn(name = "APPROVAL_REQUEST_ID"))
    @MapKeyJoinColumn(name = "PERIOD_ID")
    private Map<FinancialPeriod, WorkflowContext> periodApprovalRequestMap = new HashMap<FinancialPeriod, WorkflowContext>();

   @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
   @JoinTable(name = "CONTRACT_VERSIONS", joinColumns = @JoinColumn(name = "CONTRACT_ID"), inverseJoinColumns = @JoinColumn(name = "VERSION_ID"))
   private Map<String, ContractVersion> contractVersionMap = new HashMap<String, ContractVersion>();
    
   public Contract(){
   }
   
   public Contract(String contractVersion){
       this.currentVersion = contractVersion;
   }
   
    @Override
    public int compareTo(Contract obj) {
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
    
    public List<ContractAttachment> getContractAttachments(){
        return contractVersionMap.get(this.currentVersion).getContractAttachment();
    }
    
    public List<VariableConsideration> getVariableConsiderations(){
        return contractVersionMap.get(this.currentVersion).getVariableConsideration();
    }
    
    public List<ContractConsideration> getContractConsiderations(){
        return contractVersionMap.get(this.currentVersion).getContractConsideration();
    }
    
    public List<LineItems> getLineItems(){
        return contractVersionMap.get(this.currentVersion).getLineItems();
    }
    
    public List<ContractPerformanceObligation> getContractPerformanceObligations(){
        return contractVersionMap.get(this.currentVersion).getContractPerformanceObligation();
    }

    public List<Event> getAllEventsByEventType(EventType eventType) {
        List<Event> events = new ArrayList<Event>();

        for (FinancialPeriod period : periodEventListMap.keySet()) {
            for (Event event : periodEventListMap.get(period).getEventList()) {
                if (eventType.equals(event.getEventType())) {
                    events.add(event);
                }
            }
        }

        return events;
    }

    public EventList getPeriodEventList(FinancialPeriod period) {
        return periodEventListMap.get(period);
    }

    public List<Event> getAllEventsByPeriodAndEventType(FinancialPeriod period, EventType eventType) {
        List<Event> events = new ArrayList<Event>();

        if (periodEventListMap.get(period) == null) {
            return events;
        }

        for (Event event : periodEventListMap.get(period).getEventList()) {
            if (eventType.equals(event.getEventType())) {
                events.add(event);
            }
        }

        return events;
    }

    public List<Event> getAllEventsByEventTypeAndNumber(EventType eventType, String number) {
        List<Event> events = new ArrayList<Event>();

        for (FinancialPeriod period : periodEventListMap.keySet()) {
            for (Event event : periodEventListMap.get(period).getEventList()) {
                if (eventType.equals(event.getEventType()) && number.equals(event.getNumber())) { //event.getNumber().equals(number)
                    events.add(event);
                }
            }
        }

        return events;
    }
    
    public Map<String, ContractVersion> getContractVersionMap() {
        return contractVersionMap;
    }

    public void setContractVersionMap(Map<String, ContractVersion> contractVersionMap) {
        this.contractVersionMap = contractVersionMap;
    }
    
    public WorkflowContext getPeriodApprovalRequest(FinancialPeriod period) {
        return periodApprovalRequestMap.get(period);
    }

    public void putPeriodApprovalRequest(FinancialPeriod period, WorkflowContext approvalRequest) {
        periodApprovalRequestMap.put(period, approvalRequest);
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

    public boolean eventListExistsForPeriod(FinancialPeriod period) {
        return periodEventListMap.get(period) != null;
    }

    public EventList initializeEventListForPeriod(FinancialPeriod period) {
        EventList eventList = new EventList();
        periodEventListMap.put(period, eventList);

        return eventList;
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

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
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
    
    
    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getFlsSalesOrderNumber() {
        return flsSalesOrderNumber;
    }

    public void setFlsSalesOrderNumber(String flsSalesOrderNumber) {
        this.flsSalesOrderNumber = flsSalesOrderNumber;
    }

    public String getFlsSAPNumber() {
        return flsSAPNumber;
    }

    public void setFlsSAPNumber(String flsSAPNumber) {
        this.flsSAPNumber = flsSAPNumber;
    }

}
