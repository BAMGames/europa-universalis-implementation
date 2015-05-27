package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.AuthentRequest;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.service.board.UpdateGameRequest;
import com.mkl.eu.client.service.vo.Game;
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
    private IBoardService gameService;

    /** {@inheritDoc} */
    @Override
    public Game loadGame(AuthentRequest<LoadGameRequest> loadGame) throws FunctionalException, TechnicalException {
        return gameService.loadGame(loadGame);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(AuthentRequest<UpdateGameRequest> updateGame) throws FunctionalException, TechnicalException {
        return gameService.updateGame(updateGame);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(AuthentRequest<MoveStackRequest> moveStack) throws FunctionalException, TechnicalException {
        return gameService.moveStack(moveStack);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(AuthentRequest<MoveCounterRequest> moveCounter) throws FunctionalException, TechnicalException {
        return gameService.moveCounter(moveCounter);
    }
}
