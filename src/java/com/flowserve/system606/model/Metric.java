package com.flowserve.system606.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;

// Constraint added to DB.  Figure out how to add to annotation.
// ALTER TABLE metrics ADD CONSTRAINT type_set_unique UNIQUE (metric_type_id, metric_set_id);
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "VALUE_TYPE")
@Table(name = "METRICS",
        indexes = {
            @Index(name = "IDX_METRIC_SET", columnList = "METRIC_SET_ID", unique = false)})
public abstract class Metric<T> extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "METRIC_SEQ")
    @SequenceGenerator(name = "METRIC_SEQ", sequenceName = "METRIC_SEQ", allocationSize = 100)
    @Column(name = "METRIC_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "METRIC_TYPE_ID")
    private MetricType metricType;
    @OneToOne
    @JoinColumn(name = "PERIOD_ID")
    private FinancialPeriod financialPeriod;
    @OneToOne
    @JoinColumn(name = "METRIC_SET_ID")
    private MetricSet metricSet;
    @OneToOne
    @JoinColumn(name = "CREATED_BY_ID")
    private User createdBy;
    @Temporal(TIMESTAMP)
    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;
    @Column(name = "IS_VALID")
    private boolean valid;
    @Column(name = "MESSAGE", length = 2048)
    private String message;

    public Metric() {
    }

    public abstract T getValue();

    public abstract void setValue(T value);

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FinancialPeriod getFinancialPeriod() {
        return financialPeriod;
    }

    public void setFinancialPeriod(FinancialPeriod financialPeriod) {
        this.financialPeriod = financialPeriod;
    }

    public MetricSet getMetricSet() {
        return metricSet;
    }

    public void setMetricSet(MetricSet metricSet) {
        this.metricSet = metricSet;
    }
}
