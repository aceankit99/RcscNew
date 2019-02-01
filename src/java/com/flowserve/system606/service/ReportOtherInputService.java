/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.PerformanceObligationGroup;
import com.flowserve.system606.model.ReportingUnit;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
public class ReportOtherInputService {

    @Inject
    private AdminService adminService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private MetricService metricService;
    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    @Inject
    private FinancialPeriodService financialPeriodService;
    Map<String, PerformanceObligationGroup> OEAMDisAgg = new HashMap<String, PerformanceObligationGroup>();

    public void generateReport4DisAggregatedRev(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period, ReportingUnit ru) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            OEAMDisAgg.clear();
            XSSFSheet worksheet = workbook.getSheet("Rprt 4 - Disclosures Tab 1");
            writeDisclosureDisAggRev(worksheet, period, ru);

            worksheet = workbook.getSheet("Rprt 4 - Disclosures Tab 2");
            writeDisclosureOEAMDisAggRev(worksheet, period, ru);
            OEAMDisAgg.clear();
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();
    }

    public XSSFSheet writeDisclosureDisAggRev(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

        XSSFRow rowRU = worksheet.getRow(0);
        rowRU.getCell(0).setCellValue(ru.getName());
        rowRU = worksheet.getRow(2);
        rowRU.getCell(0).setCellValue(period.getName());
        printPeriodsHeader(worksheet.getRow(2), period);

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        calcAndPrintNetRevenueByDestination(worksheet, ru, period, ytdPeriods);
        calcAndPrintNetRevenueByOEAMDisAgg(worksheet, ru, period, ytdPeriods);

        return worksheet;
    }

    public XSSFSheet writeDisclosureOEAMDisAggRev(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

        XSSFRow rowRU = worksheet.getRow(0);
        rowRU.getCell(0).setCellValue(ru.getName());
        rowRU = worksheet.getRow(2);
        rowRU.getCell(0).setCellValue(period.getName());
        printPeriodsHeader(worksheet.getRow(2), period);

        List<FinancialPeriod> ytdPeriods = financialPeriodService.getYTDFinancialPeriods(period);
        calcAndPrintBacklogByMarketForOEAMDisAgg(worksheet, ru, period, ytdPeriods);

        return worksheet;
    }

    public void printPeriodsHeader(XSSFRow row, FinancialPeriod period) {
        List<String> periodHead = financialPeriodService.getAllMonthStrings(period.getPeriodYear());
        for (int i = 0; i < periodHead.size(); i++) {
            int colNum = i + 1;
            Cell cellHead = row.getCell(colNum);
            cellHead.setCellValue(periodHead.get(i));
        }
    }

    public void calcAndPrintNetRevenueByDestination(XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period, List<FinancialPeriod> ytdPeriods) throws Exception {

        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);

        ArrayList<String> allSalesDes = new ArrayList<String>();
        allSalesDes.addAll(Arrays.asList("ASIA", "CHINA",
                "INDIA", "JAPAN", "EUROPE", "RUSCIS", "LA", "CANADA", "US", "ANTARCTICA", "OTHER", "NAFRICA", "OTHERAFRICA", "SAFRICA", "IRAQ", "IRAQ"));
        Map<String, PerformanceObligationGroup> salesMap = new HashMap<String, PerformanceObligationGroup>();

        for (PerformanceObligation pob : ru.getUnarchivedPerformanceObligations()) {
            String salesDesForPOB = (String) pob.getPeriodMetric(period, metricService.getMetricTypeByCode("SALES_DESTINATION")).getValue();
            if (allSalesDes.contains(salesDesForPOB)) {

                if (salesMap.get(salesDesForPOB) == null) {
                    PerformanceObligationGroup pg = new PerformanceObligationGroup(salesDesForPOB, ru);
                    pg.addPerformanceObligation(pob);
                    salesMap.put(salesDesForPOB, pg);
                } else {
                    salesMap.get(salesDesForPOB).addPerformanceObligation(pob);
                }
            } else {

                if (salesMap.get("None") == null) {
                    PerformanceObligationGroup pg = new PerformanceObligationGroup("None", ru);
                    pg.addPerformanceObligation(pob);
                    salesMap.put("None", pg);
                } else {
                    salesMap.get("None").addPerformanceObligation(pob);
                }
            }
        }
        XSSFRow row;
        int rowNum = 6;
        int colNum;
        PerformanceObligationGroup saleDesNoneGroup = salesMap.get("None");
        for (String salesDes : allSalesDes) {
            row = worksheet.getRow(rowNum++);
            PerformanceObligationGroup saleDesGroup = salesMap.get(salesDes);
            for (int i = 0; i < ytdPeriods.size(); i++) {
                if (period.getPeriodYear() != 2017) {
                    colNum = i + 1;
                } else {
                    colNum = i + 11;
                }
                setCellValue(row, colNum, getRevenueRecognizePeriodLCGroup(saleDesGroup, ytdPeriods.get(i)));

                if (salesDes.equalsIgnoreCase(allSalesDes.get(0))) {
                    setCellValue(worksheet.getRow(22), colNum, getRevenueRecognizePeriodLCGroup(saleDesNoneGroup, ytdPeriods.get(i)));
                    setCellValue(worksheet.getRow(23), colNum, getLiquidatesDamageToRecogPeriodLC(ru, ytdPeriods.get(i)));
                }
            }
            setCellValue(row, 14, getAccuRevenueToRecognizePeriodLCGroup(saleDesGroup, ytdPeriods));
            setCellValue(row, 16, getAccuRevenueToRecognizePeriodLCGroup(saleDesGroup, Q1));
            setCellValue(row, 17, getAccuRevenueToRecognizePeriodLCGroup(saleDesGroup, Q2));
            setCellValue(row, 18, getAccuRevenueToRecognizePeriodLCGroup(saleDesGroup, Q3));
            setCellValue(row, 19, getAccuRevenueToRecognizePeriodLCGroup(saleDesGroup, Q4));

        }
        setCellValue(worksheet.getRow(22), 14, getAccuRevenueToRecognizePeriodLCGroup(saleDesNoneGroup, ytdPeriods));
        setCellValue(worksheet.getRow(22), 16, getAccuRevenueToRecognizePeriodLCGroup(saleDesNoneGroup, Q1));
        setCellValue(worksheet.getRow(22), 17, getAccuRevenueToRecognizePeriodLCGroup(saleDesNoneGroup, Q2));
        setCellValue(worksheet.getRow(22), 18, getAccuRevenueToRecognizePeriodLCGroup(saleDesNoneGroup, Q3));
        setCellValue(worksheet.getRow(22), 19, getAccuRevenueToRecognizePeriodLCGroup(saleDesNoneGroup, Q4));

        setCellValue(worksheet.getRow(23), 14, getAccuLiquidatedDamagesPeriod(ru, ytdPeriods));
        setCellValue(worksheet.getRow(23), 16, getAccuLiquidatedDamagesPeriod(ru, Q1));
        setCellValue(worksheet.getRow(23), 17, getAccuLiquidatedDamagesPeriod(ru, Q2));
        setCellValue(worksheet.getRow(23), 18, getAccuLiquidatedDamagesPeriod(ru, Q3));
        setCellValue(worksheet.getRow(23), 19, getAccuLiquidatedDamagesPeriod(ru, Q4));
    }

    public void calcAndPrintNetRevenueByOEAMDisAgg(XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period, List<FinancialPeriod> ytdPeriods) throws Exception {

        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);
        ArrayList<String> bothDisAgg = new ArrayList<String>();
        bothDisAgg.addAll(Arrays.asList("OE", "AM"));
        for (PerformanceObligation pob : ru.getUnarchivedPerformanceObligations()) {
            String pobValue = (String) pob.getPeriodMetric(period, metricService.getMetricTypeByCode("OEAM_DISAGG")).getValue();
            String pobOEAMDisAgg = pobValue != null ? pobValue.trim() : pobValue;
            String pobRevenuMenotd = pob.getRevenueMethod().getShortName();
            if (bothDisAgg.contains(pobOEAMDisAgg) && pobRevenuMenotd == "POC") {

                if (OEAMDisAgg.get(pobOEAMDisAgg + "-POC") == null) {
                    PerformanceObligationGroup pg = new PerformanceObligationGroup(pobOEAMDisAgg + "-POC", ru);
                    pg.addPerformanceObligation(pob);
                    OEAMDisAgg.put(pobOEAMDisAgg + "-POC", pg);
                } else {
                    OEAMDisAgg.get(pobOEAMDisAgg + "-POC").addPerformanceObligation(pob);
                }
            } else if (bothDisAgg.contains(pobOEAMDisAgg) && pobRevenuMenotd != "POC") {

                if (OEAMDisAgg.get(pobOEAMDisAgg + "-Non-POC") == null) {
                    PerformanceObligationGroup pg = new PerformanceObligationGroup(pobOEAMDisAgg + "-Non-POC", ru);
                    pg.addPerformanceObligation(pob);
                    OEAMDisAgg.put(pobOEAMDisAgg + "-Non-POC", pg);
                } else {
                    OEAMDisAgg.get(pobOEAMDisAgg + "-Non-POC").addPerformanceObligation(pob);
                }
            } else {
                if (OEAMDisAgg.get("None") == null) {
                    PerformanceObligationGroup pg = new PerformanceObligationGroup("None", ru);
                    pg.addPerformanceObligation(pob);
                    OEAMDisAgg.put("None", pg);
                } else {
                    OEAMDisAgg.get("None").addPerformanceObligation(pob);
                }
            }
        }
        ArrayList<String> disAggAllList = new ArrayList<String>();
        disAggAllList.addAll(Arrays.asList("OE-POC", "OE-Non-POC", "AM-POC", "AM-Non-POC", "None"));
        XSSFRow rowRev;
        XSSFRow rowLDs;
        int rowNumRev = 29;
        int rowNumLDs = 37;
        int colNum;
        for (String disAgg : disAggAllList) {

            rowRev = worksheet.getRow(rowNumRev++);
            rowLDs = worksheet.getRow(rowNumLDs++);
            PerformanceObligationGroup disAggGroup = OEAMDisAgg.get(disAgg);
            for (int i = 0; i < ytdPeriods.size(); i++) {
                if (period.getPeriodYear() != 2017) {
                    colNum = i + 1;
                } else {
                    colNum = i + 11;
                }
                setCellValue(rowRev, colNum, getRevenueRecognizePeriodLCGroup(disAggGroup, ytdPeriods.get(i)));
                setCellValue(rowLDs, colNum, getLiquidatesDamageToRecogPeriodLCGroup(disAggGroup, ytdPeriods.get(i)));
            }
            setCellValue(rowRev, 14, getAccuRevenueToRecognizePeriodLCGroup(disAggGroup, ytdPeriods));
            setCellValue(rowRev, 16, getAccuRevenueToRecognizePeriodLCGroup(disAggGroup, Q1));
            setCellValue(rowRev, 17, getAccuRevenueToRecognizePeriodLCGroup(disAggGroup, Q2));
            setCellValue(rowRev, 18, getAccuRevenueToRecognizePeriodLCGroup(disAggGroup, Q3));
            setCellValue(rowRev, 19, getAccuRevenueToRecognizePeriodLCGroup(disAggGroup, Q4));

            setCellValue(rowLDs, 14, getAccuLiquidatedDamagesPeriodGroup(disAggGroup, ytdPeriods));
            setCellValue(rowLDs, 16, getAccuLiquidatedDamagesPeriodGroup(disAggGroup, Q1));
            setCellValue(rowLDs, 17, getAccuLiquidatedDamagesPeriodGroup(disAggGroup, Q2));
            setCellValue(rowLDs, 18, getAccuLiquidatedDamagesPeriodGroup(disAggGroup, Q3));
            setCellValue(rowLDs, 19, getAccuLiquidatedDamagesPeriodGroup(disAggGroup, Q4));
        }

    }

    public void calcAndPrintBacklogByMarketForOEAMDisAgg(XSSFSheet worksheet, ReportingUnit ru, FinancialPeriod period, List<FinancialPeriod> ytdPeriods) throws Exception {

        List<FinancialPeriod> Q1 = financialPeriodService.getQ1Periods(period);
        List<FinancialPeriod> Q2 = financialPeriodService.getQ2Periods(period);
        List<FinancialPeriod> Q3 = financialPeriodService.getQ3Periods(period);
        List<FinancialPeriod> Q4 = financialPeriodService.getQ4Periods(period);

        ArrayList<String> disAggAllList = new ArrayList<String>();
        disAggAllList.addAll(Arrays.asList("OE-POC", "OE-Non-POC", "AM-POC", "AM-Non-POC", "None"));
        XSSFRow row;
        int rowNum = 5;
        int colNum;
        for (String disAgg : disAggAllList) {

            row = worksheet.getRow(rowNum++);
            PerformanceObligationGroup disAggGroup = OEAMDisAgg.get(disAgg);
            for (int i = 0; i < ytdPeriods.size(); i++) {
                if (period.getPeriodYear() != 2017) {
                    colNum = i + 1;
                } else {
                    colNum = i + 11;
                }
                setCellValue(row, colNum, getBacklogByMarketLCGroup(disAggGroup, ytdPeriods.get(i)));
            }
            setCellValue(row, 13, getAccuBacklogByMarketLCGroup(disAggGroup, ytdPeriods));
            setCellValue(row, 14, getAccuBacklogByMarketLCGroup(disAggGroup, Q1));
            setCellValue(row, 15, getAccuBacklogByMarketLCGroup(disAggGroup, Q2));
            setCellValue(row, 16, getAccuBacklogByMarketLCGroup(disAggGroup, Q3));
            setCellValue(row, 17, getAccuBacklogByMarketLCGroup(disAggGroup, Q4));
        }

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

    public void persist(Object object) {
        em.persist(object);
    }

    private BigDecimal getRevenueRecognizePeriodLCGroup(PerformanceObligationGroup pobGroup, FinancialPeriod period) throws Exception {
        if (pobGroup != null && pobGroup.getPerformanceObligations().size() > 0) {
            return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", pobGroup, period).getLcValue();
        } else {
            return new BigDecimal(BigInteger.ZERO);
        }
    }

    private BigDecimal getLiquidatesDamageToRecogPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatesDamageToRecogPeriodLCGroup(PerformanceObligationGroup pobGroup, FinancialPeriod period) throws Exception {
        if (pobGroup != null && pobGroup.getPerformanceObligations().size() > 0) {
            return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", pobGroup, period).getLcValue();
        } else {
            return new BigDecimal(BigInteger.ZERO);
        }
    }

    private BigDecimal getBacklogByMarketLCGroup(PerformanceObligationGroup pobGroup, FinancialPeriod period) throws Exception {
        if (pobGroup != null && pobGroup.getPerformanceObligations().size() > 0) {
            return calculationService.getCurrencyMetric("TRANSACTION_PRICE_BACKLOG_CC", pobGroup, period).getLcValue();
        } else {
            return new BigDecimal(BigInteger.ZERO);
        }
    }

    private BigDecimal getAccuBacklogByMarketLCGroup(PerformanceObligationGroup pobGroup, List<FinancialPeriod> qtdPeriods) throws Exception {
        if (pobGroup != null && pobGroup.getPerformanceObligations().size() > 0) {
            return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TRANSACTION_PRICE_BACKLOG_CC", pobGroup, qtdPeriods).getLcValue();
        } else {
            return new BigDecimal(BigInteger.ZERO);
        }
    }

    private BigDecimal getAccuRevenueToRecognizePeriodLCGroup(PerformanceObligationGroup pobGroup, List<FinancialPeriod> qtdPeriods) throws Exception {
        if (pobGroup != null && pobGroup.getPerformanceObligations().size() > 0) {
            return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("REVENUE_TO_RECOGNIZE_PERIOD_CC", pobGroup, qtdPeriods).getLcValue();
        } else {
            return new BigDecimal(BigInteger.ZERO);
        }
    }

    private BigDecimal getAccuLiquidatedDamagesPeriod(Measurable measureable, List<FinancialPeriod> listPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, listPeriods).getLcValue();
    }

    private BigDecimal getAccuLiquidatedDamagesPeriodGroup(PerformanceObligationGroup pobGroup, List<FinancialPeriod> listPeriods) throws Exception {
        if (pobGroup != null && pobGroup.getPerformanceObligations().size() > 0) {
            return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", pobGroup, listPeriods).getLcValue();
        } else {
            return new BigDecimal(BigInteger.ZERO);
        }
    }
}
