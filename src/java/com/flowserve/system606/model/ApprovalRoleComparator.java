package com.flowserve.system606.model;

import java.util.Comparator;

public class ApprovalRoleComparator implements Comparator<ApprovalRole> {

    public int compare(ApprovalRole role1, ApprovalRole role2) {
        return new Integer(role1.getHierarchyLevel()).compareTo(new Integer(role2.getHierarchyLevel()));
    }
}
