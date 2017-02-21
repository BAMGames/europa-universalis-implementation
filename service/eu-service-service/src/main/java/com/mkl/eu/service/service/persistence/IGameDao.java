package com.mkl.eu.service.service.persistence;

import com.mkl.eu.client.service.service.game.FindGamesRequest;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;

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

    /**
     * Find the turn order of the specified game at the specified phase.
     *
     * @param idGame     id of the game.
     * @param gameStatus status (phase) of the game.
     * @return the turn order.
     */
    List<CountryOrderEntity> findTurnOrder(Long idGame, GameStatusEnum gameStatus);
}
