package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from BoardService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.IBoardService")
public class BoardWsServiceImpl extends SpringBeanAutowiringSupport implements IBoardService {
    /** Game Service. */
    @Autowired
    @Qualifier(value = "boardServiceImpl")
    private IBoardService boardService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(Request<MoveStackRequest> request) throws FunctionalException, TechnicalException {
        return boardService.moveStack(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse takeStackControl(Request<TakeStackControlRequest> request) throws FunctionalException, TechnicalException {
        return boardService.takeStackControl(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse endMoveStack(Request<EndMoveStackRequest> request) throws FunctionalException, TechnicalException {
        return boardService.endMoveStack(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(Request<MoveCounterRequest> request) throws FunctionalException, TechnicalException {
        return boardService.moveCounter(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveLeader(Request<MoveLeaderRequest> request) throws FunctionalException, TechnicalException {
        return boardService.moveLeader(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateMilitaryRound(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return boardService.validateMilitaryRound(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse createCounter(Request<CreateCounterRequest> request) throws FunctionalException, TechnicalException {
        return boardService.createCounter(request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse removeCounter(Request<RemoveCounterRequest> request) throws FunctionalException, TechnicalException {
        return boardService.removeCounter(request);
    }
}
