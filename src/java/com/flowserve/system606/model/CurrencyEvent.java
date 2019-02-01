package com.flowserve.system606.model;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BILLING")
@AttributeOverride(name = "value", column = @Column(name = "DECIMAL_VALUE", precision = 38, scale = 14))
public class CurrencyEvent extends Event<BigDecimal> {

    private static final Logger logger = Logger.getLogger(CurrencyEvent.class.getName());

    private BigDecimal value;
    @Column(name = "LC_VALUE", precision = 38, scale = 14)
    private BigDecimal lcValue;
    @Column(name = "CC_VALUE", precision = 38, scale = 14)
    private BigDecimal ccValue;
    @Column(name = "RC_VALUE", precision = 38, scale = 14)
    private BigDecimal rcValue;

    public CurrencyEvent() {
    }

    public BigDecimal getValue() {
        switch (getEventType().getEventCurrencyType()) {
            case LOCAL:
                return lcValue;
            case CONTRACT:
                return ccValue;
            case REPORTING:
                return rcValue;
            default:
                Logger.getLogger(CurrencyEvent.class.getName()).log(Level.INFO, "Error: Event type: " + getEventType().getId() + " CurrencyType: " + getEventType().getEventCurrencyType());
                throw new IllegalArgumentException("Invalid CurrencyType");
        }
    }

    public void setValue(BigDecimal value) {
        switch (getEventType().getEventCurrencyType()) {
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
                Logger.getLogger(CurrencyEvent.class.getName()).log(Level.INFO, "Error: Event type: " + getEventType().getId() + " CurrencyType: " + getEventType().getEventCurrencyType());
                throw new IllegalArgumentException("Invalid CurrencyType");
        }
    }

    public boolean isLocalCurrencyEvent() {
        return this.getEventType().getEventCurrencyType().equals(CurrencyType.LOCAL);
    }

    public boolean isContractCurrencyEvent() {
        return this.getEventType().getEventCurrencyType().equals(CurrencyType.CONTRACT);
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

    @Override
    public int compareTo(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
