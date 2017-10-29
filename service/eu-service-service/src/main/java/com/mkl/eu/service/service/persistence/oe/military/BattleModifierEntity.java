package com.mkl.eu.service.service.persistence.oe.military;

import javax.persistence.Embeddable;

/**
 * Modifiers for a day in battle.
 *
 * @author MKL.
 */
@Embeddable
public class BattleModifierEntity {
    /** Modifier for the fire phase. */
    private int fire;
    /** Modifier for the shock phase. */
    private int shock;
    /** Modifier for the pursuit phase. */
    private int pursuit;

    /**
     * @param fire    modifier
     * @param shock   modifier
     * @param pursuit modifier
     * @return a BattleModifierEntity with the given modifiers.
     */
    public static BattleModifierEntity create(int fire, int shock, int pursuit) {
        BattleModifierEntity battleModifierEntity = new BattleModifierEntity();

        battleModifierEntity.setFire(fire);
        battleModifierEntity.setShock(shock);
        battleModifierEntity.setPursuit(pursuit);

        return battleModifierEntity;
    }

    /**
     * Add another battle modifier to the current one.
     *
     * @param fire    modifier.
     * @param shock   modifier.
     * @param pursuit modifier.
     * @return the current instance.
     */
    public BattleModifierEntity add(int fire, int shock, int pursuit) {
        this.fire += fire;
        this.shock += shock;
        this.pursuit += pursuit;

        return this;
    }

    /** @return the fire. */
    public int getFire() {
        return fire;
    }

    /** @param fire the fire to set. */
    public void setFire(int fire) {
        this.fire = fire;
    }

    /** @return the shock. */
    public int getShock() {
        return shock;
    }

    /** @param shock the shock to set. */
    public void setShock(int shock) {
        this.shock = shock;
    }

    /** @return the pursuit. */
    public int getPursuit() {
        return pursuit;
    }

    /** @param pursuit the pursuit to set. */
    public void setPursuit(int pursuit) {
        this.pursuit = pursuit;
    }
}
