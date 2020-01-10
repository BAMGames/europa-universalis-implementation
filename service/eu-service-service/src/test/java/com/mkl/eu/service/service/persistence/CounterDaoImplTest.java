package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for CounterDao.
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/provinces.xml", "counter.xml", "tables.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class CounterDaoImplTest {

    @Autowired
    private ICounterDao counterDao;

    @Test
    public void testGetCounter() {
        Assert.assertNull(counterDao.getCounter(1L, null));
        Assert.assertNull(counterDao.getCounter(1L, 2L));
        Assert.assertNull(counterDao.getCounter(6L, 1L));
        Assert.assertNull(counterDao.getCounter(2L, 2L));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, counterDao.getCounter(1L, 1L).getType());
    }

    @Test
    public void testGetPatrons() {
        List<String> patrons = counterDao.getPatrons("sancta sedes", 1L);
        Assert.assertEquals(2, patrons.size());
        Assert.assertTrue(patrons.contains("sancta sedes"));
        Assert.assertTrue(patrons.contains("france"));

        patrons = counterDao.getPatrons("sancta sedes", 2L);
        Assert.assertEquals(2, patrons.size());
        Assert.assertTrue(patrons.contains("sancta sedes"));
        Assert.assertTrue(patrons.contains("espagne"));

        patrons = counterDao.getPatrons("iroquois", 1L);
        Assert.assertEquals(3, patrons.size());
        Assert.assertTrue(patrons.contains("iroquois"));
        Assert.assertTrue(patrons.contains("france"));
        Assert.assertTrue(patrons.contains("angleterre"));

        patrons = counterDao.getPatrons("iroquois", 2L);
        Assert.assertEquals(1, patrons.size());
        Assert.assertTrue(patrons.contains("iroquois"));

        patrons = counterDao.getPatrons("sabaudia", 1L);
        Assert.assertEquals(1, patrons.size());
        Assert.assertTrue(patrons.contains("sabaudia"));
    }

    @Test
    public void testGetMinors() {
        List<String> minors = counterDao.getMinors("france", 1L);
        Assert.assertEquals(2, minors.size());
        Assert.assertTrue(minors.contains("sancta sedes"));
        Assert.assertTrue(minors.contains("iroquois"));

        minors = counterDao.getMinors("angleterre", 1L);
        Assert.assertEquals(1, minors.size());
        Assert.assertTrue(minors.contains("iroquois"));

        minors = counterDao.getMinors("espagne", 1L);
        Assert.assertEquals(0, minors.size());

        minors = counterDao.getMinors("espagne", 2L);
        Assert.assertEquals(1, minors.size());
        Assert.assertTrue(minors.contains("sancta sedes"));
    }

    @Test
    public void testGetVassals() {
        Assert.assertEquals(0, counterDao.getVassals("france", 1L).size());
        Assert.assertEquals(0, counterDao.getVassals("espagne", 2L).size());
        Assert.assertEquals(0, counterDao.getVassals("france", 1L).size());
        Assert.assertEquals(0, counterDao.getVassals("espagne", 2L).size());
        List<String> vassals = counterDao.getVassals("hollande", 1L);
        Collections.sort(vassals);
        Assert.assertEquals(4, vassals.size());
        Assert.assertEquals("mayence", vassals.get(0));
        Assert.assertEquals("pologne", vassals.get(1));
        Assert.assertEquals("saxe", vassals.get(2));
        Assert.assertEquals("wurtenberger", vassals.get(3));
        Assert.assertEquals(0, counterDao.getVassals("hollande", 2L).size());
    }

    @Test
    public void testNeighbors() {
        List<String> neighbors = new ArrayList<>();
        Assert.assertEquals(neighbors, counterDao.getNeighboringOwners("rAden~W", 1L));

        neighbors.clear();
        neighbors.add("arabie");
        neighbors.add("turquie");
        Assert.assertEquals(neighbors, counterDao.getNeighboringOwners("rNedj~E", 1L));

        neighbors.clear();
        neighbors.add("arabie");
        neighbors.add("irak");
        Assert.assertEquals(neighbors, counterDao.getNeighboringOwners("rNedj~E", 2L));

        neighbors.clear();
        neighbors.add("astrakhan");
        neighbors.add("perse");
        Assert.assertEquals(neighbors, counterDao.getNeighboringOwners("sCaspienne", 1L));
    }

    @Test
    public void testNationalTerritoryUnderAttack() {
        List<String> provinces = counterDao.getNationalTerritoriesUnderAttack("france", Arrays.asList("russie", "poland"), 1L);
        Assert.assertEquals(0, provinces.size());
        provinces = counterDao.getNationalTerritoriesUnderAttack("russie", Arrays.asList("sweden", "poland"), 1L);
        Assert.assertEquals(0, provinces.size());
        provinces = counterDao.getNationalTerritoriesUnderAttack("russie", Arrays.asList("france", "poland"), 2L);
        Assert.assertEquals(0, provinces.size());
        provinces = counterDao.getNationalTerritoriesUnderAttack("russie", Arrays.asList("france", "poland"), 1L);
        Assert.assertEquals(2, provinces.size());
        Collections.sort(provinces);
        Assert.assertEquals(Arrays.asList("eNovgorod", "eRyazan"), provinces);
    }

    @Test
    public void testGoldExploited() {
        Assert.assertEquals(90, counterDao.getGoldExploitedRotw(1L));
        Assert.assertEquals(20, counterDao.getGoldExploitedRotw(2L));
        Assert.assertEquals(0, counterDao.getGoldExploitedRotw(3L));
    }

    @Test
    public void testGoldExploitedAmerica() {
        Assert.assertEquals(90, counterDao.getGoldExploitedAmerica("espagne", 1L));
        Assert.assertEquals(50, counterDao.getGoldExploitedAmerica("france", 1L));
        Assert.assertEquals(0, counterDao.getGoldExploitedAmerica("russie", 1L));

        Assert.assertEquals(0, counterDao.getGoldExploitedAmerica("espagne", 2L));
        Assert.assertEquals(0, counterDao.getGoldExploitedAmerica("france", 2L));
        Assert.assertEquals(0, counterDao.getGoldExploitedAmerica("russie", 2L));
    }

    @Test
    public void testGovernorInRegion() {
        // region and leader do not exist
        Assert.assertFalse(counterDao.isGovernorInSameRegion("toto", "tata", 1L));
        // governor america in american region
        Assert.assertTrue(counterDao.isGovernorInSameRegion("Azteca", "espagne", 1L));
        // governor asia in american region
        Assert.assertFalse(counterDao.isGovernorInSameRegion("Azteca", "france", 1L));
        // governor in american region
        Assert.assertTrue(counterDao.isGovernorInSameRegion("Azteca", "angleterre", 1L));
        // governor in another american region
        Assert.assertFalse(counterDao.isGovernorInSameRegion("Inca", "espagne", 1L));
        // governor in region without geo group
        Assert.assertTrue(counterDao.isGovernorInSameRegion("Siberie", "russie", 2L));
        // governor in region without geo group but not in this game
        Assert.assertFalse(counterDao.isGovernorInSameRegion("Siberie", "russie", 1L));
        // no governor in region without geo group
        Assert.assertFalse(counterDao.isGovernorInSameRegion("Siberie", "france", 1L));
        // governor is in this region but not in this game
        Assert.assertFalse(counterDao.isGovernorInSameRegion("Azteca", "espagne", 2L));
    }
}
