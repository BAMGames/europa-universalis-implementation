package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.ChooseBattleRequest;
import com.mkl.eu.client.service.service.military.SelectForceRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the military service.
 *
 * @author MKL.
 */
@WebService
public interface IMilitaryService extends INameConstants {
    /**
     * Choose a battle within the possible ones.
     * If some side has too many forces, the selectForce service must be invoked followed by the validateForcesForBattle.
     * The defender can call withdrawBeforeBattle.
     * Then the first round is computed. If battle is not finished, each side can withdrawFromBattle
     * (in sea, the one with the wind can withdrawFromBattle during first round too).
     * The second round is computed. And each side can selectDeadForcesAfterBattle if there is some choice to do.
     * Finally, the looser will retreatAfterBattle.
     *
     * @param request info about whose battle will be computed.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse chooseBattle(@WebParam(name = PARAMETER_CHOOSE_BATTLE) Request<ChooseBattleRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Select or deselect a force for the current battle.
     *
     * @param request info about the force to select/deselect.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse selectForce(@WebParam(name = PARAMETER_SELECT_FORCE) Request<SelectForceRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Validate the forces for the current battle.
     *
     * @param request info about the forces to validate/invalidate.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse validateForces(@WebParam(name = PARAMETER_SELECT_FORCE) Request<ValidateRequest> request) throws FunctionalException, TechnicalException;
}
