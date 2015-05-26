package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.AuthentRequest;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.game.LoadGameRequest;
import com.mkl.eu.client.service.service.game.MoveStackRequest;
import com.mkl.eu.client.service.service.game.UpdateGameRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from GameService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.IGameService")
public class GameWsServiceImpl extends SpringBeanAutowiringSupport implements IGameService {
    /** Game Service. */
    @Autowired
    @Qualifier(value = "gameServiceImpl")
    private IGameService gameService;

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
    public DiffResponse moveCounter(Long idGame, Long versionGame, Long idCounter, Long idStack) throws FunctionalException, TechnicalException {
        return gameService.moveCounter(idGame, versionGame, idCounter, idStack);
    }
}
