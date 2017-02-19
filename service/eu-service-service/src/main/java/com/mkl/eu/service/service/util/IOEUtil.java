package com.mkl.eu.service.service.util;

import com.mkl.eu.client.service.vo.enumeration.WarStatusEnum;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;

import java.util.List;

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
     * @param game                 the game.
     * @param country              the country.
     * @param includeInterventions flag to include enemy countries in limited or foreign intervention.
     * @return the list of country that are at war with the input country.
     */
    List<String> getEnemies(GameEntity game, PlayableCountryEntity country, boolean includeInterventions);

    /**
     * @param stack the stack to check mobility.
     * @return <code>true</code> if the stack is mobile (can be moved), <code>false</code> otherwise.
     */
    boolean isMobile(StackEntity stack);
}
