/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.BusinessRule;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.CurrencyMetricPriorPeriod;
import com.flowserve.system606.model.DateMetric;
import com.flowserve.system606.model.DateMetricPriorPeriod;
import com.flowserve.system606.model.DecimalMetric;
import com.flowserve.system606.model.DecimalMetricPriorPeriod;
import com.flowserve.system606.model.Event;
import com.flowserve.system606.model.EventList;
import com.flowserve.system606.model.EventStore;
import com.flowserve.system606.model.EventType;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.Metric;
import com.flowserve.system606.model.MetricStore;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.StringMetric;
import com.flowserve.system606.model.StringMetricPriorPeriod;
import com.flowserve.system606.model.TransientMeasurable;
import com.flowserve.system606.model.User;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.kie.api.runtime.StatelessKieSession;

/**
 *
 * @author kgraves
 */
@Stateless
public class CalculationService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private PerformanceObligationService performanceObligationService;
    @Inject
    private ContractService contractService;
    @Inject
    private CurrencyService currencyService;
    @Inject
    private AdminService adminService;
    @Inject
    private MetricService metricService;
    @Inject
    private EventService eventService;
    @Inject
    private DroolsInitService droolsInitService;
    @Inject
    private DroolsValidationInitService droolsValidationInitService;
    private StatelessKieSession kSession = null;
    private StatelessKieSession kValidationSession = null;
    private static final String PACKAGE_PREFIX = "com.flowserve.system606.model.";
    private List<MetricType> metricTypes = null;

    @PostConstruct
    public void init() {
        metricTypes = metricService.findActiveMetricTypes();
    }

    public void initBusinessRulesSession() {
        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "getKieSession");
        kSession = droolsInitService.getKieSession();
        kValidationSession = droolsValidationInitService.getKieSession();
        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Finished getKieSession");
    }

    private void convertCurrencies(Collection<Metric> metrics, Measurable measurable, FinancialPeriod period) throws Exception {
        for (Metric metric : metrics) {
            currencyService.convertCurrency(metric, measurable, period);
        }
    }

    private void convertReportingCurrencies(Collection<Metric> metrics, Measurable measurable, FinancialPeriod period) throws Exception {
        for (Metric metric : metrics) {
            currencyService.convertReportingCurrency(metric, measurable, period);
        }
    }

    // intelliGet since this is no ordinary get.  Initialize any missing metrics on the fly.
    private Metric intelliGetMetric(MetricType metricType, Measurable measurable, FinancialPeriod period) {
        if (!measurable.metricSetExistsForPeriod(period)) {
            measurable.initializeMetricSetForPeriod(period);
        }
        if (!measurable.metricExistsForPeriod(period, metricType)) {
            measurable.initializeMetricForPeriod(period, metricType);
        }

        return measurable.getPeriodMetric(period, metricType);
    }

    private Metric getMetric(String metricCode, Measurable measurable, FinancialPeriod period) {
        return intelliGetMetric(metricService.getMetricTypeByCode(metricCode), measurable, period);
    }

    public StringMetric getStringMetric(String metricTypeId, PerformanceObligation pob, FinancialPeriod period) {
        return (StringMetric) getMetric(metricTypeId, pob, period);
    }

    public DecimalMetric getDecimalMetric(String metricTypeId, Measurable measurable, FinancialPeriod period) {
        return (DecimalMetric) getMetric(metricTypeId, measurable, period);
    }

    public DateMetric getDateMetric(String metricTypeId, Measurable measurable, FinancialPeriod period) {
        return (DateMetric) getMetric(metricTypeId, measurable, period);
    }

    public CurrencyMetric getAccumulatedCurrencyMetricAcrossPeriods(String metricCode, Measurable measurable, List<FinancialPeriod> periods) throws Exception {
        CurrencyMetric metric = new CurrencyMetric();
        metric.setMetricType(metricService.getMetricTypeByCode(metricCode));
        metric.setCcValue(BigDecimal.ZERO);
        metric.setLcValue(BigDecimal.ZERO);
        metric.setRcValue(BigDecimal.ZERO);

        if (isEmptyTransientMesaurable(measurable)) {
            return metric;
        }

        for (FinancialPeriod period : periods) {

            if (isMetricStoredAtMeasurable(metric.getMetricType(), measurable)) {
                CurrencyMetric rootCurrencyMetric = (CurrencyMetric) getMetric(metricCode, measurable, period);
                if (rootCurrencyMetric == null) {
                    continue;
                }
                BigDecimal rootCcValue = rootCurrencyMetric.getCcValue();
                if (rootCcValue != null) {
                    metric.setCcValue(metric.getCcValue().add(rootCcValue));
                }
                BigDecimal rootLcValue = rootCurrencyMetric.getLcValue();
                if (rootLcValue != null) {
                    metric.setLcValue(metric.getLcValue().add(rootLcValue));
                }
                BigDecimal rootRcValue = rootCurrencyMetric.getRcValue();
                if (rootRcValue != null) {
                    metric.setRcValue(metric.getRcValue().add(rootRcValue));
                }
                continue;
            }

            for (Measurable childMeasurable : measurable.getChildMeasurables()) {
                /**
                 * We always want to use the non convertible accumulator here since we are accum across periods. Need to maintain historical conversion rates.
                 */
                CurrencyMetric childMetric = getAccumulatedNonConvertibleCurrencyMetric(metric.getMetricType(), childMeasurable, period);

                if (childMetric.getCcValue() != null) {
                    metric.setCcValue(metric.getCcValue().add(childMetric.getCcValue()));
                }
                if (childMetric.getLcValue() != null) {
                    metric.setLcValue(metric.getLcValue().add(childMetric.getLcValue()));
                }
                if (childMetric.getRcValue() != null) {
                    metric.setRcValue(metric.getRcValue().add(childMetric.getRcValue()));
                }
            }
        }

        return metric;
    }

    public CurrencyMetric getCurrencyMetric(String metricCode, Measurable measurable, FinancialPeriod period) throws Exception {
        if (measurable instanceof MetricStore) {
            CurrencyMetric currencyMetric = (CurrencyMetric) getMetric(metricCode, measurable, period);
            if (currencyMetric != null || measurable instanceof PerformanceObligation) {
                return currencyMetric;
            }
        }

        return getAccumulatedCurrencyMetric(metricCode, measurable, period);
    }

    private EventList intelliGetEventList(EventStore eventStore, FinancialPeriod period) {
        if (!eventStore.eventListExistsForPeriod(period)) {
            eventStore.initializeEventListForPeriod(period);
        }

        return eventStore.getPeriodEventList(period);
    }

    private EventList getEventList(EventStore eventStore, FinancialPeriod period) {
        return intelliGetEventList(eventStore, period);
    }

    private List<Event> getPeriodEvents(Measurable measurable, FinancialPeriod period) {
        List<Event> events = new ArrayList<Event>();

        if (measurable instanceof EventStore) {
            events.addAll(getEventList((EventStore) measurable, period).getEventList());
        }

        return events;
    }

    public void addEvent(EventStore eventStore, FinancialPeriod period, Event event) throws Exception {
        if (eventStore instanceof EventStore) {
            EventList eventList = getEventList(eventStore, period);
            event.setEventList(eventList);
            eventList.addEvent(event);
        }
    }

    public CurrencyEvent addBillingEvent(Contract contract, FinancialPeriod period, User user) throws Exception {
        EventType billingEventType = eventService.getEventTypeByCode("BILLING_EVENT_CC");
        CurrencyEvent billingEvent = new CurrencyEvent();
        billingEvent.setCreatedBy(user);
        billingEvent.setCreationDate(LocalDateTime.now());
        billingEvent.setEventType(billingEventType);
        billingEvent.setContract(contract);
        billingEvent.setEventDate(period.getStartDate());
        billingEvent.setFinancialPeriod(period);
        billingEvent.setCcValue(BigDecimal.ZERO);
        billingEvent.setLcValue(BigDecimal.ZERO);
        billingEvent = (CurrencyEvent) eventService.update(billingEvent);
        billingEvent.setName("BillingEvent " + billingEvent.getId());
        eventService.persist(billingEvent);
        addEvent(contract, period, billingEvent);
        update(contract);

        return billingEvent;
    }

    public void removeBillingEvent(Event event, FinancialPeriod period, User user) throws Exception {
        Contract contract = event.getContract();
        removeEventFromEventStoreList(event.getContract(), period, event);
        event = eventService.update(event);
        eventService.remove(event);
        executeBusinessRules(contract, period);
        contractService.update(contract);
    }

    private void removeEventFromEventStoreList(EventStore eventStore, FinancialPeriod period, Event event) throws Exception {
        if (eventStore instanceof EventStore) {
            EventList eventList = getEventList(eventStore, period);
            eventList.removeEvent(event);
        }
    }

    public void calculateAndSave(ReportingUnit reportingUnit, FinancialPeriod period) throws Exception {

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Calculating RU: " + reportingUnit.getCode() + " " + reportingUnit.getName());

        FinancialPeriod calculationPeriod = period;
        //do {
        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Recalcing POBs for period: " + calculationPeriod.getId() + " RU: " + reportingUnit.getCode());
        executeBusinessRulesAndSave((new ArrayList<Measurable>(reportingUnit.getUnarchivedPerformanceObligations())), calculationPeriod);
        //} while ((calculationPeriod = financialPeriodService.calculateNextPeriodUntilCurrent(calculationPeriod)) != null);

        //calculationPeriod = period;
        //do {
        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Recalcing Contracts for period: " + calculationPeriod.getId() + " RU: " + reportingUnit.getCode());
        executeBusinessRulesAndSave((new ArrayList<Measurable>(reportingUnit.getUnarchivedContracts())), calculationPeriod);
        //} while ((calculationPeriod = financialPeriodService.calculateNextPeriodUntilCurrent(calculationPeriod)) != null);

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Completed calcs RU: " + reportingUnit.getCode());
    }

    public void convertRCAndSave(ReportingUnit reportingUnit, FinancialPeriod calculationPeriod) throws Exception {

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Converting RU: " + reportingUnit.getCode() + " " + reportingUnit.getName());

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Converting POBs for period: " + calculationPeriod.getId() + " RU: " + reportingUnit.getCode());
        convertReportingCurrencyAndSave((new ArrayList<Measurable>(reportingUnit.getUnarchivedPerformanceObligations())), calculationPeriod);

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Converting Contracts for period: " + calculationPeriod.getId() + " RU: " + reportingUnit.getCode());
        convertReportingCurrencyAndSave((new ArrayList<Measurable>(reportingUnit.getUnarchivedContracts())), calculationPeriod);

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Completed Conversion RU: " + reportingUnit.getCode());
    }

    public void processBillingAmountCCChange(CurrencyEvent billingEvent, FinancialPeriod period, User user) throws Exception {
        billingEvent.setLcValue(BigDecimal.ZERO);
        billingEvent.getContract().setLastUpdatedBy(user);
        billingEvent.getContract().setLastUpdateDate(LocalDateTime.now());
        executeBusinessRules(billingEvent.getContract(), period);
        contractService.update(billingEvent.getContract());
    }

    public void processBillingAmountLCChange(CurrencyEvent billingEvent, FinancialPeriod period, User user) throws Exception {
        billingEvent.getContract().setLastUpdatedBy(user);
        billingEvent.getContract().setLastUpdateDate(LocalDateTime.now());
        executeBusinessRules(billingEvent.getContract(), period);
        contractService.update(billingEvent.getContract());
    }

    @TransactionAttribute(NOT_SUPPORTED)
    public void executeBusinessRules(Measurable measurable, FinancialPeriod period) throws Exception {
        if (kSession == null || kValidationSession == null) {
            initBusinessRulesSession();
        }

        List<Object> facts = new ArrayList<Object>();
        facts.add(measurable);
        Collection<Metric> periodMetrics = getAllCurrentPeriodMetrics(measurable, period);
        /**
         * KJG 9/22/2018 - TransientMesasureable should not run currency conversion since there is no contract currency defined at that level.
         */
        if (measurable instanceof MetricStore) {
            convertCurrencies(periodMetrics, measurable, period);
        }
        facts.addAll(periodMetrics);
        facts.add(period);
        facts.add(currencyService);
        facts.addAll(getAllPriorPeriodMetrics(measurable, period));
        facts.addAll(getPeriodEvents(measurable, period));
        kSession.execute(facts);
        kValidationSession.execute(facts);
        if (measurable instanceof MetricStore) {
            convertCurrencies(periodMetrics, measurable, period);
        }
    }

    public void executeBusinessRulesAndSave(List<Measurable> measurables, FinancialPeriod period) throws Exception {
        if (period.isOpen()) {
            for (Measurable measurable : measurables) {
                executeBusinessRules(measurable, period);
                update(measurable);
            }
        } else {
            Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, period.getId() + " is not open.  Skipping calculations");
        }
    }

    public void convertReportingCurrencyAndSave(List<Measurable> measurables, FinancialPeriod period) throws Exception {
        if (period.isOpen()) {
            for (Measurable measurable : measurables) {
                Collection<Metric> periodMetrics = getAllCurrentPeriodMetrics(measurable, period);
                if (measurable instanceof MetricStore) {
                    convertReportingCurrencies(periodMetrics, measurable, period);
                }
                update(measurable);
            }
        } else {
            Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, period.getId() + " is not open.  Skipping calculations");
        }
    }

    public void executeBusinessRulesAndSave(Measurable measurable, FinancialPeriod period) throws Exception {
        if (period.isOpen()) {
            executeBusinessRules(measurable, period);
            update(measurable);
        } else {
            Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, period.getId() + " is not open.  Skipping calculations");
        }
    }

    public Measurable update(Measurable measurable) throws Exception {
        return em.merge(measurable);
    }

    private Collection<Metric> getAllCurrentPeriodMetrics(Measurable measurable, FinancialPeriod period) throws Exception {
        return getAllMetrics(measurable, period);
    }

    private Collection<Metric> getAllMetrics(Measurable measurable, FinancialPeriod period) throws Exception {

        List<Metric> metrics = new ArrayList<Metric>();
        for (MetricType metricType : metricTypes) {

            if (metricType.isCurrency()) {
                Metric currencyMetric = getCurrencyMetric(metricType.getCode(), measurable, period);
                if (currencyMetric != null) {
                    metrics.add(currencyMetric);
                }
            } else {
                // Quick hack to test getting decimal metric at all levels.
                if (metricType.isDecimal()) {
                    Metric metric = intelliGetMetric(metricType, measurable, period);
                    if (metric != null) {
                        metrics.add(metric);
                    }
                }
                if (measurable instanceof MetricStore) {
                    Metric metric = intelliGetMetric(metricType, measurable, period);
                    if (metric != null) {
                        metrics.add(metric);
                    }
                }
            }
        }

        return metrics;
    }

    public List<Event> getAllEventsByPeriodAndEventType(EventStore eventStore, EventType eventType, FinancialPeriod period) {
        return eventStore.getAllEventsByPeriodAndEventType(period, eventType);
    }

    public List<Event> getAllEventsByEventType(EventStore eventStore, EventType eventType) {
        return eventStore.getAllEventsByEventType(eventType);
    }

    private Collection<Object> getAllPriorPeriodMetrics(Measurable measurable, FinancialPeriod currentPeriod) throws Exception {
        FinancialPeriod priorPeriod = currentPeriod.getPriorPeriod();

        Collection<Metric> metrics = getAllMetrics(measurable, priorPeriod);
        List<Object> priorPeriodMetrics = new ArrayList<Object>();
        for (Metric metric : metrics) {
            if (metric instanceof CurrencyMetric) {
                priorPeriodMetrics.add(new CurrencyMetricPriorPeriod((CurrencyMetric) metric));
            }
            if (metric instanceof DecimalMetric) {
                priorPeriodMetrics.add(new DecimalMetricPriorPeriod((DecimalMetric) metric));
            }
            if (metric instanceof DateMetric) {
                priorPeriodMetrics.add(new DateMetricPriorPeriod((DateMetric) metric));
            }
            if (metric instanceof StringMetric) {
                priorPeriodMetrics.add(new StringMetricPriorPeriod((StringMetric) metric));
            }
        }

        return priorPeriodMetrics;
    }

    public BusinessRule findByRuleKey(String ruleKey) {
        Query query = em.createQuery("SELECT bu FROM BusinessRule bu WHERE bu.ruleKey = :RULE_KEY");
        query.setParameter("RULE_KEY", ruleKey);
        return (BusinessRule) query.getSingleResult();  // we want an exception if not one and only one.
    }

    public void persist(BusinessRule bu) throws Exception {
        em.persist(bu);
    }

    public BusinessRule update(BusinessRule bu) throws Exception {
        return em.merge(bu);
    }

    /**
     * For metrics eligible for automatic system currency conversion, we can just convert the base 'value' and convert to the other currencies. If not then we
     * have to accumulate each currency type separately. Splitting this into two methods as the non-convertible is 3x slower.
     */
    private CurrencyMetric getAccumulatedCurrencyMetric(String metricCode, Measurable measurable, FinancialPeriod period) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);

        if (metricType.isConvertible() && measurable.getContractCurrency() != null && measurable.getLocalCurrency() != null) {
            return getAccumulatedConvertibleCurrencyMetric(metricType, measurable, period);
        } else {
            return getAccumulatedNonConvertibleCurrencyMetric(metricType, measurable, period);
        }
    }

    private CurrencyMetric getAccumulatedNonConvertibleCurrencyMetric(MetricType metricType, Measurable measurable, FinancialPeriod period) throws Exception {
        BigDecimal ccValueSum = BigDecimal.ZERO;
        BigDecimal lcValueSum = BigDecimal.ZERO;
        BigDecimal rcValueSum = BigDecimal.ZERO;

        CurrencyMetric metric = new CurrencyMetric();
        metric.setFinancialPeriod(period);
        metric.setMetricType(metricType);
        metric.setCcValue(ccValueSum);
        metric.setLcValue(lcValueSum);
        metric.setRcValue(rcValueSum);

        if (isEmptyTransientMesaurable(measurable)) {
            return metric;
        }

        if (isMetricStoredAtMeasurable(metricType, measurable)) {
            CurrencyMetric rootCurrencyMetric = (CurrencyMetric) getMetric(metricType.getCode(), measurable, period);
            if (rootCurrencyMetric == null) {
                return metric;
            }
            if (measurable.getContractCurrency() != null) {
                BigDecimal ccValue = rootCurrencyMetric.getCcValue();
                if (ccValue != null) {
                    ccValueSum = ccValueSum.add(ccValue);
                    metric.setCcValue(ccValueSum);
                }
            }
            if (measurable.getLocalCurrency() != null) {
                BigDecimal lcValue = rootCurrencyMetric.getLcValue();
                if (lcValue != null) {
                    lcValueSum = lcValueSum.add(lcValue);
                    metric.setLcValue(lcValueSum);

                }
            }
            BigDecimal rcValue = rootCurrencyMetric.getRcValue();
            if (rcValue != null) {
                rcValueSum = rcValueSum.add(rcValue);
                metric.setRcValue(rcValueSum);
            }

            return metric;
        }

        for (Measurable childMeasurable : measurable.getChildMeasurables()) {
            if (measurable.getContractCurrency() != null) {
                ccValueSum = ccValueSum.add(getAccumulatedNonConvertibleCurrencyMetric(metricType, childMeasurable, period).getCcValue());
            }
            if (measurable.getLocalCurrency() != null) {
                lcValueSum = lcValueSum.add(getAccumulatedNonConvertibleCurrencyMetric(metricType, childMeasurable, period).getLcValue());
            }
            rcValueSum = rcValueSum.add(getAccumulatedNonConvertibleCurrencyMetric(metricType, childMeasurable, period).getRcValue());
        }

        if (measurable.getContractCurrency() != null) {
            metric.setCcValue(ccValueSum);
        }
        if (measurable.getLocalCurrency() != null) {
            metric.setLcValue(lcValueSum);
        }

        metric.setRcValue(rcValueSum);

        return metric;
    }

    private boolean isMetricStoredAtMeasurable(MetricType metricType, Measurable measurable) {
        if (measurable instanceof MetricStore) {
            if (metricType.isContractLevel() && measurable instanceof Contract) {
                return true;
            }
            if (metricType.isPobLevel() && measurable instanceof PerformanceObligation) {
                return true;
            }
        }

        return false;
    }

    private CurrencyMetric getAccumulatedConvertibleCurrencyMetric(MetricType metricType, Measurable measurable, FinancialPeriod period) throws Exception {
        BigDecimal valueSum = BigDecimal.ZERO;
        CurrencyMetric metric = new CurrencyMetric();
        metric.setFinancialPeriod(period);
        metric.setMetricType(metricType);
        metric.setValue(valueSum);

        if (isEmptyTransientMesaurable(measurable)) {
            return metric;
        }

        if (isMetricStoredAtMeasurable(metricType, measurable)) {
            CurrencyMetric rootCurrencyMetric = (CurrencyMetric) getMetric(metricType.getCode(), measurable, period);
            if (rootCurrencyMetric == null) {
                return metric;
            }
            BigDecimal value = rootCurrencyMetric.getValue();
            if (value != null) {
                valueSum = valueSum.add(value);
            }
            metric.setValue(valueSum);

            return metric;
        }

        for (Measurable childMeasurable : measurable.getChildMeasurables()) {
            valueSum = valueSum.add(getAccumulatedConvertibleCurrencyMetric(metricType, childMeasurable, period).getValue());
        }

        metric.setValue(valueSum);
        currencyService.convertCurrency(metric, measurable, period);

        return metric;
    }

    private boolean isEmptyTransientMesaurable(Measurable measurable) {
        if (measurable.getChildMeasurables().isEmpty() && measurable instanceof TransientMeasurable) {
            return true;
        }

        return false;
    }

    public BusinessRule findBusinessRuleByRuleKey(String ruleKey) {
        Query query = em.createQuery("SELECT bu FROM BusinessRule bu WHERE bu.ruleKey = :RULE_KEY");
        query.setParameter("RULE_KEY", ruleKey);
        List<BusinessRule> businessRule = query.getResultList();
        if (businessRule.size() > 0) {
            return businessRule.get(0);
        }
        return null;
    }

    public void initBusinessRules() throws Exception {
        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "initBusinessRules");
        String content = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/business_rules/massive_rule.drl").toURI())));

        if (findBusinessRuleByRuleKey("massive.rule") == null) {
            BusinessRule businessRule = new BusinessRule();
            businessRule.setRuleKey("massive.rule");
            businessRule.setVersionNumber(1L);
            businessRule.setContent(content);
            persist(businessRule);
        } else {
            BusinessRule businessRule = findByRuleKey("massive.rule");
            businessRule.setContent(content);
            update(businessRule);
        }

        String contentValidate = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/business_rules/post_calc_validation_rules.drl").toURI())));

        if (findBusinessRuleByRuleKey("validation.rule") == null) {
            BusinessRule businessRule = new BusinessRule();
            businessRule.setRuleKey("validation.rule");
            businessRule.setVersionNumber(2L);
            businessRule.setContent(contentValidate);
            persist(businessRule);
        } else {
            BusinessRule businessRule = findByRuleKey("validation.rule");
            businessRule.setContent(contentValidate);
            update(businessRule);
        }

        Logger.getLogger(CalculationService.class.getName()).log(Level.INFO, "Finished initBusinessRules");
    }
}
