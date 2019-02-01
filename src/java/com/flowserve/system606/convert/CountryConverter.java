/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.convert;

/**
 *
 * @author shubhamv
 */
import com.flowserve.system606.model.Country;
import com.flowserve.system606.service.AdminService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@FacesConverter(value = "countryConverter", managed = true)
public class CountryConverter implements Converter {

    private AdminService adminService;

    public CountryConverter() {
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
            Logger.getLogger(UserConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return adminService.findCountryById(value);
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent component, Object value) {

        if (value instanceof String) {
            return null;
        }
        return ((Country) value).getId().toString();
    }

}
