/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.AttributeType;
import com.flowserve.system606.model.CurrencyType;
import com.flowserve.system606.model.MetricDirection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author constacloud
 */
@Stateless
public class AttributeService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;
    private static Map<String, AttributeType> attributeCodeCache = new HashMap<String, AttributeType>();

    @EJB
    private AdminService adminService;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public AttributeType getAttributeTypeByCode(String attributeCode) {
        if (attributeCodeCache.get(attributeCode) != null) {
            return attributeCodeCache.get(attributeCode);
        }

        return findAttributeTypeByCode(attributeCode);
    }

    public AttributeType findAttributeTypeByCode(String attributeCode) {
        Query query = em.createQuery("SELECT it FROM AttributeType it WHERE it.code = :IN");
        query.setParameter("IN", attributeCode);
        AttributeType attributeType = (AttributeType) query.getSingleResult();
        attributeCodeCache.put(attributeCode, attributeType);

        return attributeType;
    }

    public void initAttributeTypes() throws Exception {

        logger.info("Initializing AttributeTypes");
        BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/init_attribute_types.txt"), "UTF-8"));
        String metricCurrencyType = null;
        int count = 0;
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            count = 0;
            String[] values = line.split("\\|", 16);

            AttributeType attributeType = new AttributeType();
            attributeType.setDirection(MetricDirection.valueOf(values[count++]));
            attributeType.setCode(values[count++]);
            try {
                if (getAttributeTypeByCode(attributeType.getCode()) != null) {
                    continue;
                }
            } catch (Exception e) {
                Logger.getLogger(MetricService.class.getName()).log(Level.FINE, "Adding MetricType: " + line);
            }

            attributeType.setOwnerEntityType(values[count++]);
            attributeType.setRequired("REQUIRED".equals(values[count++]));
            attributeType.setMetricClass(values[count++] + "Version");
            metricCurrencyType = values[count++];
            attributeType.setMetricCurrencyType(metricCurrencyType == null || "".equals(metricCurrencyType) ? null : CurrencyType.fromShortName(metricCurrencyType));
            attributeType.setConvertible("Convertible".equals(values[count++]));
            attributeType.setName(values[count++]);
            attributeType.setDescription(values[count++]);
            attributeType.setExcelSheet(values[count++]);
            attributeType.setExcelCol(values[count++]);
            attributeType.setGroupName(values[count++]);
            attributeType.setGroupPosition(Integer.parseInt(values[count++]));
            attributeType.setEffectiveFrom(LocalDate.now());
            attributeType.setActive(true);
            //metricType.setAccount(adminService.findAccountById(values[count++]));
            logger.info("Creating attributeType: " + attributeType.getName());
            adminService.persist(attributeType);
        }

        reader.close();
        logger.info("Finished initializing attributeType.");
    }
}
