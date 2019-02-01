/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Account;
import com.flowserve.system606.model.AccountMapping;
import com.flowserve.system606.model.AccountType;
import com.flowserve.system606.model.BusinessRule;
import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.Company;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.Country;
import com.flowserve.system606.model.CurrencyEvent;
import com.flowserve.system606.model.Customer;
import com.flowserve.system606.model.DataImportFile;
import com.flowserve.system606.model.ExchangeRate;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Holiday;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.RevenueMethod;
import com.flowserve.system606.model.User;
import com.flowserve.system606.model.WorkflowAction;
import com.flowserve.system606.model.WorkflowContext;
import com.flowserve.system606.model.WorkflowStatus;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author shubhamv
 */
@Named
@Stateless
public class AdminService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    @Inject
    private FinancialPeriodService financialPeriodService;
    @Inject
    ContractService contractService;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public List<User> searchUsers(String searchString) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE UPPER(u.name) LIKE :NAME OR UPPER(u.flsId) LIKE :NAME ORDER BY UPPER(u.name)", User.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        return (List<User>) query.getResultList();
    }

    public List<BusinessUnit> findBusinessUnits() throws Exception {  // Need an application exception type defined.

        TypedQuery<BusinessUnit> query = em.createQuery("SELECT b FROM BusinessUnit b", BusinessUnit.class);
        return (List<BusinessUnit>) query.getResultList();
    }

    public List<BusinessUnit> getBusinessUnit(String searchString) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }
        Logger.getLogger(AdminService.class.getName()).log(Level.FINE, "Search" + searchString.toUpperCase());
        TypedQuery<BusinessUnit> query = em.createQuery("SELECT u FROM BusinessUnit u WHERE UPPER(u.name) LIKE :NAME ORDER BY UPPER(u.name)", BusinessUnit.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        return (List<BusinessUnit>) query.getResultList();
    }

    public BusinessUnit findBusinessUnitById(String id) {

        return em.find(BusinessUnit.class, id);
    }

    public List<DataImportFile> findDataImportFileByType(String type) throws Exception {  // Need an application exception type defined.

        TypedQuery<DataImportFile> query = em.createQuery("SELECT b FROM DataImportFile b  WHERE UPPER(b.type) = :TYPE ORDER BY b.uploadDate DESC", DataImportFile.class);
        query.setParameter("TYPE", type.toUpperCase());
        return (List<DataImportFile>) query.getResultList();
    }

    public void updateUser(User u) throws Exception {
        em.merge(u);
    }

    public void updateBusinessUnit(BusinessUnit u) throws Exception {
        em.merge(u);
    }

    public void persist(BusinessUnit bu) throws Exception {
        em.persist(bu);
    }

    public void persist(CurrencyEvent be) throws Exception {
        em.persist(be);
    }

    public CurrencyEvent update(CurrencyEvent be) throws Exception {
        return em.merge(be);
    }

    public void persist(Object object) {
        em.persist(object);
    }

    public void persist(WorkflowAction action) {
        em.persist(action);
    }

    public void flushAndClear() {
        em.flush();
        em.clear();
    }

    public List<Holiday> findHolidayList() throws Exception {  // Need an application exception type defined.

        TypedQuery<Holiday> query = em.createQuery("SELECT b FROM Holiday b", Holiday.class);
        return (List<Holiday>) query.getResultList();
    }

    public void updateHoliday(Holiday h) {
        em.merge(h);
    }

    public void deleteHoliday(Holiday h) throws Exception {
        if (!em.contains(h)) {
            h = em.merge(h);
        }
        em.remove(h);
    }

