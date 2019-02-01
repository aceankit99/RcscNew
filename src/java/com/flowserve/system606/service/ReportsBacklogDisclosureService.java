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
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.PerformanceObligationGroup;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.RevenueMethod;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author shubhamv
 */
@Stateless
public class ReportsBacklogDisclosureService {

    @Inject
    private CalculationService calculationService;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private MetricService metricService;

    public void generateReport4BacklogRollfoward(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period, ReportingUnit ru) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet worksheet = workbook.getSheet("Rprt 4 - Disclosures Tab 3");
            writeDisclosureBacklogRollfoward(worksheet, period, ru);

            worksheet = workbook.getSheet("Rprt 4 - Disclosures Tab 4");
            writeDisclosureBacklogRULevel(worksheet, period, ru);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();
    }

    public XSSFSheet writeDisclosureBacklogRollfoward(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

        XSSFRow rowRU = worksheet.getRow(0);
        rowRU.getCell(0).setCellValue(ru.getName());
        rowRU = worksheet.getRow(2);
        rowRU.getCell(0).setCellValue(period.getName());

        PerformanceObligationGroup pocPobs = new PerformanceObligationGroup("pocPobs", ru, RevenueMethod.PERC_OF_COMP, ru.getPobsByRevenueMethod(RevenueMethod.PERC_OF_COMP));
        PerformanceObligationGroup pitPobs = new PerformanceObligationGroup("pitPobs", ru, RevenueMethod.POINT_IN_TIME, ru.getPobsByRevenueMethod(RevenueMethod.POINT_IN_TIME));
        List<PerformanceObligation> performanceObligations = new ArrayList<PerformanceObligation>();
        List<RevenueMethod> revenueMethods = new ArrayList<RevenueMethod>();
        performanceObligations.addAll(ru.getPobsByRevenueMethod(RevenueMethod.STRAIGHT_LINE));
        performanceObligations.addAll(ru.getPobsByRevenueMethod(RevenueMethod.RIGHT_TO_INVOICE));
        revenueMethods.add(RevenueMethod.STRAIGHT_LINE);
        revenueMethods.add(RevenueMethod.RIGHT_TO_INVOICE);
        PerformanceObligationGroup slRtiPobs = new PerformanceObligationGroup("slRtiPobs", ru, revenueMethods, performanceObligations);

        XSSFRow rowPeriods = worksheet.getRow(6);
        rowPeriods.getCell(1).setCellValue(period.getName());
        int cellNum = 3;
        for (int col = 0; col < 9; col++) {
            rowPeriods.getCell(cellNum++).setCellValue("YTD " + period.getName());
        }
        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        printRollBackDataPOBGroup(worksheet.getRow(8), pocPobs, period, ytdPeriods);
        printRollBackDataPOBGroup(worksheet.getRow(9), pitPobs, period, ytdPeriods);
        printRollBackDataPOBGroup(worksheet.getRow(10), slRtiPobs, period, ytdPeriods);

        int rowNum = 17;
        int intCol = 0;
        for (Contract contract : ru.getUnarchivedContracts()) {
            XSSFRow rowContract = worksheet.getRow(rowNum);
            if (rowContract == null) {
                rowContract = worksheet.createRow(rowNum);
            }
            setStringCellValue(rowContract, intCol, contract.getName());
            printRollBackDataPOBGroup(rowContract, contract, period, ytdPeriods);
            rowNum++;
        }
        XSSFRow rowTotal = worksheet.getRow(rowNum);
        if (rowTotal == null) {
            rowTotal = worksheet.createRow(rowNum);
        }
        setStringCellValue(rowTotal, intCol, "RF_BACKLOG - RF for Backlog");
        printRollBackDataPOBGroup(worksheet.getRow(rowNum), ru, period, ytdPeriods);

        return worksheet;
    }

    private void printRollBackDataPOBGroup(XSSFRow row, Measurable measureable, FinancialPeriod period, List<FinancialPeriod> ytdPeriods) throws Exception {
        int cellNum = 1;
        setCellValue(row, cellNum++, getTransactionPriceBacklog(measureable, period));
        setCellValue(row, cellNum++, getTransactionPriceAtBookPeriodRate(measureable, period));
        setCellValue(row, cellNum++, getAccuRevenueToRecognizePeriod(measureable, ytdPeriods));
        setCellValue(row, cellNum++, BigDecimal.ZERO);
        setCellValue(row, cellNum++, getAccuTransactionPriceADJ(measureable, ytdPeriods));
        setCellValue(row, cellNum++, BigDecimal.ZERO);
        setCellValue(row, cellNum++, BigDecimal.ZERO);
        setCellValue(row, cellNum++, getAccuLiquidatedDamagesPeriod(measureable, ytdPeriods));
        setCellValue(row, cellNum++, BigDecimal.ZERO);
        setCellValue(row, cellNum++, BigDecimal.ZERO);
        setCellValue(row, cellNum++, BigDecimal.ZERO);
    }

    public XSSFSheet writeDisclosureBacklogRULevel(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {
        XSSFRow rowRU = worksheet.getRow(0);
        rowRU.getCell(0).setCellValue(ru.getName());
        rowRU = worksheet.getRow(2);
        rowRU.getCell(0).setCellValue(period.getName());

        List<String> periodHead = financialPeriodService.getAllMonthStrings(period.getPeriodYear());
        for (int i = 0; i < periodHead.size(); i++) {
            int colNum = i + 2;
            Cell cellHead = worksheet.getRow(3).getCell(colNum);
            cellHead.setCellValue(periodHead.get(i));
        }
        Map<FinancialPeriod, List<Contract>> bookDateContractsInAllPeriods = new HashMap<FinancialPeriod, List<Contract>>();
        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        int colNum;
        for (int i = 0; i < ytdPeriods.size(); i++) {
            if (period.getPeriodYear() != 2017) {
                colNum = i + 2;
            } else {
                colNum = i + 12;
            }
            setCellValue(worksheet.getRow(5), colNum, getTransactionPriceBacklog(ru, ytdPeriods.get(i)));
            setCellValue(worksheet.getRow(7), colNum, getTransactionPriceAtBookPeriodRateForPeriod(ru, ytdPeriods.get(i), bookDateContractsInAllPeriods));
            setCellValue(worksheet.getRow(9), colNum, getRevenueToRecognizePeriod(ru, ytdPeriods.get(i)));
            setCellValue(worksheet.getRow(11), colNum, getTransactionPriceADJ(ru, ytdPeriods.get(i)));
            setCellValue(worksheet.getRow(13), colNum, getLiquidatedDamagesPeriod(ru, ytdPeriods.get(i)));
            setCellValue(worksheet.getRow(15), colNum, BigDecimal.ZERO);

        }
        ytdValueForBacklogRULevel(worksheet.getRow(5), "TRANSACTION_PRICE_BACKLOG_CC", ru, period, ytdPeriods);
        ytdValueAtBookPeriodRateRULevel(worksheet.getRow(7), period, bookDateContractsInAllPeriods);
        ytdValueForBacklogRULevel(worksheet.getRow(9), "REVENUE_TO_RECOGNIZE_PERIOD_CC", ru, period, ytdPeriods);
        ytdValueForBacklogRULevel(worksheet.getRow(11), "TRANSACTION_PRICE_ADJ_LC", ru, period, ytdPeriods);
        ytdValueForBacklogRULevel(worksheet.getRow(13), "LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", ru, period, ytdPeriods);
//        ytdValueForBacklogRULevel(worksheet.getRow(15), "TODO", ru, period, ytdPeriods);
        return worksheet;
    }

    public void ytdValueForBacklogRULevel(XSSFRow row, String metricCode, ReportingUnit ru, FinancialPeriod period, List<FinancialPeriod> ytdPeriods) throws Exception {

        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);
        setCellValue(row, 15, getAccuValuePeriodMetricType(metricCode, ru, ytdPeriods));
        setCellValue(row, 17, getAccuValuePeriodMetricType(metricCode, ru, Q1));
        setCellValue(row, 18, getAccuValuePeriodMetricType(metricCode, ru, Q2));
        setCellValue(row, 19, getAccuValuePeriodMetricType(metricCode, ru, Q3));
        setCellValue(row, 20, getAccuValuePeriodMetricType(metricCode, ru, Q4));
    }

    public void ytdValueAtBookPeriodRateRULevel(XSSFRow row, FinancialPeriod period, Map<FinancialPeriod, List<Contract>> bookDateContractsInAllPeriods) throws Exception {

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);

        setCellValue(row, 15, getAccuTransactionPriceAtBookPeriodRate(bookDateContractsInAllPeriods, ytdPeriods));
        setCellValue(row, 17, getAccuTransactionPriceAtBookPeriodRate(bookDateContractsInAllPeriods, Q1));
        setCellValue(row, 18, getAccuTransactionPriceAtBookPeriodRate(bookDateContractsInAllPeriods, Q2));
        setCellValue(row, 19, getAccuTransactionPriceAtBookPeriodRate(bookDateContractsInAllPeriods, Q3));
        setCellValue(row, 20, getAccuTransactionPriceAtBookPeriodRate(bookDateContractsInAllPeriods, Q4));

    }

    public BigDecimal getAccuTransactionPriceAtBookPeriodRate(Map<FinancialPeriod, List<Contract>> bookDateContractsInAllPeriods, List<FinancialPeriod> periods) throws Exception {
        CurrencyMetric metric = new CurrencyMetric();
        metric.setMetricType(metricService.getMetricTypeByCode("TRANSACTION_PRICE_CC"));
        metric.setLcValue(BigDecimal.ZERO);
        for (FinancialPeriod period : periods) {
            for (Contract contract : bookDateContractsInAllPeriods.get(period)) {
                BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", contract, period).getLcValue();
                metric.setLcValue(metric.getLcValue().add(value));
            }
        }
        return metric.getLcValue();
    }

    private BigDecimal getTransactionPriceAtBookPeriodRate(Measurable measureable, FinancialPeriod period) throws Exception {
        CurrencyMetric metric = new CurrencyMetric();
        metric.setMetricType(metricService.getMetricTypeByCode("TRANSACTION_PRICE_CC"));
        metric.setLcValue(BigDecimal.ZERO);

        if (measureable instanceof PerformanceObligationGroup) {
            for (PerformanceObligation pob : ((PerformanceObligationGroup) measureable).getPerformanceObligations()) {
                LocalDate contractBookingDate = pob.getContract().getBookingDate();
                FinancialPeriod bookPeriod = financialPeriodService.findByNumericString(contractBookingDate.getYear() + "-" + contractBookingDate.getMonthValue());
                if (period.getPeriodYear() == contractBookingDate.getYear()) {
                    BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", pob, bookPeriod).getLcValue();
                    metric.setLcValue(metric.getLcValue().add(value));
                }
            }
        } else if (measureable instanceof Contract) {
            LocalDate contractBookingDate = ((Contract) measureable).getBookingDate();
            FinancialPeriod bookPeriod = financialPeriodService.findByNumericString(contractBookingDate.getYear() + "-" + contractBookingDate.getMonthValue());
            if (period.getPeriodYear() == contractBookingDate.getYear()) {
                BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", measureable, bookPeriod).getLcValue();
                metric.setLcValue(value);
            }

        } else if (measureable instanceof ReportingUnit) {
            for (Contract contract : ((ReportingUnit) measureable).getUnarchivedContracts()) {
                LocalDate contractBookingDate = contract.getBookingDate();
                FinancialPeriod bookPeriod = financialPeriodService.findByNumericString(contractBookingDate.getYear() + "-" + contractBookingDate.getMonthValue());
                if (period.getPeriodYear() == contractBookingDate.getYear()) {
                    BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", contract, bookPeriod).getLcValue();
                    metric.setLcValue(metric.getLcValue().add(value));
                }
            }
        }

        return metric.getLcValue();
    }

    private BigDecimal getTransactionPriceAtBookPeriodRateForPeriod(Measurable measureable, FinancialPeriod period, Map<FinancialPeriod, List<Contract>> bookDateContractsInAllPeriods) throws Exception {
        CurrencyMetric metric = new CurrencyMetric();
        metric.setMetricType(metricService.getMetricTypeByCode("TRANSACTION_PRICE_CC"));
        metric.setLcValue(BigDecimal.ZERO);
        List<Contract> bookPeriodMatchedContracts = new ArrayList<Contract>();
        if (measureable instanceof ReportingUnit) {
            for (Contract contract : ((ReportingUnit) measureable).getUnarchivedContracts()) {
                LocalDate contractBookingDate = contract.getBookingDate();
                FinancialPeriod bookPeriod = financialPeriodService.findByNumericString(contractBookingDate.getYear() + "-" + contractBookingDate.getMonthValue());
                if (period.getPeriodYear() == contractBookingDate.getYear() && period.getPeriodMonth().compareTo(contractBookingDate.getMonthValue()) == 0) {
                    BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", contract, bookPeriod).getLcValue();
                    metric.setLcValue(metric.getLcValue().add(value));
                    bookPeriodMatchedContracts.add(contract);
                }
            }
            bookDateContractsInAllPeriods.put(period, bookPeriodMatchedContracts);
        }

        return metric.getLcValue();
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

    private void setStringCellValue(XSSFRow row, int cellNum, String value) {
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);

    }

    private BigDecimal getTransactionPriceBacklog(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("TRANSACTION_PRICE_BACKLOG_CC", measureable, period).getLcValue();
    }

    private BigDecimal getRevenueToRecognizePeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatedDamagesPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getTransactionPriceADJ(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("TRANSACTION_PRICE_ADJ_LC", measureable, period).getLcValue();
    }

    private BigDecimal getAccuRevenueToRecognizePeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuTransactionPriceADJ(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TRANSACTION_PRICE_ADJ_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuLiquidatedDamagesPeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuValuePeriodMetricType(String metricCode, Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods(metricCode, measureable, qtdPeriods).getLcValue();
    }
}
