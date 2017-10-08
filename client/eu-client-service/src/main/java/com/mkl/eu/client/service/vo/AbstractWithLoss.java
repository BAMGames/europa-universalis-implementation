package com.mkl.eu.client.service.vo;

/**
 * Abstract for VO that has the notion of losses.
 *
 * @author MKL.
 */
public class AbstractWithLoss extends EuObject {
    /** Number of round loss. */
    private Integer roundLoss;
    /** Number of third loss (between 0 and 2). */
    private Integer thirdLoss;
    /** Number of moral loss. */
    private Integer moraleLoss;

    /** @return the roundLoss. */
    public Integer getRoundLoss() {
        return roundLoss;
    }

    /** @param roundLoss the roundLoss to set. */
    public void setRoundLoss(Integer roundLoss) {
        this.roundLoss = roundLoss;
    }

    /** @return the thirdLoss. */
    public Integer getThirdLoss() {
        return thirdLoss;
    }

    /** @param thirdLoss the thirdLoss to set. */
    public void setThirdLoss(Integer thirdLoss) {
        this.thirdLoss = thirdLoss;
    }

    /** @return the moraleLoss. */
    public Integer getMoraleLoss() {
        return moraleLoss;
    }

    /** @param moraleLoss the moraleLoss to set. */
    public void setMoraleLoss(Integer moraleLoss) {
        this.moraleLoss = moraleLoss;
    }
}
