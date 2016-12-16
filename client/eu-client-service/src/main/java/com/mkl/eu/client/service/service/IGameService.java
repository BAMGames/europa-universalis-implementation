package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.board.FindGamesRequest;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.List;

/**
 * Interface for the game service.
 *
 * @author MKL.
 */
@WebService
public interface IGameService extends INameConstants {
    /**
     * Find games given criteria.
     *
     * @param findGames criteria to use for the search.
     * @return the games matching the criteria.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE_GAMES)
    List<GameLight> findGames(@WebParam(name = PARAMETER_FIND_GAMES) SimpleRequest<FindGamesRequest> findGames) throws FunctionalException, TechnicalException;

    /**
     * Load a game given its id.
     *
     * @param loadGame info of the game to load.
     * @return the game.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE_GAME)
    Game loadGame(@WebParam(name = PARAMETER_LOAD_GAME) SimpleRequest<LoadGameRequest> loadGame) throws FunctionalException, TechnicalException;

    /**
     * Get all diffs since last client version of the game.
     * The version given should be the one of the game.
     * The service will return all the diffs concernend by this game and whose
     * version is greater than (not equal) the given version.
     *
     * @param updateGame info of the game to update.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse updateGame(@WebParam(name = PARAMETER_UPDATE_GAME) Request<Void> updateGame) throws FunctionalException, TechnicalException;
}
