package com.mkl.eu.client.service.vo.tables;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all tables described in the appendix.
 *
 * @author MKL.
 */
public class Tables {
    /** List of periods. */
    private List<Period> periods = new ArrayList<>();
    /** List of technologies. */
    private List<Tech> techs = new ArrayList<>();
    /** Foreign trade income tables. */
    private List<TradeIncome> foreignTrades = new ArrayList<>();
    /** Domestic trade incomes. */
    private List<TradeIncome> domesticTrades = new ArrayList<>();
    /** Periods of the game. */
//    private List<Period>
    /** Basic forces of major powers. */
    private List<BasicForce> basicForces = new ArrayList<>();
    /** Unit purchase and maintenance costs of major powers. */
    private List<Unit> units = new ArrayList<>();
    /** Actions limits of major powers. */
    private List<Limit> limits = new ArrayList<>();
    /** Results of actions. */
    private List<Result> results = new ArrayList<>();
    /** Techs in battle. */
    private List<BattleTech> battleTechs = new ArrayList<>();
    /** Results of combats. */
    private List<CombatResult> combatResults = new ArrayList<>();
    /** Size of army classes. */
    private List<ArmyClasse> armyClasses = new ArrayList<>();
    /** Artillery of armies. */
    private List<ArmyArtillery> armyArtilleries = new ArrayList<>();
    /** Bonus of artilleries on sieges. */
    private List<ArtillerySiege> artillerySieges = new ArrayList<>();

    /** @return the periods. */
    public List<Period> getPeriods() {
        return periods;
    }

    /** @param periods the periods to set. */
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    /** @return the techs. */
    public List<Tech> getTechs() {
        return techs;
    }

    /** @param techs the techs to set. */
    public void setTechs(List<Tech> techs) {
        this.techs = techs;
    }

    /** @return the foreignTrades. */
    public List<TradeIncome> getForeignTrades() {
        return foreignTrades;
    }

    /** @param foreignTrades the foreignTrades to set. */
    public void setForeignTrades(List<TradeIncome> foreignTrades) {
        this.foreignTrades = foreignTrades;
    }

    /** @return the domesticTrades. */
    public List<TradeIncome> getDomesticTrades() {
        return domesticTrades;
    }

    /** @param domesticTrades the domesticTrades to set. */
    public void setDomesticTrades(List<TradeIncome> domesticTrades) {
        this.domesticTrades = domesticTrades;
    }

    /** @return the basicForces. */
    public List<BasicForce> getBasicForces() {
        return basicForces;
    }

    /** @param basicForces the basicForces to set. */
    public void setBasicForces(List<BasicForce> basicForces) {
        this.basicForces = basicForces;
    }

    /** @return the units. */
    public List<Unit> getUnits() {
        return units;
    }

    /** @param units the units to set. */
    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    /** @return the limits. */
    public List<Limit> getLimits() {
        return limits;
    }

    /** @param limits the limits to set. */
    public void setLimits(List<Limit> limits) {
        this.limits = limits;
    }

    /** @return the results. */
    public List<Result> getResults() {
        return results;
    }

    /** @param results the results to set. */
    public void setResults(List<Result> results) {
        this.results = results;
    }

    /** @return the battleTechs. */
    public List<BattleTech> getBattleTechs() {
        return battleTechs;
    }

    /** @param battleTechs the battleTechs to set. */
    public void setBattleTechs(List<BattleTech> battleTechs) {
        this.battleTechs = battleTechs;
    }

    /** @return the combatResults. */
    public List<CombatResult> getCombatResults() {
        return combatResults;
    }

    /** @param combatResults the combatResults to set. */
    public void setCombatResults(List<CombatResult> combatResults) {
        this.combatResults = combatResults;
    }

    /** @return the armyClasses. */
    public List<ArmyClasse> getArmyClasses() {
        return armyClasses;
    }

    /** @param armyClasses the armyClasses to set. */
    public void setArmyClasses(List<ArmyClasse> armyClasses) {
        this.armyClasses = armyClasses;
    }

    /** @return the armyArtilleries. */
    public List<ArmyArtillery> getArmyArtilleries() {
        return armyArtilleries;
    }

    /** @param armyArtilleries the armyArtilleries to set. */
    public void setArmyArtilleries(List<ArmyArtillery> armyArtilleries) {
        this.armyArtilleries = armyArtilleries;
    }

    /** @return the artillerySieges. */
    public List<ArtillerySiege> getArtillerySieges() {
        return artillerySieges;
    }

    /** @param artillerySieges the artillerySieges to set. */
    public void setArtillerySieges(List<ArtillerySiege> artillerySieges) {
        this.artillerySieges = artillerySieges;
    }
}
