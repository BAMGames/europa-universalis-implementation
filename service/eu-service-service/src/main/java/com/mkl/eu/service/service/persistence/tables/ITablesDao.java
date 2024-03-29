package com.mkl.eu.service.service.persistence.tables;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.tables.*;

import java.util.List;

/**
 * Interface of the Tables DAO.
 *
 * @author MKL.
 */
public interface ITablesDao extends IGenericDao<TradeIncomeEntity, Long> {
    /**
     * Return the foreign or domestic trade income given the input value and the foreign DTI/FTI.
     *
     * @param inputValue   the blocked trade/land income.
     * @param countryValue the FTI/DTI.
     * @param foreignTrade true/false.
     * @return the foreign or domestic trade income given the input value and the foreign DTI/FTI.
     */
    int getTradeIncome(int inputValue, int countryValue, boolean foreignTrade);

    /**
     * @return the periods tables.
     */
    List<PeriodEntity> getPeriods();

    /**
     * @return the technologies tables.
     */
    List<TechEntity> getTechs();

    /**
     * @return the basic forces tables.
     */
    List<BasicForceTableEntity> getBasicForces();

    /**
     * @return the units tables.
     */
    List<UnitEntity> getUnits();

    /**
     * @return the actions limits tables.
     */
    List<LimitTableEntity> getLimits();

    /**
     * @return the results tables.
     */
    List<ResultEntity> getResults();

    /**
     * @return the battle tech tables.
     */
    List<BattleTechEntity> getBattleTechs();

    /**
     * @return the combat result tables.
     */
    List<CombatResultEntity> getCombatResults();

    /**
     * @return the army class tables.
     */
    List<ArmyClasseEntity> getArmyClasses();

    /**
     * @return the army artillery tables.
     */
    List<ArmyArtilleryEntity> getArmyArtilleries();

    /**
     * @return the artillery siege tables.
     */
    List<ArtillerySiegeEntity> getArtillerySieges();

    /**
     * @return the fortress resistance tables.
     */
    List<FortressResistanceEntity> getFortressResistance();

    /**
     * @return the assault result tables.
     */
    List<AssaultResultEntity> getAssaultResults();

    /**
     * @return the exchequer result tables.
     */
    List<ExchequerEntity> getExchequers();

    /**
     * @return the leader tables.
     */
    List<LeaderEntity> getLeaders();

    /**
     * @return the discovery tables.
     */
    List<DiscoveryTableEntity> getDiscoveries();

    /**
     * @return the attrition land in Europe tables.
     */
    List<AttritionLandEuropeEntity> getAttritionLandEurope();

    /**
     * @return the attrition naval or rotw tables.
     */
    List<AttritionOtherEntity> getAttritionOther();
}
