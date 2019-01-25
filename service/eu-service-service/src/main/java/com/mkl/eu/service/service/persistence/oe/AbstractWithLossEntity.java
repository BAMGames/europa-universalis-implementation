package com.mkl.eu.service.service.persistence.oe;

import com.mkl.eu.client.service.vo.Losses;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;

/**
 * Abstract for entity that has the notion of losses.
 *
 * @author MKL.
 */
@MappedSuperclass
public class AbstractWithLossEntity implements Losses {
    /** Number of round loss. */
    private Integer roundLoss;
    /** Number of third loss (between 0 and 2). */
    private Integer thirdLoss;
    /** Number of moral loss. */
    private Integer moraleLoss;

    /** {@inheritDoc} */
    public Integer getRoundLoss() {
        return roundLoss;
    }

    /** @param roundLoss the roundLoss to set. */
    public void setRoundLoss(Integer roundLoss) {
        this.roundLoss = roundLoss;
    }

    /** {@inheritDoc} */
    public Integer getThirdLoss() {
        return thirdLoss;
    }

    /** @param thirdLoss the thirdLoss to set. */
    public void setThirdLoss(Integer thirdLoss) {
        this.thirdLoss = thirdLoss;
    }

    /** {@inheritDoc} */
    public Integer getMoraleLoss() {
        return moraleLoss;
    }

    /** @param moraleLoss the moraleLoss to set. */
    public void setMoraleLoss(Integer moraleLoss) {
        this.moraleLoss = moraleLoss;
    }

    /**
     * @return the total third of this losses (transform each round loss in three third losses).
     */
    @Transient
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

    /**
     * @param size of a stack.
     * @return if the losses are more than a size of a stack.
     */
    @Transient
    public boolean isGreaterThanSize(double size) {
        return getTotalThird() + EPSILON >= 3 * size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (roundLoss != null) {
            sb.append(roundLoss).append("-");
        }
        if (thirdLoss != null) {
            sb.append(thirdLoss).append("/3");
        }
        if (sb.length() == 0) {
            sb.append("0");
        }
        return sb.toString();
    }

    /**
     * Creates a Loss entity.
     * If third are more than 3, then it will creates the round losses accordingly.
     *
     * @param third number of third of the losses.
     * @return the loss entity created.
     */
    public static AbstractWithLossEntity create(Integer third) {
        AbstractWithLossEntity result = new AbstractWithLossEntity();
        result.setRoundLoss(third / 3);
        result.setThirdLoss(third % 3);
        return result;
    }
}
