package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ResultEnum;

/**
 * VO for the exchequer result (tables).
 *
 * @author MKL.
 */
public class Exchequer extends EuObject {
    /** Result of the exchequer test. */
    private ResultEnum result;
    /** Regular income in percentage of the gross income. */
    private Integer regular;
    /** Prestige income in percentage of the gross income. */
    private Integer prestige;
    /** Maximum national loan in percentage of the gross income. */
    private Integer natLoan;
    /** Maximum international loan in percentage of the gross income. */
    private Integer interLoan;

    /** @return the result. */
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }

    /** @return the regular. */
    public Integer getRegular() {
        return regular;
    }

    /** @param regular the regular to set. */
    public void setRegular(Integer regular) {
        this.regular = regular;
    }

    /** @return the prestige. */
    public Integer getPrestige() {
        return prestige;
    }

    /** @param prestige the prestige to set. */
    public void setPrestige(Integer prestige) {
        this.prestige = prestige;
    }

    /** @return the natLoan. */
    public Integer getNatLoan() {
        return natLoan;
    }

    /** @param natLoan the natLoan to set. */
    public void setNatLoan(Integer natLoan) {
        this.natLoan = natLoan;
    }

    /** @return the interLoan. */
    public Integer getInterLoan() {
        return interLoan;
    }

    /** @param interLoan the interLoan to set. */
    public void setInterLoan(Integer interLoan) {
        this.interLoan = interLoan;
    }
}
