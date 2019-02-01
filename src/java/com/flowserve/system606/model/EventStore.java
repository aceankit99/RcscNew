package com.flowserve.system606.model;

import java.util.List;

/**
 *
 * @author kgraves
 */
public interface EventStore {

    public Long getId();

    public List<Event> getAllEventsByEventType(EventType eventType);

    public List<Event> getAllEventsByPeriodAndEventType(FinancialPeriod period, EventType eventType);

    public EventList getPeriodEventList(FinancialPeriod period);

    public boolean eventListExistsForPeriod(FinancialPeriod period);

    public EventList initializeEventListForPeriod(FinancialPeriod period);
}
