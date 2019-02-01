package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.Event;
import com.flowserve.system606.model.EventType;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author kgraves
 */
@Stateless
public class DataExtractionService {

    @Inject
    private AdminService adminService;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private EventService eventService;

    public void generatePobOutput(String inputFile) throws Exception {

        // General Note: NOV-17 is our start point for historical data.
        Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Processing POCI Data: " + inputFile);
        Connection connection = null;
        // Step 1: Loading or registering Oracle JDBC driver class
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException cnfex) {
            Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Problem in loading or registering MS Access JDBC driver");
        }

        try {
            String dbURL = "jdbc:ucanaccess://" + inputFile;
            Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "dbURL : " + dbURL);
            connection = DriverManager.getConnection(dbURL);

            PreparedStatement pst = connection.prepareStatement("INSERT INTO POB_Output "
                    + "(PERIOD_ID, RU_CODE, CONTRACT_ID, POB_ID, POB_NAME, REVENUE_RECOGNITION_METHOD, TRANSACTION_PRICE_CC,LIQUIDATED_DAMAGES_CTD_CC,"
                    + "ESTIMATED_COST_AT_COMPLETION_LC,LOCAL_COSTS_CTD_LC,THIRD_PARTY_COSTS_CTD_LC,INTERCOMPANY_COSTS_CTD_LC,DELIVERY_DATE,"
                    + "PARTIAL_SHIPMENT_COSTS_LC,ESTIMATED_GROSS_PROFIT_LC,"
                    + "ESTIMATED_GROSS_MARGIN,PERCENT_COMPLETE,COST_OF_GOODS_SOLD_CTD_LC,COST_OF_GOODS_SOLD_PERIOD_LC,"
                    + "COST_GOODS_SOLD_BACKLOG_LC,CHANGE_IN_EAC_LC,REVENUE_TO_RECOGNIZE_CTD_CC,REVENUE_TO_RECOGNIZE_CTD_LC,"
                    + "REVENUE_TO_RECOGNIZE_PERIOD_CC,REVENUE_TO_RECOGNIZE_PERIOD_LC,LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC,"
                    + "LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_LC,LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC,"
                    + "LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_LC,LIQUIDATED_DAMAGES_BACKLOG_CC,LIQUIDATED_DAMAGES_BACKLOG_LC,"
                    + "NET_PERIOD_SALES_CC,NET_PERIOD_SALES_LC,TRANSACTION_PRICE_BACKLOG_CC,TRANSACTION_PRICE_BACKLOG_LC,"
                    + "TRANSACTION_PRICE_NET_LD_LC,REMAINING_ESTIMATE_COMPLETE_LC,PROJECTED_GAIN_LOSS_LC,"
                    + "PROJECTED_GAIN_LOSS_BACKLOG_LC,SALES_DESTINATION,"
                    + "OEAM_DISAGG,SL_START_DATE,SL_END_DATE,COSTS_INCURRED_CTD_LC,GROSS_PROFIT_CTD_LC,GROSS_PROFIT_PERIOD_LC,"
                    + "GROSS_MARGIN_CTD,GROSS_MARGIN_PERIOD,COST_TO_COMPLETE_LC) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?, ?, ?,?,?,?,?,?, ?,?,?,?)");

