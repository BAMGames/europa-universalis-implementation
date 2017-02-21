package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.service.service.game.FindGamesRequest;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Test for GameDao.
 *
 * @author MKL
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        RollbackTransactionalDataSetTestExecutionListener.class
})
@ContextConfiguration(locations = {"classpath:com/mkl/eu/service/service/eu-service-service-applicationContext.xml",
        "classpath:com/mkl/eu/service/service/test-database-applicationContext.xml"})
@DataSet(value = {"game.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class GameDaoImplTest {

    @Autowired
    private IGameDao gameDao;

    @Test
    public void testLock() {
        gameDao.lock(1L);
        // Impossible to test that lock throws an exception
        // when called twice because it only does so in different sessions
    }

    @Test
    public void testFindGames() {
        List<GameEntity> games = gameDao.findGames(null);

        Assert.assertEquals(5, games.size());

        FindGamesRequest request = new FindGamesRequest();
        games = gameDao.findGames(request);

        Assert.assertEquals(5, games.size());

        request.setUsername(AuthentInfo.USERNAME_ANONYMOUS);
        games = gameDao.findGames(request);

        Assert.assertEquals(5, games.size());

        request.setUsername("toto");
        games = gameDao.findGames(request);

        Assert.assertEquals(2, games.size());
        Assert.assertEquals(1L, games.get(0).getId().longValue());
        Assert.assertEquals(2L, games.get(1).getId().longValue());

        request.setUsername("tata");
        games = gameDao.findGames(request);

        Assert.assertEquals(2, games.size());
        Assert.assertEquals(2L, games.get(0).getId().longValue());
        Assert.assertEquals(3L, games.get(1).getId().longValue());

        request.setUsername("titi");
        games = gameDao.findGames(request);

        Assert.assertEquals(1, games.size());
        Assert.assertEquals(1L, games.get(0).getId().longValue());

        request.setUsername("tutu");
        games = gameDao.findGames(request);

        Assert.assertEquals(1, games.size());
        Assert.assertEquals(2L, games.get(0).getId().longValue());

        request.setUsername("tyty");
        games = gameDao.findGames(request);

        Assert.assertEquals(1, games.size());
        Assert.assertEquals(5L, games.get(0).getId().longValue());

        request.setUsername("tete");
        games = gameDao.findGames(request);

        Assert.assertEquals(0, games.size());
    }

    @Test
    public void testFindTurnOrder() {
        List<CountryOrderEntity> orders;

        orders = gameDao.findTurnOrder(1L, GameStatusEnum.MILITARY_HIERARCHY);

        Assert.assertEquals(0, orders.size());

        orders = gameDao.findTurnOrder(1L, GameStatusEnum.DIPLOMACY);

        Assert.assertEquals(1, orders.size());
        Assert.assertEquals("france", orders.get(0).getCountry().getName());
        Assert.assertEquals(0, orders.get(0).getPosition());

        orders = gameDao.findTurnOrder(1L, GameStatusEnum.MILITARY_MOVE);

        Assert.assertEquals(2, orders.size());
        Assert.assertEquals("france", orders.get(0).getCountry().getName());
        Assert.assertEquals(0, orders.get(0).getPosition());
        Assert.assertEquals("angleterre", orders.get(1).getCountry().getName());
        Assert.assertEquals(1, orders.get(1).getPosition());

        orders = gameDao.findTurnOrder(2L, GameStatusEnum.DIPLOMACY);

        Assert.assertEquals(2, orders.size());
        Assert.assertEquals("angleterre", orders.get(0).getCountry().getName());
        Assert.assertEquals(1, orders.get(0).getPosition());
        Assert.assertEquals("france", orders.get(1).getCountry().getName());
        Assert.assertEquals(0, orders.get(1).getPosition());

        orders = gameDao.findTurnOrder(2L, GameStatusEnum.MILITARY_MOVE);

        Assert.assertEquals(2, orders.size());
        Assert.assertEquals("france", orders.get(0).getCountry().getName());
        Assert.assertEquals(0, orders.get(0).getPosition());
        Assert.assertEquals("angleterre", orders.get(1).getCountry().getName());
        Assert.assertEquals(1, orders.get(1).getPosition());

        orders = gameDao.findTurnOrder(3L, GameStatusEnum.DIPLOMACY);

        Assert.assertEquals(0, orders.size());

        orders = gameDao.findTurnOrder(3L, GameStatusEnum.MILITARY_MOVE);

        Assert.assertEquals(0, orders.size());
    }
}
