/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.DataImportFile;
import com.flowserve.system606.model.EventType;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.RevenueMethod;
import com.flowserve.system606.model.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.apache.commons.lang.StringUtils;

/**
 * @author kgraves
 *
 * This EJB uses bean managed transactions due to high load batch processing
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class BatchProcessingService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    @Inject
    private CalculationService calculationService;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private PerformanceObligationService performanceObligationService;
    @Inject
    private ContractService contractService;
    @Inject
    private AdminService adminService;
    @Inject
    private EventService eventService;
    @Resource
    private UserTransaction ut;
    private Map<String, RevenueMethod> methodMap = new HashMap<String, RevenueMethod>();
    private static Logger logger = Logger.getLogger(BatchProcessingService.class.getName());
    private List<String> reportingUnitsToProcess = new ArrayList<String>();

    public final String STRUCTURAL_UPDATE_FILE = "STRUCTURAL_UPDATE_FILE";
    public final String CONTRACT_BOOKINGDATE_FILE = "CONTRACT_BOOKINGDATE_FILE";

    public BatchProcessingService() {
    }

    @PostConstruct
    public void init() {
    }

    //@Asynchronous
    public void processUploadedCalculationData(String msAccDB, String fileName) throws Exception {
        logger.log(Level.INFO, "Processing POB input data: " + msAccDB);

        Connection connection = null;

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String dbURL = "jdbc:ucanaccess://" + msAccDB;
            connection = DriverManager.getConnection(dbURL);
            connection.setAutoCommit(false);
            logger.info("Processing POB input data..");
            processPOCIData(connection, fileName);
            logger.info("Processing contract billing data..");
            processBillingInfoFromPOCI(connection, fileName);
            logger.info("Processing contract third party commission data..");
            processPOCIThirdPartyCommissions(connection, fileName);
            adminService.jpaEvictAllCache();
        } catch (Exception e) {
            logger.log(Level.INFO, "Error processing POB input data: ", e);
            throw (e);
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sqlex) {
                logger.log(Level.INFO, "Error closing Access DB connection.", sqlex);
            }
        }

        logger.log(Level.INFO, "POB input data import completed.");
    }

    void processPOCIData(Connection connection, String fileName) throws SQLException, Exception {

        ResultSet resultSet = null;
        String exPeriod = null;
        DataImportFile dataImport = new DataImportFile();
        List<String> importMessages = new ArrayList<String>();
        Set<ReportingUnit> rusToSave = new HashSet<ReportingUnit>();

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(
                "SELECT Period,`POb NAME (TAB NAME)`,`Transaction Price/Changes to Trans Price (excl LDs)`,`Estimated at Completion (EAC)/ Changes to EAC (excl TPCs)`,"
                + "`Cumulative Costs Incurred`,`Liquidated Damages (LDs)/Changes to LDs`, `Contract Sales Destination`, `POB Identifiers (DRM)`, `SL POb Revenue Start Date`, "
                + "`SL  POb Revenue End Date`, `Partial Shipments`, `Delivery Date` FROM `tbl_POCI_POb Inputs` ORDER BY Period");

        logger.log(Level.INFO, "Period\tPOCC File Name\tC Page Number\tReporting Unit Number");
        logger.log(Level.INFO, "==\t================\t===\t=======");

        int count = 0;
        long timeInterval = System.currentTimeMillis();

        ut.begin();

        try {
            while (resultSet.next()) {
                FinancialPeriod period = financialPeriodService.findById(resultSet.getString(1));

                if (period == null) {
                    String message = "Attempting to load data for a non-existent period.  Aborting job.  Please create the proper period.";
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }

                if (!period.isOpen()) {
                    String message = "Attempting to load data for a closed period: " + period.getId() + " aborting job.  Please check the source data or open the required peirod.";
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }
                if (period != null) {
                    String id = resultSet.getString(2);
                    if (id != null) {
                        String[] sp = id.split("-");
                        String lastId = sp[sp.length - 1].trim();
                        try {
                            BigDecimal tp = resultSet.getBigDecimal(3);
                            BigDecimal eac = resultSet.getBigDecimal(4);
                            BigDecimal costs = resultSet.getBigDecimal(5);
                            BigDecimal ld = resultSet.getBigDecimal(6);
                            String salesDestination = resultSet.getString(7);
                            String oeam = resultSet.getString(8);
                            Date slStart = resultSet.getDate(9);
                            Date slEnd = resultSet.getDate(10);
                            BigDecimal partialShipments = resultSet.getBigDecimal(11);
                            Date deliveryDate = resultSet.getDate(12);
                            // checking valid integer using parseInt() method
                            Integer.parseInt(lastId);
                            PerformanceObligation pob = performanceObligationService.findPerformanceObligationById(new Long(lastId));

                            if (pob != null) {
                                ReportingUnit ru = pob.getContract().getReportingUnit();
                                if (ru == null) {
                                    importMessages.add("RU is null for POB ID: " + lastId);
                                    logger.log(Level.INFO, "RU is null for POB ID: " + lastId);
                                    continue;
                                }
                                if (ru.getLocalCurrency() == null) {
                                    String msg = "No local currency found for RU: " + ru.getCode();
                                    importMessages.add(msg);
                                    logger.log(Level.INFO, "RU local currency is null for RU: " + ru.getCode());
                                    continue;
                                }
                                //logger.log(Level.INFO, "Populating POB metrics: " + pob.getId());

                                calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", pob, period).setValue(tp);
                                calculationService.getCurrencyMetric("ESTIMATED_COST_AT_COMPLETION_LC", pob, period).setValue(eac);
                                calculationService.getCurrencyMetric("LOCAL_COSTS_CTD_LC", pob, period).setValue(costs);
                                // KJG 09/29/18 - These next two lines were commented out, re-enabling.
                                calculationService.getCurrencyMetric("THIRD_PARTY_COSTS_CTD_LC", pob, period).setValue(BigDecimal.ZERO);
                                calculationService.getCurrencyMetric("INTERCOMPANY_COSTS_CTD_LC", pob, period).setValue(BigDecimal.ZERO);
                                // KJG 09/29/18 - Safe to default LD to zero if missing.
                                if (ld == null) {
                                    calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", pob, period).setValue(BigDecimal.ZERO);
                                } else {
                                    calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", pob, period).setValue(ld);
                                }
                                calculationService.getStringMetric("SALES_DESTINATION", pob, period).setValue(salesDestination);
                                calculationService.getStringMetric("OEAM_DISAGG", pob, period).setValue(oeam);
                                if (slStart != null) {
                                    calculationService.getDateMetric("SL_START_DATE", pob, period).setValue(slStart.toLocalDate());
                                }
                                if (slEnd != null) {
                                    calculationService.getDateMetric("SL_END_DATE", pob, period).setValue(slEnd.toLocalDate());
                                }
                                // KJG 09/29/18 - Safe to default to zero if missing.
                                if (partialShipments == null) {
                                    calculationService.getCurrencyMetric("PARTIAL_SHIPMENT_COSTS_LC", pob, period).setValue(BigDecimal.ZERO);
                                } else {
                                    calculationService.getCurrencyMetric("PARTIAL_SHIPMENT_COSTS_LC", pob, period).setValue(partialShipments);
                                }
                                if (deliveryDate != null) {
                                    calculationService.getDateMetric("DELIVERY_DATE", pob, period).setValue(deliveryDate.toLocalDate());
                                }

                                if (period.equals(financialPeriodService.getCurrentFinancialPeriod())) {
                                    if (tp == null || eac == null) {
                                        pob.setValid(false);
                                    } else {
                                        pob.setValid(true);
                                    }
                                }

                                rusToSave.add(pob.getContract().getReportingUnit());
                            } else {
                                importMessages.add("POB not found : " + id);
                                //logger.log(Level.FINER, "These POBs not found : " + id);
                            }
                        } catch (NumberFormatException e) {
                            importMessages.add("This is not a Valid POB ID : " + lastId);
                            //logger.log(Level.INFO, "Error " + e);
                        }
                    }

                } else {
                    ut.rollback();
                    importMessages.add("Financial Period not available : " + exPeriod);
                    throw new IllegalStateException("Financial Period not available :" + exPeriod);
                }

                if ((count % 1000) == 0) {
                    logger.log(Level.INFO, "Processed " + count + " POB Input Sets.");
                    logger.log(Level.INFO, "Committing data to database...");
                    ut.commit();
                    em.clear();
                    ut.begin();
                }
                count++;
            }
        } catch (Exception exception) {
            Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, "Error POCI Data Import: ", exception);
            ut.rollback();
            throw (exception);
        }

        dataImport.setFilename(fileName);
        dataImport.setUploadDate(LocalDateTime.now());
        dataImport.setCompany(adminService.findCompanyById("FLS"));
        dataImport.setDataImportMessages(importMessages);
        dataImport.setType("POCI DATA");
        adminService.persist(dataImport);
        logger.log(Level.INFO, "Committing data to database...");
        ut.commit();
        em.clear();

    }

    void processPOCIThirdPartyCommissions(Connection connection, String fileName) throws SQLException, Exception {

        ResultSet resultSet = null;
        DataImportFile dataImport = null;
        List<String> importMessages = new ArrayList<String>();
        Set<ReportingUnit> rusToSave = new HashSet<ReportingUnit>();

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT Period, `C Page Number`,`LOCL CCY - TPC` FROM `tbl_POCI-TPC` ORDER BY Period");
        int count = 0;
        long timeInterval = System.currentTimeMillis();
        dataImport = new DataImportFile();
        ut.begin();

        try {
            while (resultSet.next()) {
                FinancialPeriod period = financialPeriodService.findById(resultSet.getString(1));

                if (period == null) {
                    String message = "Attempting to load data for a non-existent period.  Aborting job.  Please create the proper period";
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }

                if (!period.isOpen()) {
                    String message = "Attempting to load data for a closed period: " + period.getId() + " aborting job.  Please check the source data or open the required peirod.";
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }

                Long contractId = resultSet.getLong(2);
                if (contractId != null) {
                    try {
                        BigDecimal thirdParyComm = resultSet.getBigDecimal(3);
                        Contract contract = contractService.findContractById((contractId));

                        if (contract != null) {
                            ReportingUnit reportingUnit = contract.getReportingUnit();
                            if (reportingUnit == null) {
                                String msg = "RU is null for contract id: " + contractId;
                                importMessages.add(msg);
                                logger.log(Level.SEVERE, msg);
                                continue;
                            }
                            if (reportingUnit.getLocalCurrency() == null) {
                                String msg = "No local currency found for RU: " + reportingUnit.getCode();
                                importMessages.add(msg);
                                logger.log(Level.INFO, msg);
                                continue;
                            }
//                            if (thirdParyComm.compareTo(BigDecimal.ZERO) > 0) {
//                                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, "TPC: " + thirdParyComm.toPlainString());
//                            }
                            // KJG 09/29/18 - Ok to set to zeor if missing.
                            if (thirdParyComm == null) {
                                calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", contract, period).setLcValue(BigDecimal.ZERO);
                            } else {
                                calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", contract, period).setLcValue(thirdParyComm);
                            }
                            rusToSave.add(contract.getReportingUnit());

                        } else {
                            importMessages.add("Contract not found when processing TPC.  Contract id: " + contractId);
                        }
                        if ((count % 1000) == 0) {
                            logger.log(Level.INFO, "Processed " + count + " POCI TPC records");
                            timeInterval = System.currentTimeMillis();
                            logger.log(Level.INFO, "Committing data to database...");
                            ut.commit();
                            em.clear();
                            ut.begin();
                        }
                        count++;

                    } catch (NumberFormatException e) {
                        importMessages.add("Invalid Contract ID : " + contractId);
                        Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, "Invalid Contract ID : " + contractId);
                    }
                }

            }
        } catch (Exception exception) {
            Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, "Error TPC Import: ", exception);
            ut.rollback();
            throw (exception);
        }

        dataImport.setFilename(fileName + " - tbl_POCI (TPC Data run)");
        dataImport.setUploadDate(LocalDateTime.now());
        dataImport.setCompany(adminService.findCompanyById("FLS"));
        dataImport.setDataImportMessages(importMessages);
        dataImport.setType("POCI TPC DATA");
        adminService.persist(dataImport);
        logger.log(Level.INFO, "Committing data to database...");
        ut.commit();
        em.clear();
    }

    void processBillingInfoFromPOCI(Connection connection, String fileName) throws SQLException, Exception {

        ResultSet resultSet = null;
        String exPeriod = null;
        DataImportFile dataImport = null;
        int count = 0;
        List<String> importMessages = new ArrayList<String>();

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT Period, `C Page Number`, `Contract Billings`, `Local Billings` FROM `tbl_POCI-Billings` ORDER BY Period");
        dataImport = new DataImportFile();

        ut.begin();
        EventType billingEventType = eventService.getEventTypeByCode("BILLING_EVENT_CC");

        try {
            while (resultSet.next()) {
                FinancialPeriod period = financialPeriodService.findById(resultSet.getString(1));

                if (period == null) {
                    String message = "Attempting to load data for a non-existent period.  Aborting job.  Please create the proper period.";
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }

                if (!period.isOpen()) {
                    String message = "Attempting to load data for a closed period: " + period.getId() + " aborting job.  Please check the source data or open the required peirod.";
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }

                if (period != null) {
                    int contractId = resultSet.getInt(2);
                    if (contractId != 0) {
                        BigDecimal cc = resultSet.getBigDecimal(3);
                        BigDecimal lc = resultSet.getBigDecimal(4);
                        Contract contract = contractService.findContractById(new Long(contractId));
                        if (cc == null && lc == null) {
                            continue;
                        }
                        if (BigDecimal.ZERO.equals(cc) && BigDecimal.ZERO.equals(lc)) {
                            continue;
                        }
                        if (cc != null && cc.floatValue() == 0.0f && lc != null && lc.floatValue() == 0.0f) {
                            continue;
                        }
                        if (contract != null) {
                            CurrencyEvent billingEvent = new CurrencyEvent();
                            billingEvent.setCcValue(cc);
                            billingEvent.setLcValue(lc);
                            billingEvent.setName("Migrated Billing");
                            billingEvent.setNumber("Migrated Billing");
                            billingEvent.setEventDate(LocalDate.of(period.getPeriodYear(), period.getPeriodMonth(), 1));
                            billingEvent.setCreationDate(LocalDateTime.now());
                            billingEvent.setEventType(billingEventType);
                            billingEvent.setFinancialPeriod(period);
                            billingEvent.setContract(contract);
                            //billingEvent = (CurrencyEvent) eventService.update(billingEvent);
                            eventService.persist(billingEvent);
                            //Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, "Adding billing event: " + billingEvent.getId());
                            calculationService.addEvent(contract, period, billingEvent);
                            contract = contractService.update(contract);
                        } else {
                            importMessages.add("Contract not found when processing billing: " + contractId);
                            logger.log(Level.INFO, "Contract not found : " + contractId);
                        }
                    }
                    if ((count % 1000) == 0) {
                        logger.log(Level.INFO, "Processed " + count + " billing records");
                        logger.log(Level.INFO, "Committing data to database...");
                        //em.flush();
                        ut.commit();
                        em.clear();
                        ut.begin();
                    }
                    count++;
                } else {
                    ut.rollback();
                    importMessages.add("Financial Period not available : " + exPeriod);
                    throw new IllegalStateException("Financial Period not available :" + exPeriod);
                }
            }
        } catch (Exception exception) {
            Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, "Error billing: ", exception);
            ut.rollback();
            throw (exception);
        }

        dataImport.setFilename(fileName + " - tbl_POCI-Billings");
        dataImport.setUploadDate(LocalDateTime.now());
        dataImport.setCompany(adminService.findCompanyById("FLS"));
        dataImport.setDataImportMessages(importMessages);
        dataImport.setType("POCI DATA");
        adminService.persist(dataImport);
        logger.log(Level.INFO, "Cimitting billing data to database...");
        ut.commit();
        em.clear();
        logger.log(Level.INFO, "Billing data import completed.");
    }

    private boolean isGreaterThanZero(BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) == 1) {
            return true;
        }

        return false;
    }

    public void processInitialSystemLoadStructuralData(String msAccDB, String fileName) throws Exception {
        Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Processing contract and POB structural data: " + msAccDB);

        Connection connection = null;

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        try {
            String dbURL = "jdbc:ucanaccess://" + msAccDB;
            connection = DriverManager.getConnection(dbURL);
            //connection.setTransactionIsolation(Connection.TRANSACTION_NONE);
            connection.setAutoCommit(false);
            User admin = adminService.findUserByFlsId("rcs_admin");
            processInitialStructuralContractData(connection, fileName, admin);
            processInitialStructuralPobData(connection, fileName, admin);
            adminService.jpaEvictAllCache();
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }

        logger.log(Level.INFO, "Contract and POB import completed.");
    }

    void processInitialStructuralPobData(Connection connection, String fileName, User user) throws SQLException, Exception {
        ResultSet resultSet = null;

        methodMap.put("POC/Cost-to-Cost", RevenueMethod.PERC_OF_COMP);
        methodMap.put("Not Over-Time", RevenueMethod.POINT_IN_TIME);
        methodMap.put("Straight-line", RevenueMethod.STRAIGHT_LINE);
        methodMap.put("Right to Invoice", RevenueMethod.RIGHT_TO_INVOICE);

        long contractId = -1;
        long pobId = -1;
        String revRecMethod = null;

        DataImportFile dataImport = null;
        List<String> importMessages = new ArrayList<String>();
        dataImport = new DataImportFile();
        int count = 0;

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT ID, Name, Stage, Folders, `Name of POb`, `If OT identify the revenue recognition method`, `C-Page ID` FROM tbl_POC_PObs");

        try {
            ut.begin();

            while (resultSet.next()) {

                PerformanceObligation pob = new PerformanceObligation();
                pobId = resultSet.getInt(1);

                if (performanceObligationService.findById(pobId) != null) {
                    continue;
                }
                revRecMethod = resultSet.getString(6);
                contractId = resultSet.getInt(7);
                Contract contract = contractService.findContractById(contractId);

                if (contract == null) {
                    importMessages.add("POB refers to non-existent contract.  Invalid.  POB ID: " + pobId + " contract Id: " + contractId);
                    //Logger.getLogger(AppInitializeService.class.getName()).log(Level.WARNING, "POB refers to non-existent contract.  Invalid.  POB ID: " + pobId + " contract Id: " + contractId);
                    continue;
                } else {
                    pob.setContract(contract);
                }

                pob.setId(pobId);
                pob.setCreationDate(LocalDateTime.now());
                if (methodMap.get(revRecMethod) == null) {
                    importMessages.add("POB revrec method not in our list: " + revRecMethod);
                    logger.log(Level.INFO, "POB revrec method not in our list: " + revRecMethod + " pobId: " + pobId);
                }
                RevenueMethod revMethod = methodMap.get(revRecMethod);
                pob.setRevenueMethod(revMethod);
                pob.setName((revMethod != null ? revMethod.getShortName() : "") + "-" + pobId);
                pob.setDescription(resultSet.getString(2));
                pob.setCreatedBy(user);
                pob.setActive(true);
                pob.setCreationDate(LocalDateTime.now());
                pob.setLastUpdateDate(LocalDateTime.now());
                //pobService.update(pob);  // update or persist?
                performanceObligationService.persist(pob);
                contract.getPerformanceObligations().add(pob);
                contractService.update(contract);

                count++;
                if ((count % 500) == 0) {
                    Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "POB import count: " + count);
                }
            }
            dataImport.setFilename(fileName + " - tbl_POb");
            dataImport.setUploadDate(LocalDateTime.now());
            dataImport.setCompany(adminService.findCompanyById("FLS"));
            dataImport.setDataImportMessages(importMessages);
            dataImport.setType("Contract and Pobs");
            adminService.persist(dataImport);
            logger.log(Level.INFO, "Flushing and clearing EntityManager");
            resultSet.close();
            em.flush();
            em.clear();
            ut.commit();
        } catch (Exception e) {
            Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, "Error: ", e);
            ut.rollback();
        }

        adminService.jpaEvictAllCache();

        logger.log(Level.INFO, "POB imoprt complete.");
    }

    void processInitialStructuralContractData(Connection connection, String fileName, User user) throws SQLException, Exception {
        ResultSet resultSet = null;

        String ru = null;
        Long contractId;
        String contractCurrencyCode = null;
        DataImportFile dataImport = null;
        List<String> importMessages = new ArrayList<String>();
        dataImport = new DataImportFile();
        int count = 0;
        String line = null;

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT ID, Name, `BPC Reporting Unit`, `Sales Order #`, `Contract Currency`, `Customer Name`, `Contract Description`, `Contract Date` FROM tbl_POC_Contracts");
        ut.begin();
        while (resultSet.next()) {

            contractId = resultSet.getLong(1);
            if (contractService.findContractById(contractId) != null) {
                continue;  // we've already processed this contract.  dont' process the repeated lines.
            }

            Contract contract = new Contract();
            contract.setCreatedBy(user);
            contract.setCreationDate(LocalDateTime.now());
            contract.setLastUpdateDate(LocalDateTime.now());
            contract.setActive(true);
            contract.setId(contractId);
            contract.setSalesOrderNumber(resultSet.getString(4));
            contractCurrencyCode = resultSet.getString(5);
            if (resultSet.getDate(8) != null) {
                contract.setBookingDate(new java.sql.Date(resultSet.getDate(8).getTime()).toLocalDate());
            }
            if (resultSet.getString(2).startsWith("WAIVER")) {
                importMessages.add("Skipping WAIVER contract.  ID: " + contractId);
                continue;
            }
            contract.setName("" + resultSet.getLong(1) + " " + (resultSet.getString(6) == null ? "" : resultSet.getString(6)));
            contract.setDescription(resultSet.getString(2));
            String ruStr = StringUtils.substringBefore(resultSet.getString(3).trim(), "-");
            ru = ruStr.replace("RU", "").trim();
            if (contractCurrencyCode != null && !contractCurrencyCode.isEmpty()) {
                contract.setContractCurrency(Currency.getInstance(contractCurrencyCode));
            } else {
                importMessages.add("Contract currency not found for contract: " + contractId + " code: " + contractCurrencyCode);
                logger.log(Level.SEVERE, "Contract currency not found for contract: " + contractId + " code: " + contractCurrencyCode);
                continue;
            }

            ReportingUnit reportingUnit = adminService.findReportingUnitByCode(ru);

            if (reportingUnit == null) {
                importMessages.add("Countract refers to a non-existent RU : " + resultSet.getString(3));
                //throw new IllegalStateException("Countract refers to a non-existent RU.  Invalid.");
            }

            if (reportingUnit != null) {
                contract.setReportingUnit(reportingUnit);
            }
            //contract = contractService.update(contract);   // this gives us the JPA managed object.

            contractService.persist(contract);   // persist or update?
            if (reportingUnit != null) {
                reportingUnit.getContracts().add(contract);
                adminService.update(reportingUnit);
            }

            count++;

            if ((count % 500) == 0) {
                Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Contract import count: " + count);
            }
        }
        dataImport.setFilename(fileName + " - tbl_Contracts");
        dataImport.setUploadDate(LocalDateTime.now());
        dataImport.setCompany(adminService.findCompanyById("FLS"));
        dataImport.setDataImportMessages(importMessages);
        dataImport.setType("Contract and Pobs");
        adminService.persist(dataImport);
        ut.commit();
        resultSet.close();

        adminService.jpaEvictAllCache();
        logger.log(Level.INFO, "Contract import complete.");
    }

    public void processStructuralUpdateFile(String msAccDB, String fileName) throws Exception {
        Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Processing structural data update DB: " + msAccDB);

        Connection connection = null;

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        try {
            String dbURL = "jdbc:ucanaccess://" + msAccDB;
            connection = DriverManager.getConnection(dbURL);
            //connection.setTransactionIsolation(Connection.TRANSACTION_NONE);
            connection.setAutoCommit(false);

            ut.begin();
            DataImportFile dataImport = new DataImportFile();
            dataImport.setDataImportMessages(new ArrayList<String>());

            processContractAdditions(connection, dataImport);
            processContractChanges(connection, dataImport);
            processPobAdditions(connection, dataImport);
            processPobChanges(connection, dataImport);
            processPobDeletions(connection, dataImport);
            processContractDeletions(connection, dataImport);

            dataImport.setFilename(fileName);
            dataImport.setUploadDate(LocalDateTime.now());
            dataImport.setCompany(adminService.findCompanyById("FLS"));
            dataImport.setType(STRUCTURAL_UPDATE_FILE);
            adminService.persist(dataImport);

            ut.commit();
            adminService.jpaEvictAllCache();
        } catch (Exception e) {
            ut.rollback();
            throw (e);
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }

        logger.log(Level.INFO, "Structural data update completed.");
    }

    void processContractAdditions(Connection connection, DataImportFile dataImport) throws SQLException, Exception {
        ResultSet resultSet = null;
        String ru = null;
        Long contractId;
        String contractCurrencyCode = null;
        Set<ReportingUnit> reportingUnitsToRecalculate = new HashSet<ReportingUnit>();

        int count = 0;

        Statement statement = connection.createStatement();

        resultSet = statement.executeQuery("SELECT ID, Name, `BPC Reporting Unit`, `Sales Order #`, `Contract Currency`, `Customer Name`, `Contract Description`, `Contract Date` FROM tbl_POC_Contracts_Additions");

        FinancialPeriod currentFinancialPeriod = financialPeriodService.getCurrentFinancialPeriod();

        if (currentFinancialPeriod.isClosed()) {
            throw new IllegalStateException("The current fiancial period : " + currentFinancialPeriod.getId() + " must be open for contract additions.");
        }

        while (resultSet.next()) {

//            if (currentFinancialPeriod.isClosed()) {
//                throw new IllegalStateExcepotion("Attempting to add contract while current financial period is close.  This is invalid due to the fact that the contract must be included in this period's ")
//            }
            contractId = resultSet.getLong(1);
            if (contractService.findContractById(contractId) != null) {
                dataImport.getDataImportMessages().add("Contract already exists, skipping contract Id: " + contractId);
                continue;
            }

            Contract contract = new Contract();
            contract.setCreationDate(LocalDateTime.now());
            contract.setLastUpdateDate(LocalDateTime.now());
            contract.setActive(true);
            contract.setId(contractId);
            contract.setSalesOrderNumber(resultSet.getString(4));
            contractCurrencyCode = resultSet.getString(5);
            if (resultSet.getDate(8) != null) {
                contract.setBookingDate(new java.sql.Date(resultSet.getDate(8).getTime()).toLocalDate());
            }
            if (resultSet.getString(2).startsWith("WAIVER")) {
                dataImport.getDataImportMessages().add("Skipping WAIVER contract.  ID: " + contractId);
                continue;
            }

            contract.setName("" + resultSet.getLong(1) + " " + (resultSet.getString(6) == null ? "" : resultSet.getString(6)));
            contract.setDescription(resultSet.getString(2));
            String ruStr = StringUtils.substringBefore(resultSet.getString(3).trim(), "-");
            ru = ruStr.replace("RU", "").trim();

            if (contractCurrencyCode != null && !contractCurrencyCode.isEmpty()) {
                contract.setContractCurrency(Currency.getInstance(contractCurrencyCode));
            } else {
                String errorMsg = "Contract currency not found for contract: " + contractId + " code: " + contractCurrencyCode;
                dataImport.getDataImportMessages().add(errorMsg);
                logger.log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            ReportingUnit reportingUnit = adminService.findReportingUnitByCode(ru);

            if (reportingUnit == null) {
                String errorMsg = "Countract refers to a non-existent RU : " + resultSet.getString(3);
                dataImport.getDataImportMessages().add(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            contract.setReportingUnit(reportingUnit);
            contractService.persist(contract);   // persist or update?
            reportingUnit.getContracts().add(contract);
            adminService.update(reportingUnit);

            reportingUnitsToRecalculate.add(reportingUnit);

            count++;

            if ((count % 1000) == 0) {
                Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Contract add count: " + count);
            }
        }

        resultSet.close();

        for (ReportingUnit reportingUnit : reportingUnitsToRecalculate) {
            calculationService.calculateAndSave(reportingUnit, currentFinancialPeriod);
        }

        logger.log(Level.INFO, "Structural data upload contract additions complete.");
    }

    void processContractChanges(Connection connection, DataImportFile dataImport) throws SQLException, Exception {
        ResultSet resultSet = null;
        Long contractId;
        int count = 0;

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT ID, Name, `BPC Reporting Unit`, `Sales Order #`, `Contract Currency`, `Customer Name`, `Contract Description`, `Contract Date` FROM tbl_POC_Contracts_Changes");

        while (resultSet.next()) {

            if (resultSet.getString(2).startsWith("WAIVER")) {
                dataImport.getDataImportMessages().add("Skipping WAIVER contract.");
                continue;
            }

            contractId = resultSet.getLong(1);
            Contract contract = contractService.findContractById(contractId);
            if (contract == null) {
                String errorMsg = "Contract does not exist: " + contractId + " invalid change record, aborting.";
                dataImport.getDataImportMessages().add(errorMsg);
                logger.log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            contract.setLastUpdateDate(LocalDateTime.now());

            String existingSalesOrderNumber = contract.getSalesOrderNumber();
            String newSalesOrderNumber = resultSet.getString(4);
            if (existingSalesOrderNumber == null || !existingSalesOrderNumber.equals(newSalesOrderNumber)) {
                String message = "Updated sales order number, contract Id: " + contract.getId() + " new number: " + newSalesOrderNumber;
                contract.setSalesOrderNumber(newSalesOrderNumber);
                contract.setLastUpdateDate(LocalDateTime.now());
                dataImport.getDataImportMessages().add(message);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, message);
            }

            String existingDesc = contract.getDescription();
            String newDesc = resultSet.getString(2);
            if (existingDesc == null || !existingDesc.equals(newDesc)) {
                String message = "Updated description, contract Id: " + contract.getId() + " new desc: " + newDesc;
                contract.setDescription(newDesc);
                contract.setLastUpdateDate(LocalDateTime.now());
                dataImport.getDataImportMessages().add(message);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, message);
            }

            LocalDate existingBookingDate = contract.getBookingDate();
            if (resultSet.getDate(8) != null) {
                LocalDate newBookingDate = new java.sql.Date(resultSet.getDate(8).getTime()).toLocalDate();
                if (existingBookingDate == null || !existingBookingDate.equals(newBookingDate)) {
                    String message = "Updated booking date, contract Id: " + contract.getId() + " new booking date: " + newBookingDate;
                    contract.setBookingDate(newBookingDate);
                    contract.setLastUpdateDate(LocalDateTime.now());
                    dataImport.getDataImportMessages().add(message);
                    Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, message);
                }
            }

            contractService.update(contract);

            count++;
            if ((count % 1000) == 0) {
                Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Contract update count: " + count);
            }
        }

        resultSet.close();

        logger.log(Level.INFO, "Structural data upload contract change complete.");
    }

    void processPobAdditions(Connection connection, DataImportFile dataImport) throws SQLException, Exception {
        ResultSet resultSet = null;

        methodMap.put("POC/Cost-to-Cost", RevenueMethod.PERC_OF_COMP);
        methodMap.put("Not Over-Time", RevenueMethod.POINT_IN_TIME);
        methodMap.put("Straight-line", RevenueMethod.STRAIGHT_LINE);
        methodMap.put("Right to Invoice", RevenueMethod.RIGHT_TO_INVOICE);

        long contractId = -1;
        long pobId = -1;
        String revRecMethod = null;
        int count = 0;
        Set<ReportingUnit> reportingUnitsToRecalculate = new HashSet<ReportingUnit>();

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT ID, Name, Stage, Folders, `Name of POb`, `If OT identify the revenue recognition method`, `C-Page ID` FROM tbl_POC_POb_Additions");

        FinancialPeriod currentFinancialPeriod = financialPeriodService.getCurrentFinancialPeriod();
        if (currentFinancialPeriod.isClosed()) {
            throw new IllegalStateException("The current fiancial period : " + currentFinancialPeriod.getId() + " must be open for POb additions.");
        }

        while (resultSet.next()) {

            PerformanceObligation pob = new PerformanceObligation();
            pobId = resultSet.getInt(1);

            if (performanceObligationService.findById(pobId) != null) {
                dataImport.getDataImportMessages().add("POB already exists, skipping POB Id: " + pobId);
                continue;
            }

            revRecMethod = resultSet.getString(6);
            contractId = resultSet.getInt(7);
            Contract contract = contractService.findContractById(contractId);

            if (contract == null) {
                String errorMsg = "POB refers to non-existent contract.  Invalid.  POB ID: " + pobId + " contract Id: " + contractId;
                dataImport.getDataImportMessages().add(errorMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            if (methodMap.get(revRecMethod) == null) {
                dataImport.getDataImportMessages().add("POB revrec method not valid: " + revRecMethod);
                logger.log(Level.INFO, "POB revrec method not valid: " + revRecMethod + " pobId: " + pobId);
                throw new IllegalStateException("POB revrec method not valid: " + revRecMethod);
            }

            pob.setContract(contract);
            pob.setId(pobId);
            pob.setCreationDate(LocalDateTime.now());
            pob.setLastUpdateDate(LocalDateTime.now());
            RevenueMethod revMethod = methodMap.get(revRecMethod);
            pob.setRevenueMethod(revMethod);
            pob.setName((revMethod != null ? revMethod.getShortName() : "") + "-" + pobId);
            pob.setDescription(resultSet.getString(2));
            performanceObligationService.persist(pob);
            contract.getPerformanceObligations().add(pob);
            contractService.update(contract);

            reportingUnitsToRecalculate.add(contract.getReportingUnit());

            count++;
            if ((count % 1000) == 0) {
                Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "POB import count: " + count);
            }
        }

        resultSet.close();

        for (ReportingUnit reportingUnit : reportingUnitsToRecalculate) {
            calculationService.calculateAndSave(reportingUnit, currentFinancialPeriod);
        }

        logger.log(Level.INFO, "Structural update POB complete.");
    }

    void processPobChanges(Connection connection, DataImportFile dataImport) throws SQLException, Exception {

        methodMap.put("POC/Cost-to-Cost", RevenueMethod.PERC_OF_COMP);
        methodMap.put("Not Over-Time", RevenueMethod.POINT_IN_TIME);
        methodMap.put("Straight-line", RevenueMethod.STRAIGHT_LINE);
        methodMap.put("Right to Invoice", RevenueMethod.RIGHT_TO_INVOICE);

        long contractId = -1;
        long pobId = -1;
        int count = 0;
        String revRecMethod = null;
        Set<ReportingUnit> reportingUnitsToRecalculate = new HashSet<ReportingUnit>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT ID, Name, Stage, Folders, `Name of POb`, `If OT identify the revenue recognition method`, `C-Page ID` FROM tbl_POC_POb_Changes");

        FinancialPeriod currentFinancialPeriod = financialPeriodService.getCurrentFinancialPeriod();
        if (currentFinancialPeriod.isClosed()) {
            throw new IllegalStateException("The current fiancial period : " + currentFinancialPeriod.getId() + " must be open for POb changes.");
        }

        while (resultSet.next()) {

            pobId = resultSet.getInt(1);
            PerformanceObligation pob = performanceObligationService.findById(pobId);

            if (pob == null) {
                String errorMsg = "POB does not exist: " + pobId + " invalid change record, aborting.";
                dataImport.getDataImportMessages().add(errorMsg);
                logger.log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            contractId = resultSet.getInt(7);
            Contract newContract = contractService.findContractById(contractId);

            if (newContract == null) {
                String errorMsg = "POB refers to non-existent contract.  Invalid.  POB ID: " + pobId + " contract Id: " + contractId;
                dataImport.getDataImportMessages().add(errorMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            if (!pob.getContract().getId().equals(contractId)) {
                Contract existingContract = pob.getContract();
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, "Updating contract ID for POB: " + pob.getId() + " to contract ID: " + contractId);
                existingContract.getPerformanceObligations().remove(pob);
                pob.setContract(newContract);
                newContract.getPerformanceObligations().add(pob);
                contractService.update(existingContract);
                contractService.update(newContract);

                reportingUnitsToRecalculate.add(existingContract.getReportingUnit());
                reportingUnitsToRecalculate.add(newContract.getReportingUnit());
            }

            revRecMethod = resultSet.getString(6);
            if (methodMap.get(revRecMethod) == null) {
                dataImport.getDataImportMessages().add("POB revrec method not valid: " + revRecMethod);
                logger.log(Level.INFO, "POB revrec method not valid: " + revRecMethod + " pobId: " + pobId);
                throw new IllegalStateException("POB revrec method not valid: " + revRecMethod);
            }

            RevenueMethod revMethod = methodMap.get(revRecMethod);
            if (!pob.getRevenueMethod().equals(revMethod)) {
                logger.info("Updating revenue method for POB ID: " + pob.getId() + " to: " + revMethod.getShortName());
                pob.setRevenueMethod(revMethod);
                pob.setName((revMethod != null ? revMethod.getShortName() : "") + "-" + pobId);
                reportingUnitsToRecalculate.add(pob.getContract().getReportingUnit());
            }

            String existingDesc = pob.getDescription();
            String newDesc = resultSet.getString(2);
            if (existingDesc == null || !existingDesc.equals(newDesc)) {
                String message = "Updated description, pob Id: " + pob.getId() + " new desc: " + newDesc;
                pob.setDescription(newDesc);
                pob.setLastUpdateDate(LocalDateTime.now());
                dataImport.getDataImportMessages().add(message);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, message);
            }

            performanceObligationService.update(pob);

            count++;
            if ((count % 1000) == 0) {
                Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "POB update count: " + count);
            }
        }
        resultSet.close();

        for (ReportingUnit reportingUnit : reportingUnitsToRecalculate) {
            calculationService.calculateAndSave(reportingUnit, currentFinancialPeriod);
        }

        logger.log(Level.INFO, "POB update complete.");
    }

    void processPobDeletions(Connection connection, DataImportFile dataImport) throws SQLException, Exception {

        long contractId = -1;
        long pobId = -1;
        int count = 0;
        Set<ReportingUnit> reportingUnitsToRecalculate = new HashSet<ReportingUnit>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT ID, Name, Stage, Folders, `Name of POb`, `If OT identify the revenue recognition method`, `C-Page ID` FROM tbl_POC_POb_Deletions");

        FinancialPeriod currentFinancialPeriod = financialPeriodService.getCurrentFinancialPeriod();
        if (currentFinancialPeriod.isClosed()) {
            throw new IllegalStateException("The current fiancial period : " + currentFinancialPeriod.getId() + " must be open for POb deletions.");
        }

        while (resultSet.next()) {

            pobId = resultSet.getInt(1);
            PerformanceObligation pob = performanceObligationService.findById(pobId);

            if (pob == null) {
                String errorMsg = "POB does not exist: " + pobId + " invalid deletion record, continuing...";
                dataImport.getDataImportMessages().add(errorMsg);
                logger.log(Level.INFO, errorMsg);
                continue;
            }

            contractId = resultSet.getInt(7);
            Contract contract = contractService.findContractById(contractId);

            if (contract == null) {
                String errorMsg = "POB refers to non-existent contract.  Invalid.  POB ID: " + pobId + " contract Id: " + contractId + " aborting job.";
                dataImport.getDataImportMessages().add(errorMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            if (!pob.getContract().getId().equals(contractId)) {
                String errorMsg = "POB existing contract ID does not equal that found in the deltion record.  Invalid.  POB ID: " + pobId + " contract Id: " + contractId + " aborting job.";
                dataImport.getDataImportMessages().add(errorMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            if (pob.getContract().getId().equals(contractId)) {
                Contract existingContract = pob.getContract();
                existingContract.getPerformanceObligations().remove(pob);
                contractService.update(existingContract);

                reportingUnitsToRecalculate.add(existingContract.getReportingUnit());
                String deleteMsg = "Deleting POB record.  POB ID: " + pobId;
                performanceObligationService.delete(pob);

                dataImport.getDataImportMessages().add(deleteMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, deleteMsg);
            }

            count++;
        }
        resultSet.close();

        for (ReportingUnit reportingUnit : reportingUnitsToRecalculate) {
            calculationService.calculateAndSave(reportingUnit, currentFinancialPeriod);
        }

        Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "POB delete count: " + count);
        logger.log(Level.INFO, "POB deletion complete.");
    }

    void processContractDeletions(Connection connection, DataImportFile dataImport) throws SQLException, Exception {
        ResultSet resultSet = null;
        Long contractId;
        int count = 0;
        Set<ReportingUnit> reportingUnitsToRecalculate = new HashSet<ReportingUnit>();

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT ID, Name, `BPC Reporting Unit`, `Sales Order #`, `Contract Currency`, `Customer Name`, `Contract Description` FROM tbl_POC_Contracts_Deletions");

        FinancialPeriod currentFinancialPeriod = financialPeriodService.getCurrentFinancialPeriod();
        if (currentFinancialPeriod.isClosed()) {
            throw new IllegalStateException("The current fiancial period : " + currentFinancialPeriod.getId() + " must be open for contract additions.");
        }

        while (resultSet.next()) {

            contractId = resultSet.getLong(1);
            Contract contract = contractService.findContractById(contractId);

            if (contract == null) {
                String errorMsg = "Contract does not exist: " + contractId + " invalid change record, skipping.";
                dataImport.getDataImportMessages().add(errorMsg);
                logger.log(Level.INFO, errorMsg);
                continue;
            }

            if (!contract.getPerformanceObligations().isEmpty()) {
                String errorMsg = "Contract not empty.  Please delete all POBs via deletion table.  Aborting job.";
                dataImport.getDataImportMessages().add(errorMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            ReportingUnit ru = adminService.findReportingUnitById(contract.getReportingUnit().getId());

            if (ru == null) {
                String errorMsg = "Reporting unit not found. Aborting job.";
                dataImport.getDataImportMessages().add(errorMsg);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.SEVERE, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            ru.getContracts().remove(contract);
            adminService.update(ru);
            reportingUnitsToRecalculate.add(ru);
            String deleteMsg = "Deleting Contract record.  Contract ID: " + contract.getId();
            contractService.delete(contract);
            dataImport.getDataImportMessages().add(deleteMsg);
            Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, deleteMsg);

            count++;
        }

        resultSet.close();
        Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Contract deletion count: " + count);

        for (ReportingUnit reportingUnit : reportingUnitsToRecalculate) {
            calculationService.calculateAndSave(reportingUnit, currentFinancialPeriod);
        }

        logger.log(Level.INFO, "Contract delete complete.");
    }

    public void processContractBookingDateUpdate(String msAccDB, String fileName) throws Exception {
        Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Processing contract booking date update DB: " + msAccDB);

        Connection connection = null;

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        try {
            String dbURL = "jdbc:ucanaccess://" + msAccDB;
            connection = DriverManager.getConnection(dbURL);
            connection.setAutoCommit(false);

            ut.begin();
            DataImportFile dataImport = new DataImportFile();
            dataImport.setDataImportMessages(new ArrayList<String>());

            updateContractBookingDate(connection, dataImport);

            dataImport.setFilename(fileName);
            dataImport.setUploadDate(LocalDateTime.now());
            dataImport.setCompany(adminService.findCompanyById("FLS"));
            dataImport.setType(CONTRACT_BOOKINGDATE_FILE);
            adminService.persist(dataImport);

            ut.commit();
            adminService.jpaEvictAllCache();
        } catch (Exception e) {
            ut.rollback();
            throw (e);
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }

        logger.log(Level.INFO, "Booking date update completed.");
    }

    void updateContractBookingDate(Connection connection, DataImportFile dataImport) throws SQLException, Exception {
        ResultSet resultSet = null;
        Long contractId;
        int count = 0;

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT CONTRACT_ID, BOOKING_DATE FROM Contract_Dates");

        while (resultSet.next()) {

            contractId = resultSet.getLong(1);
            Contract contract = contractService.findContractById(contractId);
            if (contract == null) {
                String errorMsg = "Contract does not exist: " + contractId + " invalid change record.";
                dataImport.getDataImportMessages().add(errorMsg);
                logger.log(Level.SEVERE, errorMsg);
                //throw new IllegalStateException(errorMsg);
                continue;
            }

            contract.setLastUpdateDate(LocalDateTime.now());

            LocalDate existingBookingDate = contract.getBookingDate();
            LocalDate newBookingDate = new java.sql.Date(resultSet.getDate(2).getTime()).toLocalDate();
            if (existingBookingDate == null || !existingBookingDate.equals(newBookingDate)) {
                String message = "Updated booking date, contract Id: " + contract.getId() + " booking date: " + newBookingDate;
                contract.setBookingDate(newBookingDate);
                contract.setLastUpdateDate(LocalDateTime.now());
                dataImport.getDataImportMessages().add(message);
                Logger.getLogger(BatchProcessingService.class.getName()).log(Level.INFO, message);
            }
            contractService.update(contract);
            count++;
            if ((count % 1000) == 0) {
                Logger.getLogger(AppInitializeService.class.getName()).log(Level.INFO, "Contract update count: " + count);
            }
        }
        resultSet.close();
        logger.log(Level.INFO, "data upload for contract booking date completed.");
    }
}
