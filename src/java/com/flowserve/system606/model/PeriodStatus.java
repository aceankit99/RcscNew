/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.model;

/**
 *
 * @author kgraves
 */
public enum PeriodStatus {
    OPENED("O"), CLOSED("C"), NEVER_OPENED("NO"), USER_FREEZE("UF");

    private String shortName;

    private PeriodStatus(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public static PeriodStatus fromShortName(String shortName) {
        switch (shortName) {
            case "O":
                return PeriodStatus.OPENED;

            case "C":
                return PeriodStatus.CLOSED;

            case "NO":
                return PeriodStatus.NEVER_OPENED;
            case "UF":
                return PeriodStatus.USER_FREEZE;

            default:
                throw new IllegalArgumentException("ShortName [" + shortName + "] not supported.");
        }
    }
}
