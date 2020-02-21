package com.mkl.eu.service.service.persistence.oe.attrition;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Composite id for relationship table between attrition and counter.
 *
 * @author MKL.
 */
@Embeddable
public class AttritionCounterId implements Serializable {
    /** The battle key. */
    private AttritionEntity attrition;
    /** The counter key. */
    private Long counter;

    /**
     * Default constructor.
     */
    public AttritionCounterId() {

    }

    /**
     * Constructor with all keys.
     *
     * @param attrition the attrition.
     * @param counter   the counter.
     */
    public AttritionCounterId(AttritionEntity attrition, Long counter) {
        this.attrition = attrition;
        this.counter = counter;
    }

    /** @return the attrition. */
    @ManyToOne
    public AttritionEntity getAttrition() {
        return attrition;
    }

    /** @param attrition the attrition to set. */
    public void setAttrition(AttritionEntity attrition) {
        this.attrition = attrition;
    }

    /** @return the counter. */
    public Long getCounter() {
        return counter;
    }

    /** @param counter the counter to set. */
    public void setCounter(Long counter) {
        this.counter = counter;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttritionCounterId that = (AttritionCounterId) o;

        if (!getAttrition().equals(that.getAttrition())) return false;
        return getCounter().equals(that.getCounter());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = getAttrition().hashCode();
        result = 31 * result + getCounter().hashCode();
        return result;
    }
}
