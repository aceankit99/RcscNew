/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.time.LocalDate;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author shubhamv
 */
@Entity
@DiscriminatorValue("DATE")
@AttributeOverride(name = "value", column = @Column(name = "DATE_VALUE"))
public class DateMetricVersion extends Attribute<LocalDate> {

    private LocalDate value;

    public DateMetricVersion() {
    }

    public LocalDate getValue() {
        return value;
    }

    public void setValue(LocalDate value) {
        this.value = value;
    }
}
