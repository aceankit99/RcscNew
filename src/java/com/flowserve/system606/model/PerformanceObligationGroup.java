package com.flowserve.system606.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

/**
 *
 * @author kgraves
 */
public class PerformanceObligationGroup extends TransientMeasurable<String> implements Measurable {

    private String id;
    private Currency localCurrency;
    private Currency contractCurrency;
    private Company company;
    private RevenueMethod revenueMethod;

    private List<PerformanceObligation> performanceObligations = new ArrayList<PerformanceObligation>();
    private List<RevenueMethod> revenueMethodList = new ArrayList<RevenueMethod>();

    public PerformanceObligationGroup(String id, Currency localCurrency, Currency contractCurrency, Company company) {
        this.id = id;
        this.localCurrency = localCurrency;
        this.contractCurrency = contractCurrency;
        this.company = company;
    }

    public PerformanceObligationGroup(String id, Contract contract) {
        this.id = id;
        this.localCurrency = contract.getLocalCurrency();
        this.contractCurrency = contract.getContractCurrency();
        this.company = contract.getReportingUnit().getCompany();
    }

    public PerformanceObligationGroup(String id, ReportingUnit reportingUnit) {
        this.id = id;
        this.localCurrency = reportingUnit.getLocalCurrency();
        this.company = reportingUnit.getCompany();
    }

    public PerformanceObligationGroup(String id, Contract contract, List<PerformanceObligation> pobs) {
        this.id = id;
        this.localCurrency = contract.getLocalCurrency();
        this.contractCurrency = contract.getContractCurrency();
        this.company = contract.getReportingUnit().getCompany();
        this.performanceObligations = pobs;
    }

    public PerformanceObligationGroup(String id, Contract contract, RevenueMethod revenueMethod, List<PerformanceObligation> pobs) {
        this.id = id;
        this.localCurrency = contract.getLocalCurrency();
        this.contractCurrency = contract.getContractCurrency();
        this.company = contract.getReportingUnit().getCompany();
        this.revenueMethod = revenueMethod;
        this.performanceObligations = pobs;
    }

    public PerformanceObligationGroup(String id, ReportingUnit reportingUnit, RevenueMethod revenueMethod, List<PerformanceObligation> pobs) {
        this.id = id;
        this.localCurrency = reportingUnit.getLocalCurrency();
        this.company = reportingUnit.getCompany();
        this.revenueMethod = revenueMethod;
        this.performanceObligations = pobs;
    }

    public PerformanceObligationGroup(String id, ReportingUnit reportingUnit, List<RevenueMethod> revenueMethods, List<PerformanceObligation> pobs) {
        this.id = id;
        this.localCurrency = reportingUnit.getLocalCurrency();
        this.company = reportingUnit.getCompany();
        this.revenueMethodList = revenueMethods;
        this.performanceObligations = pobs;
    }

    public PerformanceObligationGroup() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Currency getLocalCurrency() {
        return localCurrency;
    }

    public void setLocalCurrency(Currency localCurrency) {
        this.localCurrency = localCurrency;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Currency getReportingCurrency() {
        return company.getReportingCurrency();
    }

    public Currency getContractCurrency() {
        return contractCurrency;
    }

    public void setContractCurrency(Currency contractCurrency) {
        this.contractCurrency = contractCurrency;
    }

    public List<Measurable> getChildMeasurables() {
        return new ArrayList<Measurable>(performanceObligations);
    }

    public void addPerformanceObligation(PerformanceObligation pob) {
        performanceObligations.add(pob);
    }

    public void addPerformanceObligations(Collection<PerformanceObligation> pobs) {
        performanceObligations.addAll(pobs);
    }

    public List<PerformanceObligation> getPerformanceObligations() {
        return performanceObligations;
    }

    public RevenueMethod getRevenueMethod() {
        return revenueMethod;
    }

    public void setRevenueMethod(RevenueMethod revenueMethod) {
        this.revenueMethod = revenueMethod;
    }

    public List<RevenueMethod> getRevenueMethodList() {
        return revenueMethodList;
    }

    public void addRevenueMethodList(RevenueMethod revMethod) {
        revenueMethodList.add(revMethod);
    }

    public void addrevenueMethodList(Collection<RevenueMethod> revMethod) {
        revenueMethodList.addAll(revMethod);
    }

}
