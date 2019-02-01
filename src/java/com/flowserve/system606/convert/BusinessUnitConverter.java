/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowserve.system606.convert;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.service.AdminService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@FacesConverter(value = "businessUnitConverter", managed = true)
public class BusinessUnitConverter implements Converter {

    AdminService adminService;

    public BusinessUnitConverter() {
    }

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || "".equals(value)) {
            return null;
        }

        try {
            InitialContext ic = new InitialContext();
            adminService = (AdminService) ic.lookup("java:global/rcscr/AdminService");
        } catch (NamingException ex) {
            Logger.getLogger(BusinessUnitConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return adminService.findBusinessUnitById(value);

    }

    public String getAsString(FacesContext fc, UIComponent uic, Object value) {
        if (value instanceof String) {
            return null;
        }

        return ((BusinessUnit) value).getId().toString();
    }
}
