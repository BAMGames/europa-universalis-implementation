package com.mkl.eu.service.service.persistence.oe.military;

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
    private Long counter;

    /**
     * Default constructor.
     */
    public BattleCounterId() {

    }

    /**
     * Constructor with all keys.
     *
     * @param battle  the battle.
     * @param counter the counter.
     */
    public BattleCounterId(BattleEntity battle, Long counter) {
        this.battle = battle;
        this.counter = counter;
    }

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

        BattleCounterId that = (BattleCounterId) o;

        if (!getBattle().equals(that.getBattle())) return false;
        return getCounter().equals(that.getCounter());

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = getBattle().hashCode();
        result = 31 * result + getCounter().hashCode();
        return result;
    }
}
