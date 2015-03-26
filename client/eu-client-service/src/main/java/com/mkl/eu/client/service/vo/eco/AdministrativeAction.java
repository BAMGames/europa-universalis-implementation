package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.Country;
import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.AdminActionResultEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;

/**
 * Administrative action of a country at a given turn.
 * A country can have zero or multiple administrative actions at a given turn.
 *
 * @author MKL
 */
public class AdministrativeAction extends EuObject {
    /** Owner of the administrative action. */
    private Country owner;
    /** Turn of the administrative action. */
    private Integer turn;
    /** Type of the administrative action (ie MNU, DTI, COL, Exc Levies,...). */
    private AdminActionTypeEnum type;
    /** Column to test the administrative action. */
    private Integer column;
    /** Bonus to the die of the test of the administrative action. */
    private Integer bonus;
    /** Result of the die of the test of the administrative action. */
    private Integer die;
    /** Result of the administrative action. */
    private AdminActionResultEnum result;

    /** @return the owner. */
    public Country getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(Country owner) {
        this.owner = owner;
    }

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the type. */
    public AdminActionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(AdminActionTypeEnum type) {
        this.type = type;
    }

    /** @return the column. */
    public Integer getColumn() {
        return column;
    }

    /** @param column the column to set. */
    public void setColumn(Integer column) {
        this.column = column;
    }

    /** @return the bonus. */
    public Integer getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(Integer bonus) {
        this.bonus = bonus;
    }

    /** @return the die. */
    public Integer getDie() {
        return die;
    }

    /** @param die the die to set. */
    public void setDie(Integer die) {
        this.die = die;
    }

    /** @return the result. */
    public AdminActionResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(AdminActionResultEnum result) {
        this.result = result;
    }
}
