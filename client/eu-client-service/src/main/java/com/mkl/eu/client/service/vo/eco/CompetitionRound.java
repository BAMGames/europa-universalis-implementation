package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ResultEnum;

/**
 * Round of a country of an automatic competition.
 *
 * @author MKL.
 */
public class CompetitionRound extends EuObject {
    /** Country rolling for the competition (can be minor). */
    private String country;
    /** Round of the competition. */
    private Integer round;
    /** Column to test the administrative action. */
    private Integer column;
    /** Result of the die (without bonus) of the test of the administrative action. */
    private Integer die;
    /** Result of the die (without bonus) of an eventual secondary test of the administrative action (often test under FTI). */
    private Integer secondaryDie;
    /** Result of the administrative action. */
    private ResultEnum result;
    /** Secondary result of the administrative action (when secondary tests are needed). */
    private Boolean secondaryResult;

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the round. */
    public Integer getRound() {
        return round;
    }

    /** @param round the round to set. */
    public void setRound(Integer round) {
        this.round = round;
    }

    /** @return the column. */
    public Integer getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(Integer column) {
        this.column = column;
    }

    /** @return the die. */
    public Integer getDie() {
        return die;
    }

    /** @param die the die to set. */
    public void setDie(Integer die) {
        this.die = die;
    }

    /** @return the secondaryDie. */
    public Integer getSecondaryDie() {
        return secondaryDie;
    }

    /** @param secondaryDie the secondaryDie to set. */
    public void setSecondaryDie(Integer secondaryDie) {
        this.secondaryDie = secondaryDie;
    }

    /** @return the result. */
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }

    /** @return the secondaryResult. */
    public Boolean isSecondaryResult() {
        return secondaryResult;
    }

    /** @param secondaryResult the secondaryResult to set. */
    public void setSecondaryResult(Boolean secondaryResult) {
        this.secondaryResult = secondaryResult;
    }
}
