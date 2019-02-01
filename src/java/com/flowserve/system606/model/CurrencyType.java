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
public enum CurrencyType {
    LOCAL("LC"), CONTRACT("CC"), REPORTING("RC");

    private String shortName;

    private CurrencyType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public static CurrencyType fromShortName(String shortName) {
        switch (shortName) {
            case "LC":
                return CurrencyType.LOCAL;

            case "CC":
                return CurrencyType.CONTRACT;

            case "RC":
                return CurrencyType.REPORTING;

            default:
                throw new IllegalArgumentException("ShortName [" + shortName + "] not supported.");
        }
    }
}
