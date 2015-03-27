package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.EuObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack of counters (regroupment).
 *
 * @author MKL
 */
public class Stack extends EuObject {
    /** Province where the stack is located (String or Province ?). */
    private String province;
    /** Counters of the stack. */
    private List<Counter> counters = new ArrayList<>();

    /**
     * Constructor.
     */
    public Stack() {

    }

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the counters. */
    public List<Counter> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(List<Counter> counters) {
        this.counters = counters;
    }
}
