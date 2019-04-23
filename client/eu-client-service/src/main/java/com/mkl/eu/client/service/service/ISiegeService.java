package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.military.ChooseProvinceRequest;
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
}
