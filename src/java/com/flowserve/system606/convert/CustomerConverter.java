/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.convert;

import com.flowserve.system606.model.Customer;
import com.flowserve.system606.service.AdminService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author shubhamv
 */
@FacesConverter(value = "customerConverter", managed = true)
public class CustomerConverter implements Converter {

    private AdminService adminService;

    public CustomerConverter() {
    }

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent component, String value) {

        if (value == null || "".equals(value)) {
            return null;
        }

        try {
            InitialContext ic = new InitialContext();
            adminService = (AdminService) ic.lookup("java:global/rcscr/AdminService");
        } catch (NamingException ex) {
            Logger.getLogger(CustomerConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return adminService.findCustomerById(new Long(value));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent component, Object value) {

        if (value instanceof String) {
            return null;
        }
        return ((Customer) value).getId().toString();
    }
}
