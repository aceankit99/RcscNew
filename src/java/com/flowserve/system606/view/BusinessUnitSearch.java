package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class BusinessUnitSearch implements Serializable {

    // private static final long serialVersionUID = -1438027991420003830L;
    List<BusinessUnit> businessUnits = new ArrayList<BusinessUnit>();
    @Inject
    private AdminService adminService;
    private String searchString = "";

    public void search() throws Exception {

        //  businessUnits = adminService.searchUsers(searchString);
        //  Collections.sort(businessUnits);
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public List<BusinessUnit> getBusinessUnits() throws Exception {
        System.out.println("getBusinessUnits");
        businessUnits = adminService.findBusinessUnits();
        Collections.sort(businessUnits);
        return businessUnits;
    }

    public void setBusinessUnits(List<BusinessUnit> businessUnits) {
        this.businessUnits = businessUnits;
    }
}
