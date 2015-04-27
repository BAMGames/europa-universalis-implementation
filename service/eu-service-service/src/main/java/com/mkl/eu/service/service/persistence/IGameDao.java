package com.mkl.eu.service.service.persistence;

import com.mkl.eu.service.service.persistence.oe.GameEntity;

/**
 * Interface of the Game DAO.
 *
 * @author MKL.
 */
public interface IGameDao extends IGenericDao<GameEntity, Long> {
    /**
     * Lock a game to work on it and return it.
     *
     * @param idGame id of the game to lock.
     * @return the locked game.
     */
    GameEntity lock(Long idGame);

    /**
     * Lock a game to work on it.
     *
     * @param game to lock.
     */
    void lock(GameEntity game);
}
