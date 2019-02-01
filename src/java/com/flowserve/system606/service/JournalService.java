package com.flowserve.system606.service;

import com.flowserve.system606.model.Account;
import com.flowserve.system606.model.AccountMapping;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.ContractJournal;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.JournalEntryHeader;
import com.flowserve.system606.model.JournalEntryLine;
import com.flowserve.system606.model.JournalEntryType;
import com.flowserve.system606.model.JournalStatus;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.PerformanceObligationGroup;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.ReportingUnitJournal;
import com.flowserve.system606.model.RevenueMethod;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Stateless
public class JournalService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    @Inject
    private CalculationService calculationService;

    public void removeExistingJournals(ReportingUnit ru, FinancialPeriod period) throws Exception {
        for (ReportingUnitJournal ruj : findAllReportingUnitJournals(ru, period)) {
            remove(ruj);
        }
    }

    public ReportingUnitJournal generateJournal(ReportingUnit ru, FinancialPeriod period) throws Exception {
        Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "Generating accounting entries.");
        ReportingUnitJournal reportingUnitJournal = new ReportingUnitJournal(ru, period);
        reportingUnitJournal.setJournalStatus(JournalStatus.NEW);

        for (Contract contract : ru.getUnarchivedContracts()) {
            ContractJournal contractJournal = new ContractJournal(reportingUnitJournal, period, contract);
            contractJournal.setJournalStatus(JournalStatus.NEW);

            for (AccountMapping accountMappingContract : findAllAccountMappingsByOwnerEntityType(MetricType.OWNER_ENTITY_TYPE_CONTRACT)) {
                CurrencyMetric contractMetric = calculationService.getCurrencyMetric(accountMappingContract.getMetricType().getCode(), contract, period);
                JournalEntryHeader journalEntryHeader = new JournalEntryHeader(contractJournal, contractMetric.getMetricType(), accountMappingContract.getAccount(),
                        contract.getLocalCurrency(), LocalDate.now(), JournalEntryType.NORMAL, accountMappingContract.isInformational());
                generateJournalEntryLines(journalEntryHeader, contractMetric);
                contractJournal.addJournalEntryHeader(journalEntryHeader);
            }
            List<PerformanceObligationGroup> pobGroupsByRevenueMethods = new ArrayList<PerformanceObligationGroup>();

            pobGroupsByRevenueMethods.add(new PerformanceObligationGroup("pocPobs", contract, RevenueMethod.PERC_OF_COMP, contract.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP)));
            pobGroupsByRevenueMethods.add(new PerformanceObligationGroup("pitPobs", contract, RevenueMethod.POINT_IN_TIME, contract.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME)));
            pobGroupsByRevenueMethods.add(new PerformanceObligationGroup("rtiPobs", contract, RevenueMethod.RIGHT_TO_INVOICE, contract.getPobsByRevenueMethod(RevenueMethod.RIGHT_TO_INVOICE)));
            pobGroupsByRevenueMethods.add(new PerformanceObligationGroup("slPobs", contract, RevenueMethod.STRAIGHT_LINE, contract.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE)));

            for (AccountMapping accountMappingPOB : findAllAccountMappingsByOwnerEntityType(MetricType.OWNER_ENTITY_TYPE_POB)) {
                for (PerformanceObligationGroup pobGroup : pobGroupsByRevenueMethods) {
                    if (pobGroup.getRevenueMethod().equals(accountMappingPOB.getRevenueMethod())) {
                        CurrencyMetric pobGroupMetric = calculationService.getCurrencyMetric(accountMappingPOB.getMetricType().getCode(), pobGroup, period);
                        JournalEntryHeader journalEntryHeader = new JournalEntryHeader(contractJournal, pobGroupMetric.getMetricType(), accountMappingPOB.getAccount(),
                                contract.getLocalCurrency(), LocalDate.now(), JournalEntryType.NORMAL, accountMappingPOB.isInformational());
                        journalEntryHeader.setRevenueMethod(pobGroup.getRevenueMethod());
                        generateJournalEntryLines(journalEntryHeader, pobGroupMetric);
                        contractJournal.addJournalEntryHeader(journalEntryHeader);
                    }
                }
            }

            reportingUnitJournal.addContractJournal(contractJournal);
        }

        persist(reportingUnitJournal);

        return reportingUnitJournal;
    }

    public void persist(ReportingUnitJournal reportingUnitJournal) throws Exception {
        em.persist(reportingUnitJournal);
    }

    public void remove(ReportingUnitJournal reportingUnitJournal) throws Exception {
        em.remove(reportingUnitJournal);
    }

    public List<Account> findAllAccounts() {
        Query query = em.createQuery("SELECT a FROM Account a");
        return (List<Account>) query.getResultList();
    }

    public List<AccountMapping> findAllAccountMappings() {
        Query query = em.createQuery("SELECT am FROM AccountMapping am");
        return (List<AccountMapping>) query.getResultList();
    }

    public Account findAccountById(String id) {
        return em.find(Account.class, id);
    }

    public List<AccountMapping> findAllAccountMappingsByOwnerEntityType(String ownerEntityType) {
        Query query = em.createQuery("SELECT am FROM AccountMapping am WHERE am.ownerEntityType = :OET");
        query.setParameter("OET", ownerEntityType);
        return (List<AccountMapping>) query.getResultList();
    }

    public List<ReportingUnitJournal> findAllReportingUnitJournals(ReportingUnit ru, FinancialPeriod period) {
        Query query = em.createQuery("SELECT ruj FROM ReportingUnitJournal ruj WHERE ruj.reportingUnit.id = :RU_ID AND ruj.period = :PERIOD");
        query.setParameter("RU_ID", ru.getId());
        query.setParameter("PERIOD", period);
        return (List<ReportingUnitJournal>) query.getResultList();
    }

    private void generateJournalEntryLines(JournalEntryHeader journalEntryHeader, CurrencyMetric metric) {
        Account account = journalEntryHeader.getAccount();
        if (account != null) {
            JournalEntryLine line = new JournalEntryLine(journalEntryHeader, account, journalEntryHeader.getCurrency(), journalEntryHeader.getRevenueMethod());
            setJournalEntryLineAmounts(metric.getLcValue(), line);
            journalEntryHeader.addJournalEntryLine(line);

            Account offsetAccount = account.getOffsetAccount();
            if (offsetAccount != null) {
                JournalEntryLine offsetLine = new JournalEntryLine(journalEntryHeader, offsetAccount, journalEntryHeader.getCurrency(), journalEntryHeader.getRevenueMethod());
                if (accountsAreSameType(account, offsetAccount) && metric.getLcValue() != null) {
                    setJournalEntryLineAmounts(metric.getLcValue().negate(), offsetLine);
                } else {
                    setJournalEntryLineAmounts(metric.getLcValue(), offsetLine);
                }
                journalEntryHeader.addJournalEntryLine(offsetLine);
            }
        }
    }

    private boolean accountsAreSameType(Account account, Account offsetAccount) {
        return ((account.isCredit() && offsetAccount.isCredit()) || (account.isDebit() && offsetAccount.isDebit()));
    }

    private void setJournalEntryLineAmounts(BigDecimal amount, JournalEntryLine line) {
        if (amount == null) {
            line.setAmount(BigDecimal.ZERO);
            return;
        }
        line.setAmount(amount);
    }

    public void generateJournalEntryReport(InputStream inputStream, FileOutputStream outputStream, ReportingUnit reportingUnit, FinancialPeriod period) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet worksheet = workbook.getSheet("JournalEntryRU");

        Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "Generating JE worksheet for RU" + reportingUnit.getCode() + " period " + period.getId());

        ReportingUnitJournal reportingUnitJournal;

        List<ReportingUnitJournal> journals = findAllReportingUnitJournals(reportingUnit, period);

        if (journals.isEmpty() || !reportingUnit.isApproved(period)) {
            if (journals.isEmpty()) {
                Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "No JE acocunting exists.  Generating.");
            }
            if (!reportingUnit.isApproved(period)) {
                Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "JE/RU not yet approved.  Regenerating accounting entries.");
            }
            removeExistingJournals(reportingUnit, period);
            reportingUnitJournal = generateJournal(reportingUnit, period);
        } else {
            if (journals.size() != 1) {
                throw new IllegalStateException("More than one journal exists for RU: " + reportingUnit.getCode() + " which should never occur for RCS phase one.  Please investigate.");
            }
            reportingUnitJournal = journals.get(0);
        }

        worksheet = writeJournalEntryReportWorkbook(reportingUnitJournal, worksheet, reportingUnit, period);

        Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "Finished generating JE worksheet.");
        ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
        ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        workbook.write(outputStream);
        inputStream.close();
        outputStream.close();
    }

    public XSSFSheet writeJournalEntryReportWorkbook(ReportingUnitJournal reportingUnitJournal, XSSFSheet worksheet, ReportingUnit reportingUnit, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        XSSFRow ru_name = worksheet.getRow(2);
        cell = ru_name.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(reportingUnit.getName());
        row = worksheet.getRow(6);
        row.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));

        //BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(reportingUnit, period);
        //BigDecimal liquidatedDamageRecognizePeriodLC = getLiquidatedDamagesRecognizePeriodLC(reportingUnit, period);
        //BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(reportingUnit, period);
        BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(reportingUnit, period);
        //BigDecimal commExp = getThirdPartyCommissionsPeriod(reportingUnit, period);
        // KJG 10/25/2018 - Use total tpc expense.
        BigDecimal commExp = getTotalTPCExpensePeriod(reportingUnit, period);
        BigDecimal fxAdj = getTotalPeriodFxADJLC(reportingUnit, period);
        BigDecimal bilingsPeriodLC = getContractBillingsPeriodLC(reportingUnit, period);

        //Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "commExp: " + commExp.toPlainString());
        //BigDecimal revenueInExcess = getContractRevenueInExcess(reportingUnit, period);
        //BigDecimal billingsInExcess = getContractBillingsInExcess(reportingUnit, period);
        //Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "revenueToRecognizePeriod: " + revenueToRecognizePeriod.toPlainString());
        row = worksheet.getRow(13);
        setCellValue(row, 5, lossReservePeriodADJLC);
        setCellValue(row, 13, lossReservePeriodADJLC.negate());
        row = worksheet.getRow(14);
        setCellValue(row, 6, commExp);
        setCellValue(row, 14, commExp.negate());
        row = worksheet.getRow(15);
        setCellValue(row, 7, fxAdj);
        setCellValue(row, 8, bilingsPeriodLC);
        row = worksheet.getRow(16);
        //Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "getContractAssetPeriod: " + getContractAssetPeriod(reportingUnit, period));
        setCellValue(row, 11, getContractAssetPeriod(reportingUnit, period));
        setCellValue(row, 12, getContractLiabilityPeriod(reportingUnit, period));

        // Split the RU into groups.  We need totals per type of POB, so create 3 groups.  The PerformanceObligationGroup is just a shell Measurable non-entity class used for grouping.
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", reportingUnit, RevenueMethod.PERC_OF_COMP, reportingUnit.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", reportingUnit, RevenueMethod.POINT_IN_TIME, reportingUnit.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        PerformanceObligationGroup rtiPobs = new PerformanceObligationGroup("rtiPobs", reportingUnit, RevenueMethod.RIGHT_TO_INVOICE, reportingUnit.getPobsByRevenueMethod(RevenueMethod.RIGHT_TO_INVOICE));
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", reportingUnit, RevenueMethod.STRAIGHT_LINE, reportingUnit.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));

        PerformanceObligationGroup slAndRTIPobs = new PerformanceObligationGroup("slAndRTIPobs", reportingUnit);
        slAndRTIPobs.addPerformanceObligations(rtiPobs.getPerformanceObligations());
        slAndRTIPobs.addPerformanceObligations(slPobs.getPerformanceObligations());

        printJournalEntryPobsGroups(10, worksheet, pocPobs, period);
        printJournalEntryPobsGroups(11, worksheet, pitPobs, period);
        printJournalEntryPobsGroups(12, worksheet, slAndRTIPobs, period);

        final int DEBIT_COL = 5;
        final int CREDIT_COL = 6;

        final boolean INCLUDE_INFORMATIONAL = true;
        final boolean EXCLUDE_INFORMATIONAL = false;

        /**
         * Combined entries
         */
        int combinedSectionRowNumber = 22;

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine salesPocInLineCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESPOC.IN"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesPocInLineCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesPocInLineCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine salesOutsideLineCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESOC"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesOutsideLineCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesOutsideLineCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine salesLDPenaltyCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESLDPENALTY"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesLDPenaltyCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesLDPenaltyCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine cosPocInCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COSPOC.IN"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, cosPocInCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, cosPocInCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine cosOutsideCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COSOC"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, cosOutsideCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, cosOutsideCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine invWipCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("INV.WIP"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, invWipCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, invWipCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine lossReservePeriodAdjCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("GLVAR.INVADJ"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, lossReservePeriodAdjCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, lossReservePeriodAdjCombined.getCreditAmount());

        combinedSectionRowNumber++;

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine lossReservePeriodAdjContractLiabCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("ACCLOSSRES"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, lossReservePeriodAdjContractLiabCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, lossReservePeriodAdjContractLiabCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine fxGainLossCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("OTHEXP.GLEXCH"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, fxGainLossCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, fxGainLossCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine contractPositionCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("STCONTRACT.REC.GROSS"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contractPositionCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, contractPositionCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine contrLiabCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("STCONTLIAB"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contrLiabCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, contrLiabCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine tpcExpenseCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COMMISH.EXP"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, tpcExpenseCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, tpcExpenseCombined.getCreditAmount());

        row = worksheet.getRow(combinedSectionRowNumber++);
        JournalEntryLine tpcLiabCombined = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("ACCOTHEXP.ROYALTY"), EXCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, tpcLiabCombined.getDebitAmount());
        setCellValue(row, CREDIT_COL, tpcLiabCombined.getCreditAmount());

        /**
         * Component entries
         */
        int componentSectionRowNumber = 41;

        row = worksheet.getRow(43);
        JournalEntryLine salesPocInLinePOC = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESPOC.IN"), RevenueMethod.PERC_OF_COMP, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesPocInLinePOC.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesPocInLinePOC.getCreditAmount());

        row = worksheet.getRow(44);
        JournalEntryLine salesLDPenaltyPOC = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESLDPENALTY"), RevenueMethod.PERC_OF_COMP, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesLDPenaltyPOC.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesLDPenaltyPOC.getCreditAmount());

        row = worksheet.getRow(45);
        JournalEntryLine cosPocInPOC = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COSPOC.IN"), RevenueMethod.PERC_OF_COMP, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, cosPocInPOC.getDebitAmount());
        setCellValue(row, CREDIT_COL, cosPocInPOC.getCreditAmount());

        row = worksheet.getRow(46);
        JournalEntryLine invWipPOC = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("INV.WIP"), RevenueMethod.PERC_OF_COMP, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, invWipPOC.getDebitAmount());
        setCellValue(row, CREDIT_COL, invWipPOC.getCreditAmount());

        row = worksheet.getRow(47);
        JournalEntryLine contrLiabPOC = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("STCONTLIAB"), RevenueMethod.PERC_OF_COMP, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contrLiabPOC.getDebitAmount());
        setCellValue(row, CREDIT_COL, contrLiabPOC.getCreditAmount());

        row = worksheet.getRow(50);
        JournalEntryLine lossReservePeriodAdjContract = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("GLVAR.INVADJ"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, lossReservePeriodAdjContract.getDebitAmount());
        setCellValue(row, CREDIT_COL, lossReservePeriodAdjContract.getCreditAmount());

        row = worksheet.getRow(52);
        JournalEntryLine lossReservePeriodAdjContractLiab = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("ACCLOSSRES"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, lossReservePeriodAdjContractLiab.getDebitAmount());
        setCellValue(row, CREDIT_COL, lossReservePeriodAdjContractLiab.getCreditAmount());

        row = worksheet.getRow(55);
        JournalEntryLine fxGainLoss = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("OTHEXP.GLEXCH"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, fxGainLoss.getDebitAmount());
        setCellValue(row, CREDIT_COL, fxGainLoss.getCreditAmount());

        row = worksheet.getRow(56);
        JournalEntryLine fxGainLossLiab = reportingUnitJournal.getReportingUnitSummaryJournalOffsetEntryLine(findAccountById("OTHEXP.GLEXCH"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, fxGainLossLiab.getDebitAmount());
        setCellValue(row, CREDIT_COL, fxGainLossLiab.getCreditAmount());

        row = worksheet.getRow(59);
        JournalEntryLine contractPosition = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("STCONTRACT.REC.GROSS"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contractPosition.getDebitAmount());
        setCellValue(row, CREDIT_COL, contractPosition.getCreditAmount());

        row = worksheet.getRow(60);
        JournalEntryLine contrLiabContract = reportingUnitJournal.getReportingUnitSummaryJournalOffsetEntryLine(findAccountById("STCONTRACT.REC.GROSS"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contrLiabContract.getDebitAmount());
        setCellValue(row, CREDIT_COL, contrLiabContract.getCreditAmount());

        row = worksheet.getRow(63);
        JournalEntryLine tpcExpense = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COMMISH.EXP"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, tpcExpense.getDebitAmount());
        setCellValue(row, CREDIT_COL, tpcExpense.getCreditAmount());

        row = worksheet.getRow(64);
        JournalEntryLine tpcLiab = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("ACCOTHEXP.ROYALTY"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, tpcLiab.getDebitAmount());
        setCellValue(row, CREDIT_COL, tpcLiab.getCreditAmount());

        row = worksheet.getRow(67);
        JournalEntryLine salesOutside = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESOC"), RevenueMethod.STRAIGHT_LINE, RevenueMethod.RIGHT_TO_INVOICE, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesOutside.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesOutside.getCreditAmount());

        row = worksheet.getRow(68);
        JournalEntryLine salesOutsideLD = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESLDPENALTY"), RevenueMethod.STRAIGHT_LINE, RevenueMethod.RIGHT_TO_INVOICE, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesOutsideLD.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesOutsideLD.getCreditAmount());

        row = worksheet.getRow(69);
        JournalEntryLine cosOsSLRTI = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COSOC"), RevenueMethod.STRAIGHT_LINE, RevenueMethod.RIGHT_TO_INVOICE, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, cosOsSLRTI.getDebitAmount());
        setCellValue(row, CREDIT_COL, cosOsSLRTI.getCreditAmount());

        row = worksheet.getRow(70);
        JournalEntryLine invWipOs = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("INV.WIP"), RevenueMethod.STRAIGHT_LINE, RevenueMethod.RIGHT_TO_INVOICE, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, invWipOs.getDebitAmount());
        setCellValue(row, CREDIT_COL, invWipOs.getCreditAmount());

        row = worksheet.getRow(71);
        JournalEntryLine contrLiabSLRTI = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("STCONTLIAB"), RevenueMethod.STRAIGHT_LINE, RevenueMethod.RIGHT_TO_INVOICE, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contrLiabSLRTI.getDebitAmount());
        setCellValue(row, CREDIT_COL, contrLiabSLRTI.getCreditAmount());

        row = worksheet.getRow(75);
        JournalEntryLine tradeRecContract = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("RECNET"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, tradeRecContract.getDebitAmount());
        setCellValue(row, CREDIT_COL, tradeRecContract.getCreditAmount());

        row = worksheet.getRow(76);
        JournalEntryLine liabContract = reportingUnitJournal.getReportingUnitSummaryJournalOffsetEntryLine(findAccountById("RECNET"), INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, liabContract.getDebitAmount());
        setCellValue(row, CREDIT_COL, liabContract.getCreditAmount());

        row = worksheet.getRow(79);
        JournalEntryLine salesocLinePIT = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESOC"), RevenueMethod.POINT_IN_TIME, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesocLinePIT.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesocLinePIT.getCreditAmount());

        row = worksheet.getRow(80);
        JournalEntryLine salesLDPenaltyPIT = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("SALESLDPENALTY"), RevenueMethod.POINT_IN_TIME, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, salesLDPenaltyPIT.getDebitAmount());
        setCellValue(row, CREDIT_COL, salesLDPenaltyPIT.getCreditAmount());

        row = worksheet.getRow(81);
        JournalEntryLine cosocPIT = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("COSOC"), RevenueMethod.POINT_IN_TIME, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, cosocPIT.getDebitAmount());
        setCellValue(row, CREDIT_COL, cosocPIT.getCreditAmount());

        row = worksheet.getRow(82);
        JournalEntryLine invWipPIT = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("INV.WIP"), RevenueMethod.POINT_IN_TIME, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, invWipPIT.getDebitAmount());
        setCellValue(row, CREDIT_COL, invWipPIT.getCreditAmount());

        row = worksheet.getRow(83);
        JournalEntryLine contrLiabPIT = reportingUnitJournal.getReportingUnitSummaryJournalEntryLine(findAccountById("STCONTLIAB"), RevenueMethod.POINT_IN_TIME, INCLUDE_INFORMATIONAL);
        setCellValue(row, DEBIT_COL, contrLiabPIT.getDebitAmount());
        setCellValue(row, CREDIT_COL, contrLiabPIT.getCreditAmount());

        int rowNumber = 102;
        row = worksheet.getRow(rowNumber);
        for (Contract contract : reportingUnit.getUnarchivedContracts()) {
            row = worksheet.getRow(++rowNumber);
            printJournalEntryContracts(rowNumber, worksheet, contract, period);
        }
        return worksheet;
    }

    public void printJournalEntryContracts(int rowNumber, XSSFSheet worksheet, Contract contract, FinancialPeriod period) throws Exception {

        XSSFRow row;
        BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(contract, period);
        BigDecimal liquidatedDamageRecognizePeriodLC = getLiquidatedDamagesRecognizePeriodLC(contract, period);
        BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(contract, period);
        BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(contract, period);
        BigDecimal totalPeriodFxADJLC = getTotalPeriodFxADJLC(contract, period);
        //BigDecimal commExp = getThirdPartyCommissionsPeriod(contract, period);
        // KJG 10/25/18
        // Switch to total TPC
        BigDecimal commExp = getTotalTPCExpensePeriod(contract, period);
        BigDecimal bilingsPeriodLC = getContractBillingsPeriodLC(contract, period);
        BigDecimal revenueInExcess = getContractAssetPeriod(contract, period);
        BigDecimal billingsInExcess = getContractLiabilityPeriod(contract, period);

        row = worksheet.getRow(rowNumber);
        setCellStringValue(row, 0, contract.getName());
        setCellValue(row, 2, revenueToRecognizePeriod);
        setCellValue(row, 3, liquidatedDamageRecognizePeriodLC);
        setCellValue(row, 4, costGoodsSoldPeriodLC);
        setCellValue(row, 5, lossReservePeriodADJLC);
        setCellValue(row, 6, commExp);
        setCellValue(row, 7, BigDecimal.ZERO);//TODO FX_GAIN_LOSS
        setCellValue(row, 8, bilingsPeriodLC);
        setCellValue(row, 9, costGoodsSoldPeriodLC.negate());

        /**
         *
         *
         *
         *
         *
         *
         *
         *
         * KJG - Need to limit to non-POC pobs only.
         *
         */
        setCellValue(row, 10, lossReservePeriodADJLC);
        setCellValue(row, 11, revenueInExcess);
        setCellValue(row, 12, billingsInExcess);

        /**
         *
         *
         *
         *
         *
         *
         *
         *
         *
         * KJG - Need to limit to POC pobs only.
         *
         */
        setCellValue(row, 13, lossReservePeriodADJLC);
        setCellValue(row, 14, commExp != null ? commExp.negate() : BigDecimal.ZERO);

        // KJG 12/20/18 - Test Output
        setCellValue(row, 15, totalPeriodFxADJLC);
        //Logger.getLogger(JournalService.class.getName()).log(Level.INFO, "JE CONTRACT_TOTAL_FX_ADJ_PERIOD_LC: " + totalPeriodFxADJLC);
    }

    public void printJournalEntryPobsGroups(int rowNumber, XSSFSheet worksheet, PerformanceObligationGroup pobGroup, FinancialPeriod period) throws Exception {

        XSSFRow row;
        BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(pobGroup, period);
        BigDecimal liquidatedDamageRecognizePeriodLC = getLiquidatedDamagesRecognizePeriodLC(pobGroup, period);
        BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(pobGroup, period);
//        BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(pobGroup, period);
//        BigDecimal contractAsset = getContractAssetPeriod(pobGroup, period);
//        BigDecimal contractLiability = getContractLiabilityPeriod(pobGroup, period);
        //BigDecimal billingsPeriod = getContractBillingsPeriodLC(pobGroup, period);
        row = worksheet.getRow(rowNumber);
        setCellValue(row, 2, revenueToRecognizePeriod);
        setCellValue(row, 3, liquidatedDamageRecognizePeriodLC);
        setCellValue(row, 4, costGoodsSoldPeriodLC);
//        setCellValue(row, 5, lossReservePeriodADJLC);
        setCellValue(row, 7, BigDecimal.ZERO);//TODO FX_GAIN_LOSS
        //setCellValue(row, 8, BigDecimal.ZERO);  // ? Contract level billings?
        setCellValue(row, 9, costGoodsSoldPeriodLC.negate());
//        if ("pitPobs".equals(pobGroup.getId()) || "slAndRTIPobs".equals(pobGroup.getId())) {
//            setCellValue(row, 10, lossReservePeriodADJLC);
//        }
//        setCellValue(row, 11, contractAsset);
//        setCellValue(row, 12, contractLiability);
//        if ("pocPobs".equals(pobGroup.getId())) {
//            setCellValue(row, 13, lossReservePeriodADJLC);
//        }
    }

    private void setCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        CellStyle currentStyle = cell.getCellStyle();
        //CellStyle currentStyle = row.getSheet().getWorkbook().createCellStyle();
        CreationHelper ch = row.getSheet().getWorkbook().getCreationHelper();
        cell.setCellValue(value.doubleValue());
        currentStyle.setDataFormat(ch.createDataFormat().getFormat("_(* #,##0_);_(* (#,##0);_(* \"-\"??_);_(@_)"));
        cell.setCellStyle(currentStyle);
    }

    private void setCellStringValue(XSSFRow row, int cellNum, String value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        CellStyle currentStyle = cell.getCellStyle();
        CreationHelper ch = row.getSheet().getWorkbook().getCreationHelper();
        cell.setCellValue(value);
        cell.setCellStyle(currentStyle);
    }

    private BigDecimal getRevenueRecognizePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatedDamagesRecognizePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getCostGoodsSoldPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getLossReservePeriodADJLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, period).getLcValue();
    }

    private BigDecimal getTotalPeriodFxADJLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_TOTAL_FX_ADJ_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractAssetPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_ASSET_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractLiabilityPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_LIABILITY_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractBillingsPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_BILLINGS_PERIOD_CC", measureable, period).getLcValue();
    }

    //private BigDecimal getThirdPartyCommissionsPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
    //    return calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", measureable, period).getLcValue();
    // }
    private BigDecimal getTotalTPCExpensePeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TPC_TOTAL_EXPENSE_PERIOD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }
}