//    public List<User> findUserByFlsId(String adname) {
//        Query query = em.createQuery("SELECT u FROM User u WHERE UPPER(u.flsId) = :FLS_ID ORDER BY UPPER(u.id)");
//        query.setParameter("FLS_ID", adname.toUpperCase());
//        return (List<User>) query.getResultList();
//
//    }
    public User findUserByFlsId(String adname) {
        Query query = em.createQuery("SELECT u FROM User u WHERE UPPER(u.flsId) = :FLS_ID ORDER BY UPPER(u.id)");
        query.setParameter("FLS_ID", adname.toUpperCase());

        List<User> user = query.getResultList();
        if (user.size() > 0) {
            return user.get(0);
        }
        return null;

    }

    public User findUserById(Long id) {

        return em.find(User.class, id);
    }

    public Customer findCustomerById(Long id) {

        return em.find(Customer.class, id);
    }

    public ReportingUnit findReportingUnitById(Long id) {
        return em.find(ReportingUnit.class, id);
    }

    public Country findCountryById(String id) {
        return em.find(Country.class, id);
    }

    public Company findCompanyById(String id) {
        return em.find(Company.class, id);
    }

    public WorkflowStatus findWorkflowStatusById(String id) {
        return em.find(WorkflowStatus.class, id);
    }

    public MetricType findMetricTypeByCode(String code) {
        Query query = em.createQuery("SELECT m FROM MetricType m WHERE UPPER(m.code)= :code");
        query.setParameter("code", code.toUpperCase());
        List<MetricType> list = query.getResultList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public Account findAccountById(String id) {
        return em.find(Account.class, id);
    }

    public List<ReportingUnit> findAllReportingUnits() {
        Query query = em.createQuery("SELECT ru FROM ReportingUnit ru ORDER BY ru.code");
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<ReportingUnit> findAllActiveReportingUnits() {
        Query query = em.createQuery("SELECT ru FROM ReportingUnit ru WHERE ru.active = TRUE ORDER BY ru.code");
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<Long> findAllReportingUnitIds() {
        Query query = em.createQuery("SELECT ru.id FROM ReportingUnit ru");
        return (List<Long>) query.getResultList();
    }

    public ReportingUnit findReportingUnitByCode(String code) {
        Query query = em.createQuery("SELECT reportingUnit FROM ReportingUnit reportingUnit WHERE reportingUnit.code = :CODE");
        query.setParameter("CODE", code);
        List<ReportingUnit> reportingUnits = query.getResultList();
        if (reportingUnits.size() > 0) {
            return reportingUnits.get(0);
        }
        return null;
    }

    public ReportingUnit findReportingUnitByCOERole(String role) {
        Query query = em.createQuery("SELECT reportingUnit FROM ReportingUnit reportingUnit WHERE UPPER(reportingUnit.coeRole) = :ROLE");
        query.setParameter("ROLE", role.toUpperCase());
        List<ReportingUnit> reportingUnits = query.getResultList();
        if (reportingUnits.size() > 0) {
            return reportingUnits.get(0);
        }
        return null;
    }

    public ReportingUnit findPreparersByReportingUnitCode(String code) {
        Query query = em.createQuery("SELECT reportingUnit FROM ReportingUnit reportingUnit WHERE reportingUnit.code = :CODE");
        query.setParameter("CODE", code);
        List<ReportingUnit> reportingUnits = query.getResultList();
        List<User> user = reportingUnits.get(0).getPreparers();
        if (user.size() > 0) {
            return reportingUnits.get(0);
        }
        return null;
    }

    public ReportingUnit findBUByReportingUnitCode(String code) {
        Query query = em.createQuery("SELECT reportingUnit FROM ReportingUnit reportingUnit WHERE reportingUnit.code = :CODE");
        query.setParameter("CODE", code);
        List<ReportingUnit> reportingUnits = query.getResultList();
        BusinessUnit bu = reportingUnits.get(0).getBusinessUnit();
        if (bu != null) {
            return reportingUnits.get(0);
        }
        return null;
    }

    public ReportingUnit findReportingUnitById(String id) {
        return em.find(ReportingUnit.class, id);
    }

    public ReportingUnit findParentInReportingUnitCode(String code) {
        Query query = em.createQuery("SELECT reportingUnit FROM ReportingUnit reportingUnit WHERE reportingUnit.code = :CODE");
        query.setParameter("CODE", code);
        List<ReportingUnit> reportingUnits = query.getResultList();
        ReportingUnit bu = reportingUnits.get(0).getParentReportingUnit();
        if (bu != null) {
            return reportingUnits.get(0);
        }
        return null;
    }

    public Country findCountryByCode(String code) {
        Query query = em.createQuery("SELECT country FROM Country country WHERE country.code = :CODE");
        query.setParameter("CODE", code);
        List<Country> countries = query.getResultList();
        if (countries.size() > 0) {
            return countries.get(0);
        }
        return null;
    }

    public Country findCountryByName(String name) {
        Query query = em.createQuery("SELECT country FROM Country country WHERE country.name = :NAME OR country.code = :NAME");
        query.setParameter("NAME", name);
        List<Country> countries = query.getResultList();
        if (countries.size() > 0) {
            return countries.get(0);
        }
        return null;
    }

    public void persist(WorkflowContext ar) throws Exception {
        em.persist(ar);
    }

    public void persist(MetricType inputType) throws Exception {
        em.persist(inputType);
    }

    public void persist(Country country) throws Exception {
        em.persist(country);
    }

    public void persist(DataImportFile importFile) throws Exception {
        em.persist(importFile);
    }

    public void jpaEvictAllCache() {
        em.getEntityManagerFactory().getCache().evictAll();
        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "JPA cache cleared.");
    }

    public ReportingUnit crateReportingUnit(ReportingUnit ru) throws Exception {
        ru.setLocalCurrency(Currency.getInstance(new Locale("en", ru.getCountry().getCode())));
        ru.setCompany(findCompanyById("FLS"));
        persist(ru);

        for (FinancialPeriod period : financialPeriodService.findAllPeriods()) {
            if (ru.getWorkflowContext(period) == null) {
                WorkflowContext ar = new WorkflowContext();
                ar.setReportingUnit(ru);
                ar.setFinancialPeriod(period);
                ar.setWorkflowStatus(WorkflowStatus.INITIALIZED);
                ru.putPeriodWorkflowContext(period, ar);
                update(ru);
            }
        }

        return ru;
    }

    public void persist(ReportingUnit ru) throws Exception {
        em.persist(ru);
    }

    public void update(List<ReportingUnit> rus) throws Exception {
        for (ReportingUnit ru : rus) {
            update(ru);
        }
    }

    public ReportingUnit update(ReportingUnit ru) throws Exception {
        Logger.getLogger(AdminService.class.getName()).log(Level.FINER, "Updating RU: " + ru.getCode());
        return em.merge(ru);
    }

    public Company updateCompany(Company c) throws Exception {
        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Updating Company: " + c.getId());
        return em.merge(c);
    }

    public Company update(Company c) throws Exception {
        return em.merge(c);
    }

    public void persist(User u) throws Exception {
        em.persist(u);
    }

    public User update(User u) throws Exception {
        return em.merge(u);
    }

    public void update(Country country) throws Exception {
        em.merge(country);
    }

    public List<ReportingUnit> searchReportingUnits(String searchString) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            return new ArrayList<ReportingUnit>();
        }
        TypedQuery<ReportingUnit> query = em.createQuery("SELECT ru  FROM ReportingUnit ru WHERE (UPPER(ru.name) LIKE :NAME OR UPPER(ru.code) LIKE :NAME)  AND ru.active = :ACTIVE ORDER BY UPPER(ru.name)", ReportingUnit.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        query.setParameter("ACTIVE", true);
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<ReportingUnit> searchReportingUnitsForAdmin(String searchString) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            return new ArrayList<ReportingUnit>();
        }
        TypedQuery<ReportingUnit> query = em.createQuery("SELECT ru  FROM ReportingUnit ru WHERE UPPER(ru.name) LIKE :NAME OR UPPER(ru.code) LIKE :NAME  ORDER BY UPPER(ru.name)", ReportingUnit.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        return (List<ReportingUnit>) query.getResultList();
    }

    public List<ReportingUnit> parentReportingUnits(String searchString, ReportingUnit ru) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }
        TypedQuery<ReportingUnit> query = em.createQuery("SELECT ru  FROM ReportingUnit ru WHERE ru.id != :ID AND (UPPER(ru.name) LIKE :NAME OR UPPER(ru.code) LIKE :NAME) ORDER BY UPPER(ru.name)", ReportingUnit.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        query.setParameter("ID", ru.getId());

        return (List<ReportingUnit>) query.getResultList();
    }

    public List<BusinessUnit> searchSites(String searchString) throws Exception {
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }

        TypedQuery<BusinessUnit> query = em.createQuery("SELECT s FROM BusinessUnit s WHERE UPPER(s.name) LIKE :NAME order by UPPER(s.name)", BusinessUnit.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        return (List<BusinessUnit>) query.getResultList();
    }

    public List<BusinessUnit> searchParentBu(String searchString, BusinessUnit bu) throws Exception {
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }

        TypedQuery<BusinessUnit> query = em.createQuery("SELECT s FROM BusinessUnit s WHERE s.id != :ID AND UPPER(s.name) LIKE :NAME order by UPPER(s.name)", BusinessUnit.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        query.setParameter("ID", bu.getId());
        return (List<BusinessUnit>) query.getResultList();
    }

    public List<BusinessUnit> allBusinessUnit() throws Exception {
        TypedQuery<BusinessUnit> query = em.createQuery("SELECT c  FROM BusinessUnit c ORDER BY UPPER(c.name)", BusinessUnit.class);

        return (List<BusinessUnit>) query.getResultList();
    }

    public List<Company> searchCompany(String searchString) throws Exception {
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }

        TypedQuery<Company> query = em.createQuery("SELECT s FROM Company s WHERE UPPER(s.name) LIKE :NAME OR UPPER(s.id) LIKE :NAME order by UPPER(s.name)", Company.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        Logger.getLogger(AdminService.class.getName()).log(Level.FINE, "searchSites:" + query.toString());
        return (List<Company>) query.getResultList();
    }

    public List<Country> AllCountry() throws Exception {
        TypedQuery<Country> query = em.createQuery("SELECT c  FROM Country c ORDER BY UPPER(c.name)", Country.class);

        return (List<Country>) query.getResultList();
    }

    public List<User> findByStartsWithLastName(String searchname) {
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE UPPER(u.name) LIKE :NAME OR UPPER(u.flsId) LIKE :NAME ORDER BY UPPER(u.name)", User.class);
        query.setParameter("NAME", "%" + searchname.toUpperCase() + "%");
        return (List<User>) query.getResultList();
    }

    public List<Customer> findCustomerByStartsWithName(String searchname) {
        TypedQuery<Customer> query = em.createQuery("SELECT u FROM Customer u WHERE UPPER(u.name) LIKE :NAME OR UPPER(u.legalName) LIKE :NAME ORDER BY UPPER(u.name)", Customer.class);
        query.setParameter("NAME", "%" + searchname.toUpperCase() + "%");
        return (List<Customer>) query.getResultList();
    }

    public User initUsers(String filename) throws Exception {
        logger.info("Clearing the entity cache.");
        em.flush();
        em.clear();
        logger.info("Finished clearing the entity cache.");

        User admin = findUserByFlsId("rcs_admin");
        if (admin == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Creating admin user");
            admin = new User();
            admin = new User("rcs_admin", "RCS Admin", "RCS Admin", "rcs_admin@flowserve.com");
            admin.setAdmin(true);
            admin.setLocale("en_US");
            admin.setTitle("Full Admin Access");
            persist(admin);
        }

        admin = findUserByFlsId("rcs_admin");

        if (findUserByFlsId("aloeffler") == null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream(filename), "UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split("\\t");
                if (values.length == 7 && !values[6].equalsIgnoreCase("ORG_LEVEL")) {
                    String flsId = values[0];
                    String displayName = values[1];
                    String commonNameLDAP = values[2];
                    String emailAddress = values[3];
                    String officeName = values[4];
                    String title = stripTitle(values[5]);
                    int orgLevel = Integer.parseInt(values[6]);
                    User user = new User(flsId, displayName, commonNameLDAP, emailAddress, officeName, title, orgLevel);
                    user.setLocale("en_US");
                    persist(user);
                }
            }
            reader.close();
            //this.initSupervisor();
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing users.");
        }

        setUserAsAdmin("50042974"); // wells
        setUserAsAdmin("50043609"); // pots
        setUserAsAdmin("50052748"); //mason
        setUserAsAdmin("kgraves");
        setUserAsAdmin("bvelasco");
        setUserAsAdmin("50041124");  // moeliing
        setUserAsAdmin("50047215");  // Vince

        return admin;
    }

    public void initUsers2(String filename) throws Exception {
        logger.info("Clearing the entity cache.");
        em.flush();
        em.clear();
        logger.info("Finished clearing the entity cache.");

        if (findUserByFlsId("10156942") == null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream(filename), "UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split("\\t");
                if (values.length == 7 && !values[6].equalsIgnoreCase("ORG_LEVEL")) {
                    String flsId = values[0];
                    String displayName = values[1];
                    String commonNameLDAP = values[2];
                    String emailAddress = values[3];
                    String officeName = values[4];
                    String title = stripTitle(values[5]);
                    int orgLevel = Integer.parseInt(values[6]);
                    User user = new User(flsId, displayName, commonNameLDAP, emailAddress, officeName, title, orgLevel);
                    user.setLocale("en_US");
                    Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Adding user: " + displayName);
                    persist(user);
                }
            }
            reader.close();
            //this.initSupervisor();
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing users.");
        }
    }

    private void setUserAsAdmin(String flsId) throws Exception {
        User user = findUserByFlsId(flsId);
        if (user != null) {
            if (!user.isAdmin()) {
                user.setAdmin(true);
                update(user);
            }
        }
    }

    private String stripTitle(String title) {
        if (title == null) {
            return null;
        }
        if (!title.contains("_")) {
            return title;
        }
        title = title.replaceAll("_", " ");
        if (title.length() > 9) {
            title = title.substring(9);
        }
        if (title.contains("Level")) {
            title = title.substring(0, title.indexOf("Level"));
        }

        return title;
    }

    public void initSupervisor() throws Exception {
        BufferedReader breader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/fls_supervisor_init.txt"), "UTF-8"));

        String line = null;
        while ((line = breader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            String[] values = line.split("\\t");
            User us = findUserByFlsId(values[0]);
            if (us != null) {
                us.setSupervisor(findUserByFlsId(values[1]));
                updateUser(us);
            }
        }
        breader.close();
    }

    public void initReportingUnits() throws Exception {

        if (findReportingUnitByCode("9999") == null) {
            ReportingUnit ru = new ReportingUnit();
            ru.setActive(true);
            ru.setCode("9999");
            ru.setCompany(findCompanyById("FLS"));
            ru.setDescription("CEO");
            ru.setLocalCurrency(Currency.getInstance("USD"));
            persist(ru);
        }

        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing Reporting Units");
        BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/reporting_units.txt"), "UTF-8"));

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            String[] values = line.split("\\t");
            String coeRole = null;
            if (values.length > 8) {
                coeRole = "SS" + values[7];
            }
            if (findReportingUnitByCode(values[1].trim()) != null) {
                continue;
            }
            //Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "RU line: " + line);
            ReportingUnit ru = new ReportingUnit();
            ru.setCode(values[1].trim());
            ru.setName(ru.getCode() + " " + values[0]);
            ru.setLocalCurrency(Currency.getInstance(values[2]));
            ru.setRegion(values[4]);
            ru.setCoeRole(coeRole);
            ru.setActive(true);
            Country cn = findCountryByCode(values[5]);
            if (cn != null) {
                ru.setCountry(cn);
            }
            persist(ru);
            if (cn != null) {
                ReportingUnit addRU = findReportingUnitByCode(values[1].trim());
                cn.getReportingUnit().add(addRU);
                update(cn);
            }
            ru = findReportingUnitByCode(values[1].trim());
            BusinessUnit bu = findBusinessUnitById(values[3]);
            ru.setBusinessUnit(bu);
            bu.getReportingUnit().add(ru);
            update(ru);
            updateBusinessUnit(bu);
        }
        reader.close();
        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Reporting Units.");
    }

    public void initReportingUnitWorkflowContext() throws Exception {
        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "initReportingUnitWorkflowStatus()");
        for (FinancialPeriod period : financialPeriodService.findAllPeriods()) {
            if (period.isClosed()) {
                for (ReportingUnit ru : findAllReportingUnits()) {
                    if (ru.isActive() && ru.getWorkflowContext(period) == null) {
                        WorkflowContext ar = new WorkflowContext();
                        ar.setReportingUnit(ru);
                        ar.setFinancialPeriod(period);
                        ar.setWorkflowStatus(WorkflowStatus.APPROVED);
                        ru.putPeriodWorkflowContext(period, ar);
                        update(ru);
                    }
                }
            }
            if (period.isOpen()) {
                for (ReportingUnit ru : findAllReportingUnits()) {
                    if (ru.isActive() && ru.getWorkflowContext(period) == null) {
                        WorkflowContext ar = new WorkflowContext();
                        ar.setReportingUnit(ru);
                        ar.setFinancialPeriod(period);
                        ar.setWorkflowStatus(WorkflowStatus.INITIALIZED);
                        ru.putPeriodWorkflowContext(period, ar);
                        update(ru);
                    }
                }
            }
        }
        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initReportingUnitWorkflowStatus()");
    }

    public void initBusinessUnit() throws Exception {
        if (findBusinessUnitById("AMSS") == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing Business Units");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/business_units.txt"), "UTF-8"));
            List<BusinessUnit> bunit = new ArrayList<BusinessUnit>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split("\\t");
                BusinessUnit bu = new BusinessUnit();
                bu.setId(values[0]);
                bu.setName(values[0]);
                bu.setType("Platform");
                bu.setCompany(findCompanyById("FLS"));
                persist(bu);
                Company cm = findCompanyById("FLS");

                bunit.add(findBusinessUnitById(values[0]));
                cm.setBusinessUnit(bunit);
                updateCompany(cm);

            }
            reader.close();
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Business Units.");
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Business Units." + findCompanyById("FLS").getBusinessUnit().get(0).getId());
        }
    }

    public void initBUinRU() throws Exception {
        if (findBUByReportingUnitCode("8000") == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "initBUinRU");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/business_unit_reporting.txt"), "UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split("\\t");
                if (values.length == 3) {
                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    if (ru != null) {
                        BusinessUnit bu = findBusinessUnitById(values[2]);
                        ru.setBusinessUnit(bu);
                        bu.getReportingUnit().add(ru);
                        update(ru);
                        updateBusinessUnit(bu);
                    }

                }
            }
            reader.close();
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initBUinRU.");
        }
    }

    public void initCoEtoParentRU() throws Exception {
        if (findParentInReportingUnitCode("8000") == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "initCoEtoParentRU");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/reporting_units.txt"), "UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] values = line.split("\\t");
                if (values.length > 8) {
                    //Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "message" + values[6]);
                    continue;
                } else {
                    ReportingUnit childRU = findReportingUnitByCode(values[1]);
                    ReportingUnit parentRU = findReportingUnitByCOERole(values[7]);
                    if (parentRU != null) {
                        childRU.setParentReportingUnit(parentRU);
                        parentRU.getChildReportingUnits().add(childRU);
                        update(childRU);
                        update(parentRU);
                    }

                }

            }
            reader.close();
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "initCoEtoParentRU");
        }
    }

    public void initAccounts() throws Exception {
        logger.info("Initializing SubLedgerAccounts");
        BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/init_sl_accounts.txt"), "UTF-8"));
        //  BufferedReader reader2 = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/init_sl_accounts.txt"), "UTF-8"));
        int count = 0;
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            count = 0;
            String[] values = line.split("\\|");
            if (findAccountById(values[count]) == null) {
                if ("null".equals(values[5].trim())) {
                    Account ledger = new Account();
                    ledger.setId(values[count++]);
                    ledger.setAccountType(AccountType.valueOf(values[count++]));
                    ledger.setDescription(values[count++]);
                    ledger.setName(values[count++]);
                    ledger.setCompany(findCompanyById(values[count++]));
                    ledger.setOffsetAccount(findAccountById(values[count++]));
                    persist(ledger);
                }
            }
        }
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/init_sl_accounts.txt"), "UTF-8"));
        int count2 = 0;
        String line2 = null;

        while ((line2 = reader2.readLine()) != null) {
            if (line2.trim().length() == 0) {
                continue;
            }

            count2 = 0;
            String[] values = line2.split("\\|");
            if (findAccountById(values[count2]) == null) {
                if (!"null".equals(values[5].trim())) {
                    Account ledger = new Account();
                    ledger.setId(values[count2++]);
                    ledger.setAccountType(AccountType.valueOf(values[count2++]));
                    ledger.setDescription(values[count2++]);
                    ledger.setName(values[count2++]);
                    ledger.setCompany(findCompanyById(values[count2++]));
                    ledger.setOffsetAccount(findAccountById(values[count2++]));
                    persist(ledger);
                }
            }
        }
        reader.close();
        reader2.close();
        logger.info("Finished initializing SubLedgerAccounts");
    }

    public void initAccountsMapping() throws Exception {

        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing Accounts Mapping");
        BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/metric_account_mappings.txt"), "UTF-8"));
        String line = null;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            count = 0;
            String[] values = line.split("\\|");
            if (findAccountMappingByMetricType(values[0]) == null) {
                Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Mapping line: " + line);
                AccountMapping acm = new AccountMapping();
                acm.setMetricType(findMetricTypeByCode(values[count++]));
                acm.setOwnerEntityType(values[count++]);
                acm.setRevenueMethod(RevenueMethod.fromShortName(values[count++]));
                acm.setAccount(findAccountById(values[count++]));
                String info = values[count++];
                if (info != null) {
                    acm.setInformational("INFORMATIONAL".equals(info));
                }
                persist(acm);
            }
        }
        reader.close();
        Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Accounts Mapping.");
    }

    public void initPreparersReviewerForRU() throws Exception {

        if (findPreparersByReportingUnitCode("8000") == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing Reporting Units Preparers");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/preparers_list.txt"), "UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                String[] values = line.split("\\t");
                if (values.length > 4 && values[4].equalsIgnoreCase("Preparer")) {

                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    User user = findUserByFlsId(values[2]);
                    if (ru != null && user != null && !ru.getPreparers().contains(user)) {
                        ru.getPreparers().add(user);
                        update(ru);
                    }

//                    String[] code = values[6].split(",");
//                    User user = findUserByFlsIdType(values[0]);
//                    int len = code.length;
//                    for (int i = 0; i < len; i++) {
//                        ReportingUnit ru = findReportingUnitByCode(code[i]);
//                        if (ru != null && user != null) {
//                            ru.getPreparers().add(user);
//                            update(ru);
//                        }
//
//                    }
                } else if (values.length > 4 && values[4].equalsIgnoreCase("Approver")) {

                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    User user = findUserByFlsId(values[2]);
                    if (ru != null && user != null && !ru.getApprovers().contains(user)) {
                        ru.getApprovers().add(user);
                        update(ru);
                    }

                } else if (values.length > 4 && values[4].equalsIgnoreCase("Reviewer")) {

                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    User user = findUserByFlsId(values[2]);
                    if (ru != null && user != null && !ru.getReviewers().contains(user)) {
                        ru.getReviewers().add(user);
                        update(ru);
                    }

                } else if (values.length > 4 && values[4].equalsIgnoreCase("Viewer")) {

                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    User user = findUserByFlsId(values[2]);
                    if (ru != null && user != null && !ru.getViewers().contains(user)) {
                        ru.getViewers().add(user);
                        update(ru);
                    }

                }

            }

            reader.close();

            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Reporting Units.");
        }
    }

    public void initPreparersReviewerForCOE() throws Exception {

        if (findPreparersByReportingUnitCode("CoE 1") == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing COE for Preparers and Approvers");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/coe.txt"), "UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                String[] values = line.split("\\t");
                if (values.length > 2 && values[2].equalsIgnoreCase("Preparer")) {

                    User user = findUserByFlsId(values[1]);
                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    if (ru != null && user != null && !ru.getPreparers().contains(user)) {
                        Logger.getLogger(AdminService.class.getName()).log(Level.FINER, "adding Preparer " + user.getFlsId());
                        ru.getPreparers().add(user);
                        update(ru);
                    }

                } else if (values.length > 2 && values[2].equalsIgnoreCase("Reviewer")) {

                    User user = findUserByFlsId(values[1]);
                    ReportingUnit ru = findReportingUnitByCode(values[0]);
                    if (ru != null && user != null && !ru.getApprovers().contains(user)) {
                        Logger.getLogger(AdminService.class.getName()).log(Level.FINER, "adding Reviewer " + user.getFlsId());
                        ru.getApprovers().add(user);
                        update(ru);
                    }

                }

            }

            reader.close();

            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing COEs.");
        }
    }

    public void initCompaniesInRUs() throws Exception {

        if (findReportingUnitByCode("8000").getCompany() == null) {
            Company cm = findCompanyById("FLS");
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "initializing Assign Company to RU");
            List<ReportingUnit> reportingUnits = findAllReportingUnits();
            for (ReportingUnit ru : reportingUnits) {
                cm.getReportingUnit().add(ru);
                ru.setCompany(cm);
                update(ru);
                update(cm);
            }
        }

    }

    public void initCompanies() throws Exception {
        if (findCompanyById("FLS") == null) {
            Company fls = new Company();
            fls.setId("FLS");
            fls.setName("Flowserve");
            fls.setDescription("Flowserve");
            fls.setInputFreezeWorkday(15);
            fls.setReportingCurrency(Currency.getInstance(new Locale("en", "US")));
            fls.setPociDueWorkday(10);
            fls.setExpandPobThreshold(0);
            persist(fls);
        }
    }

