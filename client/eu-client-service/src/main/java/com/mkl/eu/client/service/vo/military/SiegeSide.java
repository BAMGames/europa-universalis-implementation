package com.mkl.eu.client.service.vo.military;

import com.mkl.eu.client.service.vo.AbstractWithLoss;

/**
 * Modifiers of each side in a siege assault.
 *
 * @author MKL.
 */
public class SiegeSide {
    /** Flag saying that the side has selected its losses. */
    private Boolean lossesSelected;
    /** Size of the side. */
    private double size;
    /** Technology of the side. */
    private String tech;
    /** Moral of the side. */
    private Integer moral;
    /** Modifiers. */
    private BattleDay modifiers;
    /** Losses. */
    private AbstractWithLoss losses;

    /** @return the size. */
    public double getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(double size) {
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
    public BattleDay getModifiers() {
        return modifiers;
    }

    /** @param modifiers the modifiers to set. */
    public void setModifiers(BattleDay modifiers) {
        this.modifiers = modifiers;
    }

    /** @return the losses. */
    public AbstractWithLoss getLosses() {
        return losses;
    }

    /** @param losses the losses to set. */
    public void setLosses(AbstractWithLoss losses) {
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
