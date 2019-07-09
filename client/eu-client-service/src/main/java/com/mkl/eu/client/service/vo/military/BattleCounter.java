package com.mkl.eu.client.service.vo.military;

import com.mkl.eu.client.service.vo.board.Counter;

/**
 * VO for the relationship table between battle and counter.
 *
 * @author MKL.
 */
public class BattleCounter {
    /** The counter. */
    private Counter counter;
    /** Flag <code>true</code> when the counter is in the phasing side. */
    private boolean phasing;

    /** @return the counter. */
    public Counter getCounter() {
        return counter;
    }

    /** @param counter the counter to set. */
    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    /** @return the phasing. */
    public boolean isPhasing() {
        return phasing;
    }

    /** @param phasing the phasing to set. */
    public void setPhasing(boolean phasing) {
        this.phasing = phasing;
    }
}
