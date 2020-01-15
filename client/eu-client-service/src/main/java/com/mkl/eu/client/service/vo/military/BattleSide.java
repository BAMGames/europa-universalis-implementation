package com.mkl.eu.client.service.vo.military;

import com.mkl.eu.client.service.vo.AbstractWithLoss;

/**
 * Modifiers of each side in battle.
 *
 * @author MKL.
 */
public class BattleSide {
    /** Flag saying that the side has selected its forces. */
    private Boolean forces;
    /** Flag saying that the side has selected its losses. */
    private Boolean lossesSelected;
    /** Flag saying that the side has selected its retreat. */
    private Boolean retreatSelected;
    /** Code of the leader commanding the side. */
    private String leader;
    /** Country commanding the side. */
    private String country;
    /** Size of the side. */
    private Double size;
    /** Technology of the side. */
    private String tech;
    /** Fire column of the side. */
    private String fireColumn;
    /** Shock column of the side. */
    private String shockColumn;
    /** Moral of the side. */
    private Integer moral;
    /** First day modifiers. */
    private BattleDay firstDay = new BattleDay();
    /** Second day modifiers. */
    private BattleDay secondDay = new BattleDay();
    /** Modifier for the pursuit phase. */
    private int pursuitMod;
    /** Unmodified die roll for the pursuit phase. */
    private Integer pursuit;
    /** Losses. */
    private AbstractWithLoss losses = new AbstractWithLoss();
    /** Size diff. */
    private Integer sizeDiff;
    /** Unmodified die roll for the retreat. */
    private Integer retreat;
    /** Unmodified die roll for the leader death test. */
    private Integer leaderCheck;
    /** Number of round wounded of the leader (-1 for death). */
    private Integer leaderWounds;

    /** @return the forces. */
    public Boolean isForces() {
        return forces;
    }

    /** @param forces the forces to set. */
    public void setForces(Boolean forces) {
        this.forces = forces;
    }

    /** @return the leader. */
    public String getLeader() {
        return leader;
    }

    /** @param leader the leader to set. */
    public void setLeader(String leader) {
        this.leader = leader;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the size. */
    public Double getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(Double size) {
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

    /** @return the fireColumn. */
    public String getFireColumn() {
        return fireColumn;
    }

    /** @param fireColumn the fireColumn to set. */
    public void setFireColumn(String fireColumn) {
        this.fireColumn = fireColumn;
    }

    /** @return the shockColumn. */
    public String getShockColumn() {
        return shockColumn;
    }

    /** @param shockColumn the shockColumn to set. */
    public void setShockColumn(String shockColumn) {
        this.shockColumn = shockColumn;
    }

    /** @return the moral. */
    public Integer getMoral() {
        return moral;
    }

    /** @param moral the moral to set. */
    public void setMoral(Integer moral) {
        this.moral = moral;
    }

    /** @return the firstDay. */
    public BattleDay getFirstDay() {
        return firstDay;
    }

    /** @param firstDay the firstDay to set. */
    public void setFirstDay(BattleDay firstDay) {
        this.firstDay = firstDay;
    }

    /** @return the secondDay. */
    public BattleDay getSecondDay() {
        return secondDay;
    }

    /** @param secondDay the secondDay to set. */
    public void setSecondDay(BattleDay secondDay) {
        this.secondDay = secondDay;
    }

    /** @return the pursuitMod. */
    public int getPursuitMod() {
        return pursuitMod;
    }

    /** @param pursuitMod the pursuitMod to set. */
    public void setPursuitMod(int pursuitMod) {
        this.pursuitMod = pursuitMod;
    }

    /**
     * Add the pursuit modifier to the current one.
     *
     * @param pursuit the pursuit modifier to add.
     */
    public void addPursuit(int pursuit) {
        this.pursuitMod += pursuit;
    }

    /** @return the pursuit. */
    public Integer getPursuit() {
        return pursuit;
    }

    /** @param pursuit the pursuit to set. */
    public void setPursuit(Integer pursuit) {
        this.pursuit = pursuit;
    }

    /** @return the losses. */
    public AbstractWithLoss getLosses() {
        return losses;
    }

    /** @param losses the losses to set. */
    public void setLosses(AbstractWithLoss losses) {
        this.losses = losses;
    }

    /** @return the sizeDiff. */
    public Integer getSizeDiff() {
        return sizeDiff;
    }

    /** @param sizeDiff the sizeDiff to set. */
    public void setSizeDiff(Integer sizeDiff) {
        this.sizeDiff = sizeDiff;
    }

    /** @return the retreat. */
    public Integer getRetreat() {
        return retreat;
    }

    /** @param retreat the retreat to set. */
    public void setRetreat(Integer retreat) {
        this.retreat = retreat;
    }

    /** @return the lossesSelected. */
    public Boolean isLossesSelected() {
        return lossesSelected;
    }

    /** @param lossesSelected the lossesSelected to set. */
    public void setLossesSelected(Boolean lossesSelected) {
        this.lossesSelected = lossesSelected;
    }

    /** @return the retreatSelected. */
    public Boolean isRetreatSelected() {
        return retreatSelected;
    }

    /** @param retreatSelected the retreatSelected to set. */
    public void setRetreatSelected(Boolean retreatSelected) {
        this.retreatSelected = retreatSelected;
    }

    /** @return the leaderCheck. */
    public Integer getLeaderCheck() {
        return leaderCheck;
    }

    /** @param leaderCheck the leaderCheck to set. */
    public void setLeaderCheck(Integer leaderCheck) {
        this.leaderCheck = leaderCheck;
    }

    /** @return the leaderWounds. */
    public Integer getLeaderWounds() {
        return leaderWounds;
    }

    /** @param leaderWounds the leaderWounds to set. */
    public void setLeaderWounds(Integer leaderWounds) {
        this.leaderWounds = leaderWounds;
    }
}
