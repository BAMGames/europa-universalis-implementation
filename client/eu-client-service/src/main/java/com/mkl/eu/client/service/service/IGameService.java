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
public interface IGameService {
    /**
     * Load a game given its id.
     *
     * @param id of the game too load.
     * @return the game.
     */
    @WebResult(name = "game")
    Game loadGame(@WebParam(name = "id") Long id);

    /**
     * Get all diffs since last client version of the game.
     * The version given should be the one of the game.
     * The service will return all the diffs concernend by this game and whose
     * version is greater than (not equal) the given version.
     *
     * @param id      of the game too update.
     * @param version previous version of the game.
     * @return the diffs.
     */
    @WebResult(name = "response")
    DiffResponse updateGame(@WebParam(name = "id") Long id, @WebParam(name = "version") Long version);
}
