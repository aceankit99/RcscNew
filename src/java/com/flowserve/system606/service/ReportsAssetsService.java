/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.web.WebSession;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author shubhamc
 */
@Stateless
public class ReportsAssetsService {

    @Inject
    private WebSession webSession;
    @Inject
    private CalculationService calculationService;
    @Inject
    private FinancialPeriodService financialPeriodService;

    private CellStyle stringCellStyle;

    public void generateReport2AssetsandLiab(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period, ReportingUnit ru) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            XSSFSheet worksheet = workbook.getSheet("RCS – POC Cont Asset & Liab");

            writeReport2AssetsandLiab(worksheet, period, ru);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }

    }

    public XSSFSheet writeReport2AssetsandLiab(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

        Cell cell = null;
        XSSFRow rowRU = worksheet.getRow(2);
        cell = rowRU.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(ru.getName());
        rowRU.getCell(CellReference.convertColStringToIndex("E")).setCellValue(ru.getLocalCurrency().getCurrencyCode());

        int rowNum = 15;
        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);

        FinancialPeriod beginningBalancePeriod = financialPeriodService.findByNumericString(period.getPeriodYear() + "-1").getPriorPeriod();
        XSSFRow rowTotal = worksheet.getRow(9);
        setCellValue(rowTotal, 8, getCumulativeAssetLiabilityPositionCC(ru, beginningBalancePeriod));

        setCellValue(rowTotal, 10, getCumulativeRevenueNetLC(ru, beginningBalancePeriod));
        setCellValue(rowTotal, 11, getCumulativeBillings(ru, beginningBalancePeriod).getLcValue());
        setCellValue(rowTotal, 12, getCumulativeAssetLiabilityPositionLC(ru, beginningBalancePeriod));

        setCellValue(rowTotal, 16, getCumulativeAssetLiabilityPositionCC(ru, period));
        setCellValue(rowTotal, 18, getCumulativeRevenueNetLC(ru, period));
        setCellValue(rowTotal, 19, getCumulativeBillings(ru, period).getLcValue());
        setCellValue(rowTotal, 20, getFxGainLoss(ru, period).getLcValue());
        setCellValue(rowTotal, 21, getCumulativeAssetLiabilityPositionLC(ru, period));
        setCellValue(rowTotal, 23, getCumulativeAssetLiabilityPositionLC(ru, beginningBalancePeriod));
        setCellValue(rowTotal, 24, getAccuValueForYTDPeriods("CONTRACT_REVENUE_TO_RECOGNIZE_PERIOD_CC", ru, ytdPeriods).getLcValue());
        setCellValue(rowTotal, 25, getAccuValueForYTDPeriods("CONTRACT_BILLINGS_PERIOD_CC", ru, ytdPeriods).getLcValue());
        setCellValue(rowTotal, 26, getAccuValueForYTDPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", ru, ytdPeriods).getLcValue());
        setCellValue(rowTotal, 27, getAccuValueForYTDPeriods("CONTRACT_TOTAL_FX_ADJ_PERIOD_LC", ru, ytdPeriods).getLcValue());

        for (Contract contract : ru.getUnarchivedContracts()) {
            XSSFRow row;
            row = worksheet.getRow(rowNum++);
            setCellStringValue(row, 0, contract.getName());
            setCellStringValue(row, 1, contract.getSalesOrderNumber());
            setCellStringValue(row, 2, contract.getContractCurrency().getCurrencyCode());
            setCellValue(row, 3, getTransactionPrice(contract, period).getCcValue());

            setCellValue(row, 6, getCumulativeRevenueNetCC(contract, beginningBalancePeriod));
            setCellValue(row, 7, getCumulativeBillings(contract, beginningBalancePeriod).getCcValue());
            setCellValue(row, 8, getCumulativeAssetLiabilityPositionCC(contract, beginningBalancePeriod));

            setCellValue(row, 10, getCumulativeRevenueNetLC(contract, beginningBalancePeriod));
            setCellValue(row, 11, getCumulativeBillings(contract, beginningBalancePeriod).getLcValue());
            setCellValue(row, 12, getCumulativeAssetLiabilityPositionLC(contract, beginningBalancePeriod));

            setCellValue(row, 14, getCumulativeRevenueNetCC(contract, period));
            setCellValue(row, 15, getCumulativeBillings(contract, period).getCcValue());
            setCellValue(row, 16, getCumulativeAssetLiabilityPositionCC(contract, period));

            setCellValue(row, 18, getCumulativeRevenueNetLC(contract, period));
            setCellValue(row, 19, getCumulativeBillings(contract, period).getLcValue());
            setCellValue(row, 20, getFxGainLoss(contract, period).getLcValue());
            setCellValue(row, 21, getCumulativeAssetLiabilityPositionLC(contract, period));
            BigDecimal begBalanceColX = getCumulativeAssetLiabilityPositionLC(contract, beginningBalancePeriod);
            BigDecimal ytdRevenueColY = getAccuValueForYTDPeriods("CONTRACT_REVENUE_TO_RECOGNIZE_PERIOD_CC", contract, ytdPeriods).getLcValue();
            BigDecimal ytdBillingsColZ = getAccuValueForYTDPeriods("CONTRACT_BILLINGS_PERIOD_CC", contract, ytdPeriods).getLcValue();
            BigDecimal ytdLDPenaltyColAA = getAccuValueForYTDPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", contract, ytdPeriods).getLcValue();

            setCellValue(row, 23, begBalanceColX);
            setCellValue(row, 24, ytdRevenueColY);
            setCellValue(row, 25, ytdBillingsColZ);
            setCellValue(row, 26, ytdLDPenaltyColAA);
            setCellValue(row, 27, getAccuValueForYTDPeriods("CONTRACT_TOTAL_FX_ADJ_PERIOD_LC", contract, ytdPeriods).getLcValue());
            BigDecimal endBalanceColAC = begBalanceColX.add(ytdRevenueColY).subtract(ytdBillingsColZ).subtract(ytdLDPenaltyColAA);
            setCellValue(row, 28, endBalanceColAC);
        }

        return worksheet;
    }

    private CurrencyMetric getTransactionPrice(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", measureable, period);
    }

    private BigDecimal getCumulativeRevenueNetCC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_REVENUE_TO_RECOGNIZE_CTD_CC", measureable, period).getCcValue();
        BigDecimal revenue = value != null ? value : new BigDecimal(BigInteger.ZERO);
        value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", measureable, period).getCcValue();
        BigDecimal liquidatedDamage = value != null ? value : new BigDecimal(BigInteger.ZERO);
        return revenue.subtract(liquidatedDamage);
    }

    private BigDecimal getCumulativeRevenueNetLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_REVENUE_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
        BigDecimal revenue = value != null ? value : new BigDecimal(BigInteger.ZERO);
        value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
        BigDecimal liquidatedDamage = value != null ? value : new BigDecimal(BigInteger.ZERO);
        return revenue.subtract(liquidatedDamage);
    }

    private BigDecimal getCumulativeAssetLiabilityPositionCC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_ASSET_CTD_LC", measureable, period).getCcValue();
        BigDecimal cAsset = value != null ? value : new BigDecimal(BigInteger.ZERO);
        value = calculationService.getCurrencyMetric("CONTRACT_LIABILITY_CTD_LC", measureable, period).getCcValue();
        BigDecimal cLiability = value != null ? value : new BigDecimal(BigInteger.ZERO);
        return cAsset.subtract(cLiability);
    }

    private BigDecimal getCumulativeAssetLiabilityPositionLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_ASSET_CTD_LC", measureable, period).getLcValue();
        BigDecimal cAsset = value != null ? value : new BigDecimal(BigInteger.ZERO);
        value = calculationService.getCurrencyMetric("CONTRACT_LIABILITY_CTD_LC", measureable, period).getLcValue();
        BigDecimal cLiability = value != null ? value : new BigDecimal(BigInteger.ZERO);
        return cAsset.subtract(cLiability);
    }

    private CurrencyMetric getCumulativeBillings(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_BILLINGS_CTD_CC", measureable, period);
    }

    private CurrencyMetric getAsset(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_ASSET_PERIOD_LC", measureable, period);
    }

    private CurrencyMetric getLiab(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_LIABILITY_PERIOD_LC", measureable, period);
    }

    private CurrencyMetric getFxGainLoss(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("CONTRACT_TOTAL_FX_ADJ_PERIOD_LC", measureable, period);
    }

    private CurrencyMetric getAccuValueForYTDPeriods(String MetricCode, Measurable measureable, List<FinancialPeriod> ytdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods(MetricCode, measureable, ytdPeriods);
    }

    private void setCellStringValue(XSSFRow row, int cellNum, String value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
        //cell.setCellStyle(stringCellStyle);
    }

    private void setCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(currentStyle);
        } else {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(0);
            cell.setCellStyle(currentStyle);
        }

    }

}
