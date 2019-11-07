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
    /** Fortress resistance for assaults. */
    private List<FortressResistance> fortressResistances = new ArrayList<>();
    /** Results of assault. */
    private List<AssaultResult> assaultResults = new ArrayList<>();
    /** Results of exchequer. */
    private List<Exchequer> exchequers = new ArrayList<>();

    /** @return the periods. */
    public List<Period> getPeriods() {
        return periods;
    }

    /** @return the techs. */
    public List<Tech> getTechs() {
        return techs;
    }

    /** @return the foreignTrades. */
    public List<TradeIncome> getForeignTrades() {
        return foreignTrades;
    }

    /** @return the domesticTrades. */
    public List<TradeIncome> getDomesticTrades() {
        return domesticTrades;
    }

    /** @return the basicForces. */
    public List<BasicForce> getBasicForces() {
        return basicForces;
    }

    /** @return the units. */
    public List<Unit> getUnits() {
        return units;
    }

    /** @return the limits. */
    public List<Limit> getLimits() {
        return limits;
    }

    /** @return the results. */
    public List<Result> getResults() {
        return results;
    }

    /** @return the battleTechs. */
    public List<BattleTech> getBattleTechs() {
        return battleTechs;
    }

    /** @return the combatResults. */
    public List<CombatResult> getCombatResults() {
        return combatResults;
    }

    /** @return the armyClasses. */
    public List<ArmyClasse> getArmyClasses() {
        return armyClasses;
    }

    /** @return the armyArtilleries. */
    public List<ArmyArtillery> getArmyArtilleries() {
        return armyArtilleries;
    }

    /** @return the artillerySieges. */
    public List<ArtillerySiege> getArtillerySieges() {
        return artillerySieges;
    }

    /** @return the fortressResistances. */
    public List<FortressResistance> getFortressResistances() {
        return fortressResistances;
    }

    /** @return the assaultResults. */
    public List<AssaultResult> getAssaultResults() {
        return assaultResults;
    }

    /** @return the exchequers. */
    public List<Exchequer> getExchequers() {
        return exchequers;
    }
}
