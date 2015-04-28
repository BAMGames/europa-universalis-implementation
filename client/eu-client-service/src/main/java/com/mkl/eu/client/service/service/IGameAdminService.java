package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
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
public interface IGameAdminService extends INameConstants {

    /**
     * Add a counter on the board.
     *
     * @param idGame      id of the game.
     * @param versionGame version of the game.
     * @param counter     to create.
     * @param province    province where the counter will spawn.
     * @return the diffs involved by this service.
     * @throws TechnicalException technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse createCounter(@WebParam(name = PARAMETER_ID_GAME) Long idGame,
                               @WebParam(name = PARAMETER_VERSION_GAME) Long versionGame,
                               @WebParam(name = PARAMETER_COUNTER) CounterForCreation counter,
                               @WebParam(name = PARAMETER_PROVINCE) String province) throws TechnicalException;
}
