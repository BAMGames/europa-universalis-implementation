package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.service.service.IGameService;
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
    public Game loadGame(Long id) {
        return gameService.loadGame(id);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(Long id, Long version) {
        return gameService.updateGame(id, version);
    }
}
