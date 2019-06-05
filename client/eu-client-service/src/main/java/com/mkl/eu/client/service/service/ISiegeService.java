package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.common.RedeployRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the siege service.
 *
 * @author MKL.
 */
@WebService
public interface ISiegeService extends INameConstants {
    /**
     * Choose a siege within the possible ones.
     * If besieger has too many forces, the selectForces service must be invoked followed by the validateForcesForSiege.
     * Then the besieger will choose between undermine, assault or redeploy.
     * In a unique case, a breach can be taken to make an assault after an undermine.
     * Finally, all side will choose losses (in case of war honor, besieged will teleport some forces).
     *
     * @param request info about whose siege will be computed.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse chooseSiege(@WebParam(name = PARAMETER_CHOOSE_BATTLE) Request<ChooseProvinceRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Select the forces for the current siege.
     *
     * @param request info about the force to select.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse selectForces(@WebParam(name = PARAMETER_SELECT_FORCES) Request<SelectForcesRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Choose the mode (undermining, assault or redeploy) for the current siege.
     *
     * @param request info about the mode to choose.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse chooseMode(@WebParam(name = PARAMETER_CHOOSE_MODE) Request<ChooseModeForSiegeRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Choose to man a fortress after taking it.
     *
     * @param request info about the man to choose.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse chooseMan(@WebParam(name = PARAMETER_CHOOSE_MAN) Request<ChooseManForSiegeRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Choose to take a breach in a siege.
     *
     * @param request info about the breach to take.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse chooseBreach(@WebParam(name = PARAMETER_CHOOSE_BREACH) Request<ChooseBreachForSiegeRequest> request) throws FunctionalException, TechnicalException;

    /**
     * Redeploy forces after war honors (or leaders after end of siege).
     *
     * @param request info about the redeploy to do.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse redeploy(@WebParam(name = PARAMETER_REDEPLOY) Request<RedeployRequest> request) throws FunctionalException, TechnicalException;
}
