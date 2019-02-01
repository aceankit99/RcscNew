/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.model.User;
import com.flowserve.system606.model.WorkflowStatus;
import com.flowserve.system606.service.AdminService;
import com.flowserve.system606.service.CalculationVersionService;
import com.flowserve.system606.service.ReportingUnitService;
import com.flowserve.system606.web.WebSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections4.ListUtils;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author kgraves
 */
@Named
@ViewScoped
public class Dashboard implements Serializable {

    private static final Logger logger = Logger.getLogger(Dashboard.class.getName());
    @Inject
    private WebSession webSession;
    @Inject
    private ReportingUnitService reportingUnitService;
    @Inject
    private ViewSupport viewSupport;
    @Inject
    private CalculationVersionService calculationVersionService;
    @Inject
    private AdminService adminService;
    private Set<ReportingUnit> relevantReportingUnits = new TreeSet<ReportingUnit>();

    @PostConstruct
    public void init() {
        relevantReportingUnits.clear();

        for (ReportingUnit ru : reportingUnitService.getViewableReportingUnits(webSession.getUser())) {
            relevantReportingUnits.add(ru);
        }
        for (ReportingUnit ru : reportingUnitService.getPreparableReportingUnits(webSession.getUser())) {
            relevantReportingUnits.add(ru);
        }
        for (ReportingUnit ru : reportingUnitService.getReviewableReportingUnits(webSession.getUser())) {
            relevantReportingUnits.add(ru);
        }
        for (ReportingUnit ru : reportingUnitService.getApprovableReportingUnits(webSession.getUser())) {
            relevantReportingUnits.add(ru);
        }

        /**
         * This re-initializes the current RU in the case where we navigate back to dashboard and we might have a stale RU in the session. We probably should
         * not store the entire RU object at all. Consider changing to just store the ID for phase 2.
         */
        if (webSession.getCurrentReportingUnit() != null) {
            relevantReportingUnits.add(adminService.findReportingUnitById(webSession.getCurrentReportingUnit().getId()));
        }

        if (webSession.getCurrentReportingUnit() == null) {
            determineDefaultReportingUnit();
        }
    }

    public void SaveAndTest() throws Exception {
        Contract ru = webSession.getCurrentReportingUnit().getContracts().get(0);
        Logger.getLogger(Dashboard.class.getName()).log(Level.INFO, "message : " + ru.getWorkflowStatus());
        if (ru.getWorkflowStatus() == WorkflowStatus.COMPLETED) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.INFO, "true");
        } else {
            Logger.getLogger(Dashboard.class.getName()).log(Level.INFO, "false");
        }
    }

    private void determineDefaultReportingUnit() {
        /**
         * Added so that if user logs in and immediately clicks navigation menu links, we can take them to a default RU.
         */
        if (!relevantReportingUnits.isEmpty()) {
            webSession.setCurrentReportingUnit(((TreeSet<ReportingUnit>) relevantReportingUnits).first());
        } else {
            if (webSession.isAdmin()) {
                webSession.setCurrentReportingUnit(adminService.findBUByReportingUnitCode("1015"));
            }
        }
    }

    public List<ReportingUnit> getRelevantReportingUnits() {
        List<ReportingUnit> ruc = new ArrayList<ReportingUnit>();
        List<ReportingUnit> rus = new ArrayList<ReportingUnit>();

        for (ReportingUnit ru : relevantReportingUnits) {
            if (ru.getContractCount() != 0) {

                ruc.add(ru);

            } else {
                rus.add(ru);
            }
        }
//        List<ReportingUnit> sortruc = ruc.stream().collect(Collectors.toList());
//        List<ReportingUnit> sortrus = rus.stream().collect(Collectors.toList());
//        ruc = new TreeSet<ReportingUnit>(sortruc);
//        rus = new TreeSet<ReportingUnit>(sortrus);
//        Set<ReportingUnit> combinedSet = Sets.union(ruc, rus);
        Collections.sort(ruc, (ReportingUnit o1, ReportingUnit o2) -> o1.getCode().compareTo(o2.getCode()));
        Collections.sort(rus, (ReportingUnit o1, ReportingUnit o2) -> o1.getCode().compareTo(o2.getCode()));
        List<ReportingUnit> combinedSet = ListUtils.union(ruc, rus);

        List<Long> Rucodes = new ArrayList<Long>();

        for (ReportingUnit unit : combinedSet) {

            Rucodes.add(Long.parseLong(unit.getCode()));
        }

        webSession.setRuCodeList(Rucodes);
        return combinedSet;
    }

    public void onReportingUnitSelect(SelectEvent event) {
        webSession.setFilterText(null);
        User user = webSession.getUser();
        if (user.isAdmin() || user.isGlobalViewer()) {
            init();
        } else {
            relevantReportingUnits.clear();
            webSession.setCurrentReportingUnit((ReportingUnit) event.getObject());
            relevantReportingUnits.add(webSession.getCurrentReportingUnit());
        }
    }

    public void clearReportingUnit(SelectEvent event) {
        webSession.setFilterText(null);
        webSession.setCurrentReportingUnit(null);
        init();
    }

    public WorkflowStatus getWorkflowStatus(ReportingUnit reportingUnit) {
        return reportingUnit.getWorkflowStatus(webSession.getCurrentPeriod());
    }

    public int getContractCount(ReportingUnit reportingUnit) {
        return reportingUnit.getUnarchivedContracts().size();
    }

    public long getPobCount(ReportingUnit reportingUnit) {
        return reportingUnit.getPobCount();
    }

    public long getPobInputRequiredCount(ReportingUnit reportingUnit) {
        return reportingUnit.getPobInputRequiredCount();

    }

    public void clearTextReportingUnit(SelectEvent event) {
        webSession.setFilterText(null);
        webSession.setCurrentReportingUnit(null);
        init();
    }

}
