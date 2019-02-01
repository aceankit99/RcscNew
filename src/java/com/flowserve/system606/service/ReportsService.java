/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.PerformanceObligationGroup;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.RevenueMethod;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
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
public class ReportsService {

    @Inject
    private CalculationService calculationService;
    @Inject
    private FinancialPeriodService financialPeriodService;

    private static final int HEADER_ROW_COUNT = 10;

    public XSSFSheet writeContractSummaryReport(XSSFSheet worksheet, Contract contract, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        XSSFRow rowContract = worksheet.getRow(1);
        cell = rowContract.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(contract.getName());
        XSSFRow rowRU = worksheet.getRow(2);
        cell = rowRU.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(contract.getReportingUnit().getName());
        row = worksheet.getRow(4);
        row.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));

        // Get the contract level values for the contract total row on the "Contract Summary Totals" row.
        row = worksheet.getRow(12);
        setCellValue(row, 11, getLossReserveCTD(contract, period));

        row = worksheet.getRow(13);
        setCellValue(row, 6, getThirdPartyCommissionCTDLC(contract, period));

        // KJG 10/25/2018 - Changing to TPC total expense
        //setCellValue(row, 14, getThirdPartyCommRecognizedCTDLC(contract, period));
        setCellValue(row, 14, getTotalTPCExpenseCTD(contract, period));

        row = worksheet.getRow(14);
        setCellValue(row, 15, getContractBillingsCTDLC(contract, period));

        row = worksheet.getRow(15);
        // KJG 10/1/2018 - The report should show adjusted transaction price.
        //setCellValue(row, 1, getTransactionPrice(contract, period));
        setCellValue(row, 1, getTransactionPriceAdjusted(contract, period));
        setCellValue(row, 2, getLiquidatedDamagesCTD(contract, period));
        setCellValue(row, 3, getEAC(contract, period));
        setCellValue(row, 4, getEstimatedGrossProfit(contract, period));
        setPercentCellValue(row, 5, getEstimatedGrossMargin(contract, period));
        setCellValue(row, 6, getThirdPartyCommissionCTDLC(contract, period));

        setPercentCellValue(row, 7, getPercentComplete(contract, period));
        setCellValue(row, 8, getContractRevenueRecognizeCTD(contract, period));
        setCellValue(row, 9, getLiquidatedDamagesToRecognizeCTD(contract, period));
        setCellValue(row, 10, getCostOfGoodsSoldCTD(contract, period));
        setCellValue(row, 11, getLossReserveCTD(contract, period));
        setCellValue(row, 12, getGrossProfitCTD(contract, period));
        setPercentCellValue(row, 13, getGrossMarginCTD(contract, period));
        //setCellValue(row, 14, getThirdPartyCommRecognizedCTDLC(contract, period));
        // KJG 10/25/2018 - Changing to TPC total expense
        setCellValue(row, 14, getTotalTPCExpenseCTD(contract, period));
        setCellValue(row, 15, getContractBillingsCTDLC(contract, period));

        setCellValue(row, 16, getCostToCompleteLC(contract, period));
        setCellValue(row, 17, getContractAssetCTD(contract, period));
        setCellValue(row, 18, getContractLiabilityCTD(contract, period));

        // Split the contract into groups.  We need totals per type of POB, so create 3 groups.  The PerformanceObligationGroup is just a shell Measurable non-entity class used for grouping.
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", contract, RevenueMethod.PERC_OF_COMP, contract.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", contract, RevenueMethod.POINT_IN_TIME, contract.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", contract, RevenueMethod.STRAIGHT_LINE, contract.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        printContractSummaryPobsGroups(9, 18, worksheet, pocPobs, period);
        printContractSummaryPobsGroups(10, 21, worksheet, pitPobs, period);
        printContractSummaryPobsGroups(11, 24, worksheet, slPobs, period);

        int shiftInRow = printContractSummaryPobsDetailLines(18, 24, worksheet, pocPobs, period);
        shiftInRow = printContractSummaryPobsDetailLines(shiftInRow + 3, shiftInRow + 6, worksheet, pitPobs, period);
        shiftInRow = printContractSummaryPobsDetailLines(shiftInRow + 3, shiftInRow + 6, worksheet, slPobs, period);

        return worksheet;
    }

    public void printContractSummaryPobsGroups(int single, int total, XSSFSheet worksheet, PerformanceObligationGroup pGroup, FinancialPeriod period) throws Exception {

        XSSFRow row;
        // KJG 10/1/2018 - The report should show adjusted transaction price.
        //BigDecimal transactionPrice = getTransactionPrice(pGroup, period);
        BigDecimal transactionPrice = getTransactionPriceAdjusted(pGroup, period);
        BigDecimal liquidatedDamage = getLiquidatedDamagesCTD(pGroup, period);
        BigDecimal EAC = getEAC(pGroup, period);
        BigDecimal estimatedGrossProfit = getEstimatedGrossProfit(pGroup, period);
        BigDecimal estimatedGrossMargin = getEstimatedGrossMargin(pGroup, period);

        BigDecimal localCostCTDLC = getCostOfGoodsSoldCTD(pGroup, period);
        BigDecimal percentComplete = getPercentComplete(pGroup, period);
        BigDecimal revenueCTD = getRevenueRecognizeCTD(pGroup, period);
        BigDecimal liquidatedDamageToRecCTD = getLiquidatedDamagesToRecognizeCTD(pGroup, period);
        BigDecimal lossReserveCTD = getLossReserveCTD(pGroup, period);
        BigDecimal grossProfitCTD = getGrossProfitCTD(pGroup, period);
        BigDecimal grossMarginCTD = getGrossMarginCTD(pGroup, period);
        BigDecimal costToComplete = getCostToCompleteLC(pGroup, period);
        BigDecimal billingsInExcess = getContractLiabilityCTD(pGroup, period);
        BigDecimal revenueInExcess = getContractAssetCTD(pGroup, period);
        BigDecimal billToDate = new BigDecimal(BigInteger.ZERO);

        // Percentage of completion Pobs.  Set total row for POC POBs
        row = worksheet.getRow(single);
        setCellValue(row, 1, transactionPrice);
        setCellValue(row, 2, liquidatedDamage);
        setCellValue(row, 3, EAC);
        setCellValue(row, 4, estimatedGrossProfit);
        setPercentCellValue(row, 5, estimatedGrossMargin);

        setPercentCellValue(row, 7, percentComplete);
        setCellValue(row, 8, revenueCTD);
        setCellValue(row, 9, liquidatedDamageToRecCTD);
        setCellValue(row, 10, localCostCTDLC);
        setCellValue(row, 12, grossProfitCTD);
        setPercentCellValue(row, 13, grossMarginCTD);
        setCellValue(row, 15, billToDate);
        setCellValue(row, 16, costToComplete);
        setCellValue(row, 17, revenueInExcess);
        setCellValue(row, 18, billingsInExcess);

        // We have the same total 8 rows down
        row = worksheet.getRow(total);
        setCellValue(row, 1, transactionPrice);
        setCellValue(row, 2, liquidatedDamage);
        setCellValue(row, 3, EAC);
        setCellValue(row, 4, estimatedGrossProfit);
        setPercentCellValue(row, 5, estimatedGrossMargin);

        //setCellValue(row, 7, percentComplete);
        setCellValue(row, 8, revenueCTD);
        setCellValue(row, 9, liquidatedDamageToRecCTD);
        setCellValue(row, 10, localCostCTDLC);
        setCellValue(row, 12, grossProfitCTD);
        setPercentCellValue(row, 13, grossMarginCTD);
        setCellValue(row, 15, billToDate);
        setCellValue(row, 16, costToComplete);
        setCellValue(row, 17, revenueInExcess);
        setCellValue(row, 18, billingsInExcess);
    }

    public int printContractSummaryPobsDetailLines(int insertRow, int shiftRow, XSSFSheet worksheet, PerformanceObligationGroup pGroup, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        if (pGroup.getPerformanceObligations().size() > 0) {
            worksheet.shiftRows(insertRow, shiftRow, pGroup.getPerformanceObligations().size(), true, false);

            for (PerformanceObligation pob : pGroup.getPerformanceObligations()) {
                row = worksheet.createRow(insertRow);
                cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(pob.getName());
                // KJG 10/1/2018 - The report should show adjusted transaction price.
                //setCellValue(row, 1, getTransactionPrice(pob, period));
                setCellValue(row, 1, getTransactionPriceAdjusted(pob, period));
                setCellValue(row, 2, getLiquidatedDamagesCTD(pob, period));
                setCellValue(row, 3, getEAC(pob, period));
                setCellValue(row, 4, getEstimatedGrossProfit(pob, period));
                setPercentCellValue(row, 5, getEstimatedGrossMargin(pob, period));

                setPercentCellValue(row, 7, getPercentComplete(pob, period));
                setCellValue(row, 8, getRevenueRecognizeCTD(pob, period));
                setCellValue(row, 9, getLiquidatedDamagesToRecognizeCTD(pob, period));
                setCellValue(row, 10, getCostOfGoodsSoldCTD(pob, period));
                setCellValue(row, 12, getGrossProfitCTD(pob, period));
                setPercentCellValue(row, 13, getGrossMarginCTD(pob, period));
                setCellValue(row, 16, getCostToCompleteLC(pob, period));

                insertRow++;
            }
        }
        return insertRow;
    }

