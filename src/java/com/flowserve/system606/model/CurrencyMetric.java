package com.flowserve.system606.model;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CURRENCY")
@AttributeOverride(name = "value", column = @Column(name = "DECIMAL_VALUE", precision = 38, scale = 14))
public class CurrencyMetric extends Metric<BigDecimal> {

    private static final Logger logger = Logger.getLogger(CurrencyMetric.class.getName());

    private BigDecimal value;
    @Column(name = "LC_VALUE", precision = 38, scale = 14)
    private BigDecimal lcValue;
    @Column(name = "CC_VALUE", precision = 38, scale = 14)
    private BigDecimal ccValue;
    @Column(name = "RC_VALUE", precision = 38, scale = 14)
    private BigDecimal rcValue;

    public CurrencyMetric() {
    }

    public BigDecimal getValue() {
        switch (getMetricType().getMetricCurrencyType()) {
            case LOCAL:
                return lcValue;
            case CONTRACT:
                return ccValue;
            case REPORTING:
                return rcValue;
            default:
                Logger.getLogger(CurrencyMetric.class.getName()).log(Level.INFO, "Error: Metric type: " + getMetricType().getId() + " CurrencyType: " + getMetricType().getMetricCurrencyType());
                throw new IllegalArgumentException("Invalid CurrencyType");
        }
    }

    public void setValue(BigDecimal value) {
        switch (getMetricType().getMetricCurrencyType()) {
            case LOCAL:
                lcValue = value;
                return;
            case CONTRACT:
                ccValue = value;
                return;
            case REPORTING:
                rcValue = value;
                return;
            default:
                Logger.getLogger(CurrencyMetric.class.getName()).log(Level.INFO, "Error: Metric type: " + getMetricType().getId() + " CurrencyType: " + getMetricType().getMetricCurrencyType());
                throw new IllegalArgumentException("Invalid CurrencyType");
        }
    }

    public boolean isLocalCurrencyMetric() {
        return this.getMetricType().getMetricCurrencyType().equals(CurrencyType.LOCAL);
    }

    public boolean isContractCurrencyMetric() {
        return this.getMetricType().getMetricCurrencyType().equals(CurrencyType.CONTRACT);
    }

    public BigDecimal getLcValue() {
        return lcValue;
    }

    public void setLcValue(BigDecimal lcValue) {
        this.lcValue = lcValue;
    }

    public BigDecimal getCcValue() {
        return ccValue;
    }

    public void setCcValue(BigDecimal ccValue) {
        this.ccValue = ccValue;
    }

    public BigDecimal getRcValue() {
        return rcValue;
    }

    public void setRcValue(BigDecimal rcValue) {
        this.rcValue = rcValue;
    }
}
