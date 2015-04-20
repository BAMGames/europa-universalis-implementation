package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.EuObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack of counters (regroupment).
 *
 * @author MKL
 */
public class Stack extends EuObject<Long> {
    /** Province where the stack is located. */
    private AbstractProvince province;
    /** Counters of the stack. */
    private List<Counter> counters = new ArrayList<>();

    /** @return the province. */
    public AbstractProvince getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(AbstractProvince province) {
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

    /** {@inheritDoc} */
    @Override
    public void afterUnmarshal(Object target, Object parent) {
        if (this.province != null) {
            this.province.getStacks().add(this);
        }
        if (this.counters != null) {
            for (Counter counter : counters) {
                counter.setOwner(this);
            }
        }
    }
}
