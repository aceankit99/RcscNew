/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamc
 */

@Named
@ViewScoped
public class ReportBusinessUnit implements Serializable {
     private BusinessUnit businessUnit = new BusinessUnit();
     @Inject
    private WebSession webSession;
     
       @PostConstruct
    public void init() {
        businessUnit = webSession.getEditBusinessUnit();
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }
    
    
}
