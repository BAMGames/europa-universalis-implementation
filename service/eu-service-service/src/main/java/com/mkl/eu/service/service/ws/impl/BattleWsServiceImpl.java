package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.*;
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
@WebService(endpointInterface = "com.mkl.eu.client.service.service.IBattleService")
public class BattleWsServiceImpl extends SpringBeanAutowiringSupport implements IBattleService {
    /** Military Service. */
    @Autowired
    @Qualifier(value = "battleServiceImpl")
    private IBattleService battleService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseBattle(Request<ChooseBattleRequest> request) throws FunctionalException, TechnicalException {
        return battleService.chooseBattle(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse selectForce(Request<SelectForceRequest> request) throws FunctionalException, TechnicalException {
        return battleService.selectForce(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateForces(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return battleService.validateForces(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse withdrawBeforeBattle(Request<WithdrawBeforeBattleRequest> request) throws FunctionalException, TechnicalException {
        return battleService.withdrawBeforeBattle(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse retreatFirstDay(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return battleService.retreatFirstDay(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseLossesFromBattle(Request<ChooseLossesRequest> request) throws FunctionalException, TechnicalException {
        return battleService.chooseLossesFromBattle(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse retreatAfterBattle(Request<RetreatAfterBattleRequest> request) throws FunctionalException, TechnicalException {
        return battleService.retreatAfterBattle(request);
    }
}
