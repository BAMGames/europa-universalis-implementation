package com.mkl.eu.service.service.persistence.oe.military;

import javax.persistence.Embeddable;

/**
 * Modifiers of each side in a siege assault.
 *
 * @author MKL.
 */
@Embeddable
public class SiegeSideEntity {
    /** Flag saying that the side has selected its losses. */
    private Boolean lossesSelected;
    /** Size of the side. */
    private Double size;
    /** Technology of the side. */
    private String tech;
    /** Moral of the side. */
    private Integer moral;
    /** Modifiers. */
    private BattleDayEntity modifiers = new BattleDayEntity();
    /** Losses. */
    private BattleLossesEntity losses = new BattleLossesEntity();


    /** @return the size. */
    public Double getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(Double size) {
        this.size = size;
    }

    /** @return the tech. */
    public String getTech() {
        return tech;
    }

    /** @param tech the tech to set. */
    public void setTech(String tech) {
        this.tech = tech;
    }

    /** @return the moral. */
    public Integer getMoral() {
        return moral;
    }

    /** @param moral the moral to set. */
    public void setMoral(Integer moral) {
        this.moral = moral;
    }

    /** @return the modifiers. */
    public BattleDayEntity getModifiers() {
        return modifiers;
    }

    /** @param modifiers the modifiers to set. */
    public void setModifiers(BattleDayEntity modifiers) {
        this.modifiers = modifiers;
    }

    /** @return the losses. */
    public BattleLossesEntity getLosses() {
        return losses;
    }

    /** @param losses the losses to set. */
    public void setLosses(BattleLossesEntity losses) {
        this.losses = losses;
    }

    /** @return the lossesSelected. */
    public Boolean isLossesSelected() {
        return lossesSelected;
    }

    /** @param lossesSelected the lossesSelected to set. */
    public void setLossesSelected(Boolean lossesSelected) {
        this.lossesSelected = lossesSelected;
    }
}
