/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.CurrencyType;
import com.flowserve.system606.model.Event;
import com.flowserve.system606.model.EventType;
import com.flowserve.system606.model.MetricDirection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

@Stateless
public class EventService {

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    private static Map<String, EventType> eventCodeCache = new HashMap<String, EventType>();

    @EJB
    private AdminService adminService;

    private static Logger logger = Logger.getLogger("com.flowserve.system606");

    public List<EventType> findActiveEventTypes() {
        Query query = em.createQuery("SELECT et FROM EventType et WHERE et.active = TRUE");
        return (List<EventType>) query.getResultList();
    }

    public EventType findEventTypeById(Long id) {
        return em.find(EventType.class, id);
    }

    public EventType findEventTypeByName(String metricName) {
        Query query = em.createQuery("SELECT it FROM EventType it WHERE it.name = :IN");
        query.setParameter("IN", metricName);
        return (EventType) query.getSingleResult();  // we want an exception if not one and only one.
    }

    public EventType getEventTypeByCode(String eventCode) {
        if (eventCodeCache.get(eventCode) != null) {
            return eventCodeCache.get(eventCode);
        }

        return findEventTypeByCode(eventCode);
    }

    private EventType findEventTypeByCode(String eventCode) {
        Query query = em.createQuery("SELECT it FROM EventType it WHERE it.code = :IN");
        query.setParameter("IN", eventCode);
        EventType eventType = (EventType) query.getSingleResult();
        eventCodeCache.put(eventCode, eventType);

        return eventType;
    }

    public void initEventTypes() throws Exception {

        logger.info("Initializing EventTypes");
        BufferedReader reader = new BufferedReader(new InputStreamReader(AppInitializeService.class.getResourceAsStream("/resources/app_data_init_files/init_event_types.txt"), "UTF-8"));
        String eventCurrencyType = null;
        int count = 0;
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }

            count = 0;
            String[] values = line.split("\\|", 16);

            EventType eventType = new EventType();
            eventType.setDirection(MetricDirection.valueOf(values[count++]));
            eventType.setCode(values[count++]);
            try {
                if (getEventTypeByCode(eventType.getCode()) != null) {
                    continue;
                }
            } catch (Exception e) {
                Logger.getLogger(EventService.class.getName()).log(Level.FINE, "Adding EventType: " + line);
            }

            eventType.setOwnerEntityType(values[count++]);
            eventType.setRequired("REQUIRED".equals(values[count++]));
            eventType.setEventClass(values[count++]);
            eventCurrencyType = values[count++];
            eventType.setEventCurrencyType(eventCurrencyType == null || "".equals(eventCurrencyType) ? null : CurrencyType.fromShortName(eventCurrencyType));
            eventType.setConvertible("Convertible".equals(values[count++]));
            eventType.setName(values[count++]);
            eventType.setDescription(values[count++]);
            eventType.setExcelSheet(values[count++]);
            eventType.setExcelCol(values[count++]);
            eventType.setGroupName(values[count++]);
            eventType.setGroupPosition(Integer.parseInt(values[count++]));
            eventType.setEffectiveFrom(LocalDate.now());
            eventType.setActive(true);
            eventType.setCreditAccount(adminService.findAccountById(values[count++]));
            eventType.setDebitAccount(adminService.findAccountById(values[count++]));
            logger.info("Creating EventType: " + eventType.getName());
            adminService.persist(eventType);
        }

        reader.close();
        logger.info("Finished initializing EventTypes.");
    }

    public List<EventType> findEventType() throws Exception {

        TypedQuery<EventType> query = em.createQuery("SELECT b FROM EventType b", EventType.class);
        return (List<EventType>) query.getResultList();
    }

    public EventType update(EventType m) throws Exception {
        eventCodeCache.clear();

        return em.merge(m);
    }

    public void persist(Event event) throws Exception {
        em.persist(event);
    }

    public Event update(Event event) throws Exception {
        return em.merge(event);
    }

    public void remove(Event event) throws Exception {
        em.remove(event);
    }
}
