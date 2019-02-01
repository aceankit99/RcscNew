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
public enum RevenueMethod {
    PERC_OF_COMP("POC"), POINT_IN_TIME("PIT"), STRAIGHT_LINE("SL"), RIGHT_TO_INVOICE("RTI");

    private String shortName;

    private RevenueMethod(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public static RevenueMethod fromShortName(String shortName) {
        switch (shortName) {
            case "POC":
                return RevenueMethod.PERC_OF_COMP;

            case "PIT":
                return RevenueMethod.POINT_IN_TIME;

            case "SL":
                return RevenueMethod.STRAIGHT_LINE;

            case "RTI":
                return RevenueMethod.RIGHT_TO_INVOICE;
            case "":
                return null;

            default:
                throw new IllegalArgumentException("ShortName [" + shortName + "] not supported.");
        }
    }
}
