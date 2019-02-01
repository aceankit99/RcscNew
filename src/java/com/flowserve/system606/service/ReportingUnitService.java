/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.DataImportFile;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.User;
import com.flowserve.system606.model.WorkflowAction;
import com.flowserve.system606.model.WorkflowActionType;
import com.flowserve.system606.view.PobInput;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author kgraves
 */
@Stateless
public class ReportingUnitService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    @Inject
    private AdminService adminService;

    public List<ReportingUnit> getViewableReportingUnits(User user) {
        Query query = em.createQuery("SELECT ru FROM ReportingUnit ru where :USER MEMBER OF ru.viewers AND ru.active = :ACTIVE ORDER BY ru.code");
        query.setParameter("USER", user);
        query.setParameter("ACTIVE", true);
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<ReportingUnit> getPreparableReportingUnits(User user) {
        Query query = em.createQuery("SELECT ru FROM ReportingUnit ru WHERE ru.active = :ACTIVE AND :USER MEMBER OF ru.preparers ORDER BY ru.code", ReportingUnit.class);
        query.setParameter("USER", user);
        query.setParameter("ACTIVE", true);
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<ReportingUnit> getReviewableReportingUnits(User user) {
        Query query = em.createQuery("SELECT ru FROM ReportingUnit ru where :USER MEMBER OF ru.reviewers AND ru.active = :ACTIVE ORDER BY ru.code");
        query.setParameter("USER", user);
        query.setParameter("ACTIVE", true);
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<ReportingUnit> getApprovableReportingUnits(User user) {
        Query query = em.createQuery("SELECT ru FROM ReportingUnit ru where :USER MEMBER OF ru.approvers AND ru.active = :ACTIVE ORDER BY ru.code");
        query.setParameter("USER", user);
        query.setParameter("ACTIVE", true);
        return (List<ReportingUnit>) query.getResultList();
    }

    public boolean isUpdatable(ReportingUnit ru, FinancialPeriod period, User user) {
        if (period.isOpen() && ru.isDraft(period)) {
            return true;
        }

        return false;
    }

    /**
     * KJG Enhance the methods below to check user role levels. Deferring that check due to lack of time. We'll handle that check in the UI for now.
     */
    public void submitForReview(ReportingUnit reportingUnit, FinancialPeriod period, User user) throws Exception {
        if (reportingUnit.isPreparable(period, user)) {
            WorkflowAction action = new WorkflowAction(WorkflowActionType.REQUEST_REVIEW, user);
            adminService.persist(action);
            reportingUnit.addWorkflowAction(period, action);
            reportingUnit.setPrepared(period);
            adminService.update(reportingUnit);
        }
    }

    public void submitForApproval(ReportingUnit reportingUnit, FinancialPeriod period, User user) throws Exception {
        if (reportingUnit.isReviewable(period, user)) {
            WorkflowAction action = new WorkflowAction(WorkflowActionType.REQUEST_APPROVAL, user);
            adminService.persist(action);
            reportingUnit.addWorkflowAction(period, action);
            reportingUnit.setReviewed(period);
            adminService.update(reportingUnit);
        }
    }

    public void approve(ReportingUnit reportingUnit, FinancialPeriod period, User user) throws Exception {
        if (reportingUnit.isApprovable(period, user)) {
            WorkflowAction action = new WorkflowAction(WorkflowActionType.APPROVE, user);
            adminService.persist(action);
            reportingUnit.addWorkflowAction(period, action);
            reportingUnit.setApproved(period);
            adminService.update(reportingUnit);
        }
    }

    public void review(ReportingUnit reportingUnit, FinancialPeriod period, User user) throws Exception {
        if (reportingUnit.isReviewable(period, user)) {
            WorkflowAction action = new WorkflowAction(WorkflowActionType.REVIEW, user);
            adminService.persist(action);
            reportingUnit.addWorkflowAction(period, action);
            reportingUnit.setReviewed(period);
            adminService.update(reportingUnit);
        }
    }

    public void reject(ReportingUnit reportingUnit, FinancialPeriod period, User user) throws Exception {
        if (reportingUnit.isRejectable(period, user)) {
            WorkflowAction action = new WorkflowAction(WorkflowActionType.REJECT, user);
            adminService.persist(action);
            reportingUnit.addWorkflowAction(period, action);
            reportingUnit.setRejected(period);
            adminService.update(reportingUnit);
        }
    }

    public void approveIfEmpty(FinancialPeriod period, User user) throws Exception {
        for (ReportingUnit ru : adminService.findAllReportingUnits()) {
            if (ru.getUnarchivedContracts().isEmpty()) {
                WorkflowAction action = new WorkflowAction(WorkflowActionType.APPROVE, user);
                adminService.persist(action);
                ru.addWorkflowAction(period, action);
                ru.setApproved(period);
                adminService.update(ru);
            }
        }
    }

    public void approveAllRUsIfNotApproved(FinancialPeriod period, User user) throws Exception {
        for (ReportingUnit ru : adminService.findAllReportingUnits()) {
            if (!ru.isApproved(period)) {
                WorkflowAction action = new WorkflowAction(WorkflowActionType.APPROVE, user);
                adminService.persist(action);
                ru.addWorkflowAction(period, action);
                ru.setApproved(period);
                adminService.update(ru);
            }
        }
    }

    public void initialize(ReportingUnit reportingUnit, FinancialPeriod period, User user) throws Exception {
        //Logger.getLogger(ReportingUnitService.class.getName()).log(Level.INFO, "initializeDraft()");
        if (period.isOpen()) {
            //Logger.getLogger(ReportingUnitService.class.getName()).log(Level.INFO, "initializeDraft() it's open.");
            WorkflowAction action = new WorkflowAction(WorkflowActionType.INITIALIZE, user);
            adminService.persist(action);
            reportingUnit.addWorkflowAction(period, action);
            reportingUnit.setInitialized(period);
            adminService.update(reportingUnit);
        }
    }

    public void removeAllUserAssignments() throws Exception {
        for (ReportingUnit reportingUnit : adminService.findAllReportingUnits()) {
            reportingUnit.getViewers().clear();
            reportingUnit.getPreparers().clear();
            reportingUnit.getApprovers().clear();
            reportingUnit.getReviewers().clear();

            adminService.update(reportingUnit);
        }
    }

    public void reportingUnitList() throws Exception {
        InputStream inputStream;
        Logger.getLogger(TemplateService.class.getName()).log(Level.INFO, "Start: ");
        String folder = "/tmp/";
        try {

            File fout = new File(folder + "reporting_units.txt");
            //Create the file
            if (fout.createNewFile()) {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists." + fout);
            }

            FileOutputStream fos = new FileOutputStream(fout);

            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
                //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                inputStream = PobInput.class.getResourceAsStream("/resources/excel_input_templates/RCS User Listing - by Coe and Role 09.17.18.xlsx");
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet worksheet = workbook.getSheet("CoE RU Structure");
                Cell cellFrom = null;
                for (Row rowFrom : worksheet) {
                    String name = "";
                    int code = 0;
                    String currency = "";
                    String platform = "";
                    String region = "";
                    String c_code = "";
                    String c_name = "";
                    String role = "";
                    String rcs_role = "";
                    if (rowFrom.getRowNum() < 1) {
                        continue;
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("A"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        String first = cellFrom.getStringCellValue().trim();
                        String[] splitStr = first.split("-");
                        name = splitStr[1];
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("B"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        code = (int) cellFrom.getNumericCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("C"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        currency = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("D"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        platform = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("E"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        region = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("F"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        c_code = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("G"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        c_name = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("H"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        role = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("I"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        rcs_role = cellFrom.getStringCellValue();
                    }

                    bw.newLine();
                    bw.write(name + "\t" + code + "\t" + currency + "\t" + platform + "\t" + region + "\t" + c_code + "\t" + c_name + "\t" + role + "\t" + rcs_role);
                    Logger.getLogger(TemplateService.class.getName()).log(Level.INFO, "coe : " + name + " fsl_id : " + code + " role : " + currency + " monthlyRate : " + region + " monthlyRate : " + role);
                }
//
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reportingPreparersList(InputStream inputStream) throws Exception {
        //InputStream inputStream;
        Logger.getLogger(TemplateService.class.getName()).log(Level.INFO, "reportingPreparersList start: ");
        String folder = "/tmp/";
        try {
            File fout = new File(folder + "prepare_user.txt");
            fout.delete();
            //Create the file
            if (fout.createNewFile()) {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists." + fout);
            }

            FileOutputStream fos = new FileOutputStream(fout);

            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
                //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                // inputStream = PobInput.class.getResourceAsStream("/resources/excel_input_templates/RCS User Listing - by Coe and Role 09.18.18.xlsx");
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet worksheet = workbook.getSheet("RCS POC User Access & Role List");
                Cell cellFrom = null;
                for (Row rowFrom : worksheet) {
                    String ru = "";
                    String user = "";
                    String userid = "";
                    String email = "";
                    String role = "";
                    if (rowFrom.getRowNum() < 37) {
                        continue;
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("A"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        String first = cellFrom.getStringCellValue().trim();
                        String second = first.substring(0, 6);
                        ru = second.substring(2, 6);
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("B"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        user = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("C"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {

                    } else {
                        if (cellFrom.getCellTypeEnum() == CellType.STRING) {
                            userid = cellFrom.getStringCellValue();
                        } else {
                            int id = (int) cellFrom.getNumericCellValue();
                            userid = String.valueOf(id);
                        }

                    }

                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("D"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        email = cellFrom.getStringCellValue();
                    }
                    cellFrom = rowFrom.getCell(CellReference.convertColStringToIndex("F"));
                    if (cellFrom == null || ((XSSFCell) cellFrom).getRawValue() == null) {
                        //continue;
                    } else {
                        role = cellFrom.getStringCellValue();
                    }

                    bw.newLine();
                    bw.write(ru + "\t" + user + "\t" + userid + "\t" + email + "\t" + role);
                    //Logger.getLogger(TemplateService.class.getName()).log(Level.INFO, "coe : " + ru + " fsl_id : " + user + " role : " + userid + " monthlyRate : " + email + " monthlyRate : " + role);
                }
//
            }
            inputStream.close();
            removeAllUserAssignments();
            initReportingUnitUserAssignments();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.getLogger(TemplateService.class.getName()).log(Level.INFO, "reportingPreparersList end.");
    }

    public void initReportingUnitUserAssignments() throws Exception {

        DataImportFile dataImport = new DataImportFile();
        List<String> importMessages = new ArrayList<String>();
        try {
            if (adminService.findPreparersByReportingUnitCode("8000") == null) {
                Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing Reporting Unit user assignments");
                BufferedReader reader = new BufferedReader(new FileReader("/tmp/prepare_user.txt"));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }

                    String[] values = line.split("\\t");

                    if (values.length <= 1) {
                        continue;
                    }

                    ReportingUnit ru = adminService.findReportingUnitByCode(values[0]);
                    User user = adminService.findUserByFlsId(values[2]);

                    if (ru == null) {
                        importMessages.add("User assignmant to RU failed reason :  RU not found - " + values[0]);
                        Logger.getLogger(ReportingUnitService.class.getName()).log(Level.SEVERE, "RU user assignmant:  RU not found: " + values[0] + " line: " + line);
                        continue;
                    }
                    if (user == null) {
                        importMessages.add("User assignmant to RU failed reason :  User not found -  " + values[2]);
                        Logger.getLogger(ReportingUnitService.class.getName()).log(Level.SEVERE, "RU user assignment:  User not found: " + values[2] + " line: " + line);
                        continue;
                    }

                    if (values.length > 4 && values[4].equalsIgnoreCase("Preparer")) {
                        if (!ru.getPreparers().contains(user)) {
                            ru.getPreparers().add(user);
                            adminService.update(ru);
                        }

                    } else if (values.length > 4 && values[4].equalsIgnoreCase("Approver")) {
                        if (!ru.getApprovers().contains(user)) {
                            ru.getApprovers().add(user);
                            adminService.update(ru);
                        }

                    } else if (values.length > 4 && values[4].equalsIgnoreCase("Reviewer")) {
                        if (!ru.getReviewers().contains(user)) {
                            ru.getReviewers().add(user);
                            adminService.update(ru);
                        }

                    } else if (values.length > 4 && values[4].equalsIgnoreCase("Viewer")) {
                        if (!ru.getViewers().contains(user)) {
                            ru.getViewers().add(user);
                            adminService.update(ru);
                        }
                    }
                }

                reader.close();
                importMessages.add("Finished initializing Reporting Unit user assignments.");
                Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Reporting Unit user assignments.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataImport.setFilename("TBL_USERROLE");
            dataImport.setUploadDate(LocalDateTime.now());
            dataImport.setCompany(adminService.findCompanyById("FLS"));
            dataImport.setDataImportMessages(importMessages);
            dataImport.setType("User Role");
            adminService.persist(dataImport);
        }
    }

    public void uploadedUserList(InputStream inputStream, String fileName) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line = null;
        DataImportFile dataImport = new DataImportFile();
        List<String> importMSG = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
                String flsid = null, displayname = null, commonNameLDAP = null, email = null, officeName = null, title = null;
                int org_level = 0;
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split("\\t");
                if (values.length < 4) {
                    importMSG.add("Required fields not available for user flsId : " + values[0]);
                    continue;
                } else {
                    if (adminService.findUserByFlsId(values[0]) != null) {
                        importMSG.add("Record already exist for user flsId : " + values[0]);
                        continue;
                    } else {
                        flsid = values[0];
                        displayname = values[1];
                        commonNameLDAP = values[2];
                        email = values[3];

                        if (values.length > 4) {
                            officeName = values[4];
                        }
                        if (values.length > 5) {
                            title = values[5];
                        }
                        if (values.length > 6) {
                            org_level = Integer.parseInt(values[6]);
                        }

                    }
                }

                User user = new User(flsid, displayname, commonNameLDAP, email, officeName, title, org_level);
                adminService.persist(user);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            dataImport.setFilename(fileName);
            dataImport.setUploadDate(LocalDateTime.now());
            dataImport.setCompany(adminService.findCompanyById("FLS"));
            dataImport.setDataImportMessages(importMSG);
            dataImport.setType("userUpload");
            adminService.persist(dataImport);

        }

    }

}
