package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

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
    private CounterEntity counter;

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
    @ManyToOne
    public CounterEntity getCounter() {
        return counter;
    }

    /** @param counter the counter to set. */
    public void setCounter(CounterEntity counter) {
        this.counter = counter;
    }
}
