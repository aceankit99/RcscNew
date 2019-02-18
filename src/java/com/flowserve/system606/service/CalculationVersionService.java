/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Attribute;
import com.flowserve.system606.model.AttributeSet;
import com.flowserve.system606.model.AttributeType;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.ContractVersion;
import com.flowserve.system606.model.CurrencyMetricVersion;
import com.flowserve.system606.model.DateMetricVersion;
import com.flowserve.system606.model.DecimalMetricVersion;
import com.flowserve.system606.model.ExchangeRate;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricStore;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.StringMetricVersion;
import com.flowserve.system606.model.TransientMeasurable;
import com.flowserve.system606.web.WebSession;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author shubhamv
 */
@Stateless
public class CalculationVersionService {

    private static final int SCALE = 14;

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    public void persist(Object object) {
        em.persist(object);
    }

    @Inject
    private PerformanceObligationService performanceObligationService;
    @Inject
    private ContractService contractService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private AdminService adminService;
    @Inject
    private MetricService metricService;
    @Inject
    private WebSession webSession;
    @Inject
    private CurrencyCache currencyCache;
    @Inject
    private FinancialPeriodService financialPeriodService;

    @PostConstruct
    public void init() {
        //metricTypes = metricService.findActiveMetricTypes();
    }

    private boolean isMetricStoredAtMeasurable(AttributeType attributeType, Measurable measurable) {
        if (measurable instanceof MetricStore) {
            if (attributeType.isContractLevel() && measurable instanceof Contract) {
                return true;
            }
            if (attributeType.isPobLevel() && measurable instanceof PerformanceObligation) {
                return true;
            }
        }

        return false;
    }

    private boolean isEmptyTransientMesaurable(Measurable measurable) {
        if (measurable.getChildMeasurables().isEmpty() && measurable instanceof TransientMeasurable) {
            return true;
        }

        return false;
    }

    public void convertCurrency(Attribute attribute, Measurable measurable) throws Exception {
        if (!(attribute instanceof CurrencyMetricVersion)) {
            return;
        }
        if (!attribute.getMetricType().isConvertible()) {
            return;
        }

        CurrencyMetricVersion currencyMetricVersion = (CurrencyMetricVersion) attribute;
        FinancialPeriod period = webSession.getCurrentPeriod();

        if (currencyMetricVersion.getLcValue() == null && currencyMetricVersion.getCcValue() == null) {
            return;
        }

        if (attribute.getMetricType().getMetricCurrencyType() == null) {
            throw new IllegalStateException("There is no currency type defined for the metric type " + attribute.getMetricType().getId() + ".  Please contact a system administrator.");
        }
        if (measurable.getLocalCurrency() == null) {
            throw new IllegalStateException("There is no local currency defined.  Please contact a system administrator.");
        }
        if (measurable.getContractCurrency() == null) {
            throw new IllegalStateException("There is no contract currency defined.  Please contact a system administrator.");
        }
        if (measurable.getReportingCurrency() == null) {
            throw new IllegalStateException("There is no reporting currency defined.  Please contact a system administrator.");
        }

        if (currencyMetricVersion.isLocalCurrencyMetric()) {
            if (currencyMetricVersion.getLcValue() == null) {
                return;
            }
            if (BigDecimal.ZERO.equals(currencyMetricVersion.getLcValue())) {
                currencyMetricVersion.setCcValue(BigDecimal.ZERO);
                currencyMetricVersion.setRcValue(BigDecimal.ZERO);
            } else {
                currencyMetricVersion.setCcValue(convert(currencyMetricVersion.getLcValue(), measurable.getLocalCurrency(), measurable.getContractCurrency(), period.getLocalCurrencyRatePeriod()));
                currencyMetricVersion.setRcValue(convert(currencyMetricVersion.getLcValue(), measurable.getLocalCurrency(), measurable.getReportingCurrency(), period.getReportingCurrencyRatePeriod()));
            }
        } else if (currencyMetricVersion.isContractCurrencyMetric()) {
            if (currencyMetricVersion.getCcValue() == null) {
                return;
            }

            if (BigDecimal.ZERO.equals(currencyMetricVersion.getCcValue())) {
                currencyMetricVersion.setLcValue(BigDecimal.ZERO);
                currencyMetricVersion.setRcValue(BigDecimal.ZERO);
            } else {
                currencyMetricVersion.setLcValue(convert(currencyMetricVersion.getCcValue(), measurable.getContractCurrency(), measurable.getLocalCurrency(), period.getLocalCurrencyRatePeriod()));
                currencyMetricVersion.setRcValue(convert(currencyMetricVersion.getCcValue(), measurable.getContractCurrency(), measurable.getReportingCurrency(), period.getReportingCurrencyRatePeriod()));
            }
        }
    }

    private BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency, FinancialPeriod period) throws Exception {

        ExchangeRate exchangeRate = getExchangeRate(fromCurrency, toCurrency, period);
        return amount.multiply(exchangeRate.getPeriodEndRate()).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    }

    private ExchangeRate getExchangeRate(Currency fromCurrency, Currency toCurrency, FinancialPeriod period) throws Exception {
        ExchangeRate exchangeRate = null;
        try {
            String cacheKey = period.getId() + fromCurrency.getCurrencyCode() + toCurrency.getCurrencyCode();

            exchangeRate = currencyCache.getExchangeRate(cacheKey);

            //Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "TestCache: hello: " + testCache.get("hello"));
            //Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Cache miss: " + cacheKey + " cache object ID: " + exchangeRateCache.toString());
            //exchangeRate = findRateByFromToPeriod(fromCurrency, toCurrency, period);
            //exchangeRateCache.put(cacheKey, exchangeRate);
            //exchangeRateCache.put(cacheKey, exchangeRate);
            if (exchangeRate == null) {
                throw new Exception("Missing exchange rate: " + cacheKey);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to find an exchange rate from " + fromCurrency.getCurrencyCode() + " to " + toCurrency.getCurrencyCode() + " in period " + period.getId());
        }

        return exchangeRate;
    }
}
