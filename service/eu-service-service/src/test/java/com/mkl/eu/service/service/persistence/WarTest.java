package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/referentiel.xml", "war.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class WarTest {

    @Autowired
    private IGameDao gameDao;

    @Test
    public void testWarJpaMapping() {
        GameEntity game = gameDao.load(1L);

        Assert.assertEquals(1, game.getWars().size());

        WarEntity war = game.getWars().get(0);

        Assert.assertEquals(2, war.getCountries().size());
        Assert.assertEquals("france", war.getCountries().get(0).getCountry().getName());
        Assert.assertEquals(CountryTypeEnum.MAJOR, war.getCountries().get(0).getCountry().getType());
        Assert.assertEquals(true, war.getCountries().get(0).isOffensive());
        Assert.assertEquals(WarImplicationEnum.FULL, war.getCountries().get(0).getImplication());
        Assert.assertEquals("kazan", war.getCountries().get(1).getCountry().getName());
        Assert.assertEquals(CountryTypeEnum.MINOR, war.getCountries().get(1).getCountry().getType());
        Assert.assertEquals(false, war.getCountries().get(1).isOffensive());
        Assert.assertEquals(WarImplicationEnum.LIMITED, war.getCountries().get(1).getImplication());

        Assert.assertEquals(2, game.getOrders().size());
        Assert.assertEquals("suede", game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(0).getCountry().getDti());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(5, game.getOrders().get(0).getPosition());
        Assert.assertEquals("espagne", game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getCountry().getDti());
        Assert.assertEquals(GameStatusEnum.DIPLOMACY, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(3, game.getOrders().get(1).getPosition());

    }
}
