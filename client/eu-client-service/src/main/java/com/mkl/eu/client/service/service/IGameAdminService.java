package com.mkl.eu.client.service.service;

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
public interface IGameAdminService {
    /**
     * Move a stack on the board.
     *
     * @param idGame      id of the game.
     * @param versionGame version of the game.
     * @param idStack     id of the stack to move.
     * @param provinceTo  province where the stack should move.
     * @return the diffs involved by this service.
     */
    @WebResult(name = "response")
    DiffResponse moveStack(@WebParam(name = "idGame") Long idGame,
                           @WebParam(name = "versionGame") Long versionGame,
                           @WebParam(name = "idStack") Long idStack,
                           @WebParam(name = "provinceTo") String provinceTo);
}
