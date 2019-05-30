package com.mkl.eu.service.service.util;

import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.client.service.vo.enumeration.WarStatusEnum;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;

import java.util.List;
import java.util.function.Supplier;

/**
 * Utility for OE class.
 *
 * @author MKL.
 */
public interface IOEUtil {
    /** Mercantile countries that have a higher FTI/DTI. */
    String[] MERCANTILE_COUNTRIES = new String[]{"danemark", "genes", "hollande", "portugal", "provincesne", "suede", "venise"};

    /**
     * @param country whom we want the administrative value.
     * @return the administrative value of a country.
     */
    int getAdministrativeValue(PlayableCountryEntity country);

    /**
     * @param country whom we want the diplomatic value.
     * @return the diplomatic value of a country.
     */
    int getDiplomaticValue(PlayableCountryEntity country);

    /**
     * @param country whom we want the military value.
     * @return the military value of a country.
     */
    int getMilitaryValue(PlayableCountryEntity country);

    /**
     * @param country whom we want the initiative.
     * @return the initiative of the country.
     */
    int getInitiative(PlayableCountryEntity country);

    /**
     * @param country which we want the fti.
     * @param tables  the tables (period is needed in the calculation).
     * @return the fti of the country, which can be minor, given the game (and so the period).
     */
    int getFti(GameEntity game, Tables tables, String country);

    /**
     * @param country which we want the dti.
     * @param tables  the tables (period is needed in the calculation).
     * @return the dti of the country, which can be minor, given the game (and so the period).
     */
    int getDti(GameEntity game, Tables tables, String country);

    /**
     * @param game    game containing all the counters.
     * @param country whom we want the stability.
     * @return the stability of a country.
     */
    int getStability(GameEntity game, String country);

    /**
     * @param game    game containing all the counters.
     * @param country whom we want the technology box.
     * @param land    to wether we want the land or the naval technology advance.
     * @return the number of the box where the appropriate technology counter of the country is.
     */
    int getTechnologyAdvance(GameEntity game, String country, boolean land);

    /**
     * @param province    the province to settle.
     * @param discoveries the provinces that have been discovered.
     * @param sources     the sources (COL/TP/Owned european province) of supply of the settlement.
     * @param friendlies  the friendly terrains.
     * @return <code>true</code> if the province can be settled, <code>false</code> otherwise.
     */
    boolean canSettle(AbstractProvinceEntity province, List<String> discoveries, List<String> sources, List<String> friendlies);

    /**
     * Rolls a die for a non identified country in the given game.
     *
     * @param game    the game.
     * @return the result of a die 10.
     */
    int rollDie(GameEntity game);

    /**
     * Rolls a die for a country in the given game. The country can ben <code>null</code> for general die roll.
     *
     * @param game    the game.
     * @param country the country rolling the die. Can be <code>null</code>.
     * @return the result of a die 10.
     */
    int rollDie(GameEntity game, String country);

    /**
     * Rolls a die for a country in the given game. The country can ben <code>null</code> for general die roll.
     *
     * @param game    the game.
     * @param country the country rolling the die. Can be <code>null</code>.
     * @return the result of a die 10.
     */
    int rollDie(GameEntity game, PlayableCountryEntity country);

    /**
     * Returns the stacks on the province for a given game.
     * The main purpose is to have the possibility to mock this method in tests.
     *
     * @param game     the game.
     * @param province the province.
     * @return the stacks on the province.
     */
    List<StackEntity> getStacksOnProvince(GameEntity game, String province);

    /**
     * @param game    the game.
     * @param country the country.
     * @return the war status of the country.
     */
    WarStatusEnum getWarStatus(GameEntity game, PlayableCountryEntity country);

    /**
     * @param country              the country.
     * @param game                 the game.
     * @param includeInterventions flag to include enemy countries in limited or foreign intervention.
     * @return the list of country that are at war with the input country.
     */
    List<String> getEnemies(PlayableCountryEntity country, GameEntity game, boolean includeInterventions);

    /**
     * @param stack the stack to check mobility.
     * @return <code>true</code> if the stack is mobile (can be moved), <code>false</code> otherwise.
     */
    boolean isMobile(StackEntity stack);

    /**
     * @param provinceFrom       origin.
     * @param provinceTo         destination.
     * @param provinceToFriendly if the provinceTo is friendly (in case of doubt, it is not).
     * @return the move points needed to move from provinceFrom to provinceTo. If provinces
     * are not adjacent, returns -1.
     */
    int getMovePoints(AbstractProvinceEntity provinceFrom, AbstractProvinceEntity provinceTo, boolean provinceToFriendly);

    /**
     * @param province the province.
     * @param game     the game.
     * @return the name of the country owning the province.
     */
    String getOwner(AbstractProvinceEntity province, GameEntity game);

