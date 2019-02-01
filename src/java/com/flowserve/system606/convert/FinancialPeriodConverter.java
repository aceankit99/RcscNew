package com.flowserve.system606.convert;

import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.service.FinancialPeriodService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@FacesConverter(value = "periodConverter", managed = true)
public class FinancialPeriodConverter implements Converter {

    FinancialPeriodService financialPeriodService;

    public FinancialPeriodConverter() {
    }

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || "".equals(value)) {
            return null;
        }

        try {
            InitialContext ic = new InitialContext();
            financialPeriodService = (FinancialPeriodService) ic.lookup("java:global/rcscr/FinancialPeriodService");
        } catch (NamingException ex) {
            Logger.getLogger(FinancialPeriodConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return financialPeriodService.findById(value);

    }

    public String getAsString(FacesContext fc, UIComponent uic, Object value) {
        if (value instanceof String) {
            return null;
        }

        return ((FinancialPeriod) value).getId().toString();
    }
}
