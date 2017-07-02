package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Composite id for relationship table between battle and counter.
 *
 * @author MKL.
 */
@Embeddable
public class BattleCounterId implements Serializable {
    /** The battle key. */
    private BattleEntity battle;
    /** The counter key. */
    private CounterEntity counter;

    /** @return the battle. */
    @ManyToOne
    public BattleEntity getBattle() {
        return battle;
    }

    /** @param battle the battle to set. */
    public void setBattle(BattleEntity battle) {
        this.battle = battle;
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
