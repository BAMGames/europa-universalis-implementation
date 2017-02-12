package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

/**
 * Utility methods for testing code in AbstractService.
 *
 * @author MKL.
 */
public abstract class AbstractGameServiceTest {
    protected static final Long GAME_ID = 12L;

    @Mock
    protected IGameDao gameDao;

    protected GameEntity createGameUsingMocks() {
        GameEntity game = new GameEntity();
        game.setId(GAME_ID);
        game.setVersion(5L);
        when(gameDao.lock(GAME_ID)).thenReturn(game);

        return game;
    }

    protected <V> Pair<Request<V>, GameEntity> testCheckGame(IServiceWithCheckGame<V> service, String method) {

        try {
            service.run(null);
            Assert.fail("Should break because " + method + " is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method, e.getParams()[0]);
        }

        Request<V> request = new Request<>();

        try {
            service.run(request);
            Assert.fail("Should break because " + method + ".game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            service.run(request);
            Assert.fail("Should break because " + method + " is null");
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(GAME_ID);

        try {
            service.run(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            service.run(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(GAME_ID);

        when(gameDao.lock(GAME_ID)).thenReturn(game);

        try {
            service.run(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        return new ImmutablePair<>(request, game);
    }

    protected <V> void testCheckStatus(GameEntity game, Request<V> request, IServiceWithCheckGame<V> service, String method, GameStatusEnum status) {
        try {
            service.run(request);
            Assert.fail("Should break because game.status is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
            Assert.assertEquals(method + ".request", e.getParams()[0]);
        }

        GameStatusEnum wrongStatus = GameStatusEnum.MILITARY;
        if (status == GameStatusEnum.MILITARY) {
            wrongStatus = GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE;
        }
        game.setStatus(wrongStatus);

        try {
            service.run(request);
            Assert.fail("Should break because game.status is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
            Assert.assertEquals(method + ".request", e.getParams()[0]);
        }

        game.setStatus(status);
    }

    protected interface IServiceWithCheckGame<V> {
        void run(Request<V> request) throws TechnicalException, FunctionalException;
    }
}
