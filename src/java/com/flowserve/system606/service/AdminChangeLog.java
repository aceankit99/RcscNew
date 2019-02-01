/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.WorkflowAction;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author shubhamv
 */
@Stateless
public class AdminChangeLog {

    @Inject
    private AdminService adminService;

    public void generateAdminChangeLog(InputStream inputStream, FileOutputStream outputStream) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet worksheet = workbook.getSheet("Rprt 3 Tab 1 - Admin Change Log");
            writeChangeLogForAdmin(worksheet);
            ((XSSFSheet) worksheet).getCTWorksheet().getSheetViews().getSheetViewArray(0).setTopLeftCell("A1");
            ((XSSFSheet) worksheet).setActiveCell(new CellAddress("A2"));

            workbook.write(outputStream);
        }
        inputStream.close();
        outputStream.close();
    }

    public XSSFSheet writeChangeLogForAdmin(XSSFSheet worksheet) throws Exception {

        int rowNum = 3;
        for (ReportingUnit ru : adminService.findAllReportingUnits()) {

            for (WorkflowAction workflowAction : ru.getWorkflowHistory()) {
                XSSFRow row = worksheet.getRow(rowNum);
                if (row == null) {
                    row = worksheet.createRow(rowNum);
                }
                setStringCellValue(row, 0, workflowAction.getWorkflowActionType().getName());
                setStringCellValue(row, 1, workflowAction.getUser().getName());
                setStringCellValue(row, 2, ru.getCode());
                setDateCellValue(row, 5, workflowAction.getActionDate().toLocalDate());
                setStringCellValue(row, 6, "Reporting Unit");
                setStringCellValue(row, 7, workflowAction.getName());
                setStringCellValue(row, 8, workflowAction.getComments());
                rowNum++;
            }
        }

        return worksheet;
    }

    private void setStringCellValue(XSSFRow row, int cellNum, String value) {
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value);

    }

    private void setDateCellValue(XSSFRow row, int cellNum, LocalDate value) {
        if (value != null) {
            Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            CellStyle currentStyle = cell.getCellStyle();
            cell.setCellValue(value.toString());
            cell.setCellStyle(currentStyle);
        }

    }
}
