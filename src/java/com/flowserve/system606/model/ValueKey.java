/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

import java.util.Objects;

/**
 *
 * @author shubhamv
 */
public class ValueKey {

    private MetricType metricCode;
    private FinancialPeriod period;
    private Long pID;

    public ValueKey(MetricType metricCode, FinancialPeriod period, Long pID) {
        this.metricCode = metricCode;
        this.period = period;
        this.pID = pID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValueKey)) {
            return false;
        }
        ValueKey that = (ValueKey) obj;
        return (this.metricCode.equals(that.metricCode))
                && (this.period.equals(that.period))
                && (this.pID.equals(that.pID));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.metricCode, this.period, this.pID);
    }

}
