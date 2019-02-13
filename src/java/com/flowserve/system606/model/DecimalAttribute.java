package com.flowserve.system606.model;

import java.math.BigDecimal;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DECIMAL")
@AttributeOverride(name = "value", column = @Column(name = "DECIMAL_VALUE", precision = 38, scale = 14))
public class DecimalAttribute extends Attribute<BigDecimal> {

    private BigDecimal value;

    public DecimalAttribute() {
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

}
