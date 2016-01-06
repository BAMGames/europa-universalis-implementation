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
     *
     * @param country to check.
     * @param idGame  id of the game.
     * @return the patrons of the country.
     */
    List<String> getPatrons(String country, Long idGame);

    /**
     * Returns the vassals (and annexed) minors of a country.
     *
     * @param country owner of the vassals/annexed.
     * @param idGame  id of the game.
     * @return the vassals of the country.
     */
    List<String> getVassals(String country, Long idGame);
}
