package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Test for CountryDao.
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/provinces.xml", "playableCountry.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class PlayableCountryDaoImplTest {

    @Autowired
    private IPlayableCountryDao playableCountryDao;

    @Test
    public void testGetOwnedProvinces() {
        List<String> provinces = playableCountryDao.getOwnedProvinces("toto", 3L);

        Assert.assertEquals(0, provinces.size());

        provinces = playableCountryDao.getOwnedProvinces("france", 3L);

        Assert.assertEquals(26, provinces.size());
        Assert.assertEquals(true, provinces.contains("eIle-de-France"));
        Assert.assertEquals(false, provinces.contains("eCornwall"));
        Assert.assertEquals(true, provinces.contains("eLyonnais"));
        Assert.assertEquals(false, provinces.contains("eHinterpommern"));
        Assert.assertEquals(true, provinces.contains("eVendee"));

        provinces = playableCountryDao.getOwnedProvinces("angleterre", 3L);

        Assert.assertEquals(12, provinces.size());
        Assert.assertEquals(false, provinces.contains("eIle-de-France"));
        Assert.assertEquals(true, provinces.contains("eCornwall"));
        Assert.assertEquals(false, provinces.contains("eLyonnais"));

        provinces = playableCountryDao.getOwnedProvinces("france", 2L);

        Assert.assertEquals(26, provinces.size());
        Assert.assertEquals(true, provinces.contains("eIle-de-France"));
        Assert.assertEquals(false, provinces.contains("eCornwall"));
        Assert.assertEquals(true, provinces.contains("eLyonnais"));
        Assert.assertEquals(false, provinces.contains("eHinterpommern"));
        Assert.assertEquals(true, provinces.contains("eVendee"));

        provinces = playableCountryDao.getOwnedProvinces("angleterre", 2L);

        Assert.assertEquals(12, provinces.size());
        Assert.assertEquals(false, provinces.contains("eIle-de-France"));
        Assert.assertEquals(true, provinces.contains("eCornwall"));
        Assert.assertEquals(false, provinces.contains("eLyonnais"));

        provinces = playableCountryDao.getOwnedProvinces("france", 1L);

        Assert.assertEquals(25, provinces.size());
        Assert.assertEquals(false, provinces.contains("eIle-de-France"));
        Assert.assertEquals(true, provinces.contains("eCornwall"));
        Assert.assertEquals(true, provinces.contains("eLyonnais"));
        Assert.assertEquals(false, provinces.contains("eHinterpommern"));
        Assert.assertEquals(false, provinces.contains("eVendee"));

        provinces = playableCountryDao.getOwnedProvinces("angleterre", 1L);

        Assert.assertEquals(12, provinces.size());
        Assert.assertEquals(true, provinces.contains("eIle-de-France"));
        Assert.assertEquals(false, provinces.contains("eCornwall"));
        Assert.assertEquals(false, provinces.contains("eLyonnais"));
    }

    @Test
    public void testFatherlandInDanger() {
        List<String> enemies = new ArrayList<>();
        Assert.assertFalse(playableCountryDao.isFatherlandInDanger(null, null, null));
        Assert.assertFalse(playableCountryDao.isFatherlandInDanger(null, enemies, null));
        Assert.assertFalse(playableCountryDao.isFatherlandInDanger("france", enemies, null));
        Assert.assertFalse(playableCountryDao.isFatherlandInDanger("france", enemies, 2L));
        Assert.assertFalse(playableCountryDao.isFatherlandInDanger("france", enemies, 1L));

        enemies.add("angleterre");
        enemies.add("espagne");
        Assert.assertTrue(playableCountryDao.isFatherlandInDanger("france", enemies, 1L));

        enemies.clear();
        enemies.add("france");
        Assert.assertFalse(playableCountryDao.isFatherlandInDanger("angleterre", enemies, 1L));
    }
}
