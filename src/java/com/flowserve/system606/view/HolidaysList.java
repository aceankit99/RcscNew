/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Holiday;
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
public class HolidaysList implements Serializable {
   
    
    @Inject
    private AdminService adminService;
    
    List<Holiday> holidays=new ArrayList<Holiday>();
    
    public HolidaysList(){}

    public List<Holiday> getHolidays() throws Exception {
        holidays=adminService.findHolidayList();
       
        return holidays;
    }

    public void setHolidays(List<Holiday> holidays) {
        this.holidays = holidays;
    }
    
    
}
