package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * Entry in the foreign or domestic trade income (Page 2 of tables).
 *
 * @author MKL.
 */
public class TradeIncome extends EuObject {
    /** Country FTI/DTI (depends of foreignTrade). */
    private int countryValue;
    /** Min value of blocked trade/land income (depends of foreignTrade). Can be <code>null</code>. */
    private Integer minValue;
    /** Max value of blocked trade/land income (depends of foreignTrade). Can be <code>null</code>. */
    private Integer maxValue;
    /** Value of the trade income to be reported on the economical sheet. */
    private int value;
    /** Flag saying that it is foreign or domestic trade. */
    private boolean foreignTrade;

    /** @return the countryValue. */
    public int getCountryValue() {
        return countryValue;
    }

    /** @param countryValue the countryValue to set. */
    public void setCountryValue(int countryValue) {
        this.countryValue = countryValue;
    }

    /** @return the minValue. */
    public Integer getMinValue() {
        return minValue;
    }

    /** @param minValue the minValue to set. */
    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    /** @return the maxValue. */
    public Integer getMaxValue() {
        return maxValue;
    }

    /** @param maxValue the maxValue to set. */
    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    /** @return the value. */
    public int getValue() {
        return value;
    }

    /** @param value the value to set. */
    public void setValue(int value) {
        this.value = value;
    }

    /** @return the foreignTrade. */
    public boolean isForeignTrade() {
        return foreignTrade;
    }

    /** @param foreignTrade the foreignTrade to set. */
    public void setForeignTrade(boolean foreignTrade) {
        this.foreignTrade = foreignTrade;
    }
}
