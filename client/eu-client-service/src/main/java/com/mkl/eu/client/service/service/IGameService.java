package com.mkl.eu.client.service.service;

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
     * @param idGame of the game too load.
     * @return the game.
     */
    @WebResult(name = RESPONSE_GAME)
    Game loadGame(@WebParam(name = PARAMETER_ID_GAME) Long idGame);

    /**
     * Get all diffs since last client version of the game.
     * The version given should be the one of the game.
     * The service will return all the diffs concernend by this game and whose
     * version is greater than (not equal) the given version.
     *
     * @param idGame      of the game too update.
     * @param versionGame previous version of the game.
     * @return the diffs.
     */
    @WebResult(name = RESPONSE)
    DiffResponse updateGame(@WebParam(name = PARAMETER_ID_GAME) Long idGame, @WebParam(name = PARAMETER_VERSION_GAME) Long versionGame);

    /**
     * Move a stack on the board.
     *
     * @param idGame      id of the game.
     * @param versionGame version of the game.
     * @param idStack     id of the stack to move.
     * @param provinceTo  province where the stack should move.
     * @return the diffs involved by this service.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveStack(@WebParam(name = PARAMETER_ID_GAME) Long idGame,
                           @WebParam(name = PARAMETER_VERSION_GAME) Long versionGame,
                           @WebParam(name = PARAMETER_ID_STACK) Long idStack,
                           @WebParam(name = PARAMETER_PROVINCE_TO) String provinceTo);
}
