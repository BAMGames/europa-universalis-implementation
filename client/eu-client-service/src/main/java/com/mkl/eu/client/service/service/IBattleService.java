package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.*;
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
public interface IBattleService extends INameConstants {
    /**
     * Choose a battle within the possible ones.
     * If some side has too many forces, the selectForces service must be invoked.
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
    DiffResponse chooseBattle(@WebParam(name = PARAMETER_CHOOSE_BATTLE) Request<ChooseProvinceRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Select the forces for the current battle.
     *
     * @param request info about the force to select.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse selectForces(@WebParam(name = PARAMETER_SELECT_FORCES) Request<SelectForcesRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Withdraw the current battle for the non phasing forces.
     *
     * @param request info about province where to withdraw.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse withdrawBeforeBattle(@WebParam(name = PARAMETER_WITHDRAW_BEFORE_BATTLE) Request<WithdrawBeforeBattleRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Retreat the current battle at the end of the first day.
     *
     * @param request info about province if a retreat is asked.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse retreatFirstDay(@WebParam(name = PARAMETER_RETREAT_FIRST_DAY) Request<ValidateRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Choose losses from the current battle.
     *
     * @param request info about on which units the losses will be taken.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse chooseLossesFromBattle(@WebParam(name = PARAMETER_CHOOSE_LOSSES) Request<ChooseLossesRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Retreat at the end of the current battle.
     *
     * @param request info about where to retreat.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse retreatAfterBattle(@WebParam(name = PARAMETER_RETREAT_AFTER_BATTLE) Request<RetreatAfterBattleRequest> request) throws FunctionalException, TechnicalException;
}
