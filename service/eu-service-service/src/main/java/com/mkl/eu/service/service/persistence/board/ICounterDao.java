package com.mkl.eu.service.service.persistence.board;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

import java.util.List;

/**
 * Interface of the Counter DAO.
 *
 * @author MKL.
 */
public interface ICounterDao extends IGenericDao<CounterEntity, Long> {
    /**
     * Returns the counter from its id and the id of the game (to check if the counter belongs to the right game).
     *
     * @param idCounter id of the counter.
     * @param idGame    id of the game.
     * @return the counter if it exists.
     */
    CounterEntity getCounter(Long idCounter, Long idGame);

    /**
     * Returns the patrons (can be multiple for rotw countries) of the country.
     * A country is always a patron of itself.
     *
     * @param country to check.
     * @param idGame  id of the game.
     * @return the patrons of the country.
     */
    List<String> getPatrons(String country, Long idGame);

    /**
     * Returns the minors of the country.
     *
     * @param country to check.
     * @param idGame  id of the game.
     * @return the minors of the country.
     */
    List<String> getMinors(String country, Long idGame);

    /**
     * Returns the vassals (and annexed) minors of a country.
     *
     * @param country owner of the vassals/annexed.
     * @param idGame  id of the game.
     * @return the vassals of the country.
     */
    List<String> getVassals(String country, Long idGame);

    /**
     * @param province province whose we want the neighbor owners.
     * @param idGame   id of the game.
     * @return the countries owning european provinces bordering the province.
     */
    List<String> getNeighboringOwners(String province, Long idGame);

    /**
     * @param country country of the national territory.
     * @param enemies enemies of the country.
     * @param idGame  id of the game.
     * @return list of national province names of the country where there is an enemy army.
     */
    List<String> getNationalTerritoriesUnderAttack(String country, List<String> enemies, Long idGame);

    /**
     * @param idGame id of the game.
     * @return the total gold exploited in the Rotw.
     */
    int getGoldExploitedRotw(Long idGame);

    /**
     * @param country the country.
     * @param idGame  id of the game.
     * @return the total gold exploited by a country in America.
     */
    int getGoldExploitedAmerica(String country, Long idGame);
}
