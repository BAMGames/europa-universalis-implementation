package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * VO for the period.
 *
 * @author MKL.
 */
public class Period extends EuObject {
    /** Name of the first period. */
    public static final String PERIOD_I = "I";
    /** Name of the second period. */
    public static final String PERIOD_II = "II";
    /** Name of the third period. */
    public static final String PERIOD_III = "III";
    /** Name of the fourth period. */
    public static final String PERIOD_IV = "IV";
    /** Name of the fifth period. */
    public static final String PERIOD_V = "V";
    /** Name of the sixth period. */
    public static final String PERIOD_VI = "VI";
    /** Name of the seventh period. */
    public static final String PERIOD_VII = "VII";
    /** name of the period. */
    private String name;
    /** Turn when the period begins (inclusive). */
    private Integer begin;
    /** Turn when the period ends (inclusive). */
    private Integer end;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the begin. */
    public Integer getBegin() {
        return begin;
    }

    /** @param begin the begin to set. */
    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    /** @return the end. */
    public Integer getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(Integer end) {
        this.end = end;
    }
}
