package com.flowserve.system606.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author kgraves
 */
@Converter(autoApply = true)
public class CurrencyTypeAttributeConverter implements AttributeConverter<CurrencyType, String> {

    @Override
    public String convertToDatabaseColumn(CurrencyType currencyType) {
        if (currencyType == null) {
            return null;
        }

        return currencyType.getShortName();
    }

    @Override
    public CurrencyType convertToEntityAttribute(String currencyType) {
        if (currencyType == null) {
            return null;
        }
        return CurrencyType.fromShortName(currencyType);
    }
}
