/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Metric;
import com.flowserve.system606.model.MetricSet;
import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.model.User;
import com.flowserve.system606.model.WorkflowStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

/**
 *
 * @author span
 */
@Stateless
public class ContractService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public long getContractCount() {
        return (long) em.createQuery("SELECT COUNT(c.id) FROM Contract c").getSingleResult();
    }

    public Contract findContractById(Long id) {
        return em.find(Contract.class, id);
    }

    public List<MetricType> findInputTypes() {
        Query query = em.createQuery("SELECT inputType FROM InputType inputType");
        return (List<MetricType>) query.getResultList();
    }

    public void persist(Metric input) throws Exception {
        em.persist(input);
    }

    @Transactional
    public void persist(MetricSet inputSet) throws Exception {
        em.persist(inputSet);
    }

    public MetricType findInputTypeById(String id) {
        return em.find(MetricType.class, id);
    }

    public void persist(Contract contract) throws Exception {
        em.persist(contract);
    }

    public Contract update(Contract contract) throws Exception {
        // contract.setLastUpdateDate(LocalDateTime.now());
        return em.merge(contract);
    }

    public void delete(Contract contract) throws Exception {
        if (!em.contains(contract)) {
            contract = em.merge(contract);
        }
        em.remove(contract);
    }

    public void submitForReview(FinancialPeriod period, Contract contract, User user) throws Exception {
        contract.getPeriodApprovalRequest(period).setWorkflowStatus(WorkflowStatus.PREPARED);
        contract.setLastUpdateDate(LocalDateTime.now());
        contract.setLastUpdatedBy(user);

        update(contract);
    }
}
