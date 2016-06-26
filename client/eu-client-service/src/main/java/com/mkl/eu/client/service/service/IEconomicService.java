package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.eco.*;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.List;

/**
 * Interface for the economic service.
 *
 * @author MKL.
 */
@WebService
public interface IEconomicService extends INameConstants {
    /**
     * Compute the economical sheets of the game for the current turn.
     *
     * @param idGame id of the game.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse computeEconomicalSheets(@WebParam(name = PARAMETER_ID_GAME) Long idGame) throws FunctionalException, TechnicalException;

    /**
     * Load economical sheets.
     *
     * @param loadEcoSheets info about the sheets to load.
     * @return the economical sheets.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    List<EconomicalSheetCountry> loadEconomicSheets(@WebParam(name = PARAMETER_LOAD_ECO_SHEETS) SimpleRequest<LoadEcoSheetsRequest> loadEcoSheets) throws FunctionalException, TechnicalException;

    /**
     * Adds a PLANNED administrative action (does not compute it).
     *
     * @param request info of the administrative action to add.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse addAdminAction(@WebParam(name = PARAMETER_ADD_ADM_ACT) Request<AddAdminActionRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Remove a PLANNED administrative action.
     *
     * @param request info of the administrative action to remove.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse removeAdminAction(@WebParam(name = PARAMETER_REMOVE_ADM_ACT) Request<RemoveAdminActionRequest> request) throws FunctionalException, TechnicalException;


    /**
     * Validate/Invalidate the administrative actions for a country.
     * If all countries have their administrative actions validated, compute the administrative actions.
     *
     * @param request info about whose country wants to validate/invalidate its administrative actions
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse validateAdminActions(@WebParam(name = PARAMETER_VALIDATE_ADM_ACT) Request<ValidateAdminActionsRequest> request) throws FunctionalException, TechnicalException;

}
