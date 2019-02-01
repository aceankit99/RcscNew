package com.flowserve.system606.model;

import java.util.Comparator;

public class ApproverSetComparator implements Comparator<ApproverSet> {

    public int compare(ApproverSet hrset1, ApproverSet hrset2) {
        return new Integer(hrset1.getApprovalRole().getHierarchyLevel()).compareTo(new Integer(hrset2.getApprovalRole().getHierarchyLevel()));
    }
}
