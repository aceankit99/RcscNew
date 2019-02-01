package com.flowserve.system606.view;

import com.flowserve.system606.model.Company;
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
public class CompanyList implements Serializable {

    // private static final long serialVersionUID = -1438027991420003830L;
    List<Company> companyLists = new ArrayList<Company>();
    @Inject
    private AdminService adminService;

    public List<Company> getCompanyLists() throws Exception {
        companyLists = adminService.findAllCompany();
        Collections.sort(companyLists);
        return companyLists;
    }

    public void setCompanyLists(List<Company> companyLists) {
        this.companyLists = companyLists;
    }

}
