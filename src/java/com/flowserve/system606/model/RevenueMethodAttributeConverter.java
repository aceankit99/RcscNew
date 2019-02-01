package com.flowserve.system606.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author kgraves
 */
@Converter(autoApply = true)
public class RevenueMethodAttributeConverter implements AttributeConverter<RevenueMethod, String> {

    @Override
    public String convertToDatabaseColumn(RevenueMethod revenueMethod) {
        if (revenueMethod == null) {
            return null;
        }

        return revenueMethod.getShortName();
    }

    @Override
    public RevenueMethod convertToEntityAttribute(String revenueMethod) {
        if (revenueMethod == null) {
            return null;
        }
        return RevenueMethod.fromShortName(revenueMethod);
    }
}
