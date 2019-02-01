/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author kgraves
 */
@Entity
@Table(name = "JOURNAL_ENTRY_HEADERS")
public class JournalEntryHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JE_SEQ")
    @SequenceGenerator(name = "JE_SEQ", sequenceName = "JE_SEQ", allocationSize = 30)
    @Column(name = "JE_HEADER_ID")
    private Long id;
    @OneToOne
    @JoinColumn(name = "METRIC_TYPE_ID")
    private MetricType metricType;
    @Column(name = "CURRENCY")
    private Currency currency;
    @Column(name = "TRX_DATE")
    private LocalDate transactionDate;
    @Column(name = "JOURNAL_ENTRY_TYPE")
    private JournalEntryType journalEntryType;
    @Column(name = "REVENUE_METHOD")
    private RevenueMethod revenueMethod;
    @OneToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;
    @OneToOne
    @JoinColumn(name = "CONTRACT_JOURNAL_ID")
    private ContractJournal contractJournal;
    @Column(name = "IS_INFORMATIONAL")
    private boolean informational;
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "journalEntryHeader", orphanRemoval = true)
    private List<JournalEntryLine> journalEntryLines = new ArrayList<JournalEntryLine>();

    public JournalEntryHeader() {
    }

    public JournalEntryHeader(ContractJournal contractJournal, MetricType metricType, Account account, Currency currency, LocalDate transactionDate, JournalEntryType journalEntryType, boolean informational) {
        this.contractJournal = contractJournal;
        this.metricType = metricType;
        this.account = account;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.journalEntryType = journalEntryType;
        this.informational = informational;
    }

    public void addJournalEntryLine(JournalEntryLine line) {
        journalEntryLines.add(line);
    }

    public void addJournalEntryLines(Collection<JournalEntryLine> lines) {
        journalEntryLines.addAll(journalEntryLines);
    }

    public boolean containsAccount(Account account) {
        if (this.account != null && this.account.equals(account)) {
            return true;
        }
        if (this.account != null && this.account.getOffsetAccount() != null && this.account.getOffsetAccount().equals(account)) {
            return true;
        }

        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public JournalEntryType getJournalEntryType() {
        return journalEntryType;
    }

    public void setJournalEntryType(JournalEntryType journalEntryType) {
        this.journalEntryType = journalEntryType;
    }

    public RevenueMethod getRevenueMethod() {
        return revenueMethod;
    }

    public void setRevenueMethod(RevenueMethod revenueMethod) {
        this.revenueMethod = revenueMethod;
    }

    public ContractJournal getContractJournal() {
        return contractJournal;
    }

    public void setContractJournal(ContractJournal contractJournal) {
        this.contractJournal = contractJournal;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<JournalEntryLine> getJournalEntryLines() {
        return journalEntryLines;
    }

    public boolean isInformational() {
        return informational;
    }

    public void setInformational(boolean informational) {
        this.informational = informational;
    }
}
