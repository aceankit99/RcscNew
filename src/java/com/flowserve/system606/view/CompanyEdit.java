package com.flowserve.system606.view;

import com.flowserve.system606.controller.AdminController;
import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author span
 */
@Named
@ViewScoped
public class CompanyEdit implements Serializable {

    private Company company = new Company();

    List<FinancialPeriod> fpList = new ArrayList<FinancialPeriod>();

    public List<Integer> getPocidates() {
        return pocidates;
    }
    List<Integer> pocidates = new ArrayList();
    @Inject
    private WebSession webSession;

    @Inject
    private AdminService adminService;

    @Inject
    private FinancialPeriodService financialPeriodService;

    @Inject
    private AdminController adminController;
    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public CompanyEdit() {

        Collections.addAll(pocidates, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);

    }

    @PostConstruct
    public void init() {
        company = webSession.getEditCompany();

    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<FinancialPeriod> getFpList() {
        fpList = financialPeriodService.findValidDataPeriods();
        return fpList;
    }

    public void setFpList(List<FinancialPeriod> fpList) {
        this.fpList = fpList;
    }

}
