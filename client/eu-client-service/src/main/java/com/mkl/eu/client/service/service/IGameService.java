package com.mkl.eu.client.service.service;

import com.mkl.eu.client.service.vo.Game;

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
}
