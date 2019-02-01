package com.flowserve.system606.model;

import java.time.LocalDate;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DATE")
@AttributeOverride(name = "value", column = @Column(name = "DATE_VALUE"))
public class DateMetric extends Metric<LocalDate> {

    private LocalDate value;

    public DateMetric() {
    }

    public LocalDate getValue() {
        return value;
    }

    public void setValue(LocalDate value) {
        this.value = value;
    }
}
