/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.User;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.AppInitializeService;
import com.flowserve.system606.service.CalculationService;
import com.flowserve.system606.service.EventService;
import com.flowserve.system606.service.FinancialPeriodService;
import com.flowserve.system606.service.MetricService;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named(value = "initializeApp")
@ViewScoped
public class InitializeApp implements Serializable {

    /**
     * Creates a new instance of initializeApp
     */
    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    User adminUser;

    @Inject
    private AdminService adminService;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private MetricService metricService;
    @Inject
    private EventService eventService;
    @Inject
    private CalculationService calculationService;

    public InitializeApp() {
    }

    public void initApp() {

        // Property is set via Payara - server-config - System Properties
        String env = System.getProperty("rcs_environment") == null ? "" : System.getProperty("rcs_environment");

        try {
            if (true) {
                calculationService.initBusinessRules();
                metricService.initMetricTypes();

                return;
            }

            // Limited system init in production.
            if ("Prod".equals(env)) {
                Logger.getLogger(InitializeApp.class.getName()).log(Level.INFO, "PROD");
                return;
            }

            logger.info("Initializing App Objects");

            adminUser = adminService.initUsers("/resources/app_data_init_files/fls_user_init.txt");
            //adminService.initUsers2("/resources/app_data_init_files/fls_user_init2.txt");
            adminService.initCompanies();
            financialPeriodService.initFinancialPeriods(adminUser);
            adminService.initAccounts();
            metricService.initMetricTypes();
            adminService.initAccountsMapping();
            eventService.initEventTypes();
            adminService.initCountries();
            adminService.initBusinessUnit();
            adminService.initReportingUnits();
            adminService.initCoEtoParentRU();
            adminService.initCompaniesInRUs();

            /**
             * We'll probably rely on the initReportingUnits() above instead of this one, but need to decide.
             */
            //reportingUnitService.reportingUnitList();
            Company fls = adminService.findCompanyById("FLS");
            if (fls.getCurrentPeriod() == null) {
                fls.setCurrentPeriod(financialPeriodService.findById("SEP-18"));
            }

            adminService.initReportingUnitWorkflowContext();
            // Uncomment for local file based POB loading current month only.
            //contractService.initContracts();
            //pobService.initPOBs();
            //currencyService.initCurrencyConverter(financialPeriodService.findById("APR-18"));
            // end
        } catch (Exception ex) {
            Logger.getLogger(AppInitializeService.class.getName()).log(Level.SEVERE, null, ex);
        }

        logger.info("Initializing App Objects Done");
    }

    public User getAdminUser() {
        return adminUser;
    }
}
