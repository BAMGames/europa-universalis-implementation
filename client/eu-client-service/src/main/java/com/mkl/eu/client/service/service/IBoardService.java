package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.board.EndMoveStackRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
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
     * @param moveStack info of the stack to move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveStack(@WebParam(name = PARAMETER_MOVE_STACK) Request<MoveStackRequest> moveStack) throws FunctionalException, TechnicalException;

    /**
     * End the movement of a stack on the board.
     *
     * @param endMoveStack info of the stack to end move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse endMoveStack(@WebParam(name = PARAMETER_END_MOVE_STACK) Request<EndMoveStackRequest> endMoveStack) throws FunctionalException, TechnicalException;

    /**
     * Move a counter from a stack to another..
     *
     * @param moveCounter info of the counter to move.
     * @return the diffs involved by this service.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse moveCounter(@WebParam(name = PARAMETER_MOVE_COUNTER) Request<MoveCounterRequest> moveCounter) throws FunctionalException, TechnicalException;

    /**
     * Validate/Invalidate the military round for a country.
     * If all active countries have their military round validated, go to next round.
     *
     * @param request info about whose country wants to validate/invalidate its administrative actions
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse validateMilitaryRound(@WebParam(name = PARAMETER_VALIDATE_MIL_ROUND) Request<ValidateRequest> request) throws FunctionalException, TechnicalException;

}
