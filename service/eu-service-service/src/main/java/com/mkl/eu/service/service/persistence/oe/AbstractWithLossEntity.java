package com.mkl.eu.service.service.persistence.oe;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Description of the class.
 *
 * @author MKL.
 */
@MappedSuperclass
public class AbstractWithLossEntity {
    /** Number of round loss. */
    private Integer roundLoss;
    /** Number of third loss (between 0 and 2). */
    private Integer thirdLoss;
    /** Number of moral loss. */
    private Integer moraleLoss;

    /** @return the roundLoss. */
    @Column(name = "ROUND_LOSS")
    public Integer getRoundLoss() {
        return roundLoss;
    }

    /** @param roundLoss the roundLoss to set. */
    public void setRoundLoss(Integer roundLoss) {
        this.roundLoss = roundLoss;
    }

    /** @return the thirdLoss. */
    @Column(name = "THIRD_LOSS")
    public Integer getThirdLoss() {
        return thirdLoss;
    }

    /** @param thirdLoss the thirdLoss to set. */
    public void setThirdLoss(Integer thirdLoss) {
        this.thirdLoss = thirdLoss;
    }

    /** @return the moraleLoss. */
    @Column(name = "MORALE_LOSS")
    public Integer getMoraleLoss() {
        return moraleLoss;
    }

    /** @param moraleLoss the moraleLoss to set. */
    public void setMoraleLoss(Integer moraleLoss) {
        this.moraleLoss = moraleLoss;
    }
}
