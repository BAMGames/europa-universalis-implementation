package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

import javax.persistence.*;

/**
 * Entity for the relationship table between battle and counter.
 *
 * @author MKL.
 */
@Entity
@Table(name = "BATTLE_COUNTER")
@AssociationOverrides({
        @AssociationOverride(name = "id.battle", joinColumns = @JoinColumn(name = "ID_BATTLE")),
        @AssociationOverride(name = "id.counter", joinColumns = @JoinColumn(name = "ID_COUNTER"))
})
public class BattleCounterEntity {
    /** Composite id. */
    private BattleCounterId id = new BattleCounterId();
    /** Flag <code>true</code> when the counter is in the phasing side. */
    private boolean phasing;

    /** @return the id. */
    @EmbeddedId
    public BattleCounterId getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(BattleCounterId id) {
        this.id = id;
    }

    /** @return the battle. */
    @Transient
    public BattleEntity getBattle() {
        return id.getBattle();
    }

    /** @param battle the battle to set. */
    public void setBattle(BattleEntity battle) {
        id.setBattle(battle);
    }

    /** @return the counter. */
    @Transient
    public CounterEntity getCounter() {
        return id.getCounter();
    }

    /** @param counter the counter to set. */
    public void setCounter(CounterEntity counter) {
        id.setCounter(counter);
    }

    /** @return the phasing. */
    @Column(name = "PHASING")
    public boolean isPhasing() {
        return phasing;
    }

    /** @param phasing the phasing to set. */
    public void setPhasing(boolean phasing) {
        this.phasing = phasing;
    }
}
