package com.flowserve.system606.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author kgraves
 */
@Converter(autoApply = true)
public class PeriodStatusAttributeConverter implements AttributeConverter<PeriodStatus, String> {

    @Override
    public String convertToDatabaseColumn(PeriodStatus status) {
        return status.getShortName();
    }

    @Override
    public PeriodStatus convertToEntityAttribute(String status) {
        return PeriodStatus.fromShortName(status);
    }
}
