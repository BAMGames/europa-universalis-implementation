package com.mkl.eu.client.service.vo.military;

/**
 * Modifiers for a day in battle.
 *
 * @author MKL.
 */
public class BattleDay {
    /** Modifier for the fire phase. */
    private int fireMod;
    /** Unmodified die roll for the fire phase. */
    private Integer fire;
    /** Modifier for the shock phase. */
    private int shockMod;
    /** Unmodified die roll for the shock phase. */
    private Integer shock;

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
