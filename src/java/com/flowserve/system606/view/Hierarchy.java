/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.view;

import com.flowserve.system606.model.BusinessUnit;
import com.flowserve.system606.model.ReportingUnit;
import com.flowserve.system606.service.AdminService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author shubhamc
 */
@Named
@ViewScoped
public class Hierarchy implements Serializable {

    List<BusinessUnit> bu = new ArrayList<BusinessUnit>();
    private TreeNode root;
    private TreeNode coeHierarchy;
    private List<ReportingUnit> parent;
    @Inject
    private AdminService adminService;
    @Inject
    private ViewSupport viewSupport;

    @PostConstruct
    public void init() {
        try {
            bu = adminService.findBusinessUnits();
            Collections.sort(bu);
        } catch (Exception ex) {
            Logger.getLogger(Hierarchy.class.getName()).log(Level.SEVERE, null, ex);
        }
        root = new DefaultTreeNode("root", null);
        TreeNode fls = new DefaultTreeNode("Flowserve", root);
        TreeNode buHierarchy = new DefaultTreeNode("Business Unit Hierarchy", fls);
        TreeNode coExcellence = new DefaultTreeNode("Center of Expertise Hierarchy", fls);
        for (BusinessUnit businessUnit : bu) {
            TreeNode bunits = new DefaultTreeNode("Business Unit : " + businessUnit.getName(), buHierarchy);
            for (ReportingUnit reportingUnit : businessUnit.getReportingUnit()) {
                TreeNode runits = new DefaultTreeNode("RU-" + reportingUnit.getName(), bunits);

            }

        }

        parent = adminService.findAllReportingUnits();

        for (ReportingUnit ru : parent) {

            if (ru.isParent()) {

                TreeNode parentRu = new DefaultTreeNode("Parent-RU : " + ru.getName(), coExcellence);
                for (ReportingUnit child : ru.getChildReportingUnits()) {
                    TreeNode childRu = new DefaultTreeNode("RU " + child.getName(), parentRu);

                }

            }

        }

    }

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode getCoeHierarchy() {
        return coeHierarchy;
    }

}
