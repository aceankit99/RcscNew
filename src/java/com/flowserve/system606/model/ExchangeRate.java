/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/*
Performance tuning showed that currency rate lookups where a major cause of slowness.
Tried to use Eclipselink query cache, but it does not seem to work, possibly becuase the
resulting object contains relationships.  Ended up implementing an ExchangeRate object
cache in CurrencyService.java.  Leaving the query cache settings in place for now.
 */
@Entity
@Table(name = "EXCHANGE_RATES")
@NamedQueries({
    @NamedQuery(name = "ExchangeRate.findRateByFromToPeriod",
            query = "SELECT er FROM ExchangeRate er WHERE er.financialPeriod = :PERIOD and er.fromCurrency = :FROM and er.toCurrency = :TO",
            hints = {
                @QueryHint(name = "eclipselink.query-results-cache", value = "true")
                ,
                     @QueryHint(name = "eclipselink.query-results-cache.size", value = "1000")}
    )
})
public class ExchangeRate implements Serializable, Comparable<ExchangeRate> {

    private static final long serialVersionUID = -383220321690601009L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXCHANGE_RATE_SEQ")
    @SequenceGenerator(name = "EXCHANGE_RATE_SEQ", sequenceName = "EXCHANGE_RATE_SEQ", allocationSize = 50)
    @Column(name = "EXCHANGE_RATE_ID")
    private Long id;
    @Column(name = "TYPE")
    private String type;
    @Column(name = "FROM_CURRENCY", nullable = false)
    private Currency fromCurrency;
    @Column(name = "TO_CURRENCY", nullable = false)
    private Currency toCurrency;
    @JoinColumn(name = "PERIOD_ID", nullable = false)
    private FinancialPeriod financialPeriod;
    @Column(name = "PERIOD_END_RATE", precision = 38, scale = 14, nullable = false)
    private BigDecimal periodEndRate;
    @Column(name = "MONTHLY_AVG_RATE", precision = 38, scale = 14, nullable = false)
    private BigDecimal monthlyAverageRate;
    @Column(name = "YTD_AVG_RATE", precision = 38, scale = 14, nullable = false)
    private BigDecimal ytdAverageRate;

    public ExchangeRate() {
    }

    public ExchangeRate(String type, Currency fromCurrency, Currency toCurrency, FinancialPeriod financialPeriod, BigDecimal periodEndRate, BigDecimal monthlyAverageRate, BigDecimal ytdAverageRate) {
        this.type = type;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.financialPeriod = financialPeriod;
        this.periodEndRate = periodEndRate;
        this.monthlyAverageRate = monthlyAverageRate;
        this.ytdAverageRate = ytdAverageRate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExchangeRate) {
            return this.id.equals(((ExchangeRate) obj).getId());
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Currency getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(Currency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public Currency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(Currency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public FinancialPeriod getFinancialPeriod() {
        return financialPeriod;
    }

    public void setFinancialPeriod(FinancialPeriod financialPeriod) {
        this.financialPeriod = financialPeriod;
    }

    @Override
    public int compareTo(ExchangeRate o) {
        return this.type.compareTo(o.getType());
    }

    public BigDecimal getPeriodEndRate() {
        return periodEndRate;
    }

    public void setPeriodEndRate(BigDecimal periodEndRate) {
        this.periodEndRate = periodEndRate;
    }

    public BigDecimal getMonthlyAverageRate() {
        return monthlyAverageRate;
    }

    public void setMonthlyAverageRate(BigDecimal monthlyAverageRate) {
        this.monthlyAverageRate = monthlyAverageRate;
    }

    public BigDecimal getYtdAverageRate() {
        return ytdAverageRate;
    }

    public void setYtdAverageRate(BigDecimal ytdAverageRate) {
        this.ytdAverageRate = ytdAverageRate;
    }

}
