package com.mkl.eu.service.service.persistence;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description of the class.
 *
 * @author MKL
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mkl/eu/service/service/eu-service-service-applicationContext.xml",
                                   "classpath:com/mkl/eu/service/service/test-database-applicationContext.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class CounterDaoImplTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private ICounterDao counterDao;

    @Before
    public void initDb() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSetProvinces());
    }

    private IDataSet getDataSet() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader()
                                      .getResourceAsStream("com/mkl/eu/service/service/persistence/counter.xml");
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        return builder.build(inputStream);

    }

    private IDataSet getDataSetProvinces() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader()
                                      .getResourceAsStream("com/mkl/eu/service/service/persistence/provinces.xml");
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        return builder.build(inputStream);
    }

    private IDatabaseConnection getConnection() throws Exception {
        Connection jdbcConnection = dataSource.getConnection();
        return new DatabaseConnection(jdbcConnection);
    }

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
        Assert.assertEquals(1, counterDao.getPatrons("sancta sedes", 1L).size());
        Assert.assertEquals("france", counterDao.getPatrons("sancta sedes", 1L).get(0));
        Assert.assertEquals(1, counterDao.getPatrons("sancta sedes", 2L).size());
        Assert.assertEquals("espagne", counterDao.getPatrons("sancta sedes", 2L).get(0));
        Assert.assertEquals(2, counterDao.getPatrons("iroquois", 1L).size());
        Assert.assertEquals("france", counterDao.getPatrons("iroquois", 1L).get(0));
        Assert.assertEquals("angleterre", counterDao.getPatrons("iroquois", 1L).get(1));
        Assert.assertEquals(0, counterDao.getPatrons("iroquois", 2L).size());
        Assert.assertEquals(0, counterDao.getPatrons("sabaudia", 1L).size());
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
}
