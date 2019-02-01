/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.MetricType;
import com.flowserve.system606.service.MetricService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author shubhamv
 */
@Named
@ViewScoped
public class MetricTypeList implements Serializable {

    private static final long serialVersionUID = -1438027991420003830L;
    private List<MetricType> metricTypes = new ArrayList<MetricType>();
    @Inject
    private MetricService metricService;

    public List<MetricType> getMetricTypes() throws Exception {
        metricTypes = metricService.findMetricType();
        Collections.sort(metricTypes);
        return metricTypes;
    }

    public void setMetricTypes(List<MetricType> metricTypes) {
        this.metricTypes = metricTypes;
    }

}