    /**
     * @param province the province.
     * @param game     the game.
     * @return the name of the country controlling the province.
     */
    String getController(AbstractProvinceEntity province, GameEntity game);

    /**
     * @param country the country.
     * @param game    the game.
     * @return the allies of the country (a country is considered allied to itself).
     */
    List<String> getAllies(PlayableCountryEntity country, GameEntity game);

    /**
     * @param country the country.
     * @param game    the game.
     * @return the enemies of the country.
     */
    List<String> getEnemies(PlayableCountryEntity country, GameEntity game);

    /**
     * @param province the province.
     * @param game     the game.
     * @return the natural level of the fortress of the province.
     */
    int getNaturalFortressLevel(AbstractProvinceEntity province, GameEntity game);

    /**
     * @param province the province.
     * @param game     the game.
     * @return the level of the fortress of the province.
     */
    int getFortressLevel(AbstractProvinceEntity province, GameEntity game);

    /**
     * @param country     the country.
     * @param land        <code>true</code> if we want the land tech, naval tech otherwise.
     * @param referential the referential.
     * @param game        the game.
     * @return the technology of the country.
     */
    String getTechnology(String country, boolean land, Referential referential, GameEntity game);

    /**
     * @param counters    stack whom we want the technology.
     * @param land        <code>true</code> if we want the land tech, naval tech otherwise.
     * @param referential the referential.
     * @param tables      the tables.
     * @param game        the game.
     * @return the technology of a List of counters.
     */
    String getTechnology(List<CounterEntity> counters, boolean land, Referential referential, Tables tables, GameEntity game);

    /**
     * @param counters    stack whom we want the artillery bonus.
     * @param referential the referential.
     * @param tables      the tables.
     * @param game        the game.
     * @return the artillery bonus of a List of counters for siege and artillery fire modifier.
     */
    int getArtilleryBonus(List<CounterEntity> counters, Referential referential, Tables tables, GameEntity game);

    /**
     * @param counters    stack whom we want additional information.
     * @param referential the referential.
     * @return a List of ArmyInfo that holds additional information over the counters.
     */
    List<ArmyInfo> getArmyInfo(List<CounterEntity> counters, Referential referential);

    /**
     * @param counters stack whom we want the artillery bonus.
     * @param tables   the tables.
     * @param game     the game.
     * @return the artillery bonus of a List of counters for siege and artillery fire modifier.
     */
    int getArtilleryBonus(List<ArmyInfo> counters, Tables tables, GameEntity game);

    /**
     * @param counters stack whom we want the artillery bonus.
     * @param terrain  the terrain where the battle occurs.
     * @param tables   the tables.
     * @param game     the game.
     * @return <code>true</code> if the stack has a cavalry bonus in this terrain and turn.
     */
    boolean getCavalryBonus(List<ArmyInfo> counters, TerrainEnum terrain, Tables tables, GameEntity game);

    /**
     * @param province   to retreat.
     * @param inFortress to know if the stack will hide in fortress.
     * @param stackSize  size of the stack in case of hiding in the fortress.
     * @param country    doing the retreat.
     * @param game       the game.
     * @return <code>true</code> if the country can retreat in the province.
     */
    boolean canRetreat(AbstractProvinceEntity province, boolean inFortress, double stackSize, PlayableCountryEntity country, GameEntity game);

    /**
     * @param counters the stack of counters.
     * @return <code>true</code> if the stack is veteran.
     */
    boolean isStackVeteran(List<CounterEntity> counters);

    /**
     * @param losses   initial losses.
     * @param sizeDiff size differential between the stacks.
     * @return the loss after size computation.
     */
    AbstractWithLossEntity lossModificationSize(AbstractWithLossEntity losses, Integer sizeDiff);

    /**
     * @param counters of the stack.
     * @param tables   the tables.
     * @param game     the game.
     * @return the size of the stack for size comparison rules.
     */
    Double getArmySize(List<ArmyInfo> counters, Tables tables, GameEntity game);

    /**
     * @param sizeFor     size of the current stack.
     * @param sizeAgainst size of the opposing stack.
     * @return the size diff that the current stacks has over the opposing one.
     */
    Integer getSizeDiff(Double sizeFor, Double sizeAgainst);

    /**
     * @param size    of the stack.
     * @param land    if it is a land battle.
     * @param rollDie for special case. If <code>null</code> then worst case happens.
     * @return the losses mitigation of the stack at the end of the battle because of its small size.
     */
    AbstractWithLossEntity lossesMitigation(Double size, boolean land, Supplier<Integer> rollDie);

    /**
     * @param modifiedRollDie the modified die roll. Can be lower than <code>0</code>.
     * @return the losses of the retreat.
     */
    AbstractWithLossEntity retreat(Integer modifiedRollDie);
}
