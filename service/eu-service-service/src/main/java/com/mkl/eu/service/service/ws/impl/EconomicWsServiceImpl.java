package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.eco.*;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.Competition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.List;

/**
 * Separation from EconomicService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.IEconomicService")
public class EconomicWsServiceImpl extends SpringBeanAutowiringSupport implements IEconomicService {
    /** Game Service. */
    @Autowired
    @Qualifier(value = "economicServiceImpl")
    private IEconomicService economicService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse computeEconomicalSheets(Long idGame) throws FunctionalException, TechnicalException {
        return economicService.computeEconomicalSheets(idGame);
    }

    /** {@inheritDoc} */
    @Override
    public List<EconomicalSheetCountry> loadEconomicSheets(SimpleRequest<LoadEcoSheetsRequest> request) throws FunctionalException, TechnicalException {
        return economicService.loadEconomicSheets(request);
    }

    /** {@inheritDoc} */
    @Override
    public List<AdministrativeActionCountry> loadAdminActions(SimpleRequest<LoadAdminActionsRequest> request) throws FunctionalException, TechnicalException {
        return economicService.loadAdminActions(request);
    }

    /** {@inheritDoc} */
    @Override
    public List<Competition> loadCompetitions(SimpleRequest<LoadCompetitionsRequest> request) throws FunctionalException, TechnicalException {
        return economicService.loadCompetitions(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse addAdminAction(Request<AddAdminActionRequest> request) throws FunctionalException, TechnicalException {
        return economicService.addAdminAction(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse removeAdminAction(Request<RemoveAdminActionRequest> request) throws FunctionalException, TechnicalException {
        return economicService.removeAdminAction(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateAdminActions(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return economicService.validateAdminActions(request);
    }
}
