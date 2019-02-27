package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IMilitaryService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.ChooseBattleRequest;
import com.mkl.eu.client.service.service.military.ChooseLossesRequest;
import com.mkl.eu.client.service.service.military.SelectForceRequest;
import com.mkl.eu.client.service.service.military.WithdrawBeforeBattleRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from MilitaryService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.IMilitaryService")
public class MilitaryWsServiceImpl extends SpringBeanAutowiringSupport implements IMilitaryService {
    /** Military Service. */
    @Autowired
    @Qualifier(value = "militaryServiceImpl")
    private IMilitaryService militaryService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseBattle(Request<ChooseBattleRequest> request) throws FunctionalException, TechnicalException {
        return militaryService.chooseBattle(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse selectForce(Request<SelectForceRequest> request) throws FunctionalException, TechnicalException {
        return militaryService.selectForce(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateForces(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return militaryService.validateForces(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse withdrawBeforeBattle(Request<WithdrawBeforeBattleRequest> request) throws FunctionalException, TechnicalException {
        return militaryService.withdrawBeforeBattle(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse retreatFirstDay(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return militaryService.retreatFirstDay(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseLossesFromBattle(Request<ChooseLossesRequest> request) throws FunctionalException, TechnicalException {
        return militaryService.chooseLossesFromBattle(request);
    }
}