//    public void initBilings() throws Exception {
//        Contract ct = contractService.findContractById(new Long(3822));
//        if (findBillingEvents().isEmpty()) {
//            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "initBilings");
//            BillingEvent be = new BillingEvent();
//            be.setAmountContractCurrency(new BigDecimal(50));
//            be.setAmountLocalCurrency(new BigDecimal(50));
//            be.setBillingDate(LocalDate.now());
//            be.setContract(ct);
//            be.setDeliveryDate(LocalDate.now());
//            be.setInvoiceNumber("1234");
//            persist(be);
//        }
//    }
    public void initCountries() throws Exception {
        if (findCountryById("USA") == null) {
            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Initializing Countries");

            String[] countryCodes = Locale.getISOCountries();
            for (String countryCode : countryCodes) {

                Locale locale = new Locale("", countryCode);
                Country country = new Country(locale.getISO3Country(), locale.getCountry(), locale.getDisplayCountry());
                persist(country);
            }

            Logger.getLogger(AdminService.class.getName()).log(Level.INFO, "Finished initializing Countries.");
        }
    }

    public List<ExchangeRate> searchExchangeRates(String searchString) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }
        TypedQuery<ExchangeRate> query = em.createQuery("SELECT er FROM ExchangeRate er WHERE er.financialPeriod = :PERIOD", ExchangeRate.class);
        query.setParameter("PERIOD", financialPeriodService.findById(searchString.toUpperCase()));
        return (List<ExchangeRate>) query.getResultList();
    }

    public ExchangeRate findExchangeRatesByFinancialPeriod(FinancialPeriod fp) {
        Query query = em.createQuery("SELECT er FROM ExchangeRate er WHERE er.financialPeriod = :fPeriod");
        query.setParameter("fPeriod", fp);

        List<ExchangeRate> er = query.getResultList();
        if (er.size() > 0) {
            return er.get(0);
        }
        return null;

    }

    public ExchangeRate findExchangeRateByCurrenciesAndPeriod(Currency frmCurrency, Currency tCurrency, FinancialPeriod fp) {
        Query query = em.createQuery("SELECT er FROM ExchangeRate er WHERE er.fromCurrency = :fromCurrency AND er.toCurrency = :toCurrency AND er.financialPeriod = :fPeriod");
        query.setParameter("fromCurrency", frmCurrency);
        query.setParameter("toCurrency", tCurrency);
        query.setParameter("fPeriod", fp);

        List<ExchangeRate> er = query.getResultList();
        if (er.size() > 0) {
            return er.get(0);
        }
        return null;

    }

