package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.Losses;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;

import javax.persistence.Embeddable;

/**
 * Losses taken by a side in a battle.
 *
 * @author MKL.
 */
@Embeddable
public class BattleLossesEntity extends AbstractWithLossEntity {
    /**
     * Add losses to current ones.
     *
     * @param losses to add.
     * @return the new total losses.
     */
    public BattleLossesEntity add(Losses losses) {
        setMoraleLoss(CommonUtil.add(getMoraleLoss(), losses.getMoraleLoss()));
        Integer thirdLosses = CommonUtil.add(getThirdLoss(), losses.getThirdLoss());
        Integer remain = null;
        if (thirdLosses != null && thirdLosses >= 3) {
            remain = thirdLosses / 3;
            thirdLosses = thirdLosses % 3;
        }
        setThirdLoss(thirdLosses);
        setRoundLoss(CommonUtil.add(getRoundLoss(), losses.getRoundLoss(), remain));

        return this;
    }

    /**
     * Round the losses to the closest integer for european battles.
     *
     * @return the new losses.
     */
    public BattleLossesEntity roundToClosestInteger() {
        if (getThirdLoss() != null) {
            if (getThirdLoss() == 2) {
                setRoundLoss(getRoundLoss() == null ? 1 : getRoundLoss() + 1);
            }
            setThirdLoss(0);
        }

        return this;
    }

    /**
     * Sets the losses at max to the given size. Does nothing if already lower.
     *
     * @param size the size.
     * @return the new losses.
     */
    public BattleLossesEntity maxToSize(double size) {
        if (getTotalThird() > 3 * size) {
            Integer third = (int) (3 * size);
            setRoundLoss(third / 3);
            setThirdLoss(third % 3);
        }

        return this;
    }
}
