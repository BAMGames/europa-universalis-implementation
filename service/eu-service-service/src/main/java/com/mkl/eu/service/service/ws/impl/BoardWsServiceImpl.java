package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.board.EndMoveStackRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
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
    public DiffResponse moveStack(Request<MoveStackRequest> moveStack) throws FunctionalException, TechnicalException {
        return boardService.moveStack(moveStack);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse endMoveStack(Request<EndMoveStackRequest> endMoveStack) throws FunctionalException, TechnicalException {
        return boardService.endMoveStack(endMoveStack);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(Request<MoveCounterRequest> moveCounter) throws FunctionalException, TechnicalException {
        return boardService.moveCounter(moveCounter);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateMilitaryRound(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        return boardService.validateMilitaryRound(request);
    }
}
