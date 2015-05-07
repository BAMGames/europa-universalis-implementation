package com.mkl.eu.service.service.persistence.board;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;

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
}
