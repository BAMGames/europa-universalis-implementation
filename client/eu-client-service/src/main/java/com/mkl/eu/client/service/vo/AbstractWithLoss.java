package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.service.vo.tables.Tech;

/**
 * Abstract for VO that has the notion of losses.
 *
 * @author MKL.
 */
public class AbstractWithLoss extends EuObject implements Losses {
    /** Number of round loss. */
    private Integer roundLoss;
    /** Number of third loss (between 0 and 2). */
    private Integer thirdLoss;
    /** Number of moral loss. */
    private Integer moraleLoss;

    /**
     * @param tech the tech.
     * @return the loss adjusted to tech (fire damage mitigated in early techs).
     */
    public AbstractWithLoss adjustToTech(String tech) {
        AbstractWithLoss loss = this;
        switch (tech) {
            case Tech.RENAISSANCE:
                loss = new StandardLoss(null, null, getMoraleLoss());
                break;
            case Tech.ARQUEBUS:
                Integer thirdLosses = getThirdLoss() == null ? 0 : getThirdLoss();
                if (getRoundLoss() != null) {
                    thirdLosses += 3 * getRoundLoss();
                }
                thirdLosses /= 2;
                loss = new StandardLoss(thirdLosses / 3, thirdLosses % 3, getMoraleLoss());
                break;
            case Tech.GALLEASS:
                // TODO NGD without VGD
                break;
        }

        return loss;
    }

    /**
     * @return the total third of this losses (transform each round loss in three third losses).
     */
    public int getTotalThird() {
        int third = 0;
        if (getRoundLoss() != null) {
            third += 3 * getRoundLoss();
        }
        if (getThirdLoss() != null) {
            third += getThirdLoss();
        }
        return third;
    }

    /** {@inheritDoc} */
    @Override
    public Integer getRoundLoss() {
        return roundLoss;
    }

    /** @param roundLoss the roundLoss to set. */
    public void setRoundLoss(Integer roundLoss) {
        this.roundLoss = roundLoss;
    }

    /** {@inheritDoc} */
    @Override
    public Integer getThirdLoss() {
        return thirdLoss;
    }

    /** @param thirdLoss the thirdLoss to set. */
    public void setThirdLoss(Integer thirdLoss) {
        this.thirdLoss = thirdLoss;
    }

    /** {@inheritDoc} */
    @Override
    public Integer getMoraleLoss() {
        return moraleLoss;
    }

    /** @param moraleLoss the moraleLoss to set. */
    public void setMoraleLoss(Integer moraleLoss) {
        this.moraleLoss = moraleLoss;
    }

    /**
     * Default implementation.
     */
    private static class StandardLoss extends AbstractWithLoss {
        /**
         * Constructor.
         *
         * @param roundLoss the roundLoss to set.
         * @param thirdLoss the thirdLoss to set.
         * @param moralLoss the moralLoss to set.
         */
        public StandardLoss(Integer roundLoss, Integer thirdLoss, Integer moralLoss) {
            setRoundLoss(roundLoss);
            setThirdLoss(thirdLoss);
            setMoraleLoss(moralLoss);
        }
    }

    /**
     * Creates a Loss entity.
     * If third are more than 3, then it will creates the round losses accordingly.
     *
     * @param third number of third of the losses.
     * @return the loss entity created.
     */
    public static AbstractWithLoss create(Integer third) {
        AbstractWithLoss result = new AbstractWithLoss();
        result.setRoundLoss(third / 3);
        result.setThirdLoss(third % 3);
        return result;
    }
}
