package com.mkl.eu.front.client.map.marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a stack on the map.
 *
 * @author MKL
 */
public class StackMarker {
    /** Id of the stack. */
    private Long id;
    /** Province marker where the stack is. */
    private IMapMarker province;
    /** Counters marker of the stack. */
    private List<CounterMarker> counters = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param id       of the stack.
     * @param province where the stack is.
     */
    public StackMarker(Long id, IMapMarker province) {
        this.id = id;
        this.province = province;
    }

    /** @return the id. */
    public Long getId() {
        return id;
    }

    /** @return the province. */
    public IMapMarker getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(IMapMarker province) {
        this.province = province;
    }

    /** @return the counters. */
    public List<CounterMarker> getCounters() {
        return counters;
    }

    /**
     * Remove a counter of the stack.
     *
     * @param counter to be removed.
     */
    public void removeCounter(CounterMarker counter) {
        counter.setOwner(null);
        counters.remove(counter);
    }

    /**
     * Add a counter of the stack.
     *
     * @param counter to be added.
     */
    public void addCounter(CounterMarker counter) {
        if (counter.getOwner() != null) {
            counter.getOwner().removeCounter(counter);
        }
        counters.add(counter);
        counter.setOwner(this);
    }
}
