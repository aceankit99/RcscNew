/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.EventType;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricSet;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.view.ViewSupport;
import com.flowserve.system606.web.WebSession;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Stateless
public class TemplateService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    @Inject
    private MetricService metricService;
    @Inject
    private EventService eventService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private PerformanceObligationService pobService;
    @Inject
    private ContractService contractService;
    @Inject
    FinancialPeriodService financialPeriodService;
    @Inject
    CurrencyService currencyService;
    @Inject
    AdminService adminService;
    @Inject
    ViewSupport viewSupport;
    private static final int HEADER_ROW_COUNT = 2;
    private InputStream inputStream;
    @Inject
    private WebSession webSession;
    private EventType billingEventType;

    private Set<PerformanceObligation> changedPOBs = new TreeSet<PerformanceObligation>();
    private Set<Contract> changedContract = new TreeSet<Contract>();

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    @PostConstruct
    public void init() {
        billingEventType = eventService.getEventTypeByCode("BILLING_EVENT_CC");
    }

    public List<ReportingUnit> getReportingUnits() {
        return new ArrayList<ReportingUnit>();
    }

    public void processTemplateDownload(InputStream inputStream, FileOutputStream outputStream, ReportingUnit reportingUnit) throws Exception {

        List<MetricType> metricTypes = metricService.getAllPobExcelInputMetricTypes();
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet worksheet = workbook.getSheetAt(0);
        XSSFRow row;
        Cell cell = null;
        // TODO - This needs to be read from the file and then checked to make sure it's open.
        FinancialPeriod period = financialPeriodService.getCurrentFinancialPeriod();
        row = worksheet.getRow(1);
        cell = row.getCell(0);
        cell.setCellValue(period.getName());
        cell = row.getCell(1);
        cell.setCellValue(reportingUnit.getCode());
        cell = row.getCell(2);
        cell.setCellValue("RU" + reportingUnit.getName());
        int rowid = 3;
        List<Contract> contracts = reportingUnit.getUnarchivedContracts();
        for (Contract contract : contracts) {
            List<PerformanceObligation> pobs = contract.getPerformanceObligations();
            int pobCount = 1;
            for (PerformanceObligation pob : pobs) {
                row = worksheet.getRow(rowid);
                if (row == null) {
                    row = worksheet.createRow(rowid);
                }

                // Populate non-input cells
                cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(reportingUnit.getCode());
                cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(contract.getId());
                //TODO
//                cell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//                cell.setCellValue(contract.getCustomer().getName());
                cell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(contract.getSalesOrderNumber());
                cell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(pob.getDescription());
                cell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(pob.getId());
                cell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(pob.getRevenueMethod().getShortName() == null ? "" : pob.getRevenueMethod().getShortName());
                cell = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(contract.getContractCurrency().getCurrencyCode());

                BigDecimal value = BigDecimal.ZERO;
                value = calculationService.getCurrencyMetric("TRANSACTION_PRICE_CC", pob, period).getCcValue();
                setCellValue(row, 9, value);
                value = calculationService.getCurrencyMetric("LIQUIDATED_DAMAGES_CTD_CC", pob, period).getCcValue();
                setCellValue(row, 10, value);
                setStringCellValue(row, 11, contract.getLocalCurrency().getCurrencyCode());
                value = calculationService.getCurrencyMetric("ESTIMATED_COST_AT_COMPLETION_LC", pob, period).getLcValue();
                setCellValue(row, 12, value);
                value = calculationService.getCurrencyMetric("ESTIMATED_COST_AT_COMPLETION_LC", pob, webSession.getPriorPeriod()).getLcValue();
                setCellValue(row, 13, value);

                value = calculationService.getCurrencyMetric("LOCAL_COSTS_CTD_LC", pob, period).getLcValue();
                setCellValue(row, 15, value);
                value = calculationService.getCurrencyMetric("THIRD_PARTY_COSTS_CTD_LC", pob, period).getLcValue();
                setCellValue(row, 16, value);
                value = calculationService.getCurrencyMetric("INTERCOMPANY_COSTS_CTD_LC", pob, period).getLcValue();
                setCellValue(row, 17, value);

                value = calculationService.getCurrencyMetric("COSTS_INCURRED_CTD_LC", pob, period).getLcValue();
                setCellValue(row, 18, value);
                LocalDate dDate = calculationService.getDateMetric("DELIVERY_DATE", pob, period).getValue();
                setDateCellValue(row, 19, dDate);
                value = calculationService.getCurrencyMetric("PARTIAL_SHIPMENT_COSTS_LC", pob, period).getLcValue();
                setCellValue(row, 20, value);

                if (pobCount == 1) {
                    value = calculationService.getCurrencyMetric("THIRD_PARTY_COMMISSION_CTD_LC", contract, period).getLcValue();
                    setCellValue(row, 22, value);
                }
                String val = calculationService.getStringMetric("SALES_DESTINATION", pob, period).getValue();
                setStringCellValue(row, 23, val);
                val = calculationService.getStringMetric("OEAM_DISAGG", pob, period).getValue();
                setStringCellValue(row, 24, val);
                dDate = calculationService.getDateMetric("SL_START_DATE", pob, period).getValue();
                setDateCellValue(row, 25, dDate);
                dDate = calculationService.getDateMetric("SL_END_DATE", pob, period).getValue();
                setDateCellValue(row, 26, dDate);

                rowid++;
                pobCount++;
            }
        }
        ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
        ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A4"));
        workbook.write(outputStream);
        workbook.close();
        inputStream.close();
        outputStream.close();
    }

    protected void setCellComment(Cell cell, String message) {
        Drawing drawing = cell.getSheet().createDrawingPatriarch();
        CreationHelper factory = cell.getSheet().getWorkbook()
                .getCreationHelper();
        // When the comment box is visible, have it show in a 1x3 space
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 2);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 2);
        anchor.setDx1(100);
        anchor.setDx2(100);
        anchor.setDy1(100);
        anchor.setDy2(100);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(message);
        comment.setString(str);
        comment.setAuthor("POCI_Template_RU");
        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    public void processTemplateUpload(InputStream fis, String filename) throws Exception {  // Need an application exception type defined.
        try {

            // TODO - This needs to be read from the file and then checked to make sure it's open.
            FinancialPeriod period = financialPeriodService.getCurrentFinancialPeriod();

            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet worksheet = workbook.getSheetAt(0);
            MetricSet inputSet = new MetricSet();
            inputSet.setFilename(filename);
            int pobIdColNumber = CellReference.convertColStringToIndex("F");

            if (worksheet == null) {
                throw new IllegalStateException("Invalid xlsx file.  Report detail to user");
            }
            Cell cellRu = null;
            Row rowRu = worksheet.getRow(1);
            cellRu = rowRu.getCell(CellReference.convertColStringToIndex("A"));
            if (cellRu == null || cellRu.getCellTypeEnum() == CellType.BLANK) {
                throw new IllegalStateException("The financial period on the uploaded template file does not match the current financial period. Please check your template file and try again.");
            }
            if (cellRu.getCellTypeEnum() != CellType.STRING) {
                throw new IllegalStateException("The financial period on the uploaded template file does not match the current financial period. Please check your template file and try again.");
            }
            FinancialPeriod periodUp = financialPeriodService.findById(cellRu.getStringCellValue());
            if (periodUp == null) {
                throw new IllegalStateException("The financial period on the uploaded template file does not match the current financial period. Please check your template file and try again.");
            }
            if (!periodUp.equals(webSession.getCurrentPeriod())) {
                throw new IllegalStateException("The financial period on the uploaded template file does not match the current financial period. Please check your template file and try again.");
            }
            cellRu = rowRu.getCell(CellReference.convertColStringToIndex("B"));
            if (cellRu == null || cellRu.getCellTypeEnum() == CellType.BLANK) {
                throw new IllegalStateException("The reporting unit on the uploaded template file does not match the current reporting unit. Please check your template file and try again.");
            }
            if (cellRu.getCellTypeEnum() != CellType.STRING) {
                throw new IllegalStateException("The reporting unit on the uploaded template file does not match the current reporting unit. Please check your template file and try again.");
            }
            ReportingUnit ruUP = adminService.findReportingUnitByCode(cellRu.getStringCellValue());
            if (ruUP == null) {
                throw new IllegalStateException("The reporting unit on the uploaded template file does not match the current reporting unit. Please check your template file and try again.");
            }
            if (!ruUP.equals(webSession.getCurrentReportingUnit())) {
                throw new IllegalStateException("The reporting unit on the uploaded template file does not match the current reporting unit. Please check your template file and try again.");
            }
            //Logger.getLogger(TemplateService.class.getName()).log(Level.INFO, "Processing POB input template: " + filename);
            for (Row row : worksheet) {
                if (row.getRowNum() < 3) {
                    continue;
                }
                Cell pobIdCell = row.getCell(pobIdColNumber);
                if (pobIdCell == null || pobIdCell.getCellTypeEnum() == CellType.BLANK) {
                    Logger.getLogger(MetricService.class.getName()).log(Level.FINE, "POB input template processing complete.");  // TODO - figure out if we really want to stop here.
                    break;
                }
                if (pobIdCell.getCellTypeEnum() != CellType.NUMERIC) { //  TODO - Need a mechansim to report exact error to user.
                    throw new IllegalStateException("Input file invalid.  POB ID column not a numeric");
                }
                PerformanceObligation pob = pobService.findById((long) pobIdCell.getNumericCellValue());
                if (pob == null) {
                    throw new IllegalStateException("Input file invalid.  Invalid POB at row: " + row.getRowNum());
                }
                Cell cell = null;
                try {
                    cell = row.getCell(CellReference.convertColStringToIndex("J"));
                    setValueInMetricType(cell, pobIdCell, "TRANSACTION_PRICE_CC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("K"));
                    setValueInMetricType(cell, pobIdCell, "LIQUIDATED_DAMAGES_CTD_CC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("M"));
                    setValueInMetricType(cell, pobIdCell, "ESTIMATED_COST_AT_COMPLETION_LC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("P"));
                    setValueInMetricType(cell, pobIdCell, "LOCAL_COSTS_CTD_LC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("Q"));
                    setValueInMetricType(cell, pobIdCell, "THIRD_PARTY_COSTS_CTD_LC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("R"));
                    setValueInMetricType(cell, pobIdCell, "INTERCOMPANY_COSTS_CTD_LC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("S"));
                    setValueInMetricType(cell, pobIdCell, "COSTS_INCURRED_CTD_LC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("T"));
                    setValueInDate(cell, pobIdCell, "DELIVERY_DATE", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("U"));
                    setValueInMetricType(cell, pobIdCell, "PARTIAL_SHIPMENT_COSTS_LC", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("W"));
                    setValueInContractMetricType(cell, pobIdCell, "THIRD_PARTY_COMMISSION_CTD_LC", pob.getContract(), period);
                    cell = row.getCell(CellReference.convertColStringToIndex("X"));
                    setValueInStringMetricType(cell, pobIdCell, "SALES_DESTINATION", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("Y"));
                    setValueInStringMetricType(cell, pobIdCell, "OEAM_DISAGG", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("Z"));
                    setValueInDate(cell, pobIdCell, "SL_START_DATE", pob, period);
                    cell = row.getCell(CellReference.convertColStringToIndex("AA"));
                    setValueInDate(cell, pobIdCell, "SL_END_DATE", pob, period);
                } catch (Exception rce) {
                    Logger.getLogger(TemplateService.class.getName()).log(Level.SEVERE, "Error processing ");
                    throw new Exception("processTemplateUpload row: " + row.getRowNum() + " cell: " + (cell.getColumnIndex() + 1) + " " + rce.getMessage());
                }

            }
            for (PerformanceObligation pob : changedPOBs) {
                calculationService.executeBusinessRules(pob, webSession.getCurrentPeriod());
                pob.setLastUpdatedBy(webSession.getUser());
                pob.setLastUpdateDate(LocalDateTime.now());
                pobService.update(pob);
            }
            for (Contract contract : changedContract) {
                calculationService.executeBusinessRules(contract, webSession.getCurrentPeriod());
                contract.setLastUpdatedBy(webSession.getUser());
                contract.setLastUpdateDate(LocalDateTime.now());
                contractService.update(contract);
            }
            changedPOBs.clear();
            changedContract.clear();
        } catch (Exception e) {
            throw new Exception("processTemplateUpload: " + e.getMessage());
        } finally {
            fis.close();
        }
    }

    private boolean billingEventDoesNotExistInContract(Contract contract, String invoiceNumber) {
        return contract.getAllEventsByEventTypeAndNumber(billingEventType, invoiceNumber).isEmpty();
    }

    private boolean currencyMetricIsNotNull(MetricType metricType, Measurable measurable, FinancialPeriod period) throws Exception {
        return calculationService.getCurrencyMetric(metricType.getCode(), measurable, period) != null
                && calculationService.getCurrencyMetric(metricType.getCode(), measurable, period).getValue() != null;
    }

    private void setCellValue(XSSFRow row, int cellNum, BigDecimal value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(currentStyle);
        }

    }

    private void setStringCellValue(XSSFRow row, int cellNum, String value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value);
            cell.setCellStyle(currentStyle);
        }

    }

    private void setDateCellValue(XSSFRow row, int cellNum, LocalDate value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value.toString());
            cell.setCellStyle(currentStyle);
        }

    }

    private void setValueInMetricType(Cell cell, Cell pobIdCell, String type, PerformanceObligation pob, FinancialPeriod period) throws Exception {
        if (cell == null || pobIdCell.getCellTypeEnum() == CellType.BLANK || ((XSSFCell) cell).getRawValue() == null) {
            BigDecimal currentValue = calculationService.getCurrencyMetric(type, pob, period).getValue();
            if (currentValue != null) {
                calculationService.getCurrencyMetric(type, pob, period).setValue(null);
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            }
        } else {
            BigDecimal currentValue = calculationService.getCurrencyMetric(type, pob, period).getValue();
            BigDecimal UpdatedValue = new BigDecimal(NumberToTextConverter.toText(cell.getNumericCellValue()));
            if (currentValue == null) {
                calculationService.getCurrencyMetric(type, pob, period).setValue(UpdatedValue);
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            } else if (currentValue.compareTo(UpdatedValue) != 0) {
                calculationService.getCurrencyMetric(type, pob, period).setValue(UpdatedValue);
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            }
        }
    }

    private void setValueInContractMetricType(Cell cell, Cell pobIdCell, String type, Contract contract, FinancialPeriod period) throws Exception {
        if (cell == null || pobIdCell.getCellTypeEnum() == CellType.BLANK || ((XSSFCell) cell).getRawValue() == null) {
            // TODO - figure out what to do in this blank case.  It will depend on the situation.
        } else {
            BigDecimal currentValue = calculationService.getCurrencyMetric(type, contract, period).getValue();
            BigDecimal updatedValue = new BigDecimal(NumberToTextConverter.toText(cell.getNumericCellValue()));
            if (currentValue == null) {
                calculationService.getCurrencyMetric(type, contract, period).setValue(updatedValue);
                changedContract.add(contract);
            } else if (currentValue.compareTo(updatedValue) != 0) {
                calculationService.getCurrencyMetric(type, contract, period).setValue(updatedValue);
                changedContract.add(contract);
            }
        }
    }

    private void setValueInStringMetricType(Cell cell, Cell pobIdCell, String type, PerformanceObligation pob, FinancialPeriod period) throws Exception {
        if (cell == null || pobIdCell.getCellTypeEnum() == CellType.BLANK || ((XSSFCell) cell).getRawValue() == null) {
            String currentValue = calculationService.getStringMetric(type, pob, period).getValue();
            if (currentValue != null) {
                calculationService.getStringMetric(type, pob, period).setValue("");
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            }
        } else {
            if (cell.getCellTypeEnum().toString().equalsIgnoreCase("STRING")) {
                String currentValue = calculationService.getStringMetric(type, pob, period).getValue();
                String updatedValue = cell.getStringCellValue();
                if (currentValue == null) {
                    calculationService.getStringMetric(type, pob, period).setValue(updatedValue);
                    changedPOBs.add(pob);
                    changedContract.add(pob.getContract());
                } else if (!currentValue.equalsIgnoreCase(updatedValue)) {
                    calculationService.getStringMetric(type, pob, period).setValue(updatedValue);
                    changedPOBs.add(pob);
                    changedContract.add(pob.getContract());
                }
            }

        }

    }

    private void setValueInDate(Cell cell, Cell pobIdCell, String type, PerformanceObligation pob, FinancialPeriod period) throws Exception {
        if (cell == null || pobIdCell.getCellTypeEnum() == CellType.BLANK || ((XSSFCell) cell).getRawValue() == null) {
            LocalDate currentValue = calculationService.getDateMetric(type, pob, period).getValue();
            if (currentValue != null) {
                calculationService.getDateMetric(type, pob, period).setValue(null);
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            }
        } else {
            LocalDate currentValue = calculationService.getDateMetric(type, pob, period).getValue();
            LocalDate updatedValue = null;
            if (cell.getCellTypeEnum().toString().equalsIgnoreCase("STRING")) {
                updatedValue = LocalDate.parse(cell.getStringCellValue());
            } else if (cell.getCellTypeEnum().toString().equalsIgnoreCase("NUMERIC")) {
                updatedValue = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (currentValue == null) {
                calculationService.getDateMetric(type, pob, period).setValue(updatedValue);
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            } else if (!currentValue.equals(updatedValue)) {
                calculationService.getDateMetric(type, pob, period).setValue(updatedValue);
                changedPOBs.add(pob);
                changedContract.add(pob.getContract());
            }

        }
    }
}
