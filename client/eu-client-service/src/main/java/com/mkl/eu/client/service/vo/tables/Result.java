package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ResultEnum;

/**
 * VO for the result of an action (tables).
 *
 * @author MKL.
 */
public class Result extends EuObject {
    /** Id. */
    private Long id;
    /** Die of the result. */
    private Integer die;
    /** Column of the result. */
    private Integer column;
    /** Result. */
    private ResultEnum result;

    /** @return the id. */
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the die. */
    public Integer getDie() {
        return die;
    }

    /** @param die the die to set. */
    public void setDie(Integer die) {
        this.die = die;
    }

    /** @return the column. */
    public Integer getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(Integer column) {
        this.column = column;
    }

    /** @return the result. */
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }
}
