/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.math.BigDecimal;
import java.util.ArrayList;
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

@Entity
@Table(name = "REPORTING_UNIT_JOURNALS")
public class ReportingUnitJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RU_JOURNAL_SEQ")
    @SequenceGenerator(name = "RU_JOURNAL_SEQ", sequenceName = "RU_JOURNAL_SEQ", allocationSize = 1)
    @Column(name = "RU_JOURNAL_ID")
    private Long id;
    @Column(name = "JOURNAL_SATUS")
    private JournalStatus journalStatus;
    @Column(name = "IS_BALANCED")
    private boolean balanced;
    @OneToOne
    @JoinColumn(name = "FINANCIAL_PERIOD_ID")
    private FinancialPeriod period;
    @OneToOne
    @JoinColumn(name = "REPORTING_UNIT_ID")
    private ReportingUnit reportingUnit;
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "reportingUnitJournal", orphanRemoval = true)
    List<ContractJournal> contractJournals = new ArrayList<ContractJournal>();

    public ReportingUnitJournal() {
    }

    public ReportingUnitJournal(ReportingUnit ru, FinancialPeriod period) {
        this.reportingUnit = ru;
        this.period = period;
    }

    public JournalEntryLine getReportingUnitSummaryJournalEntryLine(Account account, boolean includeInformational) {
        if (account == null) {
            return null;
        }
        JournalEntryLine summaryLine = new JournalEntryLine();
        summaryLine.setAccount(account);
        summaryLine.setAmount(BigDecimal.ZERO);

        for (ContractJournal contractJournal : contractJournals) {
            for (JournalEntryHeader header : contractJournal.getJournalEntryHeaders()) {
                if (includeInformational == false && header.isInformational()) {
                    continue;
                }
                if (header.containsAccount(account)) {
                    for (JournalEntryLine existingLine : header.getJournalEntryLines()) {
                        if (existingLine.getAccount().equals(account)) {
                            if (existingLine.getAmount() != null) {
                                summaryLine.setAmount(summaryLine.getAmount().add(existingLine.getAmount()));
                            }
                        }
                    }
                }
            }
        }

        return summaryLine;
    }

    public JournalEntryLine getReportingUnitSummaryJournalOffsetEntryLine(Account account, boolean includeInformational) {
        if (account == null || account.getOffsetAccount() == null) {
            return null;
        }
        JournalEntryLine summaryLine = new JournalEntryLine();
        summaryLine.setAccount(account.getOffsetAccount());
        summaryLine.setAmount(BigDecimal.ZERO);

        for (ContractJournal contractJournal : contractJournals) {
            for (JournalEntryHeader header : contractJournal.getJournalEntryHeaders()) {
                if (includeInformational == false && header.isInformational()) {
                    continue;
                }
                if (header.getAccount().equals(account)) {
                    for (JournalEntryLine existingLine : header.getJournalEntryLines()) {
                        if (existingLine.getAccount().equals(account.getOffsetAccount())) {
                            if (existingLine.getAmount() != null) {
                                summaryLine.setAmount(summaryLine.getAmount().add(existingLine.getAmount()));
                            }
                        }
                    }
                }
            }
        }

        return summaryLine;
    }

    public JournalEntryLine getReportingUnitSummaryJournalEntryLine(Account account, RevenueMethod revenueMethod, boolean includeInformational) {
        if (account == null || revenueMethod == null) {
            return null;
        }
        JournalEntryLine summaryLine = new JournalEntryLine();
        summaryLine.setAccount(account);
        summaryLine.setRevenueMethod(revenueMethod);
        summaryLine.setAmount(BigDecimal.ZERO);

        for (ContractJournal contractJournal : contractJournals) {
            for (JournalEntryHeader header : contractJournal.getJournalEntryHeaders()) {
                if (includeInformational == false && header.isInformational()) {
                    continue;
                }
                if (header.containsAccount(account) && revenueMethod.equals(header.getRevenueMethod())) {
                    for (JournalEntryLine existingLine : header.getJournalEntryLines()) {
                        if (existingLine.getAccount().equals(account) && existingLine.getRevenueMethod().equals(revenueMethod)) {
                            if (existingLine.getAmount() != null) {
                                summaryLine.setAmount(summaryLine.getAmount().add(existingLine.getAmount()));
                            }
                        }
                    }
                }
            }
        }

        return summaryLine;
    }

    /**
     * Get the specific offset lines for a specific account, across all entries.
     */
    public JournalEntryLine getReportingUnitSummaryJournalOffsetEntryLine(Account baseAccount, RevenueMethod revenueMethod, boolean includeInformational) {
        if (baseAccount == null || baseAccount.getOffsetAccount() == null || revenueMethod == null) {
            return null;
        }
        JournalEntryLine summaryLine = new JournalEntryLine();
        summaryLine.setAccount(baseAccount.getOffsetAccount());
        summaryLine.setRevenueMethod(revenueMethod);
        summaryLine.setAmount(BigDecimal.ZERO);

        for (ContractJournal contractJournal : contractJournals) {
            for (JournalEntryHeader header : contractJournal.getJournalEntryHeaders()) {
                if (includeInformational == false && header.isInformational()) {
                    continue;
                }
                if (header.getAccount().equals(baseAccount) && revenueMethod.equals(header.getRevenueMethod())) {
                    for (JournalEntryLine existingLine : header.getJournalEntryLines()) {
                        if (existingLine.getAccount().equals(baseAccount.getOffsetAccount()) && existingLine.getRevenueMethod().equals(revenueMethod)) {
                            if (existingLine.getAmount() != null) {
                                summaryLine.setAmount(summaryLine.getAmount().add(existingLine.getAmount()));
                            }
                        }
                    }
                }
            }
        }

        return summaryLine;
    }

    public JournalEntryLine getReportingUnitSummaryJournalEntryLine(Account account, RevenueMethod revenueMethod1, RevenueMethod revenueMethod2, boolean includeInformational) {
        if (account == null || revenueMethod1 == null || revenueMethod2 == null) {
            return null;
        }
        JournalEntryLine summaryLine = new JournalEntryLine();
        summaryLine.setAccount(account);
        //summaryLine.setRevenueMethod(revenueMethod);
        summaryLine.setAmount(BigDecimal.ZERO);

        for (ContractJournal contractJournal : contractJournals) {
            for (JournalEntryHeader header : contractJournal.getJournalEntryHeaders()) {
                if (includeInformational == false && header.isInformational()) {
                    continue;
                }
                if (header.containsAccount(account) && (revenueMethod1.equals(header.getRevenueMethod()) || revenueMethod2.equals(header.getRevenueMethod()))) {
                    for (JournalEntryLine existingLine : header.getJournalEntryLines()) {
                        if (existingLine.getAccount().equals(account) && (existingLine.getRevenueMethod().equals(revenueMethod1) || existingLine.getRevenueMethod().equals(revenueMethod2))) {
                            if (existingLine.getAmount() != null) {
                                summaryLine.setAmount(summaryLine.getAmount().add(existingLine.getAmount()));
                            }
                        }
                    }
                }
            }
        }

        return summaryLine;
    }

    public JournalEntryLine getContractSummaryJournalEntryLine(Contract contract, Account account, RevenueMethod revenueMethod, boolean includeInformational) {
        if (account == null || revenueMethod == null) {
            return null;
        }
        JournalEntryLine summaryLine = new JournalEntryLine();
        summaryLine.setAccount(account);
        summaryLine.setRevenueMethod(revenueMethod);
        summaryLine.setAmount(BigDecimal.ZERO);

        for (ContractJournal contractJournal : contractJournals) {
            if (contractJournal.getContract().equals(contract)) {
                for (JournalEntryHeader header : contractJournal.getJournalEntryHeaders()) {
                    if (includeInformational == false && header.isInformational()) {
                        continue;
                    }
                    if (header.containsAccount(account) && revenueMethod.equals(header.getRevenueMethod())) {
                        for (JournalEntryLine existingLine : header.getJournalEntryLines()) {
                            if (existingLine.getAccount().equals(account) && existingLine.getRevenueMethod().equals(revenueMethod)) {
                                if (existingLine.getAmount() != null) {
                                    summaryLine.setAmount(summaryLine.getAmount().add(existingLine.getAmount()));
                                }
                            }
                        }
                    }
                }
            }
        }

        return summaryLine;
    }

    public void addContractJournal(ContractJournal contractJournal) {
        contractJournals.add(contractJournal);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JournalStatus getJournalStatus() {
        return journalStatus;
    }

    public void setJournalStatus(JournalStatus journalStatus) {
        this.journalStatus = journalStatus;
    }

    public boolean isBalanced() {
        return balanced;
    }

    public void setBalanced(boolean balanced) {
        this.balanced = balanced;
    }

    public FinancialPeriod getPeriod() {
        return period;
    }

    public void setPeriod(FinancialPeriod period) {
        this.period = period;
    }

    public ReportingUnit getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(ReportingUnit reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

}
