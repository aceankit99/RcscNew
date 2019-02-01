package com.flowserve.system606.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STRING")
@AttributeOverride(name = "value", column = @Column(name = "STRING_VALUE"))
public class StringMetricVersion extends Attribute<String> {

    private String value;

    public StringMetricVersion() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
