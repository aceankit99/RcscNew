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

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.service.ContractService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@FacesConverter(value = "contractConverter", managed = true)
public class ContractConverter implements Converter {

    ContractService contractService;

    public ContractConverter() {
    }

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || "".equals(value)) {
            return null;
        }

        try {
            InitialContext ic = new InitialContext();
            contractService = (ContractService) ic.lookup("java:global/rcscr/ContractService");
        } catch (NamingException ex) {
            Logger.getLogger(ContractConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contractService.findContractById(new Long(value));

    }

    public String getAsString(FacesContext fc, UIComponent uic, Object value) {
        if (value instanceof String) {
            return null;
        }

        return ((Contract) value).getId().toString();
    }
}
