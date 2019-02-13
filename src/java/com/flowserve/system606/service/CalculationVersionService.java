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
import com.flowserve.system606.model.CurrencyAttribute;
import com.flowserve.system606.model.DateAttribute;
import com.flowserve.system606.model.DecimalAttribute;
import com.flowserve.system606.model.ExchangeRate;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricStore;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.StringAttribute;
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
    private AttributeService attributeService;
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

    public void saveTest() throws Exception {
        Contract contract = contractService.findContractById(new Long(4707));
        ContractVersion contractVersion = new ContractVersion();
        contractVersion.setId(new Long(1000));
        contractVersion.setName("Test1000");
        contractVersion.setReportingUnit(webSession.getCurrentReportingUnit());
        contractVersion.setContractCurrency(Currency.getInstance("USD"));
//        contractVersion.setContract(contract);
//        contractVersion.setVersion(2);
        AttributeSet attributeSet = new AttributeSet();
        contractVersion.setAttributeSet(attributeSet);
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", contract, financialPeriodService.findById("SEP-18")).getLcValue();
        //adminService.persist(contractVersion);
        Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "message : " + value);
        Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "contract : " + contract.getId());
        getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", contractVersion).setLcValue(value);
        adminService.persist(contractVersion);

    }

    private Attribute intelliGetMetric(AttributeType attributeType, Measurable measurable) {

        if (!measurable.attributeExistsForVersion(attributeType)) {
            measurable.initializeAttributeForVersion(attributeType);
        }

        return measurable.getVersionAttribute(attributeType);
    }

    private Attribute getMetric(String attributeCode, Measurable measurable) {
        return intelliGetMetric(attributeService.getAttributeTypeByCode(attributeCode), measurable);
    }

    public StringAttribute getStringMetric(String attributeTypeId, PerformanceObligation pob) {
        return (StringAttribute) getMetric(attributeTypeId, pob);
    }

    public DecimalAttribute getDecimalMetric(String attributeTypeId, Measurable measurable) {
        return (DecimalAttribute) getMetric(attributeTypeId, measurable);
    }

    public DateAttribute getDateMetric(String attributeTypeId, Measurable measurable, ContractVersion contractVersion) {
        return (DateAttribute) getMetric(attributeTypeId, measurable);
    }

    public CurrencyAttribute getCurrencyMetric(String attributeCode, Measurable measurable) throws Exception {
        if (measurable instanceof MetricStore) {
            CurrencyAttribute currencyMetricVersion = (CurrencyAttribute) getMetric(attributeCode, measurable);
            if (currencyMetricVersion != null || measurable instanceof PerformanceObligation) {
                return currencyMetricVersion;
            }
        }

        return getAccumulatedCurrencyMetric(attributeCode, measurable);
    }

    private CurrencyAttribute getAccumulatedCurrencyMetric(String attributeCode, Measurable measurable) throws Exception {
        AttributeType attributeType = attributeService.getAttributeTypeByCode(attributeCode);

        if (attributeType.isConvertible() && measurable.getContractCurrency() != null && measurable.getLocalCurrency() != null) {
            return getAccumulatedConvertibleCurrencyMetric(attributeType, measurable);
        } else {
            return getAccumulatedNonConvertibleCurrencyMetric(attributeType, measurable);
        }
    }

    private CurrencyAttribute getAccumulatedNonConvertibleCurrencyMetric(AttributeType attributeType, Measurable measurable) throws Exception {
        BigDecimal ccValueSum = BigDecimal.ZERO;
        BigDecimal lcValueSum = BigDecimal.ZERO;
        BigDecimal rcValueSum = BigDecimal.ZERO;

        CurrencyAttribute attribute = new CurrencyAttribute();
        // attribute.setContractVersion(contractVersion);
        attribute.setMetricType(attributeType);
        attribute.setCcValue(ccValueSum);
        attribute.setLcValue(lcValueSum);
        attribute.setRcValue(rcValueSum);

        if (isEmptyTransientMesaurable(measurable)) {
            return attribute;
        }

        if (isMetricStoredAtMeasurable(attributeType, measurable)) {
            CurrencyAttribute rootCurrencyMetric = (CurrencyAttribute) getMetric(attributeType.getCode(), measurable);
            if (rootCurrencyMetric == null) {
                return attribute;
            }
            if (measurable.getContractCurrency() != null) {
                BigDecimal ccValue = rootCurrencyMetric.getCcValue();
                if (ccValue != null) {
                    ccValueSum = ccValueSum.add(ccValue);
                    attribute.setCcValue(ccValueSum);
                }
            }
            if (measurable.getLocalCurrency() != null) {
                BigDecimal lcValue = rootCurrencyMetric.getLcValue();
                if (lcValue != null) {
                    lcValueSum = lcValueSum.add(lcValue);
                    attribute.setLcValue(lcValueSum);

                }
            }
            BigDecimal rcValue = rootCurrencyMetric.getRcValue();
            if (rcValue != null) {
                rcValueSum = rcValueSum.add(rcValue);
                attribute.setRcValue(rcValueSum);
            }

            return attribute;
        }

        for (Measurable childMeasurable : measurable.getChildMeasurables()) {
            if (measurable.getContractCurrency() != null) {
                ccValueSum = ccValueSum.add(getAccumulatedNonConvertibleCurrencyMetric(attributeType, childMeasurable).getCcValue());
            }
            if (measurable.getLocalCurrency() != null) {
                lcValueSum = lcValueSum.add(getAccumulatedNonConvertibleCurrencyMetric(attributeType, childMeasurable).getLcValue());
            }
            rcValueSum = rcValueSum.add(getAccumulatedNonConvertibleCurrencyMetric(attributeType, childMeasurable).getRcValue());
        }

        if (measurable.getContractCurrency() != null) {
            attribute.setCcValue(ccValueSum);
        }
        if (measurable.getLocalCurrency() != null) {
            attribute.setLcValue(lcValueSum);
        }

        attribute.setRcValue(rcValueSum);

        return attribute;
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

    private CurrencyAttribute getAccumulatedConvertibleCurrencyMetric(AttributeType attributeType, Measurable measurable) throws Exception {
        BigDecimal valueSum = BigDecimal.ZERO;
        CurrencyAttribute attribute = new CurrencyAttribute();
        //attribute.setContractVersion(contractVersion);
        attribute.setMetricType(attributeType);
        attribute.setValue(valueSum);

        if (isEmptyTransientMesaurable(measurable)) {
            return attribute;
        }

        if (isMetricStoredAtMeasurable(attributeType, measurable)) {
            CurrencyAttribute rootCurrencyMetric = (CurrencyAttribute) getMetric(attributeType.getCode(), measurable);
            if (rootCurrencyMetric == null) {
                return attribute;
            }
            BigDecimal value = rootCurrencyMetric.getValue();
            if (value != null) {
                valueSum = valueSum.add(value);
            }
            attribute.setValue(valueSum);

            return attribute;
        }

        for (Measurable childMeasurable : measurable.getChildMeasurables()) {
            valueSum = valueSum.add(getAccumulatedConvertibleCurrencyMetric(attributeType, childMeasurable).getValue());
        }

        attribute.setValue(valueSum);
        convertCurrency(attribute, measurable);

        return attribute;
    }

    private boolean isEmptyTransientMesaurable(Measurable measurable) {
        if (measurable.getChildMeasurables().isEmpty() && measurable instanceof TransientMeasurable) {
            return true;
        }

        return false;
    }

    public void convertCurrency(Attribute attribute, Measurable measurable) throws Exception {
        if (!(attribute instanceof CurrencyAttribute)) {
            return;
        }
        if (!attribute.getMetricType().isConvertible()) {
            return;
        }

        CurrencyAttribute currencyMetricVersion = (CurrencyAttribute) attribute;
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
