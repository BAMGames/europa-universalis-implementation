package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.AbstractWithLoss;

/**
 * Entity for the combat result table.
 *
 * @author MKL.
 */
public class CombatResult extends AbstractWithLoss {
    /** Column used. */
    private String column;
    /** Result of the modified dice. */
    private Integer dice;

    /** @return the column. */
    public String getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(String column) {
        this.column = column;
    }

    /** @return the dice. */
    public Integer getDice() {
        return dice;
    }

    /** @param dice the dice to set. */
    public void setDice(Integer dice) {
        this.dice = dice;
    }
}
