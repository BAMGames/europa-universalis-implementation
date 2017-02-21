package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.game.FindGamesRequest;
import com.mkl.eu.client.service.service.game.LoadGameRequest;
import com.mkl.eu.client.service.service.game.LoadTurnOrderRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.List;

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
    public List<GameLight> findGames(SimpleRequest<FindGamesRequest> findGames) throws FunctionalException, TechnicalException {
        return gameService.findGames(findGames);
    }

    /** {@inheritDoc} */
    @Override
    public Game loadGame(SimpleRequest<LoadGameRequest> loadGame) throws FunctionalException, TechnicalException {
        return gameService.loadGame(loadGame);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(Request<Void> updateGame) throws FunctionalException, TechnicalException {
        return gameService.updateGame(updateGame);
    }

    /** {@inheritDoc} */
    @Override
    public List<CountryOrder> loadTurnOrder(SimpleRequest<LoadTurnOrderRequest> loadTurnOrder) throws FunctionalException, TechnicalException {
        return gameService.loadTurnOrder(loadTurnOrder);
    }
}
