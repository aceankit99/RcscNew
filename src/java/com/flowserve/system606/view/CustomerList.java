/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Customer;
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
 * @author constacloud
 */
@Named(value = "customerList")
@ViewScoped
public class CustomerList implements Serializable {

    List<Customer> customers = new ArrayList<Customer>();
    //List<Country> country = new ArrayList<Country>();
    @Inject
    private AdminService adminService;
    private String searchString = "";

    public CustomerList() {
    }

    public void search() throws Exception {

        customers = adminService.findCustomerByStartsWithName(searchString);
        Collections.sort(customers);

    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

}
