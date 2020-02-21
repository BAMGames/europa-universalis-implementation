package com.mkl.eu.client.service.vo.attrition;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.enumeration.AttritionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AttritionTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * VO for an attrition.
 *
 * @author MKL.
 */
public class Attrition extends EuObject {
    /** Turn of the game when the battle occurred. */
    private Integer turn;
    /** Type of the attrition. */
    private AttritionTypeEnum type;
    /** Status of the attrition. */
    private AttritionStatusEnum status;
    /** Counters involved in the attrition. */
    private List<Counter> counters = new ArrayList<>();
    /** Provinces where the stack went through during the attrition. */
    private List<String> provinces = new ArrayList<>();
    /** Size of the stack (in case of movement, it is the maximum size reached). */
    private double size;
    /** Technology of the stack. */
    private String tech;
    /** Bonus to the attrition die roll. */
    private Integer bonus;
    /** Unmodified die roll of the attrition. */
    private Integer die;

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the type. */
    public AttritionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(AttritionTypeEnum type) {
        this.type = type;
    }

    /** @return the status. */
    public AttritionStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(AttritionStatusEnum status) {
        this.status = status;
    }

    /** @return the counters. */
    public List<Counter> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(List<Counter> counters) {
        this.counters = counters;
    }

    /** @return the provinces. */
    public List<String> getProvinces() {
        return provinces;
    }

    /** @param provinces the provinces to set. */
    public void setProvinces(List<String> provinces) {
        this.provinces = provinces;
    }

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
}
