package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.ResultEnum;

/**
 * Administrative action of a country at a given turn.
 * A country can have zero or multiple administrative actions at a given turn.
 *
 * @author MKL
 */
public class AdministrativeAction extends EuObject {
    /** Turn of the administrative action. */
    private Integer turn;
    /** Type of the administrative action (ie MNU, DTI, COL, Exc Levies,...). */
    private AdminActionTypeEnum type;
    /** Cost of the administrative action. */
    private Integer cost;
    /** Column to test the administrative action. */
    private Integer column;
    /** Bonus to the die of the test of the administrative action. */
    private Integer bonus;
    /** Result of the die of the test of the administrative action. */
    private Integer die;
    /** Result of the die (without bonus) of an eventual secondary test of the administrative action (often test under FTI). */
    private Integer secondaryDie;
    /** Result of the administrative action. */
    private ResultEnum result;
    /** Secondary result of the administrative action (when secondary tests are needed). */
    private Boolean secondaryResult;
    /** Status of the administrative action. */
    private AdminActionStatusEnum status;
    /** Eventual if of object subject of the administrative action. */
    private Long idObject;
    /** Eventual name of the province subject of the administrative action. */
    private String province;
    /** Eventual type of counter face subject of the administrative action. */
    private CounterFaceTypeEnum counterFaceType;

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

    /** @return the cost. */
    public Integer getCost() {
        return cost;
    }

    /** @param cost the cost to set. */
    public void setCost(Integer cost) {
        this.cost = cost;
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

    /** @return the secondaryDie. */
    public Integer getSecondaryDie() {
        return secondaryDie;
    }

    /** @param secondaryDie the secondaryDie to set. */
    public void setSecondaryDie(Integer secondaryDie) {
        this.secondaryDie = secondaryDie;
    }

    /** @return the result. */
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }

    /** @return the secondaryResult. */
    public Boolean isSecondaryResult() {
        return secondaryResult;
    }

    /** @param secondaryResult the secondaryResult to set. */
    public void setSecondaryResult(Boolean secondaryResult) {
        this.secondaryResult = secondaryResult;
    }

    /** @return the status. */
    public AdminActionStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(AdminActionStatusEnum status) {
        this.status = status;
    }

    /** @return the idObject. */
    public Long getIdObject() {
        return idObject;
    }

    /** @param idObject the idObject to set. */
    public void setIdObject(Long idObject) {
        this.idObject = idObject;
    }

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the counterFaceType. */
    public CounterFaceTypeEnum getCounterFaceType() {
        return counterFaceType;
    }

    /** @param counterFaceType the counterFaceType to set. */
    public void setCounterFaceType(CounterFaceTypeEnum counterFaceType) {
        this.counterFaceType = counterFaceType;
    }
}
