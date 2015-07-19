package com.mkl.eu.service.service.persistence;

import com.mkl.eu.client.service.service.board.FindGamesRequest;
import com.mkl.eu.service.service.persistence.oe.GameEntity;

import java.util.List;

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

    /**
     * Find games given criteria.
     *
     * @param findGames criteria to use for the search.
     * @return the games matching the criteria.
     */
    List<GameEntity> findGames(FindGamesRequest findGames);
}
