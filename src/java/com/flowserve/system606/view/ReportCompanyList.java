/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Company;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class ReportCompanyList implements Serializable{
    
   List<Company> company = new ArrayList<Company>();

  
   
   @Inject
   AdminService adminService;
    
   
     public List<Company> getCompany() throws Exception {
       company=adminService.findAllCompany();
       return company;
    }

    public void setCompany(List<Company> company) {
        this.company = company;
    }
}
