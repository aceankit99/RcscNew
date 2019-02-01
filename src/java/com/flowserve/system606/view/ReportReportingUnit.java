/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.ReportsService;
import com.flowserve.system606.web.WebSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class ReportReportingUnit implements Serializable{
    private StreamedContent file;
    private InputStream inputStream;
    private FileOutputStream outputStream;
    
    @Inject
    private WebSession webSession;
    @Inject
    AdminService adminService;
    @Inject
    ReportsService reportsService;
     
    ReportingUnit ru=new ReportingUnit();
    
    
     @PostConstruct
    public void init() {
        ru = webSession.getEditReportingUnit();
    }

    public ReportingUnit getRu() {
        return ru;
    }

    public void setRu(ReportingUnit ru) {
        this.ru = ru;
    }
    
     public StreamedContent getFileJournalEntry() throws Exception {
        try {

           inputStream =ReportReportingUnit.class.getResourceAsStream("/resources/excel_input_templates/Journal_Entry.xlsx");
            outputStream = new FileOutputStream(new File("Journal_Entry.xlsx"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        reportsService.generateReportJournalEntry(inputStream, outputStream);

        InputStream inputStreamFromOutputStream = new FileInputStream(new File("Journal_Entry.xlsx"));
        file = new DefaultStreamedContent(inputStreamFromOutputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Journal_Entry.xlsx");
        return file;
    }
    
}
