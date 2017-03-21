package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.CompetitionTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatic competition (normal competitions are administrative actions).
 *
 * @author MKL.
 */
public class Competition extends EuObject {
    /** Type of competition. */
    private CompetitionTypeEnum type;
    /** Province where the competition occurs. */
    private String province;
    /** Turn of the competition. */
    private Integer turn;
    /** The rounds of the competition. */
    private List<CompetitionRound> rounds = new ArrayList<>();

    /** @return the type. */
    public CompetitionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CompetitionTypeEnum type) {
        this.type = type;
    }

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the rounds. */
    public List<CompetitionRound> getRounds() {
        return rounds;
    }

    /** @param rounds the rounds to set. */
    public void setRounds(List<CompetitionRound> rounds) {
        this.rounds = rounds;
    }
}
