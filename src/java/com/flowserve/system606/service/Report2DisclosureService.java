/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.ReportingUnit;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
 * @author shubhamc
 */
@Stateless
public class Report2DisclosureService {

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

    public void generateReport1Tab2PocOutputs(InputStream inputStream, FileOutputStream outputStream, FinancialPeriod period, ReportingUnit ru) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            XSSFSheet worksheet = workbook.getSheet("POC Output Report");

            writeReport1Tab2PocOutputs(worksheet, period, ru);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();
    }

    public XSSFSheet writeReport1Tab2PocOutputs(XSSFSheet worksheet, FinancialPeriod period, ReportingUnit ru) throws Exception {

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

        List<FinancialPeriod> allPeriods = findValidDataPeriods();
        setHeaderColumnsName(worksheet, allPeriods);
        setMetricTypeName(worksheet, allPeriods);
        XSSFRow row;
        int rowNum = 8;
        List<ReportingUnit> ruList = new ArrayList<>();
        if (ru == null) {
            ruList = adminService.findAllReportingUnits();
        } else {
            ruList.add(adminService.findReportingUnitByCode(ru.getCode()));
        }
        for (ReportingUnit runit : ruList) {
            for (Contract contract : runit.getUnarchivedContracts()) {

                BigDecimal transactionPrice = getTransactionPriceAdjusted(contract, period);
                BigDecimal liquidatedDamage = getLiquidatedDamagesCTD(contract, period);
                BigDecimal EAC = getEAC(contract, period);
                BigDecimal estimatedGrossProfit = getEstimatedGrossProfit(contract, period);
                BigDecimal estimatedGrossMargin = getEstimatedGrossMargin(contract, period);
                BigDecimal thirdPartyCommCTD = getThirdPartyCommissionCTDLC(contract, period);
                //Contract From Inception to Date below
                BigDecimal percentComplete = getPercentComplete(contract, period);
                BigDecimal revenueCTD = getRevenueRecognizeCTD(contract, period);
                BigDecimal liquidatedDamageToRecCTD = getLiquidatedDamagesToRecognizeCTD(contract, period);
                BigDecimal localCostCTDLC = getCostOfGoodsSoldCTD(contract, period);
                BigDecimal lossReserveCTD = getLossReserveCTD(contract, period);
                BigDecimal grossProfitCTD = getGrossProfitCTD(contract, period);
                BigDecimal grossMarginCTD = getGrossMarginCTD(contract, period);
                BigDecimal tpcTotalExpense = getTotalTPCExpenseCTD(contract, period);
                BigDecimal billToDate = getContractBillingsCTDLC(contract, period);
                BigDecimal costToComplete = getCostToCompleteLC(contract, period);
                BigDecimal contractAsset = getContractAssetCTD(contract, period);
                BigDecimal contractLiability = getContractLiabilityCTD(contract, period);
                //Monthly Income Statement Impact below
                BigDecimal revenueToRecognizePeriod = getRevenueRecognizePeriodLC(contract, period);
                BigDecimal liquidatedDamagePeriod = getLiquidatedDamagesPeriod(contract, period);
                BigDecimal costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(contract, period);
                BigDecimal lossReservePeriodADJLC = getLossReservePeriodADJLC(contract, period);
                BigDecimal grossProfitPeriodLC = getGrossProfitPeriod(contract, period);
                BigDecimal tcpIncured = getTPCIncured(contract, period);
                BigDecimal acceleratedTCP = getAcceleratedTPC(contract, period);
                BigDecimal operatingIncome = getOperatingIncomePeriodLC(contract, period);

                //Quarterly Income Statement Impact below
                List<FinancialPeriod> qtdPeriods = financialPeriodService.getQTDFinancialPeriods(period);
                BigDecimal revenueToRecognizePeriodQTD = getAccuRevenueToRecognizePeriodLC(contract, qtdPeriods);
                BigDecimal liquidatedDamagePeriodQTD = getAccuLiquidatedDamagesPeriod(contract, qtdPeriods);
                BigDecimal costGoodsSoldPeriodLCQTD = getAccuCostGoodsSoldPeriodLC(contract, qtdPeriods);
                BigDecimal lossReservePeriodADJLCQTD = getAccuLossReservePeriodADJLC(contract, qtdPeriods);
                BigDecimal grossProfitPeriodLCQTD = getAccuGrossProfitPeriodLC(contract, qtdPeriods);
                BigDecimal tcpIncuredQTD = getAccuTPCIncured(contract, qtdPeriods);
                BigDecimal acceleratedTCPQTD = getAccuAcceleratedTPC(contract, qtdPeriods);
                BigDecimal operatingIncomeQTD = getAccuOperatingIncomePeriod(contract, qtdPeriods);

                row = worksheet.getRow(rowNum);
                if (row == null) {
                    row = worksheet.createRow(rowNum);
                }
                setCellStringValue(row, 0, "RU" + runit.getCode() + "_" + contract.getName());
                setCellValue(row, 1, BigDecimal.valueOf(contract.getId()));
                setCellStringValue(row, 2, contract.getSalesOrderNumber());
                setCellStringValue(row, 4, "RU" + contract.getReportingUnit().getCode());
                setCellStringValue(row, 5, runit.getBusinessUnit().getName());
                if (contract.getContractCurrency().equals(contract.getLocalCurrency())) {
                    setCellStringValue(row, 6, "NON-FX");
                } else {
                    setCellStringValue(row, 6, "FX");
                }
                setCellValueWithDataFormat(row, 8, transactionPrice);
                setCellValueWithDataFormat(row, 9, liquidatedDamage);
                setCellValueWithDataFormat(row, 10, EAC);
                setCellValueWithDataFormat(row, 11, estimatedGrossProfit);
                setPercentCellValue(row, 12, estimatedGrossMargin);
                setCellValueWithDataFormat(row, 13, thirdPartyCommCTD);
                setPercentCellValue(row, 15, percentComplete);
                setCellValueWithDataFormat(row, 16, revenueCTD);
                setCellValueWithDataFormat(row, 17, liquidatedDamageToRecCTD);
                setCellValueWithDataFormat(row, 18, localCostCTDLC);
                setCellValueWithDataFormat(row, 19, lossReserveCTD);
                setCellValueWithDataFormat(row, 20, grossProfitCTD);
                setPercentCellValue(row, 21, grossMarginCTD);
                setCellValueWithDataFormat(row, 22, tpcTotalExpense);
                setCellValueWithDataFormat(row, 23, billToDate);
                setCellValueWithDataFormat(row, 24, costToComplete);
                setCellValueWithDataFormat(row, 25, contractAsset);
                setCellValueWithDataFormat(row, 26, contractLiability);
                setCellValueWithDataFormat(row, 28, revenueToRecognizePeriod);
                setCellValueWithDataFormat(row, 29, liquidatedDamagePeriod);
                setCellValueWithDataFormat(row, 30, costGoodsSoldPeriodLC);
                setCellValueWithDataFormat(row, 31, lossReservePeriodADJLC);
                setCellValueWithDataFormat(row, 32, grossProfitPeriodLC);
                setCellValueWithDataFormat(row, 33, tcpIncured);
                setCellValueWithDataFormat(row, 34, acceleratedTCP);
                setCellValueWithDataFormat(row, 35, operatingIncome);
                setCellValueWithDataFormat(row, 37, revenueToRecognizePeriodQTD);
                setCellValueWithDataFormat(row, 38, liquidatedDamagePeriodQTD);
                setCellValueWithDataFormat(row, 39, costGoodsSoldPeriodLCQTD);
                setCellValueWithDataFormat(row, 40, lossReservePeriodADJLCQTD);
                setCellValueWithDataFormat(row, 41, grossProfitPeriodLCQTD);
                setCellValueWithDataFormat(row, 42, tcpIncuredQTD);
                setCellValueWithDataFormat(row, 43, acceleratedTCPQTD);
                setCellValueWithDataFormat(row, 44, operatingIncomeQTD);
                int columnIndex = 45;
                int totalPeriodCount = allPeriods.size();
                for (FinancialPeriod fp : allPeriods) {

                    revenueToRecognizePeriod = getRevenueRecognizePeriodLC(contract, fp);
                    setCellValueWithDataFormat(row, columnIndex, revenueToRecognizePeriod);

                    liquidatedDamagePeriod = getLiquidatedDamagesPeriod(contract, fp);
                    setCellValueWithDataFormat(row, totalPeriodCount + columnIndex, liquidatedDamagePeriod);

                    BigDecimal netSalesPeriodLC = getNetSalesPeriodLC(contract, fp);
                    setCellValueWithDataFormat(row, 2 * totalPeriodCount + columnIndex, netSalesPeriodLC);

                    costGoodsSoldPeriodLC = getCostGoodsSoldPeriodLC(contract, fp);
                    setCellValueWithDataFormat(row, 3 * totalPeriodCount + columnIndex, costGoodsSoldPeriodLC);

                    BigDecimal lossReservePeriodLC = getLossReservePeriodCTD(contract, fp);
                    setCellValueWithDataFormat(row, 4 * totalPeriodCount + columnIndex, lossReservePeriodLC);

                    //Disclosures-Total COGS TODO Metric type not Provided
                    grossProfitPeriodLC = getGrossProfitPeriod(contract, fp);
                    setCellValueWithDataFormat(row, 6 * totalPeriodCount + columnIndex, grossProfitPeriodLC);

                    //Disclosures-Gross Margin % TODO Metric type not Provided
                    BigDecimal tpcRecognisedLC = getTPCrecognisedPeriodLC(contract, fp);
                    setCellValueWithDataFormat(row, 8 * totalPeriodCount + columnIndex, tpcRecognisedLC);

                    operatingIncome = getOperatingIncomePeriodLC(contract, fp);
                    setCellValueWithDataFormat(row, 9 * totalPeriodCount + columnIndex, operatingIncome);

                    billToDate = getContractBillingsCTDLC(contract, fp);
                    setCellValueWithDataFormat(row, 10 * totalPeriodCount + columnIndex, billToDate);

                    //FIN & BL - Inventory - WIP TODO Metric type not Provided
                    contractAsset = getContractAssetCTD(contract, fp);
                    setCellValueWithDataFormat(row, 12 * totalPeriodCount + columnIndex, contractAsset);

                    //FIN & BL - Total Assets TODO Metric type not Provided
                    BigDecimal accruedCost = getAccruedCostPeriodLC(contract, fp);
                    setCellValueWithDataFormat(row, 14 * totalPeriodCount + columnIndex, accruedCost);

                    contractLiability = getContractLiabilityCTD(contract, fp);
                    setCellValueWithDataFormat(row, 15 * totalPeriodCount + columnIndex, contractLiability);

                    lossReserveCTD = getLossReservePeriodADJLC(contract, fp);
                    setCellValueWithDataFormat(row, 16 * totalPeriodCount + columnIndex, lossReserveCTD);

                    BigDecimal tpctoRecognizeCTD = getTPCtoRecognizeCTD(contract, fp);
                    setCellValueWithDataFormat(row, 17 * totalPeriodCount + columnIndex, tpctoRecognizeCTD);

                    //FIN & BL - Total Liabilities TODO Metric type not Provided
                    BigDecimal retainedEarnings = getRetainedEarningsLC(contract, fp);
                    setCellValueWithDataFormat(row, 19 * totalPeriodCount + columnIndex, retainedEarnings);

                    //FIN & BL - Total Liabilities and Equity TODO Metric type not Provided
                    //FIN & BL - Check TODO Metric type not Provided
                    //FIN & BL - Gross Margin % in Backlog TODO Metric type not Provided
                    columnIndex++;
                }
                rowNum++;
            }
        }
        return worksheet;
    }

    private void setHeaderColumnsName(XSSFSheet worksheet, List<FinancialPeriod> allPeriods) {

        XSSFRow headerRow = worksheet.getRow(6);
        XSSFRow headerPeriod = worksheet.getRow(7);
        headerRow.setHeight((short) 1200);
        Font font = headerRow.getSheet().getWorkbook().createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        CellStyle headStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setWrapText(true);
        headStyle.setFont(font);

        int totalPeriodCount = allPeriods.size();
        int colNum = CellReference.convertColStringToIndex("AS") + 1;
        int nextCol = 0;
        for (FinancialPeriod financialPeriod : allPeriods) {

            setHeaderCellValue(headerRow, colNum, headStyle, "Disclosures-Monthly Revenue Recongnized ");
            setHeaderCellValue(headerPeriod, colNum, headStyle, financialPeriod.getName());
            nextCol = colNum + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Monthly LDs Recongnized ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Net Revenue ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Monthly COGS (Exc Loss Res Adj) ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Monthly Loss Res Adj ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Total COGS ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Gross Margin $ ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Gross Margin % ");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Monthly TPC Recognized");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "Disclosures-Operating Income");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Billings (AR - No Collections Assumed)");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Inventory - WIP, Reserve");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Contract Asset");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Total Assets");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Accrued Costs (AP, WIP, Payroll, etc.)");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Contract Liability");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Loss Reserve");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Accrued TPCs");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Total Liabilities");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Retained Earnings");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Total Liabilities and Equity");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Check");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "FIN & BL - Gross Margin % in Backlog");
            setHeaderCellValue(headerPeriod, nextCol, headStyle, financialPeriod.getName());
            colNum++;

        }
    }

    private void setMetricTypeName(XSSFSheet worksheet, List<FinancialPeriod> allPeriods) {

        XSSFRow headerRow = worksheet.getRow(2);
        headerRow.setHeight((short) 1200);
        Font font = headerRow.getSheet().getWorkbook().createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        CellStyle headStyle = headerRow.getSheet().getWorkbook().createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setWrapText(true);
        headStyle.setFont(font);

        setHeaderCellValue(headerRow, 8, headStyle, "TRANSACTION_PRICE_ADJ_LC");
        setHeaderCellValue(headerRow, 9, headStyle, "LIQUIDATED_DAMAGES_CTD_CC");
        setHeaderCellValue(headerRow, 10, headStyle, "ESTIMATED_COST_AT_COMPLETION_LC");
        setHeaderCellValue(headerRow, 11, headStyle, "ESTIMATED_GROSS_PROFIT_LC");
        setHeaderCellValue(headerRow, 12, headStyle, "ESTIMATED_GROSS_MARGIN");
        setHeaderCellValue(headerRow, 13, headStyle, "THIRD_PARTY_COMMISSION_CTD_LC");
        setHeaderCellValue(headerRow, 15, headStyle, "PERCENT_COMPLETE");
        setHeaderCellValue(headerRow, 16, headStyle, "REVENUE_TO_RECOGNIZE_CTD_CC");
        setHeaderCellValue(headerRow, 17, headStyle, "LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC");
        setHeaderCellValue(headerRow, 18, headStyle, "COST_OF_GOODS_SOLD_CTD_LC");
        setHeaderCellValue(headerRow, 19, headStyle, "LOSS_RESERVE_CTD_LC");
        setHeaderCellValue(headerRow, 20, headStyle, "GROSS_PROFIT_CTD_LC");
        setHeaderCellValue(headerRow, 21, headStyle, "GROSS_MARGIN_CTD");
        setHeaderCellValue(headerRow, 22, headStyle, "TPC_TOTAL_EXPENSE_CTD_LC");
        setHeaderCellValue(headerRow, 23, headStyle, "CONTRACT_BILLINGS_CTD_CC");
        setHeaderCellValue(headerRow, 24, headStyle, "COST_TO_COMPLETE_LC");
        setHeaderCellValue(headerRow, 25, headStyle, "CONTRACT_ASSET_CTD_LC");
        setHeaderCellValue(headerRow, 26, headStyle, "CONTRACT_LIABILITY_CTD_LC");
        setHeaderCellValue(headerRow, 28, headStyle, "REVENUE_TO_RECOGNIZE_PERIOD_CC");
        setHeaderCellValue(headerRow, 29, headStyle, "LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC");
        setHeaderCellValue(headerRow, 30, headStyle, "COST_OF_GOODS_SOLD_PERIOD_LC");
        setHeaderCellValue(headerRow, 31, headStyle, "LOSS_RESERVE_PERIOD_ADJ_LC");
        setHeaderCellValue(headerRow, 32, headStyle, "GROSS_PROFIT_PERIOD_LC");
        setHeaderCellValue(headerRow, 33, headStyle, "THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC");
        setHeaderCellValue(headerRow, 34, headStyle, "TPC_TO_ACCEL_PERIOD_ADJ_LC");
        setHeaderCellValue(headerRow, 35, headStyle, "OPERATING_INCOME_PERIOD_LC");
        setHeaderCellValue(headerRow, 37, headStyle, "REVENUE_TO_RECOGNIZE_PERIOD_CC");
        setHeaderCellValue(headerRow, 38, headStyle, "LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC");
        setHeaderCellValue(headerRow, 39, headStyle, "COST_OF_GOODS_SOLD_PERIOD_LC");
        setHeaderCellValue(headerRow, 40, headStyle, "LOSS_RESERVE_PERIOD_ADJ_LC");
        setHeaderCellValue(headerRow, 41, headStyle, "GROSS_PROFIT_PERIOD_LC");
        setHeaderCellValue(headerRow, 42, headStyle, "THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC");
        setHeaderCellValue(headerRow, 43, headStyle, "TPC_TO_ACCEL_PERIOD_ADJ_LC");
        setHeaderCellValue(headerRow, 44, headStyle, "OPERATING_INCOME_PERIOD_LC");

        int totalPeriodCount = allPeriods.size();
        int colNum = CellReference.convertColStringToIndex("AS") + 1;
        int nextCol = 0;
        for (FinancialPeriod financialPeriod : allPeriods) {

            setHeaderCellValue(headerRow, colNum, headStyle, "REVENUE_TO_RECOGNIZE_PERIOD_CC");
            nextCol = colNum + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "NET_PERIOD_SALES_CC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "COST_OF_GOODS_SOLD_PERIOD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "LOSS_RESERVE_PERIOD_ADJ_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "GROSS_PROFIT_PERIOD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "OPERATING_INCOME_PERIOD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "CONTRACT_BILLINGS_CTD_CC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "CONTRACT_ASSET_CTD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TOTAL_COST_GOODS_SOLD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "CONTRACT_LIABILITY_CTD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "LOSS_RESERVE_CTD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "THIRD_PARTY_COMMISSION_TO_RECOGNIZE_CTD_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "RETAINED_EARNINGS_LC");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            nextCol = nextCol + totalPeriodCount;

            setHeaderCellValue(headerRow, nextCol, headStyle, "TODO");
            colNum++;

        }
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

    private void setHeaderCellValue(XSSFRow row, int cellNum, CellStyle currentStyle, String value) {
        if (value == null) {
            return;
        }

        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);
        cell.setCellStyle(currentStyle);
    }

    private void setPercentCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(percentageCellStyle);
    }

    private void setCellValueWithDataFormat(XSSFRow row, int cellNum, BigDecimal value) {
        if (value == null) {
            return;
        }
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(currencyCellStyle);
    }

    private BigDecimal getTransactionPriceAdjusted(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_ADJ_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getLiquidatedDamagesCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
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
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getPercentComplete(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("PERCENT_COMPLETE", measureable, period).getValue();
    }

    private BigDecimal getRevenueRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getLiquidatedDamagesToRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getCostOfGoodsSoldCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getLossReserveCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LOSS_RESERVE_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getTPCtoRecognizeCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getLossReservePeriodCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getGrossProfitCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("GROSS_PROFIT_CTD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossMarginCTD(Measurable measureable, FinancialPeriod period) {
        return calculationService.getDecimalMetric("GROSS_MARGIN_CTD", measureable, period).getValue();
    }

    private BigDecimal getTotalTPCExpenseCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TPC_TOTAL_EXPENSE_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getContractBillingsCTDLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_BILLINGS_CTD_CC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getCostToCompleteLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_TO_COMPLETE_LC", measureable, period).getLcValue();
    }

    private BigDecimal getContractAssetCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_ASSET_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getContractLiabilityCTD(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("CONTRACT_LIABILITY_CTD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getLiquidatedDamagesPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getRevenueRecognizePeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", measureable, period).getLcValue();
    }

    private BigDecimal getRetainedEarningsLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("RETAINED_EARNINGS_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getCostGoodsSoldPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_PERIOD_LC", measureable, period).getLcValue();
    }

    private BigDecimal getGrossProfitPeriod(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("GROSS_PROFIT_PERIOD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getLossReservePeriodADJLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
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
        BigDecimal value = calculationService.getCurrencyMetric("OPERATING_INCOME_PERIOD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getAccruedCostPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("TOTAL_COST_GOODS_SOLD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal getNetSalesPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric("NET_PERIOD_SALES_CC", measureable, period).getLcValue();
    }

    private BigDecimal getTPCrecognisedPeriodLC(Measurable measureable, FinancialPeriod period) throws Exception {
        BigDecimal value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", measureable, period).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
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

    private BigDecimal getAccuLossReservePeriodADJLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("LOSS_RESERVE_PERIOD_ADJ_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuGrossProfitPeriodLC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("GROSS_PROFIT_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuTPCIncured(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", measureable, qtdPeriods).getLcValue();
    }

    private BigDecimal getAccuAcceleratedTPC(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        return calculationService.getAccumulatedCurrencyMetricAcrossPeriods("TPC_TO_ACCEL_PERIOD_ADJ_LC", measureable, qtdPeriods).getLcValue();

        //return BigDecimal.ZERO;
    }

    private BigDecimal getAccuOperatingIncomePeriod(Measurable measureable, List<FinancialPeriod> qtdPeriods) throws Exception {
        BigDecimal value = calculationService.getAccumulatedCurrencyMetricAcrossPeriods("OPERATING_INCOME_PERIOD_LC", measureable, qtdPeriods).getLcValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    public List<FinancialPeriod> findValidDataPeriods() {
        Query query = em.createQuery("SELECT p FROM FinancialPeriod p WHERE P.startDate >= {d '2017-11-01'} ORDER BY p.startDate ASC");
        return (List<FinancialPeriod>) query.getResultList();
    }
}
