package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.AuthentRequest;
import com.mkl.eu.client.service.service.wrapper.LoadGameRequest;
import com.mkl.eu.client.service.service.wrapper.UpdateGameRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the game service.
 *
 * @author MKL.
 */
@WebService
public interface IGameService extends INameConstants {
    /**
     * Load a game given its id.
     *
     * @param loadGame info of the game to load.
     * @return the game.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException technical exception.
     */
    @WebResult(name = RESPONSE_GAME)
    Game loadGame(@WebParam(name = PARAMETER_LOAD_GAME) AuthentRequest<LoadGameRequest> loadGame) throws FunctionalException, TechnicalException;

    /**
     * Get all diffs since last client version of the game.
     * The version given should be the one of the game.
     * The service will return all the diffs concernend by this game and whose
     * version is greater than (not equal) the given version.
     *
     * @param updateGame     info of the game to update.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse updateGame(@WebParam(name = PARAMETER_UPDATE_GAME) AuthentRequest<UpdateGameRequest> updateGame) throws FunctionalException, TechnicalException;

    /**
     * Move a stack on the board.
     *
     * @param idGame      id of the game.
     * @param versionGame version of the game.
     * @param idStack     id of the stack to move.
     * @param provinceTo  province where the stack should move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveStack(@WebParam(name = PARAMETER_ID_GAME) Long idGame,
                           @WebParam(name = PARAMETER_VERSION_GAME) Long versionGame,
                           @WebParam(name = PARAMETER_ID_STACK) Long idStack,
                           @WebParam(name = PARAMETER_PROVINCE_TO) String provinceTo) throws FunctionalException, TechnicalException;

    /**
     * Move a counter from a stack to another..
     *
     * @param idGame      id of the game.
     * @param versionGame version of the game.
     * @param idCounter   id of the counter to move.
     * @param idStack     id of the stack where the counter will move to. Can be <code>null</code> for creation of a stack.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveCounter(@WebParam(name = PARAMETER_ID_GAME) Long idGame,
                             @WebParam(name = PARAMETER_VERSION_GAME) Long versionGame,
                             @WebParam(name = PARAMETER_ID_COUNTER) Long idCounter,
                             @WebParam(name = PARAMETER_ID_STACK) Long idStack) throws FunctionalException, TechnicalException;
}
