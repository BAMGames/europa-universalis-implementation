package com.mkl.eu.client.service.vo.military;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.enumeration.BattleEndEnum;
import com.mkl.eu.client.service.vo.enumeration.BattleStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.BattleWinnerEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * VO for a battle (land or naval).
 *
 * @author MKL.
 */
public class Battle extends EuObject {
    /** Id. */
    private Long id;
    /** Province where the battle occurs. */
    private String province;
    /** Turn of the game when the battle occurred. */
    private Integer turn;
    /** Status of the battle. */
    private BattleStatusEnum status;
    /** Cause of battle end. */
    private BattleEndEnum end;
    /** Winner of battle. */
    private BattleWinnerEnum winner;
    /** Phasing side. */
    private BattleSide phasing;
    /** Non phasing side. */
    private BattleSide nonPhasing;
    /** Counters involved in the battle. */
    private List<BattleCounter> counters = new ArrayList<>();
    /** War in which the battle occurs. */
    private War war;
    /** If the phasing side of the battle is the offensive side of the war. */
    private boolean phasingOffensive;

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

    /** @return the status. */
    public BattleStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(BattleStatusEnum status) {
        this.status = status;
    }

    /** @return the end. */
    public BattleEndEnum getEnd() {
        return end;
    }

    /** @param end the end to set. */
    public void setEnd(BattleEndEnum end) {
        this.end = end;
    }

    /** @return the winner. */
    public BattleWinnerEnum getWinner() {
        return winner;
    }

    /** @param winner the winner to set. */
    public void setWinner(BattleWinnerEnum winner) {
        this.winner = winner;
    }

    /** @return the phasingSide. */
    public BattleSide getPhasing() {
        return phasing;
    }

    /** @param phasingSide the phasingSide to set. */
    public void setPhasing(BattleSide phasingSide) {
        this.phasing = phasingSide;
    }

    /** @return the nonPhasingSide. */
    public BattleSide getNonPhasing() {
        return nonPhasing;
    }

    /** @param nonPhasingSide the nonPhasingSide to set. */
    public void setNonPhasing(BattleSide nonPhasingSide) {
        this.nonPhasing = nonPhasingSide;
    }

    /** @return the counters. */
    public List<BattleCounter> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(List<BattleCounter> counters) {
        this.counters = counters;
    }

    /** @return the war. */
    public War getWar() {
        return war;
    }

    /** @param war the war to set. */
    public void setWar(War war) {
        this.war = war;
    }

    /** @return the phasingOffensive. */
    public boolean isPhasingOffensive() {
        return phasingOffensive;
    }

    /** @param phasingOffensive the phasingOffensive to set. */
    public void setPhasingOffensive(boolean phasingOffensive) {
        this.phasingOffensive = phasingOffensive;
    }
}
