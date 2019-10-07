package com.mkl.eu.service.service.persistence.oe.military;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Composite id for relationship table between siege and counter.
 *
 * @author MKL.
 */
@Embeddable
public class SiegeCounterId implements Serializable {
    /** The battle key. */
    private SiegeEntity siege;
    /** The counter key. */
    private Long counter;

    /**
     * Default constructor.
     */
    public SiegeCounterId() {

    }

    /**
     * Constructor with all keys.
     *
     * @param siege   the siege.
     * @param counter the counter.
     */
    public SiegeCounterId(SiegeEntity siege, Long counter) {
        this.siege = siege;
        this.counter = counter;
    }

    /** @return the battle. */
    @ManyToOne
    public SiegeEntity getSiege() {
        return siege;
    }

    /** @param battle the battle to set. */
    public void setSiege(SiegeEntity battle) {
        this.siege = battle;
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

        SiegeCounterId that = (SiegeCounterId) o;

        if (!getSiege().equals(that.getSiege())) return false;
        return getCounter().equals(that.getCounter());

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = getSiege().hashCode();
        result = 31 * result + getCounter().hashCode();
        return result;
    }
}
