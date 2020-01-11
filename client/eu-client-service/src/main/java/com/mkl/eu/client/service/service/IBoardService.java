package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the board service.
 *
 * @author MKL.
 */
@WebService
public interface IBoardService extends INameConstants {
    /**
     * Move a stack on the board.
     *
     * @param request info of the stack to move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveStack(@WebParam(name = PARAMETER_MOVE_STACK) Request<MoveStackRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Take control of the stack.
     *
     * @param request info of the stack to take control.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse takeStackControl(@WebParam(name = PARAMETER_TAKE_STACK_CONTROL) Request<TakeStackControlRequest> request) throws FunctionalException, TechnicalException;

    /**
     * End the movement of a stack on the board.
     *
     * @param request info of the stack to end move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse endMoveStack(@WebParam(name = PARAMETER_END_MOVE_STACK) Request<EndMoveStackRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Move a counter from a stack to another.
     *
     * @param request info of the counter to move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveCounter(@WebParam(name = PARAMETER_MOVE_COUNTER) Request<MoveCounterRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Move a leader counter from a stack to another or to a province.
     *
     * @param request info of the counter leader to move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveLeader(@WebParam(name = PARAMETER_MOVE_LEADER) Request<MoveLeaderRequest> request) throws FunctionalException, TechnicalException;


    /**
     * Validate/Invalidate the military round for a country.
     * If all active countries have their military round validated, go to battle phase, siege phase or next round, given the board.
     *
     * @param request info about whose country wants to validate/invalidate its military round.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse validateMilitaryRound(@WebParam(name = PARAMETER_VALIDATE_MIL_ROUND) Request<ValidateRequest> request) throws FunctionalException, TechnicalException;


    /** Admin section */


    /**
     * Add a counter on the board.
     *
     * @param request info about the counter to create.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse createCounter(@WebParam(name = PARAMETER_CREATE_COUNTER) Request<CreateCounterRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Remove a counter on the board.
     *
     * @param request info about the counter to remove.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse removeCounter(@WebParam(name = PARAMETER_REMOVE_COUNTER) Request<RemoveCounterRequest> request) throws FunctionalException, TechnicalException;
}
