package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.RevenueMethod;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author kgraves
 */
@Stateless
@Named
public class PerformanceObligationService {

    private static final Logger logger = Logger.getLogger(PerformanceObligationService.class.getName());

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;
    @EJB
    private ContractService contractService;

    public PerformanceObligation findById(Long id) {
        return em.find(PerformanceObligation.class, id);
    }

    public PerformanceObligation update(PerformanceObligation pob) throws Exception {
        return em.merge(pob);
    }

    public void persist(PerformanceObligation pob) {
        em.persist(pob);
    }

    public void delete(PerformanceObligation pob) throws Exception {
        if (!em.contains(pob)) {
            pob = em.merge(pob);
        }
        em.remove(pob);
    }

    public void testFromDrools(PerformanceObligation pob) {
        Logger.getLogger(PerformanceObligationService.class
                .getName()).log(Level.INFO, "Test log from Drools call. Pob ID: " + pob.getId());
    }

    public PerformanceObligation findPerformanceObligationById(Long id) {
        return em.find(PerformanceObligation.class, id);
    }

    public long getPobCount() {
        return (long) em.createQuery("SELECT COUNT(pob.id) FROM PerformanceObligation pob").getSingleResult();
    }

    public void overrideAllPObsValid(List<PerformanceObligation> pobs) throws Exception {
        for (PerformanceObligation pob : pobs) {
            if (pob.isValid() == false) {
                pob.setValid(true);
                update(pob);
            }
        }
    }

    public void initPOBs() throws Exception {

        //read init_contract_pob_data.txt a second time
        if (getPobCount() == 0) {
            logger.info("Initializing POBs");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class
                    .getResourceAsStream("/resources/app_data_init_files/init_contract_pob_data.txt"), "UTF-8"));

            String platform = null;
            String ru = null;
            long contractId = -1;
            String customerName = null;
            String salesOrderNumber = null;
            String pobName = null;
            long pobId = -1;
            String revRecMethod = null;

            int count = 0;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                count = 0;
                String[] values = line.split("\\t");

                PerformanceObligation pob = new PerformanceObligation();

                platform = values[count++].trim();
                ru = values[count++].trim().replace("RU", "");
                contractId = Long.valueOf(values[count++].trim());
                customerName = values[count++].trim();
                salesOrderNumber = values[count++].trim();
                pobName = values[count++].trim();
                pobId = Long.valueOf(values[count++].trim());
                pobName = pobName + " " + pobId;
                if (findPerformanceObligationById(contractId) != null) {
                    throw new IllegalStateException("Duplicte POBs in the file.  This should never happen.  POB ID: " + pobId);
                }
                revRecMethod = values[count++].trim();

                Contract contract = contractService.findContractById(contractId);

                if (contract == null) {
                    throw new IllegalStateException("POB refers to non-existent contract.  Invalid.  POB ID: " + pobId);
                }

                pob.setContract(contract);
                pob.setName(pobName);
                pob.setId(pobId);
                // for testing....
                pob.setRevenueMethod(RevenueMethod.PERC_OF_COMP);

                pob.setActive(true);
                //performanceObligationService.initializeInputs(pob);
                //performanceObligationService.initializeOutputs(pob);

                //persist(pob);
                //KJG
                pob = update(pob);
                contract.getPerformanceObligations().add(pob);
                contractService.update(contract);

            }

            reader.close();

            logger.info("Finished initializing POBs.");
        }
    }
}
