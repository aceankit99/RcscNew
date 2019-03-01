/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author shubhamv
 */
@Singleton
@Startup
public class AppInitializeService {

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    User adminUser;

    @Inject
    private AdminService adminService;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    private MetricService metricService;
    @Inject
    private AttributeService attributeService;
    @Inject
    private EventService eventService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private MetricGroupService metricGroupService;
    @Inject
    private CalculationVersionService calculationVersionService;

    public AppInitializeService() {
    }

    @PostConstruct
    public void initApp() {

        // Property is set via Payara - server-config - System Properties
        String env = System.getProperty("rcs_environment") == null ? "" : System.getProperty("rcs_environment");

        try {
//            if (true) {
//                calculationService.initBusinessRules();
//                metricService.initMetricTypes();
//                metricGroupService.initMetricGroups();
//                return;
//            }
//
//            // Limited system init in production.
//            if ("Prod".equals(env)) {
//                Logger.getLogger(InitializeApp.class.getName()).log(Level.INFO, "PROD");
//                return;
//            }

            logger.info("Initializing App Objects");

            adminUser = adminService.initUsers("/resources/app_data_init_files/fls_user_init.txt");
            //adminService.initUsers2("/resources/app_data_init_files/fls_user_init2.txt");
            adminService.initCompanies();
            financialPeriodService.initFinancialPeriods(adminUser);
            adminService.initAccounts();
            metricService.initMetricTypes();
            metricService.initMetricTypes_1_2();
            attributeService.initAttributeTypes();
            adminService.initAccountsMapping();
            eventService.initEventTypes();
            adminService.initCountries();
            adminService.initBusinessUnit();
            adminService.initReportingUnits();
            adminService.initCoEtoParentRU();
            adminService.initCompaniesInRUs();
            adminService.initCustomers();
            metricGroupService.initMetricGroups();
            calculationService.initBusinessRules();
            //calculationVersionService.saveTest();
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
