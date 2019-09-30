package com.mkl.eu.client.service.vo.military;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.enumeration.SiegeStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.SiegeUndermineResultEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * VO for a siege.
 *
 * @author MKL.
 */
public class Siege extends EuObject {
    /** Status. */
    private SiegeStatusEnum status;
    /** Province where the competition occurs. */
    private String province;
    /** Turn of the competition. */
    private Integer turn;
    /** Level of the besieged fortress. */
    private int fortressLevel;
    /** If there was a breach in this siege previously. */
    private boolean breach;
    /** Bonus for undermining. */
    private int bonus;
    /** If mode is UNDERMINE, the undermine unmodified die. */
    private int undermineDie;
    /** If mode is UNDERMINE, the undermine result (in case of multiple results, the one selected by the besieger). */
    private SiegeUndermineResultEnum undermineResult;
    /** Flag saying that the fortress has fallen to the besieger. */
    private boolean fortressFalls;
    /** Phasing side for a possible assault. */
    private SiegeSide phasing = new SiegeSide();
    /** Non phasing side for a possible assault. */
    private SiegeSide nonPhasing = new SiegeSide();
    /** Counters involved in the battle. */
    private List<SiegeCounter> counters = new ArrayList<>();
    /** War in which the siege occurs. */
    private WarLight war;
    /** If the besieging side of the siege is the offensive side of the war. */
    private boolean besiegingOffensive;

    /** @return the status. */
    public SiegeStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(SiegeStatusEnum status) {
        this.status = status;
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

    /** @return the fortressLevel. */
    public int getFortressLevel() {
        return fortressLevel;
    }

    /** @param fortressLevel the fortressLevel to set. */
    public void setFortressLevel(int fortressLevel) {
        this.fortressLevel = fortressLevel;
    }

    /** @return the breach. */
    public boolean isBreach() {
        return breach;
    }

    /** @param breach the breach to set. */
    public void setBreach(boolean breach) {
        this.breach = breach;
    }

    /** @return the bonus. */
    public int getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    /** @return the undermineDie. */
    public int getUndermineDie() {
        return undermineDie;
    }

    /** @param undermineDie the undermineDie to set. */
    public void setUndermineDie(int undermineDie) {
        this.undermineDie = undermineDie;
    }

    /** @return the undermineResult. */
    public SiegeUndermineResultEnum getUndermineResult() {
        return undermineResult;
    }

    /** @param undermineResult the undermineResult to set. */
    public void setUndermineResult(SiegeUndermineResultEnum undermineResult) {
        this.undermineResult = undermineResult;
    }

    /** @return the fortressFalls. */
    public boolean isFortressFalls() {
        return fortressFalls;
    }

    /** @param fortressFalls the fortressFalls to set. */
    public void setFortressFalls(boolean fortressFalls) {
        this.fortressFalls = fortressFalls;
    }

    /** @return the phasing. */
    public SiegeSide getPhasing() {
        return phasing;
    }

    /** @param phasing the phasing to set. */
    public void setPhasing(SiegeSide phasing) {
        this.phasing = phasing;
    }

    /** @return the nonPhasing. */
    public SiegeSide getNonPhasing() {
        return nonPhasing;
    }

    /** @param nonPhasing the nonPhasing to set. */
    public void setNonPhasing(SiegeSide nonPhasing) {
        this.nonPhasing = nonPhasing;
    }

    /** @return the counters. */
    public List<SiegeCounter> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(List<SiegeCounter> counters) {
        this.counters = counters;
    }

    /** @return the war. */
    public WarLight getWar() {
        return war;
    }

    /** @param war the war to set. */
    public void setWar(WarLight war) {
        this.war = war;
    }

    /** @return the besiegingOffensive. */
    public boolean isBesiegingOffensive() {
        return besiegingOffensive;
    }

    /** @param besiegingOffensive the besiegingOffensive to set. */
    public void setBesiegingOffensive(boolean besiegingOffensive) {
        this.besiegingOffensive = besiegingOffensive;
    }
}
