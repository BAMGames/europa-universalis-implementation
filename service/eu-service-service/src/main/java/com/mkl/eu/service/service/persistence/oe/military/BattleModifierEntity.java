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
     * Clear all the modifiers.
     *
     * @return the current instance.
     */
    public BattleModifierEntity clear() {
        this.fire = 0;
        this.shock = 0;
        this.pursuit = 0;

        return this;
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

    /**
     * Add fire modifier to the current one.
     *
     * @param fire modifier.
     * @return the current instance.
     */
    public BattleModifierEntity addFire(int fire) {
        return add(fire, 0, 0);
    }

    /**
     * Add shock modifier to the current one.
     *
     * @param shock modifier.
     * @return the current instance.
     */
    public BattleModifierEntity addShock(int shock) {
        return add(0, shock, 0);
    }

    /**
     * Add pursuit modifier to the current one.
     *
     * @param pursuit modifier.
     * @return the current instance.
     */
    public BattleModifierEntity addPursuit(int pursuit) {
        return add(0, 0, pursuit);
    }

    /**
     * Add the same value to all modifiers (fire, shock, pursuit).
     *
     * @param all value of the modifier.
     * @return the current isntance.
     */
    public BattleModifierEntity addAll(int all) {
        return add(all, all, all);
    }

    /**
     * Add the same value to fire and shock modifiers.
     *
     * @param fireShock value of the modifier.
     * @return the current instance.
     */
    public BattleModifierEntity addFireAndShock(int fireShock) {
        return add(fireShock, fireShock, 0);
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
