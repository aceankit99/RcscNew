/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.CurrencyType;
import com.flowserve.system606.model.MetricDirection;
import com.flowserve.system606.model.MetricGroup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author shubhamv
 */
@Stateless
public class MetricGroupService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    @Inject
    private AdminService adminService;
    @Inject
    private MetricService metricService;

    private static Map<String, MetricGroup> metricGroupCodeCache = new HashMap<String, MetricGroup>();

    public List<MetricGroup> findAllMetricGroups() {
        Query query = em.createQuery("SELECT it FROM MetricGroup it");
        List<MetricGroup> metricGroup = query.getResultList();
        return metricGroup;
    }

    public List<MetricGroup> findOutputMetricGroups() {
        Query query = em.createQuery("SELECT it FROM MetricGroup it Where it.direction = :DIR ORDER BY it.metricPosition");
        query.setParameter("DIR", MetricDirection.OUTPUT);
        List<MetricGroup> metricGroup = query.getResultList();
        return metricGroup;
    }

    public List<MetricGroup> findDistinctMetricGroups() {
        Query query = em.createQuery("SELECT it FROM MetricGroup it Where it.id IN (SELECT MIN(m.id) FROM MetricGroup m GROUP BY m.groupName) AND it.direction = :DIR ORDER BY it.groupPosition");
        query.setParameter("DIR", MetricDirection.OUTPUT);
        List<MetricGroup> metricTypeGroup = query.getResultList();
        return metricTypeGroup;
    }

    public List<MetricGroup> findInputMetricGroups() {
        Query query = em.createQuery("SELECT it FROM MetricGroup it Where it.direction = :DIR ORDER BY it.metricPosition");
        query.setParameter("DIR", MetricDirection.INPUT);
        List<MetricGroup> metricGroup = query.getResultList();
        return metricGroup;
    }

    public List<MetricGroup> findInputDistinctMetricGroups() {
        Query query = em.createQuery("SELECT it FROM MetricGroup it Where it.id IN (SELECT MIN(m.id) FROM MetricGroup m GROUP BY m.groupName) AND it.direction = :DIR ORDER BY it.groupPosition");
        query.setParameter("DIR", MetricDirection.INPUT);
        List<MetricGroup> metricTypeGroup = query.getResultList();
        return metricTypeGroup;
    }

    public void initMetricGroups() throws Exception {

        if (findAllMetricGroups().isEmpty()) {
            logger.info("Initializing MetricTypesGroup");
            BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/init_metric_group.txt"), "UTF-8"));
            String metricCurrencyType = null;
            int count = 0;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                count = 0;
                String[] values = line.split("\\|", 16);

                MetricGroup metricGroup = new MetricGroup();
                metricGroup.setDirection(MetricDirection.valueOf(values[count++]));
                metricGroup.setCode(values[count++]);
                metricGroup.setOwnerEntityType(values[count++]);
                metricCurrencyType = values[count++];
                metricGroup.setInputCurrencyType(metricCurrencyType == null || "".equals(metricCurrencyType) ? null : CurrencyType.fromShortName(metricCurrencyType));
                metricGroup.setColumnGroup(values[count++]);
                metricGroup.setGroupName(values[count++]);
                metricGroup.setGroupPosition(Integer.parseInt(values[count++]));
                metricGroup.setMetricPosition(Integer.parseInt(values[count++]));
                if (values[count++].equals("1")) {
                    metricGroup.setPrior(true);
                } else {
                    metricGroup.setPrior(false);
                }
                metricGroup.setActive(true);
                metricGroup.setMetricType(metricService.getMetricTypeByCode(metricGroup.getCode()));
                if (metricGroup.getDirection().equals(MetricDirection.OUTPUT)) {
                    metricGroup.setEditable(false);
                } else {
                    if (values[count++].equals("1")) {
                        metricGroup.setEditable(true);
                    } else {
                        metricGroup.setEditable(false);
                    }
                }
                logger.info("Creating MetricTypesGroup: " + metricGroup.getCode());
                adminService.persist(metricGroup);
            }

            reader.close();
            logger.info("Finished initializing MetricTypesGroup");
        }
    }

    public void persist(Object object) {
        em.persist(object);
    }
}
