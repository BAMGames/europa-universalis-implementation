package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * VO for the attrition naval or in rotw table.
 *
 * @author MKL.
 */
public class AttritionOther extends EuObject implements HasDice {
    /** Result of the modified dice. */
    private Integer dice;
    /** Loss percentage of naval or rotw attrition. */
    private Integer lossPercentage;

    /** @return the dice. */
    public Integer getDice() {
        return dice;
    }

    /** @param dice the dice to set. */
    public void setDice(Integer dice) {
        this.dice = dice;
    }

    /** @return the lossPercentage. */
    public Integer getLossPercentage() {
        return lossPercentage;
    }

    /** @param lossPercentage the lossPercentage to set. */
    public void setLossPercentage(Integer lossPercentage) {
        this.lossPercentage = lossPercentage;
    }
}
