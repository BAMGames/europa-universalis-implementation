package com.mkl.eu.service.service.persistence.oe.military;

import javax.persistence.Embeddable;

/**
 * Modifiers for a day in battle.
 *
 * @author MKL.
 */
@Embeddable
public class BattleDayEntity {
    /** Modifier for the fire phase. */
    private int fireMod;
    /** Unmodified die roll for the fire phase. */
    private Integer fire;
    /** Modifier for the shock phase. */
    private int shockMod;
    /** Unmodified die roll for the shock phase. */
    private Integer shock;

    /**
     * @param fire    modifier
     * @param shock   modifier
     * @return a BattleDayEntity with the given modifiers.
     */
    public static BattleDayEntity create(int fire, int shock) {
        BattleDayEntity battleDayEntity = new BattleDayEntity();

        battleDayEntity.setFireMod(fire);
        battleDayEntity.setShockMod(shock);

        return battleDayEntity;
    }

    /**
     * Clear all the modifiers.
     *
     * @return the current instance.
     */
    public BattleDayEntity clear() {
        this.fire = null;
        this.fireMod = 0;
        this.shock = null;
        this.shockMod = 0;

        return this;
    }

    /**
     * Add another battle modifier to the current one.
     *
     * @param fire    modifier.
     * @param shock   modifier.
     * @return the current instance.
     */
    public BattleDayEntity add(int fire, int shock) {
        this.fireMod += fire;
        this.shockMod += shock;

        return this;
    }

    /**
     * Add fire modifier to the current one.
     *
     * @param fire modifier.
     * @return the current instance.
     */
    public BattleDayEntity addFire(int fire) {
        return add(fire, 0);
    }

    /**
     * Add shock modifier to the current one.
     *
     * @param shock modifier.
     * @return the current instance.
     */
    public BattleDayEntity addShock(int shock) {
        return add(0, shock);
    }

    /**
     * Add the same value to fire and shock modifiers.
     *
     * @param fireShock value of the modifier.
     * @return the current instance.
     */
    public BattleDayEntity addFireAndShock(int fireShock) {
        return add(fireShock, fireShock);
    }

    /** @return the fireMod. */
    public int getFireMod() {
        return fireMod;
    }

    /** @param fireMod the fireMod to set. */
    public void setFireMod(int fireMod) {
        this.fireMod = fireMod;
    }

    /** @return the fire. */
    public Integer getFire() {
        return fire;
    }

    /** @param fire the fire to set. */
    public void setFire(Integer fire) {
        this.fire = fire;
    }

    /** @return the shockMod. */
    public int getShockMod() {
        return shockMod;
    }

    /** @param shockMod the shockMod to set. */
    public void setShockMod(int shockMod) {
        this.shockMod = shockMod;
    }

    /** @return the shock. */
    public Integer getShock() {
        return shock;
    }

    /** @param shock the shock to set. */
    public void setShock(Integer shock) {
        this.shock = shock;
    }
}
