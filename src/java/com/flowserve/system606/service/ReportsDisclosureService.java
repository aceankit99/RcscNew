/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyMetric;
import com.flowserve.system606.model.DateMetric;
import com.flowserve.system606.model.DecimalMetric;
import com.flowserve.system606.model.ExchangeRate;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Metric;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.StringMetric;
import com.flowserve.system606.model.ValueKey;
import com.flowserve.system606.model.WorkflowStatus;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author shubhamv
 */
@Stateless
public class ReportsDisclosureService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    @Inject
    private MetricService metricService;
    @Inject
    private CurrencyService currencyService;
    @Inject
    private PerformanceObligationService performanceObligationService;
    private CellStyle numberCellStyle;
    private CellStyle stringCellStyle;
    private CellStyle currencyCellStyle;
    private CellStyle percentageCellStyle;

    private Configuration<String, ExchangeRate> config = new MutableConfiguration<>();

    @PostConstruct
    public void init() {
    }

    public void generateReport1InputAndCalc(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period, ReportingUnit ru) throws Exception {
        Instant start = Instant.now();
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            XSSFSheet worksheet = workbook.getSheet("Input and Calcs Report");

            writeReport1InputAndCalc(worksheet, period, ru);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();

        Instant end = Instant.now();
        Duration interval = Duration.between(start, end);
        int min = (int) (interval.getSeconds() / 60);
        int sec = (int) (interval.getSeconds() - (min * 60));
        Logger.getLogger(LoadTestServiceSet.class.getName()).log(Level.INFO, "Overall Report write to XLSX completed in MIN : " + min + " SEC : " + sec);
    }

    public XSSFSheet writeReport1InputAndCalc(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

        stringCellStyle = worksheet.getWorkbook().createCellStyle();
        Font font = worksheet.getWorkbook().createFont();
        font.setFontHeightInPoints((short) 9);
        stringCellStyle.setFont(font);

        numberCellStyle = worksheet.getWorkbook().createCellStyle();
        numberCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
        numberCellStyle.setFont(font);

        currencyCellStyle = worksheet.getWorkbook().createCellStyle();
        CreationHelper ch = worksheet.getWorkbook().getCreationHelper();
        currencyCellStyle.setDataFormat(ch.createDataFormat().getFormat("_(* #,##0_);_(* (#,##0);_(* \"-\"??_);_(@_)"));
        currencyCellStyle.setFont(font);

        percentageCellStyle = worksheet.getWorkbook().createCellStyle();
        CreationHelper pCH = worksheet.getWorkbook().getCreationHelper();
        percentageCellStyle.setDataFormat(pCH.createDataFormat().getFormat("0.00%"));

        Set<MetricType> pobMetricTypesProcessed = new HashSet<MetricType>();
        Set<MetricType> contractMetricTypesProcessed = new HashSet<MetricType>();
        Map<ValueKey, Metric> valueForAllFinancialPeriod = new HashMap<ValueKey, Metric>();
        Map<ValueKey, Metric> valueForAllPeriodContractLevel = new HashMap<ValueKey, Metric>();

        List<Contract> identifyContractFirstPOB = new ArrayList<Contract>();
        List<String> contractFirstPOBPeriod = new ArrayList<String>();
        XSSFRow row;

        List<Object[]> pobsForAllRUsDetails;
        int rowNum = 3;
        if (ru == null) {
            worksheet.getRow(0).getCell(0).setCellValue("POC Inputs & Calcs - by POb " + period.getId());
            pobsForAllRUsDetails = getAllPobsDetails();
        } else {
            worksheet.getRow(0).getCell(0).setCellValue("POC Inputs & Calcs - by POb RU" + ru.getCode() + " " + period.getId());
            pobsForAllRUsDetails = getAllPobsDetailsForRU(ru);
        }

        List<FinancialPeriod> allPeriods = findValidDataPeriods();
        int totalPeriodCount = allPeriods.size();
        setHeaderColumnsName(worksheet, allPeriods);

        Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "Total POb Count: " + pobsForAllRUsDetails.size());
        int count = 0;

        for (Object[] pobWithDetail : pobsForAllRUsDetails) {

            count++;

            if ((count % 100) == 0) {
                Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "PObs processed: " + count);
            }

            row = worksheet.getRow(rowNum);
            if (row == null) {
                row = worksheet.createRow(rowNum);
                Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "creating row");
            }
            Contract contract = (Contract) pobWithDetail[7];
            setCellStringValue(row, 0, (String) pobWithDetail[0]);
            setCellValue(row, 1, new BigDecimal((long) pobWithDetail[1]));
            setCellStringValue(row, 2, (String) pobWithDetail[2]);
            setCellStringValue(row, 3, "RU" + (String) pobWithDetail[3]);
            setCellStringValue(row, 4, (String) pobWithDetail[4]);
            Currency ccCurrency = contract.getContractCurrency();
            Currency lcCurrency = contract.getLocalCurrency();
            if (ccCurrency.equals(lcCurrency)) {
                setCellStringValue(row, 5, "NON-FX");
            } else {
                setCellStringValue(row, 5, "FX");
            }
            Long pobID = (Long) pobWithDetail[6];
            PerformanceObligation pob = performanceObligationService.findById(pobID);
            setDateCellValue(row, 6, contract.getBookingDate());
            //setCellWithDateValueContract(row, CellReference.convertColStringToIndex("G"), "BOOKING_DATE", contract.getId(), period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);

            setCellWithMetricCCValue(row, CellReference.convertColStringToIndex("H"), "TRANSACTION_PRICE_CC", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            setCellWithMetricLcValue(row, CellReference.convertColStringToIndex("I"), "ESTIMATED_COST_AT_COMPLETION_LC", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);

            setCellWithMetricStringValue(row, "K", "SALES_DESTINATION", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            //setCellWithMetricStringValue(row, "L", "TODO", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            setCellWithDateValueContract(row, CellReference.convertColStringToIndex("M"), "CONTRACT_CLOSURE_DATE", contract.getId(), period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
            setCellWithDateValueContract(row, CellReference.convertColStringToIndex("N"), "CONTRACT_COMPLETION_DATE", contract.getId(), period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
            setCellStringValue(row, CellReference.convertColStringToIndex("O"), ccCurrency.getCurrencyCode());
            setCellStringValue(row, CellReference.convertColStringToIndex("P"), lcCurrency.getCurrencyCode());
            //setCellNumericValue(row, CellReference.convertColStringToIndex("Q"), currencyService.getCCtoRCExchangeRate(pob, period));
            //setCellNumericValue(row, CellReference.convertColStringToIndex("R"), currencyService.getLCtoRCExchangeRate(pob, period));
            setCellStringValue(row, CellReference.convertColStringToIndex("AC"), (String) pobWithDetail[5]);
            setCellStringValue(row, CellReference.convertColStringToIndex("AD"), (String) pobWithDetail[8]);
            setCellWithMetricLcValue(row, CellReference.convertColStringToIndex("AE"), "TRANSACTION_PRICE_CC", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            setCellWithMetricLcValue(row, CellReference.convertColStringToIndex("AF"), "ESTIMATED_COST_AT_COMPLETION_LC", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            setCellWithMetricStringValue(row, "AH", "OEAM_DISAGG", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            //setCellWithDateValueContract(row, CellReference.convertColStringToIndex("AI"), "BOOKING_DATE", contract.getId(), period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
            setDateCellValue(row, CellReference.convertColStringToIndex("AI"), contract.getBookingDate());
            setCellWithMetricDateValue(row, "AJ", "DELIVERY_DATE", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            setCellWithMetricLcValue(row, CellReference.convertColStringToIndex("AK"), "COST_OF_GOODS_SOLD_CTD_LC", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);

            setCellWithMetricDateValue(row, "AL", "SL_START_DATE", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            setCellWithMetricDateValue(row, "AM", "SL_END_DATE", pobID, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
            if (!identifyContractFirstPOB.contains(contract)) {
                identifyContractFirstPOB.add(contract);
                setCellWithMetricLCValueContract(row, CellReference.convertColStringToIndex("J"), "THIRD_PARTY_COMMISSION_CTD_LC", contract.getId(), period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
                setCellWithMetricCCValueContract(row, CellReference.convertColStringToIndex("BA"), "CONTRACT_BILLINGS_CTD_CC", contract.getId(), period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
            }

            int colNum = CellReference.convertColStringToIndex("BA") + 1;
            int nextCol = 0;

            for (FinancialPeriod finPeriod : allPeriods) {
                String contractKey = contract.getId() + finPeriod.getId();
                if (!contractFirstPOBPeriod.contains(contractKey)) {
                    contractFirstPOBPeriod.add(contractKey);
                    setCellWithMetricCCValueContract(row, colNum, "CONTRACT_BILLINGS_PERIOD_CC", contract.getId(), finPeriod, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
                    nextCol = colNum + totalPeriodCount;
                    setCellWithMetricLCValueContract(row, nextCol, "CONTRACT_BILLINGS_PERIOD_CC", contract.getId(), finPeriod, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
                    nextCol = nextCol + totalPeriodCount;
                    setCellWithMetricCCValueContract(row, nextCol, "CONTRACT_BILLINGS_CTD_CC", contract.getId(), finPeriod, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
                    nextCol = nextCol + totalPeriodCount;
                    setCellWithMetricLCValueContract(row, nextCol, "CONTRACT_BILLINGS_CTD_CC", contract.getId(), finPeriod, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
                } else {
                    nextCol = colNum + 3 * totalPeriodCount;
                }

                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricCCValue(row, nextCol, "TRANSACTION_PRICE_CC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "ESTIMATED_COST_AT_COMPLETION_LC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "COST_OF_GOODS_SOLD_CTD_LC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricCCValue(row, nextCol, "LIQUIDATED_DAMAGES_CTD_CC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);

                nextCol = nextCol + totalPeriodCount;
                //setCellWithMeticLcValue(row, nextCol, "TODO", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "REVENUE_TO_RECOGNIZE_CTD_CC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "REVENUE_TO_RECOGNIZE_CTD_CC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "COST_OF_GOODS_SOLD_CTD_LC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                String contractKeyLCM = contract.getId() + finPeriod.getId() + "LR_CTD";
                if (!contractFirstPOBPeriod.contains(contractKeyLCM)) {
                    contractFirstPOBPeriod.add(contractKeyLCM);
                    setCellWithMetricLCValueContract(row, nextCol, "LOSS_RESERVE_CTD_LC", contract.getId(), finPeriod, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
                }
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricDecimalValue(row, nextCol, "GROSS_MARGIN_CTD", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "PROJECTED_GAIN_LOSS_BACKLOG_LC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                nextCol = nextCol + totalPeriodCount;
                setCellWithMetricLcValue(row, nextCol, "PARTIAL_SHIPMENT_COSTS_LC", pobID, finPeriod, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
                colNum++;
            }
            rowNum++;
        }

        Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "writeReport1InputAndCalc end");
        valueForAllFinancialPeriod.clear();
        valueForAllPeriodContractLevel.clear();
        pobMetricTypesProcessed.clear();
        contractMetricTypesProcessed.clear();
        return worksheet;
    }

    private void setHeaderColumnsName(XSSFSheet worksheet, List<FinancialPeriod> allPeriods) {

        XSSFRow metricCodeRow = worksheet.getRow(0);
        XSSFRow headerRow = worksheet.getRow(1);
        XSSFRow headerPeriod = worksheet.getRow(2);
        Font font = headerRow.getSheet().getWorkbook().createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        CellStyle headStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setWrapText(true);
        headStyle.setFont(font);
        int totalPeriodCount = allPeriods.size();
        int colNum = CellReference.convertColStringToIndex("BA") + 1;
        int nextCol = 0;
        for (FinancialPeriod financialPeriod : allPeriods) {

            setCellStringValue(metricCodeRow, colNum, "CONTRACT_BILLINGS_PERIOD_CC");
            setHeaderCellValue(headerRow, colNum, headStyle, "CONT CCY - Billings");
            setHeaderCellValue(headerPeriod, colNum, headStyle, financialPeriod.getName());
            nextCol = colNum + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "CONTRACT_BILLINGS_PERIOD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "LOCL CCY - Billings");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "CONTRACT_BILLINGS_CTD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "CONT CCY - Cum Billings");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "CONTRACT_BILLINGS_CTD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "LOCL CCY - Cum Billings");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "TRANSACTION_PRICE_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "CONT CCY - Transaction Price/Changes to Trans Price (excl. LDs)");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "ESTIMATED_COST_AT_COMPLETION_LC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "LOCL CCY - Estimated at Completion (EAC)/ Changes to EAC (excl. TPCs)");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "COST_OF_GOODS_SOLD_CTD_LC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "LOCL CCY - Cumulative Costs Incurred");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "LIQUIDATED_DAMAGES_CTD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "CONT CCY - Liquidated Damages (LDs)/Changes to LDs");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());

            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "TODO");
            setHeaderCellValue(headerRow, nextCol, headStyle, "LOCL CCY - Cum WIP Accumulation by PIT POb");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "REVENUE_TO_RECOGNIZE_CTD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum Rev Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum LD Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "REVENUE_TO_RECOGNIZE_CTD_CC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum Net Rev Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "COST_OF_GOODS_SOLD_CTD_LC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum Costs Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "LOSS_RESERVE_CTD_LC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum LCM Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "GROSS_MARGIN_CTD");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum Gross Margin Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "PROJECTED_GAIN_LOSS_BACKLOG_LC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "Cum Gain (Loss) Position");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;
            setCellStringValue(metricCodeRow, nextCol, "PARTIAL_SHIPMENT_COSTS_LC");
            setHeaderCellValue(headerRow, nextCol, headStyle, "LOCL CCY - Partial Shipments");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            colNum++;
        }
    }

    private void setCellWithMetricLcValue(XSSFRow row, int col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        CurrencyMetric metricValue = (CurrencyMetric) findMetricTypeValueForCurrentPeriod(metricType, value, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
        if (metricValue != null && metricValue.getLcValue() != null) {
            setCellValueWithDataFormat(row, col, metricValue.getLcValue());
        } else {
            setCellValueWithDataFormat(row, col, BigDecimal.ZERO);
        }
    }

    private void setCellWithMetricCCValue(XSSFRow row, int col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        CurrencyMetric metricValue = (CurrencyMetric) findMetricTypeValueForCurrentPeriod(metricType, value, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
        if (metricValue != null && metricValue.getCcValue() != null) {
            setCellValueWithDataFormat(row, col, metricValue.getCcValue());
        } else {
            setCellValueWithDataFormat(row, col, BigDecimal.ZERO);
        }
    }

    private void setCellWithMetricLCValueContract(XSSFRow row, int col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> contractMetricTypesProcessed, Map<ValueKey, Metric> valueForAllPeriodContractLevel) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        CurrencyMetric metricValue = (CurrencyMetric) findMetricTypeValueContractLevel(metricType, value, period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
        if (metricValue != null && metricValue.getLcValue() != null) {
            setCellValueWithDataFormat(row, col, metricValue.getLcValue());
        } else {
            setCellValueWithDataFormat(row, col, BigDecimal.ZERO);
        }
    }

    private void setCellWithMetricCCValueContract(XSSFRow row, int col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> contractMetricTypesProcessed, Map<ValueKey, Metric> valueForAllPeriodContractLevel) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        CurrencyMetric metricValue = (CurrencyMetric) findMetricTypeValueContractLevel(metricType, value, period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
        if (metricValue != null && metricValue.getCcValue() != null) {
            setCellValueWithDataFormat(row, col, metricValue.getCcValue());
        } else {
            setCellValueWithDataFormat(row, col, BigDecimal.ZERO);
        }
    }

    private void setCellWithDateValueContract(XSSFRow row, int col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> contractMetricTypesProcessed, Map<ValueKey, Metric> valueForAllPeriodContractLevel) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        DateMetric metricValue = (DateMetric) findMetricDateValueContractLevel(metricType, value, period, ru, contractMetricTypesProcessed, valueForAllPeriodContractLevel);
        if (metricValue != null && metricValue.getValue() != null) {
            setDateCellValue(row, col, metricValue.getValue());
        }
    }

    private void setCellWithMetricDecimalValue(XSSFRow row, int col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        DecimalMetric metricValue = (DecimalMetric) findMetricTypeDecimalValue(metricType, value, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
        if (metricValue != null && metricValue.getValue() != null) {
            setPercentCellValue(row, col, metricValue.getValue());
        } else {
            setPercentCellValue(row, col, BigDecimal.ZERO);
        }
    }

    private void setCellWithMetricStringValue(XSSFRow row, String col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        StringMetric metricValue = (StringMetric) findMetricTypeStringValueForCurrentPeriod(metricType, value, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
        if (metricValue != null && metricValue.getValue() != null) {
            setCellStringValue(row, CellReference.convertColStringToIndex(col), metricValue.getValue());
        }
    }

    private void setCellWithMetricDateValue(XSSFRow row, String col, String metricCode, Long value, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        MetricType metricType = metricService.getMetricTypeByCode(metricCode);
        DateMetric metricValue = (DateMetric) findMetricTypeDateValueForCurrentPeriod(metricType, value, period, ru, pobMetricTypesProcessed, valueForAllFinancialPeriod);
        if (metricValue != null && metricValue.getValue() != null) {
            setDateCellValue(row, CellReference.convertColStringToIndex(col), metricValue.getValue());
        }
    }

    public List<Object[]> getAllPobsDetails() {

        TypedQuery<Object[]> query = em.createQuery("SELECT c.name, c.id, c.salesOrderNumber, r.code, r.businessUnit.id, p.name, p.id, p.contract, p.description"
                + " FROM PerformanceObligation p join p.contract c join c.reportingUnit r "
                + "Where r.active = TRUE AND (c.workflowStatus is null OR c.workflowStatus <> :STATUS) order by r.id,c.id,p.id", Object[].class);
        //Where r.code = :RU
        query.setParameter("STATUS", WorkflowStatus.ARCHIVED);
        return (List<Object[]>) query.getResultList();
    }

    public List<Object[]> getAllPobsDetailsForRU(ReportingUnit ru) {

        TypedQuery<Object[]> query = em.createQuery("SELECT c.name, c.id, c.salesOrderNumber, r.code, r.businessUnit.id, p.name, p.id, p.contract, p.description"
                + " FROM PerformanceObligation p join p.contract c join c.reportingUnit r Where r.code = :RU "
                + " AND (c.workflowStatus is null OR c.workflowStatus <> :STATUS) order by r.id,c.id,p.id", Object[].class);
        query.setParameter("RU", ru.getCode());
        query.setParameter("STATUS", WorkflowStatus.ARCHIVED);
        return (List<Object[]>) query.getResultList();
    }

    public Metric findMetricTypeValueForCurrentPeriod(MetricType metricType, Long id, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        if (metricType == null) {
            return null;
        }
        ValueKey findWithValueKey = new ValueKey(metricType, period, id);
        if (!pobMetricTypesProcessed.contains(metricType)) {
            String condition;
            if (ru != null) {
                condition = "AND mt = :METRIC AND p.contract.reportingUnit = :RU";
            } else {
                condition = "AND mt = :METRIC ";
            }

            TypedQuery<Object[]> query = em.createQuery("SELECT m.financialPeriod, m, p.id "
                    + " FROM  PerformanceObligation p JOIN p.periodMetricSetMap pms, CurrencyMetric m JOIN m.metricSet ms JOIN m.metricType mt "
                    + "WHERE pms = ms " + condition + " AND (m.ccValue <> 0 OR m.lcValue <> 0 OR m.rcValue <> 0)", Object[].class);
            if (ru != null) {
                query.setParameter("RU", ru);
                query.setParameter("METRIC", metricType);
            } else {
                query.setParameter("METRIC", metricType);
            }
            List<Object[]> resultList = query.getResultList();

            Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "MetricType: " + metricType.getCode() + " POB resultList size: " + resultList.size());

            pobMetricTypesProcessed.add(metricType);

            if (!resultList.isEmpty()) {
                for (Object[] result : resultList) {
                    Metric cm = (Metric) result[1];
                    Long pobId = (Long) result[2];
                    ValueKey addWithValueKey = new ValueKey(cm.getMetricType(), (FinancialPeriod) result[0], pobId);
                    if (result[1] != null) {
                        valueForAllFinancialPeriod.put(addWithValueKey, cm);
                    } else {
                        valueForAllFinancialPeriod.put(addWithValueKey, null);
                    }
                }
            }
        }

        return valueForAllFinancialPeriod.get(findWithValueKey);
    }

    public Metric findMetricTypeValueContractLevel(MetricType metricType, Long id, FinancialPeriod period, ReportingUnit ru, Set<MetricType> contractMetricTypesProcessed, Map<ValueKey, Metric> valueForAllPeriodContractLevel) throws Exception {
        if (metricType == null) {
            return null;
        }
        ValueKey findWithValueKey = new ValueKey(metricType, period, id);
        if (!contractMetricTypesProcessed.contains(metricType)) {
            String condition;
            if (ru != null) {
                condition = "AND mt = :METRIC AND c.reportingUnit = :RU";
            } else {
                condition = "AND mt = :METRIC ";
            }
            TypedQuery<Object[]> query = em.createQuery("SELECT m.financialPeriod, m, c.id "
                    + " FROM  Contract c JOIN c.periodMetricSetMap pms ,CurrencyMetric m JOIN m.metricSet ms JOIN m.metricType mt "
                    //+ "WHERE pms = ms AND mt = :METRIC AND c.id = :CNT AND (m.ccValue <> 0 OR m.lcValue <> 0 OR m.rcValue <> 0)", Object[].class);
                    + "WHERE pms = ms " + condition + " AND (m.ccValue <> 0 OR m.lcValue <> 0 OR m.rcValue <> 0)", Object[].class);

            if (ru != null) {
                query.setParameter("RU", ru);
                query.setParameter("METRIC", metricType);
            } else {
                query.setParameter("METRIC", metricType);
            }

            List<Object[]> resultList = query.getResultList();

            Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "MetricType: " + metricType.getCode() + "Contract resultList size: " + resultList.size());

            contractMetricTypesProcessed.add(metricType);

            if (!resultList.isEmpty()) {
                for (Object[] result : resultList) {
                    Metric cm = (Metric) result[1];
                    Long contractId = (Long) result[2];
                    ValueKey addWithValueKey = new ValueKey(cm.getMetricType(), (FinancialPeriod) result[0], contractId);
                    if (result[1] != null) {
                        valueForAllPeriodContractLevel.put(addWithValueKey, cm);
                    } else {
                        valueForAllPeriodContractLevel.put(addWithValueKey, null);
                    }
                }
            }
        }

        return valueForAllPeriodContractLevel.get(findWithValueKey);
    }

    public Metric findMetricDateValueContractLevel(MetricType metricType, Long id, FinancialPeriod period, ReportingUnit ru, Set<MetricType> contractMetricTypesProcessed, Map<ValueKey, Metric> valueForAllPeriodContractLevel) throws Exception {
        if (metricType == null) {
            return null;
        }
        ValueKey findWithValueKey = new ValueKey(metricType, period, id);
        if (!contractMetricTypesProcessed.contains(metricType)) {
            String condition;
            if (ru != null) {
                condition = "AND mt = :METRIC AND c.reportingUnit = :RU";
            } else {
                condition = "AND mt = :METRIC ";
            }
            TypedQuery<Object[]> query = em.createQuery("SELECT m.financialPeriod, m, c.id "
                    + " FROM  Contract c JOIN c.periodMetricSetMap pms ,DateMetric m JOIN m.metricSet ms JOIN m.metricType mt "
                    + "WHERE pms = ms " + condition + " AND  m.value <> null", Object[].class);

            if (ru != null) {
                query.setParameter("RU", ru);
                query.setParameter("METRIC", metricType);
            } else {
                query.setParameter("METRIC", metricType);
            }

            List<Object[]> resultList = query.getResultList();

            Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "MetricType: " + metricType.getCode() + "Contract resultList size: " + resultList.size());

            contractMetricTypesProcessed.add(metricType);

            if (!resultList.isEmpty()) {
                for (Object[] result : resultList) {
                    Metric cm = (Metric) result[1];
                    Long contractId = (Long) result[2];
                    ValueKey addWithValueKey = new ValueKey(cm.getMetricType(), (FinancialPeriod) result[0], contractId);
                    if (result[1] != null) {
                        valueForAllPeriodContractLevel.put(addWithValueKey, cm);
                    } else {
                        valueForAllPeriodContractLevel.put(addWithValueKey, null);
                    }
                }
            }
        }

        return valueForAllPeriodContractLevel.get(findWithValueKey);
    }

    public Metric findMetricTypeDecimalValue(MetricType metricType, Long id, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        if (metricType == null) {
            return null;
        }
        ValueKey findWithValueKey = new ValueKey(metricType, period, id);
        if (!pobMetricTypesProcessed.contains(metricType)) {
            String condition;
            if (ru != null) {
                condition = "AND mt = :METRIC AND p.contract.reportingUnit = :RU";
            } else {
                condition = "AND mt = :METRIC ";
            }
            Query query = em.createQuery("SELECT m.financialPeriod, m, p.id "
                    + " FROM  PerformanceObligation p JOIN p.periodMetricSetMap pms,DecimalMetric m JOIN m.metricSet ms JOIN m.metricType mt"
                    + " WHERE pms = ms " + condition + " AND  m.value <> null");

            if (ru != null) {
                query.setParameter("RU", ru);
                query.setParameter("METRIC", metricType);
            } else {
                query.setParameter("METRIC", metricType);
            }
            List<Object[]> resultList = query.getResultList();

            pobMetricTypesProcessed.add(metricType);

            if (!resultList.isEmpty()) {
                for (Object[] result : resultList) {
                    Metric cm = (Metric) result[1];
                    Long pobId = (Long) result[2];
                    ValueKey addWithValueKey = new ValueKey(cm.getMetricType(), (FinancialPeriod) result[0], pobId);
                    if (result[1] != null) {
                        valueForAllFinancialPeriod.put(addWithValueKey, cm);
                    } else {
                        valueForAllFinancialPeriod.put(addWithValueKey, null);
                    }
                }
            }
        }

        return valueForAllFinancialPeriod.get(findWithValueKey);
    }

    public Metric findMetricTypeStringValueForCurrentPeriod(MetricType metricType, Long id, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        if (metricType == null) {
            return null;
        }
        ValueKey findWithValueKey = new ValueKey(metricType, period, id);
        if (!pobMetricTypesProcessed.contains(metricType)) {
            String condition;
            if (ru != null) {
                condition = "AND mt = :METRIC AND p.contract.reportingUnit = :RU";
            } else {
                condition = "AND mt = :METRIC ";
            }
            Query query = em.createQuery("SELECT m.financialPeriod, m, p.id "
                    + " FROM  PerformanceObligation p JOIN p.periodMetricSetMap pms,StringMetric m JOIN m.metricSet ms JOIN m.metricType mt"
                    + " WHERE pms = ms " + condition + " AND  m.value <> null");

            if (ru != null) {
                query.setParameter("RU", ru);
                query.setParameter("METRIC", metricType);
            } else {
                query.setParameter("METRIC", metricType);
            }
            List<Object[]> resultList = query.getResultList();

            pobMetricTypesProcessed.add(metricType);

            if (!resultList.isEmpty()) {
                for (Object[] result : resultList) {
                    Metric cm = (Metric) result[1];
                    Long pobId = (Long) result[2];
                    ValueKey addWithValueKey = new ValueKey(cm.getMetricType(), (FinancialPeriod) result[0], pobId);
                    if (result[1] != null) {
                        valueForAllFinancialPeriod.put(addWithValueKey, cm);
                    } else {
                        valueForAllFinancialPeriod.put(addWithValueKey, null);
                    }
                }
            }
        }

        return valueForAllFinancialPeriod.get(findWithValueKey);
    }

    public Metric findMetricTypeDateValueForCurrentPeriod(MetricType metricType, Long id, FinancialPeriod period, ReportingUnit ru, Set<MetricType> pobMetricTypesProcessed, Map<ValueKey, Metric> valueForAllFinancialPeriod) throws Exception {
        if (metricType == null) {
            return null;
        }
        ValueKey findWithValueKey = new ValueKey(metricType, period, id);
        if (!pobMetricTypesProcessed.contains(metricType)) {
            String condition;
            if (ru != null) {
                condition = "AND mt = :METRIC AND p.contract.reportingUnit = :RU";
            } else {
                condition = "AND mt = :METRIC ";
            }
            Query query = em.createQuery("SELECT m.financialPeriod, m, p.id "
                    + " FROM  PerformanceObligation p JOIN p.periodMetricSetMap pms,DateMetric m JOIN m.metricSet ms JOIN m.metricType mt"
                    + " WHERE pms = ms " + condition + " AND  m.value <> null");
            if (ru != null) {
                query.setParameter("RU", ru);
                query.setParameter("METRIC", metricType);
            } else {
                query.setParameter("METRIC", metricType);
            }

            List<Object[]> resultList = query.getResultList();

            pobMetricTypesProcessed.add(metricType);

            if (!resultList.isEmpty()) {
                for (Object[] result : resultList) {
                    Metric cm = (Metric) result[1];
                    Long pobId = (Long) result[2];
                    ValueKey addWithValueKey = new ValueKey(cm.getMetricType(), (FinancialPeriod) result[0], pobId);
                    if (result[1] != null) {
                        valueForAllFinancialPeriod.put(addWithValueKey, cm);
                    } else {
                        valueForAllFinancialPeriod.put(addWithValueKey, null);
                    }
                }
            }
        }

        return valueForAllFinancialPeriod.get(findWithValueKey);
    }

    /**
     * private ExchangeRate getExchangeRate(Currency fromCurrency, Currency toCurrency, FinancialPeriod period) throws Exception { ExchangeRate exchangeRate =
     * null; try { String cacheKey = period.getId() + fromCurrency.getCurrencyCode() + toCurrency.getCurrencyCode(); exchangeRate =
     * currencyCache.getExchangeRate(cacheKey); if (exchangeRate == null) { throw new Exception("ExchangeRate not found: " + cacheKey);
     * //Logger.getLogger(ReportsDisclosureService.class.getName()).log(Level.INFO, "from: " + fromCurrency.getCurrencyCode() + " to: " +
     * toCurrency.getCurrencyCode() + " period: " + period.getId()); //exchangeRate = currencyService.findRateByFromToPeriod(fromCurrency, toCurrency, period);
     * //exchangeRateCache.put(cacheKey, exchangeRate); } } catch (Exception e) { throw new IllegalStateException("Unable to find an exchange rate from " +
     * fromCurrency.getCurrencyCode() + " to " + toCurrency.getCurrencyCode() + " in period " + period.getId()); }
     *
     * return exchangeRate; } *
     */
    public List<ExchangeRate> findAllRates() throws Exception {
        Query query = em.createQuery("SELECT rate FROM ExchangeRate rate");
        return (List<ExchangeRate>) query.getResultList();
    }

    public List<FinancialPeriod> findValidDataPeriods() {
        Query query = em.createQuery("SELECT p FROM FinancialPeriod p WHERE P.startDate >= {d '2017-11-01'} ORDER BY p.startDate ASC");
        return (List<FinancialPeriod>) query.getResultList();
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
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(currentStyle);
        }
    }

    private void setCellNumericValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            //CellStyle currentStyle = cell.getCellStyle();
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(numberCellStyle);
        }
    }

    private void setHeaderCellValue(XSSFRow row, int cellNum, CellStyle currentStyle, String value) {
        if (value == null) {
            return;
        }

        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
        cell.setCellStyle(currentStyle);
    }

    private void setCellValueWithDataFormat(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(currencyCellStyle);
    }

    private void setDateCellValue(XSSFRow row, int cellNum, LocalDate value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value.toString());
            cell.setCellStyle(currentStyle);
        }

    }

    private void setPercentCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(percentageCellStyle);
    }
}