//    public List<ReportingUnit> getPreparableReportingUnits() {   // TODO - Move this to UserService.
//        // TODO - figure out logged in user.
//        List<ReportingUnit> rus = new ArrayList<ReportingUnit>();
//        rus.add(findReportingUnitByCode("1225"));
//        //rus.add(findReportingUnitByCode("1100"));
//        rus.add(findReportingUnitByCode("8025"));
//
//        return rus;
//    }
    public List<Company> findAllCompany() throws Exception {  // Need an application exception type defined.

        TypedQuery<Company> query = em.createQuery("SELECT c FROM Company c", Company.class);
        return (List<Company>) query.getResultList();
    }

    public List<Contract> searchContract(String searchString) throws Exception {  // Need an application exception type defined.
        if (searchString == null || searchString.trim().length() < 2) {
            throw new Exception("Please supply a search string with at least 2 characters.");
        }
        TypedQuery<Contract> query = em.createQuery("SELECT c FROM Contract c WHERE UPPER(c.name) LIKE :NAME ORDER BY UPPER(c.name)", Contract.class);
        query.setParameter("NAME", "%" + searchString.toUpperCase() + "%");
        return (List<Contract>) query.getResultList();
    }

    public List<BusinessRule> findAllBusinessRules() throws Exception {
        Query query = em.createQuery("SELECT bu FROM BusinessRule bu", BusinessRule.class);

        return (List<BusinessRule>) query.getResultList();
    }

    public AccountMapping findAccountMappingByMetricType(String code) {
        Query query = em.createQuery("SELECT a FROM AccountMapping a WHERE a.metricType  = :ID");
        query.setParameter("ID", findMetricTypeByCode(code));

        List<AccountMapping> am = query.getResultList();
        if (am.size() > 0) {
            return am.get(0);
        }
        return null;

    }

}