            PreparedStatement contractST = connection.prepareStatement("INSERT INTO Contract_Ouput "
                    + "(PERIOD_ID, RU_CODE, CONTRACT_ID, THIRD_PARTY_COMMISSION_CTD_LC,"
                    + "COST_TO_COMPLETE_LC,CONTRACT_LIABILITY_CTD_LC,CONTRACT_ASSET_CTD_LC,PERCENT_COMPLETE,"
                    + "CONTRACT_REVENUE_TO_RECOGNIZE_CTD_CC,TOTAL_COST_GOODS_SOLD_LC,LOSS_RESERVE_CTD_LC,LOSS_RESERVE_PERIOD_ADJ_LC,"
                    + "GROSS_PROFIT_PERIOD_LC,BOOKING_DATE,THIRD_PARTY_COMMISSION_TO_RECOGNIZE_CTD_LC,"
                    + "THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC,PROJECT_GAIN_LOSS_INCL_TPC_BACKLOG_LC,CONTRACT_BILLINGS_CTD_CC,CONTRACT_BILLINGS_CTD_LC,"
                    + "TPC_TO_ACCEL_PERIOD_LC,TPC_TO_ACCEL_CTD_LC,CONTRACT_BILLINGS_PERIOD_CC,CONTRACT_BILLINGS_PERIOD_LC,CONTRACT_POSITION_LC,"
                    + "FX_GAIN_LOSS_PERIOD_LC,OPERATING_INCOME_PERIOD_LC,NET_INCOME_PERIOD_LC,RETAINED_EARNINGS_LC,GROSS_PROFIT_CTD_LC,"
                    + "ESTIMATED_GROSS_MARGIN,GROSS_MARGIN_CTD,GROSS_MARGIN_PERIOD,CONTRACT_LIABILITY_PERIOD_LC,"
                    + "CONTRACT_ASSET_PERIOD_LC,TPC_TOTAL_EXPENSE_PERIOD_LC,CONTRACT_CLOSURE_DATE) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            PreparedStatement billingST = connection.prepareStatement("INSERT INTO Billings (RU_CODE, CONTRACT_ID, INVOICE_NUMBER, BILLING_DATE,DELIVERY_DATE,"
                    + "AMOUNT_LOCAL_CURRENCY,AMOUNT_CONTRACT_CURRENCY,DESCRIPTION) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            String[] reportingUnits = {"1100", "5050", "7866", "8405", "1205", "8225"};

            FinancialPeriod period = financialPeriodService.findById("NOV-17");
            do {
                for (ReportingUnit reportingUnit : adminService.findAllReportingUnits()) {
//                    if (!reportingUnit.getCode().equals("1100")) {
//                        continue;
//                    }
                    Logger.getLogger(DataExtractionService.class.getName()).log(Level.INFO, "Extracting period: " + period.getId() + " RU: " + reportingUnit.getCode());
                    for (Contract contract : reportingUnit.getContracts()) {

                        for (PerformanceObligation pob : contract.getPerformanceObligations()) {
                            if (!pob.metricSetExistsForPeriod(period)) {
                                continue;
                            }
                            //Logger.getLogger(WebSession.class.getName()).log(Level.FINER, "Adding to tree POB ID: " + pob.getId());
                            pst.setString(1, period.getId());
                            pst.setString(2, reportingUnit.getCode());
                            pst.setString(3, String.valueOf(contract.getId()));
                            pst.setString(4, String.valueOf(pob.getId()));
                            pst.setString(5, pob.getName());
                            if (pob.getRevenueMethod() != null) {
                                pst.setString(6, pob.getRevenueMethod().getShortName());
                            } else {
                                pst.setString(6, null);
                            }
                            pst.setBigDecimal(7, zeroIfNull(calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(8, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(9, zeroIfNull(calculationService.getCurrencyMetric("ESTIMATED_COST_AT_COMPLETION_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(10, zeroIfNull(calculationService.getCurrencyMetric("LOCAL_COSTS_CTD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(11, zeroIfNull(calculationService.getCurrencyMetric("THIRD_PARTY_COSTS_CTD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(12, zeroIfNull(calculationService.getCurrencyMetric("INTERCOMPANY_COSTS_CTD_LC", pob, period).getLcValue()));
                            pst.setDate(13, sqlDateConverter(calculationService.getDateMetric("DELIVERY_DATE", pob, period).getValue()));
                            pst.setBigDecimal(14, zeroIfNull(calculationService.getCurrencyMetric("PARTIAL_SHIPMENT_COSTS_LC", pob, period).getLcValue()));

                            pst.setBigDecimal(15, zeroIfNull(calculationService.getCurrencyMetric("ESTIMATED_GROSS_PROFIT_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(16, zeroIfNull(calculationService.getDecimalMetric("ESTIMATED_GROSS_MARGIN", pob, period).getValue()));
                            pst.setBigDecimal(17, zeroIfNull(calculationService.getDecimalMetric("PERCENT_COMPLETE", pob, period).getValue()));
                            pst.setBigDecimal(18, zeroIfNull(calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_CTD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(19, zeroIfNull(calculationService.getCurrencyMetric("COST_OF_GOODS_SOLD_PERIOD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(20, zeroIfNull(calculationService.getCurrencyMetric("COST_GOODS_SOLD_BACKLOG_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(21, zeroIfNull(calculationService.getCurrencyMetric("CHANGE_IN_EAC_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(22, zeroIfNull(calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_CTD_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(23, zeroIfNull(calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_CTD_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(24, zeroIfNull(calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(25, zeroIfNull(calculationService.getCurrencyMetric("REVENUE_TO_RECOGNIZE_PERIOD_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(26, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(27, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_CTD_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(28, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(29, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_TO_RECOGNIZE_PERIOD_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(30, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_BACKLOG_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(31, zeroIfNull(calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_BACKLOG_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(32, zeroIfNull(calculationService.getCurrencyMetric("NET_PERIOD_SALES_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(33, zeroIfNull(calculationService.getCurrencyMetric("NET_PERIOD_SALES_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(34, zeroIfNull(calculationService.getCurrencyMetric("TRANSACTION_PRICE_BACKLOG_CC", pob, period).getCcValue()));
                            pst.setBigDecimal(35, zeroIfNull(calculationService.getCurrencyMetric("TRANSACTION_PRICE_BACKLOG_CC", pob, period).getLcValue()));
                            pst.setBigDecimal(36, zeroIfNull(calculationService.getCurrencyMetric("TRANSACTION_PRICE_NET_LD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(37, zeroIfNull(calculationService.getCurrencyMetric("REMAINING_ESTIMATE_COMPLETE_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(38, zeroIfNull(calculationService.getCurrencyMetric("PROJECTED_GAIN_LOSS_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(39, zeroIfNull(calculationService.getCurrencyMetric("PROJECTED_GAIN_LOSS_BACKLOG_LC", pob, period).getLcValue()));
                            pst.setString(40, String.valueOf(calculationService.getStringMetric("SALES_DESTINATION", pob, period).getValue()));
                            pst.setString(41, String.valueOf(calculationService.getStringMetric("OEAM_DISAGG", pob, period).getValue()));
                            pst.setDate(42, sqlDateConverter(calculationService.getDateMetric("SL_START_DATE", pob, period).getValue()));
                            pst.setDate(43, sqlDateConverter(calculationService.getDateMetric("SL_END_DATE", pob, period).getValue()));
                            pst.setBigDecimal(44, zeroIfNull(calculationService.getCurrencyMetric("COSTS_INCURRED_CTD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(45, zeroIfNull(calculationService.getCurrencyMetric("GROSS_PROFIT_CTD_LC", pob, period).getLcValue()));
                            pst.setBigDecimal(46, zeroIfNull(calculationService.getCurrencyMetric("GROSS_PROFIT_PERIOD_LC", pob, period).getCcValue()));
                            pst.setBigDecimal(47, zeroIfNull(calculationService.getDecimalMetric("GROSS_MARGIN_CTD", pob, period).getValue()));
                            pst.setBigDecimal(48, zeroIfNull(calculationService.getDecimalMetric("GROSS_MARGIN_PERIOD", pob, period).getValue()));
                            pst.setBigDecimal(49, zeroIfNull(calculationService.getCurrencyMetric("COST_TO_COMPLETE_LC", pob, period).getLcValue()));

                            pst.executeUpdate();
                        }

                        contractST.setString(1, period.getId());
                        contractST.setString(2, reportingUnit.getCode());
                        contractST.setString(3, String.valueOf(contract.getId()));
                        contractST.setBigDecimal(4, zeroIfNull(calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(5, zeroIfNull(calculationService.getCurrencyMetric("COST_TO_COMPLETE_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(6, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_LIABILITY_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(7, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_ASSET_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(8, zeroIfNull(calculationService.getDecimalMetric("PERCENT_COMPLETE", contract, period).getValue()));
                        contractST.setBigDecimal(9, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_REVENUE_TO_RECOGNIZE_CTD_CC", contract, period).getCcValue()));
                        contractST.setBigDecimal(10, zeroIfNull(calculationService.getCurrencyMetric("TOTAL_COST_GOODS_SOLD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(11, zeroIfNull(calculationService.getCurrencyMetric("LOSS_RESERVE_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(12, zeroIfNull(calculationService.getCurrencyMetric("LOSS_RESERVE_PERIOD_ADJ_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(13, zeroIfNull(calculationService.getCurrencyMetric("GROSS_PROFIT_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setDate(14, sqlDateConverter(calculationService.getDateMetric("BOOKING_DATE", contract, period).getValue()));
                        contractST.setBigDecimal(15, zeroIfNull(calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(16, zeroIfNull(calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_TO_RECOGNIZE_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(17, zeroIfNull(calculationService.getCurrencyMetric("PROJECT_GAIN_LOSS_INCL_TPC_BACKLOG_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(18, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_BILLINGS_CTD_CC", contract, period).getCcValue()));
                        contractST.setBigDecimal(19, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_BILLINGS_CTD_CC", contract, period).getLcValue()));
                        contractST.setBigDecimal(20, zeroIfNull(calculationService.getCurrencyMetric("TPC_TO_ACCEL_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(21, zeroIfNull(calculationService.getCurrencyMetric("TPC_TO_ACCEL_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(22, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_BILLINGS_PERIOD_CC", contract, period).getCcValue()));
                        contractST.setBigDecimal(23, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_BILLINGS_PERIOD_CC", contract, period).getLcValue()));
                        contractST.setBigDecimal(24, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_POSITION_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(25, zeroIfNull(calculationService.getCurrencyMetric("FX_GAIN_LOSS_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(26, zeroIfNull(calculationService.getCurrencyMetric("OPERATING_INCOME_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(27, zeroIfNull(calculationService.getCurrencyMetric("NET_INCOME_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(28, zeroIfNull(calculationService.getCurrencyMetric("RETAINED_EARNINGS_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(29, zeroIfNull(calculationService.getCurrencyMetric("GROSS_PROFIT_CTD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(30, zeroIfNull(calculationService.getDecimalMetric("ESTIMATED_GROSS_MARGIN", contract, period).getValue()));
                        contractST.setBigDecimal(31, zeroIfNull(calculationService.getDecimalMetric("GROSS_MARGIN_CTD", contract, period).getValue()));
                        contractST.setBigDecimal(32, zeroIfNull(calculationService.getDecimalMetric("GROSS_MARGIN_PERIOD", contract, period).getValue()));
                        contractST.setBigDecimal(33, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_LIABILITY_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(34, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_ASSET_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(35, zeroIfNull(calculationService.getCurrencyMetric("TPC_TOTAL_EXPENSE_PERIOD_LC", contract, period).getLcValue()));
                        contractST.setBigDecimal(36, zeroIfNull(calculationService.getCurrencyMetric("CONTRACT_CLOSURE_DATE", contract, period).getLcValue()));
                        contractST.executeUpdate();
                    }
                }
            } while ((period = financialPeriodService.calculateNextPeriodUntilCurrent(period)) != null);

            Logger.getLogger(DataExtractionService.class.getName()).log(Level.INFO, "Extracting billing...");
            EventType billingEventType = eventService.getEventTypeByCode("BILLING_EVENT_CC");
            for (ReportingUnit reportingUnit : adminService.findAllReportingUnits()) {
                Logger.getLogger(DataExtractionService.class.getName()).log(Level.INFO, "Extracting billings RU: " + reportingUnit.getCode());
                for (Contract contract : reportingUnit.getContracts()) {
                    for (Event event : contract.getAllEventsByEventType(billingEventType)) {
                        CurrencyEvent billingEvent = (CurrencyEvent) event;
                        billingST.setString(1, reportingUnit.getCode());
                        billingST.setString(2, String.valueOf(contract.getId()));
                        billingST.setString(3, billingEvent.getNumber());
                        billingST.setDate(4, sqlDateConverter(billingEvent.getEventDate()));
                        billingST.setDate(5, sqlDateConverter(billingEvent.getEventDate()));
                        billingST.setBigDecimal(6, zeroIfNull(billingEvent.getLcValue()));
                        billingST.setBigDecimal(7, zeroIfNull(billingEvent.getCcValue()));
                        billingST.setString(8, billingEvent.getDescription());
                        billingST.executeUpdate();
                    }
                }
            }
            connection.close();

        } catch (SQLException sqlex) {
            Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "Error exporting Access DB: ", sqlex);
        }

        Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "Extraction complete.");
    }

    //@Asynchronous
    public void generateOutputDB(String fileName) throws Exception {
        generateContractPobStructural(fileName);
        generatePobOutput(fileName);
    }

    public void generateContractPobStructural(String inputFile) throws Exception {

        // General Note: NOV-17 is our start point for historical data.
        Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Processing Contract and POB Data: " + inputFile);
        Connection connection = null;

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException cnfex) {
            Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Problem in loading or registering MS Access JDBC driver");
        }

        try {
            String dbURL = "jdbc:ucanaccess://" + inputFile;
            Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "dbURL : " + dbURL);
            connection = DriverManager.getConnection(dbURL);

            PreparedStatement contractStructural = connection.prepareStatement("INSERT INTO Contracts (CONTRACT_ID, IS_ACTIVE, CONTRACT_CURRENCY, CONTRACT_NAME, SALES_ORDER_NUM, REPORTING_UNIT_ID) "
                    + "VALUES (?, ?, ?, ?, ?, ?)");

            PreparedStatement pobStructural = connection.prepareStatement("INSERT INTO POBs (POB_ID, IS_ACTIVE, POB_NAME, DESCRIPTION, REVENUE_METHOD, CONTRACT_ID) "
                    + "VALUES (?, ?, ?, ?, ?, ?)");

            //String[] reportingUnits = {"1100", "5050", "7866", "8405", "1205", "8225"};
            //String[] reportingUnits = {"1100"};
            for (ReportingUnit reportingUnit : adminService.findAllReportingUnits()) {
                for (Contract contract : reportingUnit.getContracts()) {
                    contractStructural.setLong(1, contract.getId());
                    contractStructural.setLong(2, 1);
                    contractStructural.setString(3, contract.getContractCurrency().getCurrencyCode());
                    contractStructural.setString(4, contract.getName());
                    contractStructural.setString(5, contract.getSalesOrderNumber());
                    contractStructural.setString(6, contract.getReportingUnit().getCode());
                    contractStructural.executeUpdate();

                    for (PerformanceObligation pob : contract.getPerformanceObligations()) {
                        pobStructural.setLong(1, pob.getId());
                        pobStructural.setLong(2, 1);
                        pobStructural.setString(3, pob.getName());
                        pobStructural.setString(4, pob.getDescription());
                        pobStructural.setString(5, pob.getRevenueMethod().getShortName());
                        pobStructural.setLong(6, pob.getContract().getId());
                        pobStructural.executeUpdate();
                    }
                }
            }

            connection.close();

        } catch (SQLException sqlex) {
            Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "Error exporting Access DB: ", sqlex);
        }

        Logger.getLogger(ReportsService.class.getName()).log(Level.INFO, "Complete.");
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
