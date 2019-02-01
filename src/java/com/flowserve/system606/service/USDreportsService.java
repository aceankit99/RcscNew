/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.ReportingUnit;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author shubhamc
 */
@Stateless
public class USDreportsService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    private CellStyle stringCellStyle;
    private CellStyle currencyCellStyle;
    private CellStyle percentageCellStyle;

    @Inject
    private AdminService adminService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private FinancialPeriodService financialPeriodService;

    public void generateReport5USDreportsTab1(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period, ReportingUnit ru) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            workbook.removeSheetAt(workbook.getSheetIndex("Report 5 - USD Reports Tab 2"));
            XSSFSheet worksheet = workbook.getSheet("Report 5 - USD Reports Tab 1");

            writeReport5Tab1USDreport(worksheet, period, ru);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();
    }

    public void generateReport5USDreportsTab2(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            workbook.removeSheetAt(workbook.getSheetIndex("Report 5 - USD Reports Tab 1"));
            XSSFSheet worksheet = workbook.getSheet("Report 5 - USD Reports Tab 2");
            writeReport5Tab2USDreport(worksheet, period);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();
    }

    public XSSFSheet writeReport5Tab1USDreport(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

        stringCellStyle = worksheet.getWorkbook().createCellStyle();
        Font font = worksheet.getWorkbook().createFont();
        font.setFontHeightInPoints((short) 9);
        stringCellStyle.setFont(font);

        currencyCellStyle = worksheet.getWorkbook().createCellStyle();
        CreationHelper ch = worksheet.getWorkbook().getCreationHelper();
        currencyCellStyle.setDataFormat(ch.createDataFormat().getFormat("_(* #,##0_);_(* (#,##0);_(* \"-\"??_);_(@_)"));
        currencyCellStyle.setFont(font);

        percentageCellStyle = worksheet.getWorkbook().createCellStyle();
        CreationHelper pCH = worksheet.getWorkbook().getCreationHelper();
        percentageCellStyle.setDataFormat(pCH.createDataFormat().getFormat("0.00%"));

        XSSFRow rowPeriod = worksheet.getRow(3);
        rowPeriod.getCell(0).setCellValue(period.getName());

        XSSFRow row;
        row = worksheet.getRow(1);
        setCellStringValue(row, 0, "Total Flowserve");
        int bUnitStartIndex = 8;
        for (BusinessUnit bu : adminService.findBusinessUnits()) {
            row = worksheet.getRow(bUnitStartIndex++);
            setCellStringValue(row, 0, bu.getName() + " Platform");
            setCellValue(row, 1, getTransactionPriceAdjusted(bu, period));
            setCellValue(row, 2, getLiquidatedDamagesCTD(bu, period));
            setCellValue(row, 3, getEAC(bu, period));
            setCellValue(row, 4, getEstimatedGrossProfit(bu, period));
            setPercentCellValue(row, 5, getEstimatedGrossMargin(bu, period));
            setCellValue(row, 6, getThirdPartyCommissionCTDLC(bu, period));
            setPercentCellValue(row, 7, getPercentComplete(bu, period));
            setCellValue(row, 8, getRevenueRecognizeCTD(bu, period));
            setCellValue(row, 9, getLiquidatedDamagesToRecognizeCTD(bu, period));
            setCellValue(row, 10, getCostOfGoodsSoldCTD(bu, period));
            setCellValue(row, 11, getLossReserveCTD(bu, period));
            setCellValue(row, 12, getGrossProfitCTD(bu, period));
            setPercentCellValue(row, 13, getGrossMarginCTD(bu, period));
            setCellValue(row, 14, getTotalTPCExpenseCTD(bu, period));
            setCellValue(row, 15, getContractBillingsCTDLC(bu, period));
            setCellValue(row, 16, getCostToCompleteLC(bu, period));
            setCellValue(row, 17, getContractAssetCTD(bu, period));
            setCellValue(row, 18, getContractLiabilityCTD(bu, period));
        }

        int rowNum = 16;
        List<ReportingUnit> ruList = adminService.findAllReportingUnits();

        for (ReportingUnit runit : ruList) {

            row = worksheet.getRow(rowNum);
            if (row == null) {
                row = worksheet.createRow(rowNum);

            }

            setCellStringValue(row, 0, runit.getName());
            setCellValue(row, 1, getTransactionPriceAdjusted(runit, period));
            setCellValue(row, 2, getLiquidatedDamagesCTD(runit, period));
            setCellValue(row, 3, getEAC(runit, period));
            setCellValue(row, 4, getEstimatedGrossProfit(runit, period));
            setPercentCellValue(row, 5, getEstimatedGrossMargin(runit, period));
            setCellValue(row, 6, getThirdPartyCommissionCTDLC(runit, period));
            setPercentCellValue(row, 7, getPercentComplete(runit, period));
            setCellValue(row, 8, getRevenueRecognizeCTD(runit, period));
            setCellValue(row, 9, getLiquidatedDamagesToRecognizeCTD(runit, period));
            setCellValue(row, 10, getCostOfGoodsSoldCTD(runit, period));
            setCellValue(row, 11, getLossReserveCTD(runit, period));
            setCellValue(row, 12, getGrossProfitCTD(runit, period));
            setPercentCellValue(row, 13, getGrossMarginCTD(runit, period));
            setCellValue(row, 14, getTotalTPCExpenseCTD(runit, period));
            setCellValue(row, 15, getContractBillingsCTDLC(runit, period));
            setCellValue(row, 16, getCostToCompleteLC(runit, period));
            setCellValue(row, 17, getContractAssetCTD(runit, period));
            setCellValue(row, 18, getContractLiabilityCTD(runit, period));

            rowNum++;

        }

        return worksheet;
    }

    public XSSFSheet writeReport5Tab2USDreport(XSSFSheet worksheet, FinancialPeriod period) throws Exception {

        stringCellStyle = worksheet.getWorkbook().createCellStyle();
        Font font = worksheet.getWorkbook().createFont();
        font.setFontHeightInPoints((short) 9);
        stringCellStyle.setFont(font);

        currencyCellStyle = worksheet.getWorkbook().createCellStyle();
        CreationHelper ch = worksheet.getWorkbook().getCreationHelper();
        currencyCellStyle.setDataFormat(ch.createDataFormat().getFormat("_(* #,##0_);_(* (#,##0);_(* \"-\"??_);_(@_)"));
        currencyCellStyle.setFont(font);

        percentageCellStyle = worksheet.getWorkbook().createCellStyle();
        CreationHelper pCH = worksheet.getWorkbook().getCreationHelper();
        percentageCellStyle.setDataFormat(pCH.createDataFormat().getFormat("0.00%"));

        XSSFRow rowPeriod = worksheet.getRow(3);
        rowPeriod.getCell(0).setCellValue(period.getName());

        XSSFRow row;
        int bUnitStartIndex = 8;
        for (BusinessUnit bu : adminService.findBusinessUnits()) {
            row = worksheet.getRow(bUnitStartIndex++);
            setCellStringValue(row, 0, bu.getName() + " Platform");
            setCellValue(row, 1, getRevenueRecognizePeriodLC(bu, period));
            setCellValue(row, 2, getLiquidatedDamagesPeriod(bu, period));
            setCellValue(row, 3, getCostGoodsSoldPeriodLC(bu, period));
            setCellValue(row, 4, getLossReservePeriodADJLC(bu, period));
            setCellValue(row, 5, getGrossProfitPeriod(bu, period));
            setCellValue(row, 6, getTPCIncured(bu, period));
            setCellValue(row, 7, getAcceleratedTPC(bu, period));
            setCellValue(row, 8, getOperatingIncomePeriodLC(bu, period));

            List<FinancialPeriod> qtdPeriods = financialPeriodService.getQTDFinancialPeriods(period);
            setCellValue(row, 9, getAccuRevenueToRecognizePeriodLC(bu, qtdPeriods));
            setCellValue(row, 10, getAccuLiquidatedDamagesPeriod(bu, qtdPeriods));
            setCellValue(row, 11, getAccuCostGoodsSoldPeriodLC(bu, qtdPeriods));
            setCellValue(row, 12, getAccuLossReservePeriodADJLC(bu, qtdPeriods));
            setCellValue(row, 13, getAccuGrossProfitPeriodLC(bu, qtdPeriods));
            setCellValue(row, 14, getAccuTPCIncured(bu, qtdPeriods));
            setCellValue(row, 15, getAccuAcceleratedTPC(bu, qtdPeriods));
            setCellValue(row, 16, getAccuOperatingIncomePeriod(bu, qtdPeriods));

            List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
            setCellValue(row, 17, getAccuRevenueToRecognizePeriodLC(bu, ytdPeriods));
            setCellValue(row, 18, getAccuLiquidatedDamagesPeriod(bu, ytdPeriods));
            setCellValue(row, 19, getAccuCostGoodsSoldPeriodLC(bu, ytdPeriods));
            setCellValue(row, 20, getAccuLossReservePeriodADJLC(bu, ytdPeriods));
            setCellValue(row, 21, getAccuGrossProfitPeriodLC(bu, ytdPeriods));
            setCellValue(row, 22, getAccuTPCIncured(bu, ytdPeriods));
            setCellValue(row, 23, getAccuAcceleratedTPC(bu, ytdPeriods));
            setCellValue(row, 24, getAccuOperatingIncomePeriod(bu, ytdPeriods));

        }

        int rowNum = 17;
        for (ReportingUnit runit : adminService.findAllReportingUnits()) {

            row = worksheet.getRow(rowNum);
            if (row == null) {
                row = worksheet.createRow(rowNum);
            }
            setCellStringValue(row, 0, runit.getName());
            setCellValue(row, 1, getRevenueRecognizePeriodLC(runit, period));
            setCellValue(row, 2, getLiquidatedDamagesPeriod(runit, period));
            setCellValue(row, 3, getCostGoodsSoldPeriodLC(runit, period));
            setCellValue(row, 4, getLossReservePeriodADJLC(runit, period));
            setCellValue(row, 5, getGrossProfitPeriod(runit, period));
            setCellValue(row, 6, getTPCIncured(runit, period));
            setCellValue(row, 7, getAcceleratedTPC(runit, period));
            setCellValue(row, 8, getOperatingIncomePeriodLC(runit, period));

            List<FinancialPeriod> qtdPeriods = financialPeriodService.getQTDFinancialPeriods(period);
            setCellValue(row, 9, getAccuRevenueToRecognizePeriodLC(runit, qtdPeriods));
            setCellValue(row, 10, getAccuLiquidatedDamagesPeriod(runit, qtdPeriods));
            setCellValue(row, 11, getAccuCostGoodsSoldPeriodLC(runit, qtdPeriods));
            setCellValue(row, 12, getAccuLossReservePeriodADJLC(runit, qtdPeriods));
            setCellValue(row, 13, getAccuGrossProfitPeriodLC(runit, qtdPeriods));
            setCellValue(row, 14, getAccuTPCIncured(runit, qtdPeriods));
            setCellValue(row, 15, getAccuAcceleratedTPC(runit, qtdPeriods));
            setCellValue(row, 16, getAccuOperatingIncomePeriod(runit, qtdPeriods));

            List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
            setCellValue(row, 17, getAccuRevenueToRecognizePeriodLC(runit, ytdPeriods));
            setCellValue(row, 18, getAccuLiquidatedDamagesPeriod(runit, ytdPeriods));
            setCellValue(row, 19, getAccuCostGoodsSoldPeriodLC(runit, ytdPeriods));
            setCellValue(row, 20, getAccuLossReservePeriodADJLC(runit, ytdPeriods));
            setCellValue(row, 21, getAccuGrossProfitPeriodLC(runit, ytdPeriods));
            setCellValue(row, 22, getAccuTPCIncured(runit, ytdPeriods));
            setCellValue(row, 23, getAccuAcceleratedTPC(runit, ytdPeriods));
            setCellValue(row, 24, getAccuOperatingIncomePeriod(runit, ytdPeriods));

            rowNum++;

        }

        return worksheet;
    }

    private void setCellStringValue(XSSFRow row, int cellNum, String value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
        cell.setCellStyle(stringCellStyle);
    }

    private void setCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(currencyCellStyle);
        }

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

    private BigDecimal getTransactionPriceAdjusted(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_ADJ_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getLiquidatedDamagesCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getEAC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("ESTIMATED_COST_AT_COMPLETION_LC", measureable, period).getLcValue();
    }

    private BigDecimal getEstimatedGrossProfit(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("ESTIMATED_GROSS_PROFIT_LC", measureable, period).getLcValue();
    }

    private BigDecimal getEstimatedGrossMargin(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("ESTIMATED_GROSS_MARGIN", measureable, period).getValue();
    }

    private BigDecimal getThirdPartyCommissionCTDLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getPercentComplete(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("PERCENT_COMPLETE", measureable, period).getValue();
    }

    private BigDecimal getRevenueRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatedDamagesToRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getCostOfGoodsSoldCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getLossReserveCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LOSS_RESERVE_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossProfitCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("GROSS_PROFIT_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossProfitPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("GROSS_PROFIT_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossMarginCTD(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("GROSS_MARGIN_CTD", measureable, period).getValue();
    }

    private BigDecimal getTotalTPCExpenseCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TPC_TOTAL_EXPENSE_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : new BigDecimal(BigInteger.ZERO);
    }

    private BigDecimal getContractBillingsCTDLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_BILLINGS_CTD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getCostToCompleteLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_TO_COMPLETE_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractAssetCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_ASSET_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractLiabilityCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_LIABILITY_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getRevenueRecognizePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatedDamagesPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getCostGoodsSoldPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_PERIOD_LC", measureable, period).getLcValue();
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

    private BigDecimal getOperatingIncomePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("OPERATING_INCOME_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getLossReservePeriodADJLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, period).getLcValue();
    }

    private BigDecimal getAccuRevenueToRecognizePeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuLiquidatedDamagesPeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuCostGoodsSoldPeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("COST_OF_GOODS_SOLD_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuGrossProfitPeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("GROSS_PROFIT_PERIOD_LC", measureable, qtdPeriods).getLcValue();
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

    private BigDecimal getAccuOperatingIncomePeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("OPERATING_INCOME_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }
}