//    public void generateReportByFinancialPeriod(InputStream inputStream, FileOutputStream outputStream, Contract contract) throws Exception {
//        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
//            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-1"));
//            XSSFSheet worksheet = workbook.getSheet("Contract Summary-2");
//
//            worksheet = writeReportByFinancialPeriod(worksheet, contract);
//            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
//            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));
//            workbook.write(outputStream);
//        }
//        inputStream.close();
//        outputStream.close();
//
//    }
    public XSSFSheet writeContractSummaryReportByFinancialPeriod(XSSFSheet worksheet, Contract contract, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        int rowid = HEADER_ROW_COUNT;
        XSSFRow contract_name = worksheet.getRow(1);
        cell = contract_name.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(contract.getName());
        XSSFRow ru_name = worksheet.getRow(2);
        cell = ru_name.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(contract.getReportingUnit().getName());

        row = worksheet.getRow(4);
        row.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));

        BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(contract, period);
        BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesPeriod(contract, period);
        BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(contract, period);
        BigDecimal grossProfitPeriodLC = getGrossProfitPeriod(contract, period);
        BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(contract, period);
        BigDecimal tcpIncured = getTPCIncured(contract, period);
        BigDecimal acceleratedTCP = getAcceleratedTPC(contract, period);
        BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);

        row = worksheet.getRow(12);
        setCellValue(row, 4, lossReservePeriodADJLC);

        row = worksheet.getRow(13);
        setCellValue(row, 6, tcpIncured);
        setCellValue(row, 7, acceleratedTCP);

        row = worksheet.getRow(15);
        setCellValue(row, 1, revenueToRecognizePeriod);
        setCellValue(row, 2, liquidatedDamagePeriod);
        setCellValue(row, 3, costGoodsSoldPeriodLC);
        setCellValue(row, 4, lossReservePeriodADJLC);
        setCellValue(row, 5, grossProfitPeriodLC);
        setCellValue(row, 6, tcpIncured);
        setCellValue(row, 7, acceleratedTCP);
        setCellValue(row, 8, operatingIncome);

        List<FinancialPeriod> qtdPeriods = financialPeriodService.getQTDFinancialPeriods(period);
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(contract, qtdPeriods);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(contract, qtdPeriods);
        costGoodsSoldPeriodLC = getAccuCostGoodsSoldPeriodLC(contract, qtdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(contract, qtdPeriods);
        lossReservePeriodADJLC = getAccuLossReservePeriodADJLC(contract, qtdPeriods);
        tcpIncured = getAccuTPCIncured(contract, qtdPeriods);
        acceleratedTCP = getAccuAcceleratedTPC(contract, qtdPeriods);
        operatingIncome = getAccuOperatingIncomePeriod(contract, qtdPeriods);

        row = worksheet.getRow(12);
        setCellValue(row, 12, lossReservePeriodADJLC);

        row = worksheet.getRow(13);
        setCellValue(row, 14, tcpIncured);
        setCellValue(row, 15, acceleratedTCP);

        row = worksheet.getRow(15);
        setCellValue(row, 9, revenueToRecognizePeriod);
        setCellValue(row, 10, liquidatedDamagePeriod);
        setCellValue(row, 11, costGoodsSoldPeriodLC);
        setCellValue(row, 12, lossReservePeriodADJLC);
        setCellValue(row, 13, grossProfitPeriodLC);
        setCellValue(row, 14, tcpIncured);
        setCellValue(row, 15, acceleratedTCP);
        setCellValue(row, 16, operatingIncome);

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(contract, ytdPeriods);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(contract, ytdPeriods);
        costGoodsSoldPeriodLC = getAccuCostGoodsSoldPeriodLC(contract, ytdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(contract, ytdPeriods);
        lossReservePeriodADJLC = getAccuLossReservePeriodADJLC(contract, ytdPeriods);
        tcpIncured = getAccuTPCIncured(contract, ytdPeriods);
        acceleratedTCP = getAccuAcceleratedTPC(contract, ytdPeriods);
        operatingIncome = getAccuOperatingIncomePeriod(contract, ytdPeriods);

        row = worksheet.getRow(12);
        setCellValue(row, 20, lossReservePeriodADJLC);

        row = worksheet.getRow(13);
        setCellValue(row, 22, tcpIncured);
        setCellValue(row, 23, acceleratedTCP);

        row = worksheet.getRow(15);
        setCellValue(row, 17, revenueToRecognizePeriod);
        setCellValue(row, 18, liquidatedDamagePeriod);
        setCellValue(row, 19, costGoodsSoldPeriodLC);
        setCellValue(row, 20, lossReservePeriodADJLC);
        setCellValue(row, 21, grossProfitPeriodLC);
        setCellValue(row, 22, tcpIncured);
        setCellValue(row, 23, acceleratedTCP);
        setCellValue(row, 24, operatingIncome);

        // Split the contract into groups.  We need totals per type of POB, so create 3 groups.  The PerformanceObligationGroup is just a shell Measurable non-entity class used for grouping.
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", contract, RevenueMethod.PERC_OF_COMP, contract.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", contract, RevenueMethod.POINT_IN_TIME, contract.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", contract, RevenueMethod.STRAIGHT_LINE, contract.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        printFinancialPobsGroups(9, 18, worksheet, pocPobs, period, qtdPeriods, ytdPeriods);
        printFinancialPobsGroups(10, 21, worksheet, pitPobs, period, qtdPeriods, ytdPeriods);
        printFinancialPobsGroups(11, 24, worksheet, slPobs, period, qtdPeriods, ytdPeriods);

        int shiftInRow = printFinancialPobsDetailLines(18, 24, worksheet, pocPobs, period, qtdPeriods, ytdPeriods);
        shiftInRow = printFinancialPobsDetailLines(shiftInRow + 3, shiftInRow + 6, worksheet, pitPobs, period, qtdPeriods, ytdPeriods);
        shiftInRow = printFinancialPobsDetailLines(shiftInRow + 3, shiftInRow + 6, worksheet, slPobs, period, qtdPeriods, ytdPeriods);
        return worksheet;
    }

    public void printFinancialPobsGroups(int rowNumber, int totalRow, XSSFSheet worksheet, PerformanceObligationGroup pGroup, FinancialPeriod period, List<FinancialPeriod> qtdPeriods, List<FinancialPeriod> ytdPeriods) throws Exception {

        XSSFRow row;
        //for monthly report
        BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(pGroup, period);
        BigDecimal liquidatedDamage = getLiquidatedDamagesPeriod(pGroup, period);
        BigDecimal costOfGoodsSoldPeriod = getCostGoodsSoldPeriodLC(pGroup, period);
        BigDecimal grossProfitPeriodLC = getGrossProfitPeriod(pGroup, period);

        row = worksheet.getRow(rowNumber);
        setCellValue(row, 1, revenueToRecognizePeriod);
        setCellValue(row, 2, liquidatedDamage);
        setCellValue(row, 3, costOfGoodsSoldPeriod);
        setCellValue(row, 5, grossProfitPeriodLC);
        // Also set gross profit on operating income cell since we can't include TPC at the POB level.
        setCellValue(row, 8, grossProfitPeriodLC);

        row = worksheet.getRow(totalRow);
        setCellValue(row, 1, revenueToRecognizePeriod);
        setCellValue(row, 2, liquidatedDamage);
        setCellValue(row, 3, costOfGoodsSoldPeriod);
        setCellValue(row, 5, grossProfitPeriodLC);
        // Also set gross profit on operating income cell since we can't include TPC at the POB level.
        setCellValue(row, 8, grossProfitPeriodLC);

        //for quartly report
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(pGroup, qtdPeriods);
        liquidatedDamage = getAccuLiquidatedDamagesPeriod(pGroup, qtdPeriods);
        costOfGoodsSoldPeriod = getAccuCostGoodsSoldPeriodLC(pGroup, qtdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(pGroup, qtdPeriods);

        row = worksheet.getRow(rowNumber);
        setCellValue(row, 9, revenueToRecognizePeriod);
        setCellValue(row, 10, liquidatedDamage);
        setCellValue(row, 11, costOfGoodsSoldPeriod);
        setCellValue(row, 13, grossProfitPeriodLC);
        // Also set gross profit on operating income cell since we can't include TPC at the POB level.
        setCellValue(row, 16, grossProfitPeriodLC);

        row = worksheet.getRow(totalRow);
        setCellValue(row, 9, revenueToRecognizePeriod);
        setCellValue(row, 10, liquidatedDamage);
        setCellValue(row, 11, costOfGoodsSoldPeriod);
        setCellValue(row, 13, grossProfitPeriodLC);
        // Also set gross profit on operating income cell since we can't include TPC at the POB level.
        setCellValue(row, 16, grossProfitPeriodLC);

        //for annually report
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(pGroup, ytdPeriods);
        liquidatedDamage = getAccuLiquidatedDamagesPeriod(pGroup, ytdPeriods);
        costOfGoodsSoldPeriod = getAccuCostGoodsSoldPeriodLC(pGroup, ytdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(pGroup, ytdPeriods);

        row = worksheet.getRow(rowNumber);
        setCellValue(row, 17, revenueToRecognizePeriod);
        setCellValue(row, 18, liquidatedDamage);
        setCellValue(row, 19, costOfGoodsSoldPeriod);
        setCellValue(row, 21, grossProfitPeriodLC);
        // Also set gross profit on operating income cell since we can't include TPC at the POB level.
        setCellValue(row, 24, grossProfitPeriodLC);

        row = worksheet.getRow(totalRow);
        setCellValue(row, 17, revenueToRecognizePeriod);
        setCellValue(row, 18, liquidatedDamage);
        setCellValue(row, 19, costOfGoodsSoldPeriod);
        setCellValue(row, 21, grossProfitPeriodLC);
        // Also set gross profit on operating income cell since we can't include TPC at the POB level.
        setCellValue(row, 24, grossProfitPeriodLC);

    }

    public int printFinancialPobsDetailLines(int insertRow, int shiftRow, XSSFSheet worksheet, PerformanceObligationGroup pGroup, FinancialPeriod period, List<FinancialPeriod> qtdPeriods, List<FinancialPeriod> ytdPeriods) throws Exception {
        XSSFRow row;
        Cell cell = null;
        if (pGroup.getPerformanceObligations().size() > 0) {
            worksheet.shiftRows(insertRow, shiftRow, pGroup.getPerformanceObligations().size(), true, false);

            for (PerformanceObligation pob : pGroup.getPerformanceObligations()) {
                //for monthly report
                BigDecimal revenueToRecognize = getRevenueRecognizePeriodLC(pob, period);
                BigDecimal liquidatedDamage = getLiquidatedDamagesPeriod(pob, period);
                BigDecimal costOfGoodsSoldPeriod = getCostGoodsSoldPeriodLC(pob, period);
                BigDecimal grossProfitPeriod = getGrossProfitPeriod(pob, period);

                row = worksheet.createRow(insertRow);
                cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(pob.getName());
                setCellValue(row, 1, revenueToRecognize);
                setCellValue(row, 2, liquidatedDamage);
                setCellValue(row, 3, costOfGoodsSoldPeriod);
                setCellValue(row, 5, grossProfitPeriod);
                // Also set gross profit on operating income cell since we can't include TPC at the POB level.
                setCellValue(row, 8, grossProfitPeriod);

                //for quaterly report
                revenueToRecognize = getAccuRevenueToRecognizePeriodLC(pob, qtdPeriods);
                liquidatedDamage = getAccuLiquidatedDamagesPeriod(pob, qtdPeriods);
                // KJG 11/5/2018 - Build 1.11.5.1 - This was commented out.  Enabling.
                costOfGoodsSoldPeriod = getAccuCostGoodsSoldPeriodLC(pob, qtdPeriods);
                grossProfitPeriod = getAccuGrossProfitPeriodLC(pob, qtdPeriods);

                setCellValue(row, 9, revenueToRecognize);
                setCellValue(row, 10, liquidatedDamage);
                setCellValue(row, 11, costOfGoodsSoldPeriod);
                setCellValue(row, 13, grossProfitPeriod);
                // Also set gross profit on operating income cell since we can't include TPC at the POB level.
                setCellValue(row, 16, grossProfitPeriod);

                //for annual report
                revenueToRecognize = getAccuRevenueToRecognizePeriodLC(pob, ytdPeriods);
                liquidatedDamage = getAccuLiquidatedDamagesPeriod(pob, ytdPeriods);
                costOfGoodsSoldPeriod = getAccuCostGoodsSoldPeriodLC(pob, ytdPeriods);
                grossProfitPeriod = getAccuGrossProfitPeriodLC(pob, ytdPeriods);

                setCellValue(row, 17, revenueToRecognize);
                setCellValue(row, 18, liquidatedDamage);
                setCellValue(row, 19, costOfGoodsSoldPeriod);
                setCellValue(row, 21, grossProfitPeriod);
                // Also set gross profit on operating income cell since we can't include TPC at the POB level.
                setCellValue(row, 24, grossProfitPeriod);
                insertRow++;
            }
        }
        return insertRow;
    }

    public void generateContractSummaryReport(InputStream inputStream, FileOutputStream outputStream, Contract contract, FinancialPeriod period) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            workbook.removeSheetAt(workbook.getSheetIndex("Reporting Unit Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Reporting Unit Summary-2"));
            XSSFSheet worksheet = workbook.getSheet("Contract Summary-1");
            worksheet = writeContractSummaryReport(worksheet, contract, period);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            worksheet = workbook.getSheet("Contract Summary-2");
            worksheet = writeContractSummaryReportByFinancialPeriod(worksheet, contract, period);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    public void generateReportFinancialSummary(InputStream inputStream, FileOutputStream outputStream, Contract contract, FinancialPeriod period) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-6"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-7"));
            XSSFSheet worksheet = workbook.getSheet("Financial Summary-1");

            worksheet = writeFinancialSummary(worksheet, contract, period);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    public XSSFSheet writeFinancialSummary(XSSFSheet worksheet, Contract contract, FinancialPeriod period) throws Exception {
        Cell cell = null;
        XSSFRow rowTitle = worksheet.getRow(1);
        cell = rowTitle.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(contract.getName());
        XSSFRow rowDate = worksheet.getRow(3);
        rowDate.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));
        List<String> periodHead = financialPeriodService.getAllMonthStrings(period.getPeriodYear());
        for (int i = 0; i < periodHead.size(); i++) {
            int colNum = i + 1;
            XSSFRow row = worksheet.getRow(3);
            Cell cellHead = row.getCell(colNum);
            cellHead.setCellValue(periodHead.get(i));
        }

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        for (int i = 0; i < ytdPeriods.size(); i++) {
            if (period.getPeriodYear() != 2017) {
                int colNum = i + 1;
                printFinancialSummaryByPobs(colNum, worksheet, contract, ytdPeriods.get(i));
            } else {
                int colNum = i + 11;
                printFinancialSummaryByPobs(colNum, worksheet, contract, ytdPeriods.get(i));
            }
        }

        printFinancialSummaryCTD(18, worksheet, contract, period);
        printFinancialSummaryAccummulate(17, worksheet, contract, ytdPeriods);

        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        printIfQNotZeroForContract(13, worksheet, contract, Q1);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        printIfQNotZeroForContract(14, worksheet, contract, Q2);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        printIfQNotZeroForContract(15, worksheet, contract, Q3);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);
        printIfQNotZeroForContract(16, worksheet, contract, Q4);

        return worksheet;
    }

    public void printFinancialSummaryByPobs(int colNum, XSSFSheet worksheet, Contract contract, FinancialPeriod period) throws Exception {

        XSSFRow row;
        int startRow = 7;
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", contract, RevenueMethod.PERC_OF_COMP, contract.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", contract, RevenueMethod.POINT_IN_TIME, contract.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", contract, RevenueMethod.STRAIGHT_LINE, contract.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        BigDecimal revenueToRecognize = getRevenueRecognizePeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizePeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizePeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);

        startRow = startRow + 2;
        BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);

        startRow = startRow + 2;
        BigDecimal netRevenue = getNetPeriodSalesLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getNetPeriodSalesLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getNetPeriodSalesLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);

        startRow = startRow + 2;
        BigDecimal costsIncurredCOGS = getCostGoodsSoldPeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostGoodsSoldPeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostGoodsSoldPeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);

        startRow = startRow + 1;
        BigDecimal reserveLCM_COGS = getLossReservePeriodADJLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS);

        startRow = startRow + 1;
        BigDecimal totalCOGS = getTotalCostGoodsSoldPeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, totalCOGS);

        startRow = startRow + 1;
        BigDecimal grossProfitPeriod = getGrossProfitPeriod(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitPeriod);
        grossProfitPeriod = getGrossProfitPeriod(pocPobs, period);
        //Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "grossProfitPeriod: " + grossProfitPeriod.toPlainString());
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitPeriod);
        grossProfitPeriod = getGrossProfitPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitPeriod);
        grossProfitPeriod = getGrossProfitPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitPeriod);

        startRow = startRow + 1;
        BigDecimal grossMarginCTD = getGrossMarginPeriod(contract, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);

        startRow = startRow + 1;
        BigDecimal tcp = getTPCIncured(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tcp);
        BigDecimal acceleratedTPC = getAcceleratedTPC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, acceleratedTPC);
        BigDecimal thirdPartyCommRegLC = getTotalTPCExpensePeriod(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, thirdPartyCommRegLC);

        startRow = startRow + 1;
        BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        // KJG : We are not yet calculating Fx gain/loss, so we repeat the OI value instead.
        startRow = startRow + 2;
        //BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        startRow = startRow + 4;
        BigDecimal contractBillingsCTD = getContractBillingsCTDLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractBillingsCTD);

        // KJG 10/19/2018 - Inventory WIP - Reserve - Changing to COGS credit amount
        BigDecimal cogsCTD = getCostOfGoodsSoldCTD(contract, period);
        row = worksheet.getRow(startRow++);
        // KJG 10/22/2018 - changing back to zero.
        setCellValue(row, colNum, (cogsCTD == null ? BigDecimal.ZERO : cogsCTD.negate()));
        //setCellValue(row, colNum, BigDecimal.ZERO);

        BigDecimal contractAsset = getContractAssetCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractAsset);

        startRow = startRow + 3;

        //BigDecimal cogsCTD = getCostOfGoodsSoldCTD(contract, period);
        row = worksheet.getRow(startRow++);
        //setCellValue(row, colNum, cogsCTD);
        setCellValue(row, colNum, BigDecimal.ZERO);

        BigDecimal contractLia = getContractLiabilityCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractLia);

        // KJG 10/22/18 - Using loss reserve CTD
        BigDecimal lossReserveCTD = getLossReserveCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, lossReserveCTD);
        //BigDecimal lossReservePeriodAdj = getLossReservePeriodADJLC(contract, period);
        //row = worksheet.getRow(startRow++);
        //setCellValue(row, colNum, lossReservePeriodAdj);

        BigDecimal tpcCTD = getTotalTPCExpenseCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tpcCTD);

        startRow = startRow + 2;
        BigDecimal retainedEarnings = getRetainedEarningsLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, retainedEarnings);

        startRow = startRow + 4;
        BigDecimal contractRevenueToRecognize = getRevenueRecognizeCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractRevenueToRecognize);

        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractBillingsCTD != null ? contractBillingsCTD.negate() : BigDecimal.ZERO);

        startRow = startRow + 1;
        startRow = startRow + 1;

        // KJG 10/22/18 - Changing to use backlog
        //BigDecimal projectedGainLossBacklog = getProjectedGainLossBacklogLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS != null ? reserveLCM_COGS.negate() : BigDecimal.ZERO);
        //setCellValue(row, colNum, projectedGainLossBacklog != null ? projectedGainLossBacklog : BigDecimal.ZERO);
    }

    public void printIfQNotZeroForContract(int colNum, XSSFSheet worksheet, Contract contract, List<FinancialPeriod> period) throws Exception {
        if (period.size() != 0) {
            printFinancialSummaryAccummulate(colNum, worksheet, contract, period);
        }
    }

    public void printFinancialSummaryAccummulate(int colNum, XSSFSheet worksheet, Contract contract, List<FinancialPeriod> period) throws Exception {
        XSSFRow row;
        int startRow = 7;
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", contract, RevenueMethod.PERC_OF_COMP, contract.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        //calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", contract, RevenueMethod.POINT_IN_TIME, contract.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        //calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", contract, RevenueMethod.STRAIGHT_LINE, contract.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        //calculationService.executeBusinessRules(slPobs, period);

        BigDecimal revenueToRecognize = getAccuRevenueToRecognizePeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getAccuRevenueToRecognizePeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getAccuRevenueToRecognizePeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);

        startRow = startRow + 2;
        BigDecimal liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        //getting NPE
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);

        startRow = startRow + 2;
        BigDecimal netRevenue = getAccuNetPeriodSalesLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getAccuNetPeriodSalesLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getAccuNetPeriodSalesLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);

        startRow = startRow + 2;
        BigDecimal costsIncurredCOGS = getAccuCostGoodsSoldPeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getAccuCostGoodsSoldPeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getAccuCostGoodsSoldPeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);

        startRow = startRow + 1;
        BigDecimal reserveLCM_COGS = getAccuLossReservePeriodADJLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS);

        startRow = startRow + 1;
        BigDecimal totalCOGS = getAccuTotalCostGoodsSoldPeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, totalCOGS);

        startRow = startRow + 1;
        BigDecimal grossProfitCTD = getAccuGrossProfitPeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getAccuGrossProfitPeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getAccuGrossProfitPeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getAccuGrossProfitPeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);

        startRow = startRow + 1;
        BigDecimal grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);

        startRow = startRow + 1;
        BigDecimal tcp = getAccuTPCIncured(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tcp);
        BigDecimal acceleratedTPC = getAccuAcceleratedTPC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, acceleratedTPC);
        BigDecimal thirdPartyCommRegLC = getAccuTotalTPCExpensePeriod(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, thirdPartyCommRegLC);

        startRow = startRow + 1;
        BigDecimal operatingIncome = getAccuOperatingIncomePeriod(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        // KJG : We are not yet calculating Fx gain/loss, so we repeat the OI value instead.
        startRow = startRow + 2;
        //BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);
    }

    public void printFinancialSummaryCTD(int colNum, XSSFSheet worksheet, Contract contract, FinancialPeriod period) throws Exception {
        XSSFRow row;
        int startRow = 7;
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", contract, RevenueMethod.PERC_OF_COMP, contract.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", contract, RevenueMethod.POINT_IN_TIME, contract.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", contract, RevenueMethod.STRAIGHT_LINE, contract.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        BigDecimal revenueToRecognize = getRevenueRecognizeCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizeCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizeCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);

        startRow = startRow + 2;
        BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesToRecognizeCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesToRecognizeCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        //getting NPE
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesToRecognizeCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);

        startRow = startRow + 2;
        BigDecimal netRevenue = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);

        startRow = startRow + 2;
        BigDecimal costsIncurredCOGS = getCostOfGoodsSoldCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostOfGoodsSoldCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostOfGoodsSoldCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);

        startRow = startRow + 1;
        BigDecimal reserveLCM_COGS = getLossReserveCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS);

        startRow = startRow + 1;
        BigDecimal totalCOGS = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, totalCOGS);

        startRow = startRow + 1;
        BigDecimal grossProfitCTD = getGrossProfitCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);

        startRow = startRow + 1;
        BigDecimal grossMarginCTD = getGrossMarginCTD(contract, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);

        startRow = startRow + 1;
        BigDecimal tcp = getThirdPartyCommRecognizedCTDLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tcp);
        BigDecimal acceleratedTPC = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, acceleratedTPC);
        BigDecimal thirdPartyCommRegLC = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, thirdPartyCommRegLC);

        startRow = startRow + 1;
        BigDecimal operatingIncome = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        // KJG : We are not yet calculating Fx gain/loss, so we repeat the OI value instead.
        startRow = startRow + 2;
        //BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);
    }

    public void generateRUReportFinancialSummary(InputStream inputStream, FileOutputStream outputStream, ReportingUnit ru, FinancialPeriod period) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-6"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-7"));
            XSSFSheet worksheet = workbook.getSheet("Financial Summary-1");

            worksheet = writeFinancialSummaryRULevel(worksheet, ru, period);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    public XSSFSheet writeFinancialSummaryRULevel(XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period) throws Exception {
        Cell cell = null;
        XSSFRow rowTitle = worksheet.getRow(1);
        cell = rowTitle.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(ru.getName());
        XSSFRow rowDate = worksheet.getRow(3);
        rowDate.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));

        List<String> periodHead = financialPeriodService.getAllMonthStrings(period.getPeriodYear());
        for (int i = 0; i < periodHead.size(); i++) {
            int colNum = i + 1;
            XSSFRow row = worksheet.getRow(3);
            Cell cellHead = row.getCell(colNum);
            cellHead.setCellValue(periodHead.get(i));
        }

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        for (int i = 0; i < ytdPeriods.size(); i++) {
            if (period.getPeriodYear() != 2017) {
                int colNum = i + 1;
                printFinancialSummaryByPobsRULevel(colNum, worksheet, ru, ytdPeriods.get(i));
            } else {
                int colNum = i + 11;
                printFinancialSummaryByPobsRULevel(colNum, worksheet, ru, ytdPeriods.get(i));
            }
        }

        printFSReportRULevelCTD(18, worksheet, ru, period);
        printFSReportRULevelAccummulate(17, worksheet, ru, ytdPeriods);

        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        printIfQNotZero(13, worksheet, ru, Q1);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        printIfQNotZero(14, worksheet, ru, Q2);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        printIfQNotZero(15, worksheet, ru, Q3);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);
        printIfQNotZero(16, worksheet, ru, Q4);

        return worksheet;
    }

    public void printFinancialSummaryByPobsRULevel(int colNum, XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period) throws Exception {

        XSSFRow row;
        int startRow = 7;
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", ru, RevenueMethod.PERC_OF_COMP, ru.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", ru, RevenueMethod.POINT_IN_TIME, ru.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", ru, RevenueMethod.STRAIGHT_LINE, ru.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        // Run calcs in order to get margin at RU level.  Only one calc runs so this call should be quick.
        calculationService.executeBusinessRules(ru, period);

        BigDecimal revenueToRecognize = getRevenueRecognizePeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizePeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizePeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);

        startRow = startRow + 2;
        BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        //getting NPE
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);

        startRow = startRow + 2;
        BigDecimal netRevenue = getNetPeriodSalesLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getNetPeriodSalesLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getNetPeriodSalesLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);

        startRow = startRow + 2;
        BigDecimal costsIncurredCOGS = getCostGoodsSoldPeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostGoodsSoldPeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostGoodsSoldPeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);

        startRow = startRow + 1;

        BigDecimal reserveLCM_COGS = getLossReservePeriodADJLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS);

        startRow = startRow + 1;
        BigDecimal totalCOGS = getTotalCostGoodsSoldPeriodLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, totalCOGS);

        startRow = startRow + 1;
        BigDecimal grossProfitCTD = getGrossProfitPeriod(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);

        startRow = startRow + 1;
        BigDecimal grossMarginCTD = getGrossMarginPeriod(ru, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);

        startRow = startRow + 1;
        BigDecimal tcp = getTPCIncured(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tcp);
        BigDecimal acceleratedTPC = getAcceleratedTPC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, acceleratedTPC);
        BigDecimal thirdPartyCommRegLC = getTotalTPCExpensePeriod(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, thirdPartyCommRegLC);

        startRow = startRow + 1;
        BigDecimal operatingIncome = getOperatingIncomePeriodLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        // KJG : We are not yet calculating Fx gain/loss, so we repeat the OI value instead.
        startRow = startRow + 2;
        //BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        startRow = startRow + 4;
        BigDecimal contractBillingsCTD = getContractBillingsCTDLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractBillingsCTD);

        // KJG 10/19/2018 - Inventory WIP - Reserve - Changing to COGS credit amount
        BigDecimal cogsCTD = getCostOfGoodsSoldCTD(ru, period);
        row = worksheet.getRow(startRow++);
        // KJG 10/22/2018 - changing back to zero.
        setCellValue(row, colNum, (cogsCTD == null ? BigDecimal.ZERO : cogsCTD.negate()));
        //setCellValue(row, colNum, BigDecimal.ZERO);

        BigDecimal contractAsset = getContractAssetCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractAsset);

        startRow = startRow + 3;

        //BigDecimal cogsCTD = getCostOfGoodsSoldCTD(ru, period);
        row = worksheet.getRow(startRow++);
        //setCellValue(row, colNum, cogsCTD);
        setCellValue(row, colNum, BigDecimal.ZERO);

        BigDecimal contractLia = getContractLiabilityCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractLia);

        // KJG 10/22/18 - Using loss reserve CTD
        BigDecimal lossReserveCTD = getLossReserveCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, lossReserveCTD);
        //BigDecimal lossReservePeriodAdj = getLossReservePeriodADJLC(ru, period);
        //row = worksheet.getRow(startRow++);
        //setCellValue(row, colNum, lossReservePeriodAdj);

        BigDecimal tpcCTD = getTotalTPCExpenseCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tpcCTD);

        startRow = startRow + 2;
        BigDecimal retainedEarnings = getRetainedEarningsLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, retainedEarnings);

        startRow = startRow + 4;
        BigDecimal contractRevenueToRecognize = getRevenueRecognizeCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractRevenueToRecognize);

        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, contractBillingsCTD != null ? contractBillingsCTD.negate() : BigDecimal.ZERO);

        startRow = startRow + 1;
        startRow = startRow + 1;

        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS != null ? reserveLCM_COGS.negate() : BigDecimal.ZERO);
    }

    public void printIfQNotZero(int colNum, XSSFSheet worksheet, ReportingUnit ru, List<FinancialPeriod> period) throws Exception {
        if (period.size() != 0) {
            printFSReportRULevelAccummulate(colNum, worksheet, ru, period);
        }
    }

    public void printFSReportRULevelAccummulate(int colNum, XSSFSheet worksheet, ReportingUnit ru, List<FinancialPeriod> period) throws Exception {

        XSSFRow row;
        int startRow = 7;
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", ru, RevenueMethod.PERC_OF_COMP, ru.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        //calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", ru, RevenueMethod.POINT_IN_TIME, ru.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        //calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", ru, RevenueMethod.STRAIGHT_LINE, ru.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        //calculationService.executeBusinessRules(slPobs, period);

        BigDecimal revenueToRecognize = getAccuRevenueToRecognizePeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getAccuRevenueToRecognizePeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getAccuRevenueToRecognizePeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);

        startRow = startRow + 2;
        BigDecimal liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);

        startRow = startRow + 2;
        BigDecimal netRevenue = getAccuNetPeriodSalesLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getAccuNetPeriodSalesLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = getAccuNetPeriodSalesLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);

        startRow = startRow + 2;
        BigDecimal costsIncurredCOGS = getAccuCostGoodsSoldPeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getAccuCostGoodsSoldPeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getAccuCostGoodsSoldPeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);

        startRow = startRow + 1;
        BigDecimal reserveLCM_COGS = getAccuLossReservePeriodADJLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS);

        startRow = startRow + 1;
        BigDecimal totalCOGS = getAccuTotalCostGoodsSoldPeriodLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, totalCOGS);

        startRow = startRow + 1;
        BigDecimal grossProfitCTD = getAccuGrossProfitPeriodLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getAccuGrossProfitPeriodLC(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getAccuGrossProfitPeriodLC(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getAccuGrossProfitPeriodLC(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);

        startRow = startRow + 1;
        BigDecimal grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        //setPercentCellValue(row, colNum, grossMarginCTD);

        startRow = startRow + 1;
        BigDecimal tcp = getAccuTPCIncured(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tcp);
        BigDecimal acceleratedTPC = getAccuAcceleratedTPC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, acceleratedTPC);
        BigDecimal thirdPartyCommRegLC = getAccuTotalTPCExpensePeriod(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, thirdPartyCommRegLC);

        startRow = startRow + 1;
        BigDecimal operatingIncome = getAccuOperatingIncomePeriod(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        // KJG : We are not yet calculating Fx gain/loss, so we repeat the OI value instead.
        startRow = startRow + 2;
        //BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

    }

    public void printFSReportRULevelCTD(int colNum, XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period) throws Exception {
        XSSFRow row;
        int startRow = 7;
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", ru, RevenueMethod.PERC_OF_COMP, ru.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", ru, RevenueMethod.POINT_IN_TIME, ru.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", ru, RevenueMethod.STRAIGHT_LINE, ru.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        // Run calcs in order to get margin at RU level.  Only one calc runs so this call should be quick.
        calculationService.executeBusinessRules(ru, period);

        BigDecimal revenueToRecognize = getRevenueRecognizeCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizeCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);
        revenueToRecognize = getRevenueRecognizeCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, revenueToRecognize);

        startRow = startRow + 2;
        BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesToRecognizeCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesToRecognizeCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        //getting NPE
        setCellValue(row, colNum, liquidatedDamagePeriod);
        liquidatedDamagePeriod = getLiquidatedDamagesToRecognizeCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, liquidatedDamagePeriod);

        startRow = startRow + 2;
        BigDecimal netRevenue = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);
        netRevenue = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, netRevenue);

        startRow = startRow + 2;
        BigDecimal costsIncurredCOGS = getCostOfGoodsSoldCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostOfGoodsSoldCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);
        costsIncurredCOGS = getCostOfGoodsSoldCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, costsIncurredCOGS);

        startRow = startRow + 1;
        BigDecimal reserveLCM_COGS = getLossReserveCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, reserveLCM_COGS);

        startRow = startRow + 1;
        BigDecimal totalCOGS = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, totalCOGS);

        startRow = startRow + 1;
        BigDecimal grossProfitCTD = getGrossProfitCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);
        grossProfitCTD = getGrossProfitCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, grossProfitCTD);

        startRow = startRow + 1;
        BigDecimal grossMarginCTD = getGrossMarginCTD(ru, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginCTD(pocPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginCTD(pitPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);
        grossMarginCTD = getGrossMarginCTD(slPobs, period);
        row = worksheet.getRow(startRow++);
        setPercentCellValue(row, colNum, grossMarginCTD);

        startRow = startRow + 1;
        BigDecimal tcp = getThirdPartyCommRecognizedCTDLC(ru, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, tcp);
        BigDecimal acceleratedTPC = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, acceleratedTPC);
        BigDecimal thirdPartyCommRegLC = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, thirdPartyCommRegLC);

        startRow = startRow + 1;
        BigDecimal operatingIncome = BigDecimal.ZERO;
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);

        // KJG : We are not yet calculating Fx gain/loss, so we repeat the OI value instead.
        startRow = startRow + 2;
        //BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);
        row = worksheet.getRow(startRow++);
        setCellValue(row, colNum, operatingIncome);
    }

    public void generateContractSummaryReportingUnitLevelReport(InputStream inputStream, FileOutputStream outputStream, ReportingUnit reportingUnit, FinancialPeriod period) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-2"));
            XSSFSheet worksheet = workbook.getSheet("Reporting Unit Summary-1");

            worksheet = writeContractSummaryReportingUnitLevelReport(worksheet, reportingUnit, period);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            worksheet = workbook.getSheet("Reporting Unit Summary-2");
            worksheet = writeRUReportByFinancialPeriod(worksheet, reportingUnit, period);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    public XSSFSheet writeContractSummaryReportingUnitLevelReport(XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        XSSFRow rowContract = worksheet.getRow(1);
        cell = rowContract.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(ru.getName());
        row = worksheet.getRow(3);
        row.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));

        BigDecimal lossReserveCTD = getLossReserveCTD(ru, period);

        row = worksheet.getRow(11);
        setCellValue(row, 11, lossReserveCTD);

        row = worksheet.getRow(12);
        setCellValue(row, 6, getThirdPartyCommissionCTDLC(ru, period));
        // KJG 10/25/2018 - changing to total expense.
        //setCellValue(row, 14, getThirdPartyCommRecognizedCTDLC(ru, period));
        setCellValue(row, 14, getTotalTPCExpenseCTD(ru, period));

        // Get the contract level values for the contract total row on the "Contract Summary Totals" row.
        row = worksheet.getRow(13);
        setCellValue(row, 15, getContractBillingsCTDLC(ru, period));

        row = worksheet.getRow(14);
        // KJG 10/1/2018 - The report should show adjusted transaction price.
        setCellValue(row, 1, getTransactionPriceAdjusted(ru, period));
        setCellValue(row, 2, getLiquidatedDamagesCTD(ru, period));
        setCellValue(row, 3, getEAC(ru, period));
        setCellValue(row, 4, getEstimatedGrossProfit(ru, period));
        // Run calcs in order to get margin at RU level.  Only one calc runs so this call should be quick.
        calculationService.executeBusinessRules(ru, period);
        setPercentCellValue(row, 5, getEstimatedGrossMargin(ru, period));
        setCellValue(row, 6, getThirdPartyCommissionCTDLC(ru, period));

        setPercentCellValue(row, 7, getPercentComplete(ru, period));
        setCellValue(row, 8, getRevenueRecognizeCTD(ru, period));
        setCellValue(row, 9, getLiquidatedDamagesToRecognizeCTD(ru, period));
        setCellValue(row, 10, getCostOfGoodsSoldCTD(ru, period));
        setCellValue(row, 11, getLossReserveCTD(ru, period));
        setCellValue(row, 12, getGrossProfitCTD(ru, period));
        // Is the below valud at RU level?
        setPercentCellValue(row, 13, getGrossMarginCTD(ru, period));

        //setCellValue(row, 14, getThirdPartyCommRecognizedCTDLC(ru, period));
        // KJG 10/25/2018 - Changing to show tpc total expense
        setCellValue(row, 14, getTotalTPCExpenseCTD(ru, period));
        setCellValue(row, 15, getContractBillingsCTDLC(ru, period));
        setCellValue(row, 16, getCostToCompleteLC(ru, period));
        setCellValue(row, 17, getContractAssetCTD(ru, period));
        setCellValue(row, 18, getContractLiabilityCTD(ru, period));

        // Split the POBs into groups of TransientMeasurables.  Execute rules for group level calcs.
        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", ru, RevenueMethod.PERC_OF_COMP, ru.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", ru, RevenueMethod.POINT_IN_TIME, ru.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        //PerformanceObligationGroup rtiPobs = new PerformanceObligationGroup("rtiPobs", ru, RevenueMethod.RIGHT_TO_INVOICE, ru.getPobsByRevenueMethod(RevenueMethod.RIGHT_TO_INVOICE));
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", ru, RevenueMethod.STRAIGHT_LINE, ru.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        printContractSummaryRULevelPobsGroups(8, worksheet, pocPobs, period);
        printContractSummaryRULevelPobsGroups(9, worksheet, pitPobs, period);
        printContractSummaryRULevelPobsGroups(10, worksheet, slPobs, period);

        printRUTotalContractLevel(18, worksheet, ru, period);

        return worksheet;
    }

    public void printContractSummaryRULevelPobsGroups(int rowNumber, XSSFSheet worksheet, PerformanceObligationGroup pGroup, FinancialPeriod period) throws Exception {

        XSSFRow row;
        // KJG 10/1/2018 - The report should show adjusted transaction price.
        BigDecimal transactionPrice = getTransactionPriceAdjusted(pGroup, period);
        BigDecimal liquidatedDamage = getLiquidatedDamagesCTD(pGroup, period);
        BigDecimal EAC = getEAC(pGroup, period);
        BigDecimal estimatedGrossProfit = getEstimatedGrossProfit(pGroup, period);
        //Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "ReportsService: estimatedGrossProfit: " + estimatedGrossProfit.toPlainString());
        BigDecimal estimatedGrossMargin = getEstimatedGrossMargin(pGroup, period);

        BigDecimal localCostCTDLC = getCostOfGoodsSoldCTD(pGroup, period);
        BigDecimal percentComplete = getPercentComplete(pGroup, period);
        //Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "POBGroup % Complete: " + percentComplete.toPlainString());
        BigDecimal revenueCTD = getRevenueRecognizeCTD(pGroup, period);
        BigDecimal liquidatedDamageToRecCTD = getLiquidatedDamagesToRecognizeCTD(pGroup, period);
        BigDecimal lossReserveCTD = getLossReserveCTD(pGroup, period);
        BigDecimal grossProfitCTD = getGrossProfitCTD(pGroup, period);
        BigDecimal grossMarginCTD = getGrossMarginCTD(pGroup, period);
        BigDecimal costToComplete = getCostToCompleteLC(pGroup, period);
        BigDecimal billingsInExcess = getContractLiabilityCTD(pGroup, period);
        BigDecimal revenueInExcess = getContractAssetCTD(pGroup, period);
        BigDecimal billToDate = new BigDecimal(BigInteger.ZERO);

        row = worksheet.getRow(rowNumber);

        setCellValue(row, 1, transactionPrice);
        setCellValue(row, 2, liquidatedDamage);
        setCellValue(row, 3, EAC);
        setCellValue(row, 4, estimatedGrossProfit);
        setPercentCellValue(row, 5, estimatedGrossMargin);

        setPercentCellValue(row, 7, percentComplete);
        setCellValue(row, 8, revenueCTD);
        setCellValue(row, 9, liquidatedDamageToRecCTD);
        setCellValue(row, 10, localCostCTDLC);
        setCellValue(row, 11, lossReserveCTD);
        setCellValue(row, 12, grossProfitCTD);
        setPercentCellValue(row, 13, grossMarginCTD);
        setCellValue(row, 15, billToDate);
        setCellValue(row, 16, costToComplete);
        setCellValue(row, 17, revenueInExcess);
        setCellValue(row, 18, billingsInExcess);
    }

    public void printRUTotalContractLevel(int insertRow, XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        if (ru.getUnarchivedContracts().size() > 0) {

            for (Contract contract : ru.getUnarchivedContracts()) {
                // KJG 10/1/2018 - The report should show adjusted transaction price.
                BigDecimal transactionPrice = getTransactionPriceAdjusted(contract, period);
                BigDecimal liquidatedDamage = getLiquidatedDamagesCTD(contract, period);
                BigDecimal EAC = getEAC(contract, period);
                BigDecimal estimatedGrossProfit = getEstimatedGrossProfit(contract, period);
                BigDecimal estimatedGrossMargin = getEstimatedGrossMargin(contract, period);

                BigDecimal liquidatedDamageToRecCTD = getLiquidatedDamagesToRecognizeCTD(contract, period);
                BigDecimal localCostCTDLC = getCostOfGoodsSoldCTD(contract, period);
                BigDecimal percentComplete = getPercentComplete(contract, period);
                BigDecimal revenueCTD = getRevenueRecognizeCTD(contract, period);
                BigDecimal lossReserveCTD = getLossReserveCTD(contract, period);
                BigDecimal grossProfitCTD = getGrossProfitCTD(contract, period);
                BigDecimal grossMarginCTD = getGrossMarginCTD(contract, period);
                BigDecimal costToComplete = getCostToCompleteLC(contract, period);
                BigDecimal contractLiability = getContractLiabilityCTD(contract, period);
                BigDecimal contractAsset = getContractAssetCTD(contract, period);
                BigDecimal billToDate = getContractBillingsCTDLC(contract, period);
                BigDecimal thirdPartyCommCTD = getThirdPartyCommissionCTDLC(contract, period);
                //BigDecimal thirdPartyRecogLC = getThirdPartyCommRecognizedCTDLC(contract, period);
                // KJG 10/25/2018 - Changing to TPC total expense.
                BigDecimal tpcTotalExpense = getTotalTPCExpenseCTD(contract, period);

                row = worksheet.createRow(insertRow);
                cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(contract.getName());
                setCellValue(row, 1, transactionPrice);
                setCellValue(row, 2, liquidatedDamage);
                setCellValue(row, 3, EAC);
                setCellValue(row, 4, estimatedGrossProfit);
                setPercentCellValue(row, 5, estimatedGrossMargin);
                setCellValue(row, 6, thirdPartyCommCTD);
                setPercentCellValue(row, 7, percentComplete);
                setCellValue(row, 8, revenueCTD);
                setCellValue(row, 9, liquidatedDamageToRecCTD);
                setCellValue(row, 10, localCostCTDLC);
                setCellValue(row, 11, lossReserveCTD);
                setCellValue(row, 12, grossProfitCTD);
                setPercentCellValue(row, 13, grossMarginCTD);
                setCellValue(row, 14, tpcTotalExpense);
                setCellValue(row, 15, billToDate);
                setCellValue(row, 16, costToComplete);
                setCellValue(row, 17, contractAsset);
                setCellValue(row, 18, contractLiability);

                insertRow++;
            }
        }
    }

    public XSSFSheet writeRUReportByFinancialPeriod(XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period) throws Exception {
        XSSFRow row;
        Cell cell = null;
        int rowid = HEADER_ROW_COUNT;
        XSSFRow ru_name = worksheet.getRow(1);
        cell = ru_name.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(ru.getName());

        row = worksheet.getRow(3);
        row.getCell(0).setCellValue(Date.valueOf(period.getEndDate()));

        BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(ru, period);
        BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesPeriod(ru, period);
        BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(ru, period);
        BigDecimal grossProfitPeriodLC = getGrossProfitPeriod(ru, period);
        BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(ru, period);
        BigDecimal tcpIncured = getTPCIncured(ru, period);
        BigDecimal acceleratedTCP = getAcceleratedTPC(ru, period);
        BigDecimal operatingIncome = getOperatingIncomePeriodLC(ru, period);

        row = worksheet.getRow(11);
        setCellValue(row, 4, lossReservePeriodADJLC);

        row = worksheet.getRow(12);
        setCellValue(row, 6, tcpIncured);
        setCellValue(row, 7, acceleratedTCP);

        row = worksheet.getRow(14);
        setCellValue(row, 1, revenueToRecognizePeriod);
        setCellValue(row, 2, liquidatedDamagePeriod);
        setCellValue(row, 3, costGoodsSoldPeriodLC);
        setCellValue(row, 4, lossReservePeriodADJLC);
        setCellValue(row, 5, grossProfitPeriodLC);
        setCellValue(row, 6, tcpIncured);
        setCellValue(row, 7, acceleratedTCP);
        setCellValue(row, 8, operatingIncome);

        List<FinancialPeriod> qtdPeriods = financialPeriodService.getQTDFinancialPeriods(period);
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(ru, qtdPeriods);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(ru, qtdPeriods);
        costGoodsSoldPeriodLC = getAccuCostGoodsSoldPeriodLC(ru, qtdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(ru, qtdPeriods);
        lossReservePeriodADJLC = getAccuLossReservePeriodADJLC(ru, qtdPeriods);
        tcpIncured = getAccuTPCIncured(ru, qtdPeriods);
        acceleratedTCP = getAccuAcceleratedTPC(ru, qtdPeriods);
        operatingIncome = getAccuOperatingIncomePeriod(ru, qtdPeriods);

        row = worksheet.getRow(11);
        setCellValue(row, 12, lossReservePeriodADJLC);

        row = worksheet.getRow(12);
        setCellValue(row, 14, tcpIncured);
        setCellValue(row, 15, acceleratedTCP);

        row = worksheet.getRow(14);
        setCellValue(row, 9, revenueToRecognizePeriod);
        setCellValue(row, 10, liquidatedDamagePeriod);
        setCellValue(row, 11, costGoodsSoldPeriodLC);
        setCellValue(row, 12, lossReservePeriodADJLC);
        setCellValue(row, 13, grossProfitPeriodLC);
        setCellValue(row, 14, tcpIncured);
        setCellValue(row, 15, acceleratedTCP);
        setCellValue(row, 16, operatingIncome);

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(ru, ytdPeriods);
        liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(ru, ytdPeriods);
        costGoodsSoldPeriodLC = getAccuCostGoodsSoldPeriodLC(ru, ytdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(ru, ytdPeriods);
        lossReservePeriodADJLC = getAccuLossReservePeriodADJLC(ru, ytdPeriods);
        tcpIncured = getAccuTPCIncured(ru, ytdPeriods);
        acceleratedTCP = getAccuAcceleratedTPC(ru, ytdPeriods);
        operatingIncome = getAccuOperatingIncomePeriod(ru, ytdPeriods);

        row = worksheet.getRow(11);
        setCellValue(row, 20, lossReservePeriodADJLC);

        row = worksheet.getRow(12);
        setCellValue(row, 22, tcpIncured);
        setCellValue(row, 23, acceleratedTCP);

        row = worksheet.getRow(14);
        setCellValue(row, 17, revenueToRecognizePeriod);
        setCellValue(row, 18, liquidatedDamagePeriod);
        setCellValue(row, 19, costGoodsSoldPeriodLC);
        setCellValue(row, 20, lossReservePeriodADJLC);
        setCellValue(row, 21, grossProfitPeriodLC);
        setCellValue(row, 22, tcpIncured);
        setCellValue(row, 23, acceleratedTCP);
        setCellValue(row, 24, operatingIncome);

        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", ru, RevenueMethod.PERC_OF_COMP, ru.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        calculationService.executeBusinessRules(pocPobs, period);
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", ru, RevenueMethod.POINT_IN_TIME, ru.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        calculationService.executeBusinessRules(pitPobs, period);
        PerformanceObligationGroup slPobs = new PerformanceObligationGroup("slPobs", ru, RevenueMethod.STRAIGHT_LINE, ru.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        calculationService.executeBusinessRules(slPobs, period);

        printRUFinancialPobsGroups(8, worksheet, pocPobs, period, qtdPeriods, ytdPeriods);
        printRUFinancialPobsGroups(9, worksheet, pitPobs, period, qtdPeriods, ytdPeriods);
        printRUFinancialPobsGroups(10, worksheet, slPobs, period, qtdPeriods, ytdPeriods);

        printRUFinancialContract(18, worksheet, ru, period, qtdPeriods, ytdPeriods);
        return worksheet;
    }

    public void printRUFinancialPobsGroups(int single, XSSFSheet worksheet, PerformanceObligationGroup pGroup, FinancialPeriod period, List<FinancialPeriod> qtdPeriods, List<FinancialPeriod> ytdPeriods) throws Exception {

        XSSFRow row;
        //for monthly report
        BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(pGroup, period);
        BigDecimal liquidatedDamage = getLiquidatedDamagesPeriod(pGroup, period);
        BigDecimal costOfGoodsSoldPeriod = getCostGoodsSoldPeriodLC(pGroup, period);
        BigDecimal grossProfitPeriodLC = getGrossProfitPeriod(pGroup, period);

        row = worksheet.getRow(single);
        setCellValue(row, 1, revenueToRecognizePeriod);
        setCellValue(row, 2, liquidatedDamage);
        setCellValue(row, 3, costOfGoodsSoldPeriod);
        setCellValue(row, 5, grossProfitPeriodLC);
        setCellValue(row, 8, grossProfitPeriodLC);  // duplicated for pob level

        //for quartly report
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(pGroup, qtdPeriods);
        liquidatedDamage = getAccuLiquidatedDamagesPeriod(pGroup, qtdPeriods);
        costOfGoodsSoldPeriod = getAccuCostGoodsSoldPeriodLC(pGroup, qtdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(pGroup, qtdPeriods);

        row = worksheet.getRow(single);
        setCellValue(row, 9, revenueToRecognizePeriod);
        setCellValue(row, 10, liquidatedDamage);
        setCellValue(row, 11, costOfGoodsSoldPeriod);
        setCellValue(row, 13, grossProfitPeriodLC);
        setCellValue(row, 16, grossProfitPeriodLC);  // duplicated for pob level

        //for annually report
        revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(pGroup, ytdPeriods);
        liquidatedDamage = getAccuLiquidatedDamagesPeriod(pGroup, ytdPeriods);
        costOfGoodsSoldPeriod = getAccuCostGoodsSoldPeriodLC(pGroup, ytdPeriods);
        grossProfitPeriodLC = getAccuGrossProfitPeriodLC(pGroup, ytdPeriods);

        row = worksheet.getRow(single);
        setCellValue(row, 17, revenueToRecognizePeriod);
        setCellValue(row, 18, liquidatedDamage);
        setCellValue(row, 19, costOfGoodsSoldPeriod);
        setCellValue(row, 21, grossProfitPeriodLC);
        setCellValue(row, 24, grossProfitPeriodLC);  // duplicated for pob level

    }

    public void printRUFinancialContract(int insertRow, XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period, List<FinancialPeriod> qtdPeriods, List<FinancialPeriod> ytdPeriods) throws Exception {
        XSSFRow row;
        Cell cell = null;
        if (ru.getUnarchivedContracts().size() > 0) {

            for (Contract contract : ru.getUnarchivedContracts()) {
                //for monthly report
                BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(contract, period);
                BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesPeriod(contract, period);
                BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(contract, period);
                BigDecimal grossProfitPeriodLC = getGrossProfitPeriod(contract, period);
                BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(contract, period);
                BigDecimal tcpIncured = getTPCIncured(contract, period);
                BigDecimal acceleratedTCP = getAcceleratedTPC(contract, period);
                BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);

                row = worksheet.createRow(insertRow);
                cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(contract.getName());
                setCellValue(row, 1, revenueToRecognizePeriod);
                setCellValue(row, 2, liquidatedDamagePeriod);
                setCellValue(row, 3, costGoodsSoldPeriodLC);
                setCellValue(row, 4, lossReservePeriodADJLC);
                setCellValue(row, 5, grossProfitPeriodLC);
                setCellValue(row, 6, tcpIncured);
                setCellValue(row, 7, acceleratedTCP);
                setCellValue(row, 8, operatingIncome);

                //for quaterly report
                revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(contract, qtdPeriods);
                liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(contract, qtdPeriods);
                costGoodsSoldPeriodLC = getAccuCostGoodsSoldPeriodLC(contract, qtdPeriods);
                grossProfitPeriodLC = getAccuGrossProfitPeriodLC(contract, qtdPeriods);
                lossReservePeriodADJLC = getAccuLossReservePeriodADJLC(contract, qtdPeriods);
                tcpIncured = getAccuTPCIncured(contract, qtdPeriods);
                acceleratedTCP = getAccuAcceleratedTPC(contract, qtdPeriods);
                operatingIncome = getAccuOperatingIncomePeriod(contract, qtdPeriods);

                setCellValue(row, 9, revenueToRecognizePeriod);
                setCellValue(row, 10, liquidatedDamagePeriod);
                setCellValue(row, 11, costGoodsSoldPeriodLC);
                setCellValue(row, 12, lossReservePeriodADJLC);
                setCellValue(row, 13, grossProfitPeriodLC);
                setCellValue(row, 14, tcpIncured);
                setCellValue(row, 15, acceleratedTCP);
                setCellValue(row, 16, operatingIncome);

                //for annually report
                revenueToRecognizePeriod = getAccuRevenueToRecognizePeriodLC(contract, ytdPeriods);
                liquidatedDamagePeriod = getAccuLiquidatedDamagesPeriod(contract, ytdPeriods);
                costGoodsSoldPeriodLC = getAccuCostGoodsSoldPeriodLC(contract, ytdPeriods);
                grossProfitPeriodLC = getAccuGrossProfitPeriodLC(contract, ytdPeriods);
                lossReservePeriodADJLC = getAccuLossReservePeriodADJLC(contract, ytdPeriods);
                tcpIncured = getAccuTPCIncured(contract, ytdPeriods);
                acceleratedTCP = getAccuAcceleratedTPC(contract, ytdPeriods);
                operatingIncome = getAccuOperatingIncomePeriod(contract, ytdPeriods);

                setCellValue(row, 17, revenueToRecognizePeriod);
                setCellValue(row, 18, liquidatedDamagePeriod);
                setCellValue(row, 19, costGoodsSoldPeriodLC);
                setCellValue(row, 20, lossReservePeriodADJLC);
                setCellValue(row, 21, grossProfitPeriodLC);
                setCellValue(row, 22, tcpIncured);
                setCellValue(row, 23, acceleratedTCP);
                setCellValue(row, 24, operatingIncome);
                insertRow++;
            }
        }
    }

    public void generateCompanyReportFinancialSummary(InputStream inputStream, FileOutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-6"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures-7"));

            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    public void generateReportDisclosures(InputStream inputStream, FileOutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-4"));
            workbook.removeSheetAt(workbook.getSheetIndex("Contract Summary-5"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-1"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-2"));
            workbook.removeSheetAt(workbook.getSheetIndex("Financial Summary-3"));
            workbook.removeSheetAt(workbook.getSheetIndex("Disclosures"));

            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    public void generateReportJournalEntry(InputStream inputStream, FileOutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            workbook.removeSheetAt(workbook.getSheetIndex("Journal Entry-2"));

            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

    }

    private void setCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        //CellStyle currentStyle = cell.getCellStyle();
        CellStyle currentStyle = row.getSheet().getWorkbook().createCellStyle();
        CreationHelper ch = row.getSheet().getWorkbook().getCreationHelper();
        cell.setCellValue(value.doubleValue());
        currentStyle.setDataFormat(ch.createDataFormat().getFormat("_(* #,##0_);_(* (#,##0);_(* \"-\"??_);_(@_)"));
        cell.setCellStyle(currentStyle);

    }

    private void setPercentCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        //CellStyle currentStyle = cell.getCellStyle();
        CellStyle currentStyle = row.getSheet().getWorkbook().createCellStyle();
        CreationHelper ch = row.getSheet().getWorkbook().getCreationHelper();
        cell.setCellValue(value.doubleValue());
        currentStyle.setDataFormat(ch.createDataFormat().getFormat("0.00%"));
        cell.setCellStyle(currentStyle);
    }

    private BigDecimal getTransactionPrice(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getTransactionPriceAdjusted(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_ADJ_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getLiquidatedDamagesCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getLiquidatedDamagesToRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getLiquidatedDamagesPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getTPCIncured(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getAcceleratedTPC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TPC_TO_ACCEL_PERIOD_ADJ_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;

//        return BigDecimal.ZERO;
    }

    private BigDecimal getTotalTPCExpensePeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TPC_TOTAL_EXPENSE_PERIOD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getTotalTPCExpenseCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TPC_TOTAL_EXPENSE_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getEAC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("ESTIMATED_COST_AT_COMPLETION_LC", measureable, period).getLcValue();
    }

    private BigDecimal getEstimatedGrossProfit(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("ESTIMATED_GROSS_PROFIT_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossProfitPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("GROSS_PROFIT_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossMarginPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getDecimalMetric("GROSS_MARGIN_PERIOD", measureable, period).getValue();
    }

    private BigDecimal getEstimatedGrossMargin(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("ESTIMATED_GROSS_MARGIN", measureable, period).getValue();
    }

    private BigDecimal getGrossProfitCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("GROSS_PROFIT_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossMarginCTD(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("GROSS_MARGIN_CTD", measureable, period).getValue();
    }

    private BigDecimal getCostOfGoodsSoldCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getPercentComplete(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("PERCENT_COMPLETE", measureable, period).getValue();
    }

    private BigDecimal getRevenueRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getContractRevenueRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_REVENUE_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLossReserveCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LOSS_RESERVE_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getCostToCompleteLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_TO_COMPLETE_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractLiabilityCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_LIABILITY_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractAssetCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_ASSET_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractBillingsCTDLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_BILLINGS_CTD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getRetainedEarningsLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("RETAINED_EARNINGS_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractBillingsPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_BILLINGS_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getProjectedGainLossBacklogLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("PROJECTED_GAIN_LOSS_BACKLOG_LC", measureable, period).getLcValue();
    }

    private BigDecimal getThirdPartyCommissionCTDLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getThirdPartyCommRecognizedCTDLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getThirdPartyCommissionsPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getCumulativeCostGoodsSoldLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getRevenueRecognizePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getNetPeriodSalesLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("NET_PERIOD_SALES_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatedDamagesRecognizePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getCostGoodsSoldPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getTotalCostGoodsSoldPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("TOTAL_COST_GOODS_SOLD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getOperatingIncomePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("OPERATING_INCOME_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getLossReservePeriodADJLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, period).getLcValue();
    }

    private BigDecimal getAccuLossReservePeriodADJLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuTPCIncured(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuAcceleratedTPC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TPC_TO_ACCEL_PERIOD_ADJ_LC", measureable, qtdPeriods).getLcValue();

        //return BigDecimal.ZERO;
    }

    private BigDecimal getAccuRevenueToRecognizeLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("REVENUE_TO_RECOGNIZE_CTD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuRevenueToRecognizePeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuLiquidatedDamagesPeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuOperatingIncomePeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("OPERATING_INCOME_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuLiquidatedDamageToRecognizeCTDCC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuCumulativeCostGoodsSoldLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("COST_OF_GOODS_SOLD_CTD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuCostGoodsSoldPeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("COST_OF_GOODS_SOLD_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuEstimatedGrossProfitLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("ESTIMATED_GROSS_PROFIT_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuGrossProfitPeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("GROSS_PROFIT_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuNetPeriodSalesLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("NET_PERIOD_SALES_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuTotalCostGoodsSoldPeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TOTAL_COST_GOODS_SOLD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuTotalTPCExpensePeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TPC_TOTAL_EXPENSE_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuTotalTPCExpenseCTD(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TPC_TOTAL_EXPENSE_CTD_LC", measureable, qtdPeriods).getLcValue();
    }

    BigDecimal zeroIfNull(BigDecimal decimal) {
        if (decimal == null) {
            return BigDecimal.ZERO;
        }

        return decimal;
    }

    Date sqlDateConverter(LocalDate date) {
        if (date == null) {
            return null;
        }
        return java.sql.Date.valueOf(date);
    }
}
