package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.common.RedeployRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from SiegeService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.ISiegeService")
public class SiegeWsServiceImpl extends SpringBeanAutowiringSupport implements ISiegeService {
    /** Siege Service. */
    @Autowired
    @Qualifier(value = "siegeServiceImpl")
    private ISiegeService siegeService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseSiege(Request<ChooseProvinceRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.chooseSiege(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse selectForces(Request<SelectForcesRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.selectForces(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseMode(Request<ChooseModeForSiegeRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.chooseMode(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseMan(Request<ChooseManForSiegeRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.chooseMan(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseBreach(Request<ChooseBreachForSiegeRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.chooseBreach(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse redeploy(Request<RedeployRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.redeploy(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseLossesAfterAssault(Request<ChooseLossesRequest> request) throws FunctionalException, TechnicalException {
        return siegeService.chooseLossesAfterAssault(request);
    }
}
