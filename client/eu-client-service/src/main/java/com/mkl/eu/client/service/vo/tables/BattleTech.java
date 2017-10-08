package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * VO for the battle technology table.
 *
 * @author MKL.
 */
public class BattleTech extends EuObject {
    /** Technology of the friendly stack. */
    private String technologyFor;
    /** Technology of the opponent stack. */
    private String technologyAgainst;
    /** Flag to say if this table is for land or naval battle. */
    private boolean land;
    /** Column to use for fire. */
    private String columnFire;
    /** Column to use for shock. */
    private String columnShock;
    /** Moral for battle. */
    private int moral;
    /** Flag to say if there is a moral bonus for veteran stacks. */
    private boolean moralBonusVeteran;

    /** @return the technologyFor. */
    public String getTechnologyFor() {
        return technologyFor;
    }

    /** @param technologyFor the technologyFor to set. */
    public void setTechnologyFor(String technologyFor) {
        this.technologyFor = technologyFor;
    }

    /** @return the technologyAgainst. */
    public String getTechnologyAgainst() {
        return technologyAgainst;
    }

    /** @param technologyAgainst the technologyAgainst to set. */
    public void setTechnologyAgainst(String technologyAgainst) {
        this.technologyAgainst = technologyAgainst;
    }

    /** @return the land. */
    public boolean isLand() {
        return land;
    }

    /** @param land the land to set. */
    public void setLand(boolean land) {
        this.land = land;
    }

    /** @return the columnFire. */
    public String getColumnFire() {
        return columnFire;
    }

    /** @param columnFire the columnFire to set. */
    public void setColumnFire(String columnFire) {
        this.columnFire = columnFire;
    }

    /** @return the columnShock. */
    public String getColumnShock() {
        return columnShock;
    }

    /** @param columnShock the columnShock to set. */
    public void setColumnShock(String columnShock) {
        this.columnShock = columnShock;
    }

    /** @return the moral. */
    public int getMoral() {
        return moral;
    }

    /** @param moral the moral to set. */
    public void setMoral(int moral) {
        this.moral = moral;
    }

    /** @return the moralBonusVeteran. */
    public boolean isMoralBonusVeteran() {
        return moralBonusVeteran;
    }

    /** @param moralBonusVeteran the moralBonusVeteran to set. */
    public void setMoralBonusVeteran(boolean moralBonusVeteran) {
        this.moralBonusVeteran = moralBonusVeteran;
    }
}
