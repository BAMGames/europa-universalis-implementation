package com.mkl.eu.service.service.persistence.oe.military;

import javax.persistence.Embeddable;

/**
 * Modifiers of each side in battle.
 *
 * @author MKL.
 */
@Embeddable
public class BattleSideEntity {
    /** Flag saying that the side has selected its forces. */
    private Boolean forces;
    /** Technology of the side. */
    private String tech;
    /** First day modifiers. */
    private BattleModifierEntity firstDay = new BattleModifierEntity();
    /** Second day modifiers. */
    private BattleModifierEntity secondDay = new BattleModifierEntity();

    /** @return the forces. */
    public Boolean isForces() {
        return forces;
    }

    /** @param forces the forces to set. */
    public void setForces(Boolean forces) {
        this.forces = forces;
    }

    /** @return the tech. */
    public String getTech() {
        return tech;
    }

    /** @param tech the tech to set. */
    public void setTech(String tech) {
        this.tech = tech;
    }

    /** @return the firstDay. */
    public BattleModifierEntity getFirstDay() {
        return firstDay;
    }

    /** @param firstDay the firstDay to set. */
    public void setFirstDay(BattleModifierEntity firstDay) {
        this.firstDay = firstDay;
    }

    /** @return the secondDay. */
    public BattleModifierEntity getSecondDay() {
        return secondDay;
    }

    /** @param secondDay the secondDay to set. */
    public void setSecondDay(BattleModifierEntity secondDay) {
        this.secondDay = secondDay;
    }
}
