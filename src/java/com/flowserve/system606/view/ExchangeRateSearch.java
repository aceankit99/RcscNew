/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.ExchangeRate;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named
@ViewScoped
public class ExchangeRateSearch implements Serializable {

    List<ExchangeRate> exchangeRates = new ArrayList<ExchangeRate>();

    @Inject
    AdminService adminService;
    private String searchString = "";

    public void search() throws Exception {
        exchangeRates = adminService.searchExchangeRates(searchString.replaceAll("\\s+", ""));
        Collections.sort(exchangeRates);
        //System.out.println("search()" + exchangeRates.get(0).getFinancialPeriod().getStartDate());
    }

    public List<ExchangeRate> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(List<ExchangeRate> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

}
