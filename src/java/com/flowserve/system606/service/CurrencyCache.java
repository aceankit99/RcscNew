/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.ExchangeRate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author kgraves
 */
@Singleton
public class CurrencyCache {

    @Inject
    private CurrencyService currencyService;
    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    private Map<String, ExchangeRate> exchangeRateCache = new HashMap<String, ExchangeRate>();

    @PostConstruct
    public void init() {
        try {
            exchangeRateCache.clear();
            Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Loading Fx Rates Cache. Start");
            for (ExchangeRate rate : currencyService.findAllRates()) {
                String cacheKey = rate.getFinancialPeriod().getId() + rate.getFromCurrency().getCurrencyCode() + rate.getToCurrency().getCurrencyCode();
                em.detach(rate);

                if (exchangeRateCache.get(cacheKey) == null) {
                    exchangeRateCache.put(cacheKey, rate);
                }
            }
            Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Loading Fx Rates Cache. End.");
        } catch (Exception ex) {
            Logger.getLogger(CurrencyService.class.getName()).log(Level.SEVERE, "Unable to pre-cache rates.", ex);
        }
    }

    public ExchangeRate getExchangeRate(String cacheKey) {
        return exchangeRateCache.get(cacheKey);
    }

    public void putExchangeRate(String cacheKey, ExchangeRate rate) {
        exchangeRateCache.put(cacheKey, rate);
    }

    public void clear() {
        exchangeRateCache.clear();
    }
}
