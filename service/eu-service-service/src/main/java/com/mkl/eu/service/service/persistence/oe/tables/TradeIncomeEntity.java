package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.service.service.persistence.oe.IEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entry in the foreign or domestic trade income (Page 2 of tables).
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_TRADE")
public class TradeIncomeEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
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

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the countryValue. */
    @Column(name = "COUNTRY_VALUE")
    public int getCountryValue() {
        return countryValue;
    }

    /** @param countryValue the countryValue to set. */
    public void setCountryValue(int countryValue) {
        this.countryValue = countryValue;
    }

    /** @return the minValue. */
    @Column(name = "MIN_VALUE")
    public Integer getMinValue() {
        return minValue;
    }

    /** @param minValue the minValue to set. */
    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    /** @return the maxValue. */
    @Column(name = "MAX_VALUE")
    public Integer getMaxValue() {
        return maxValue;
    }

    /** @param maxValue the maxValue to set. */
    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    /** @return the value. */
    @Column(name = "VALUE")
    public int getValue() {
        return value;
    }

    /** @param value the value to set. */
    public void setValue(int value) {
        this.value = value;
    }

    /** @return the foreignTrade. */
    @Column(name = "FOREIGN_TRADE")
    public boolean isForeignTrade() {
        return foreignTrade;
    }

    /** @param foreignTrade the foreignTrade to set. */
    public void setForeignTrade(boolean foreignTrade) {
        this.foreignTrade = foreignTrade;
    }
}
