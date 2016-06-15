package com.mkl.eu.service.service.persistence;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
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
import java.util.*;
import java.util.stream.Collectors;

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
public class EconomicalSheetDaoImplTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private IEconomicalSheetDao economicalSheetDao;

    @Before
    public void initDb() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSetProvinces());
    }

    private IDataSet getDataSet() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader()
                                      .getResourceAsStream("com/mkl/eu/service/service/persistence/eco.xml");
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
    public void testGetOwnAndControlProvinces() {
        Map<String, Integer> provinces = economicalSheetDao.getOwnedAndControlledProvinces("france", 11L);
        Assert.assertEquals(26, provinces.size());
        Assert.assertEquals(236, provinces.values().stream().collect(Collectors.summingInt(value -> value)).intValue());

        provinces = economicalSheetDao.getOwnedAndControlledProvinces("france", 1L);
        Assert.assertFalse(provinces.containsKey("eIle-de-France"));
        Assert.assertTrue(provinces.containsKey("eCornwall"));
        Assert.assertTrue(provinces.containsKey("eLyonnais"));
        Assert.assertFalse(provinces.containsKey("eHinterpommern"));
        Assert.assertEquals(26, provinces.size());
        Assert.assertEquals(224, provinces.values().stream().collect(Collectors.summingInt(value -> value)).intValue());

        provinces = economicalSheetDao.getOwnedAndControlledProvinces("angleterre", 11L);
        Assert.assertEquals(12, provinces.size());
        Assert.assertEquals(114, provinces.values().stream().collect(Collectors.summingInt(value -> value)).intValue());

        provinces = economicalSheetDao.getOwnedAndControlledProvinces("angleterre", 1L);
        Assert.assertTrue(provinces.containsKey("eIle-de-France"));
        Assert.assertFalse(provinces.containsKey("eCornwall"));
        Assert.assertFalse(provinces.containsKey("eLyonnais"));
        Assert.assertFalse(provinces.containsKey("eHinterpommern"));
        Assert.assertEquals(12, provinces.size());
        Assert.assertEquals(126, provinces.values().stream().collect(Collectors.summingInt(value -> value)).intValue());
    }

    @Test
    public void testGetPillagedProvinces() {
        List<String> provinces = new ArrayList<>();
        List<String> pillaged = economicalSheetDao.getPillagedProvinces(provinces, 11L);

        Assert.assertEquals(0, pillaged.size());

        provinces.clear();
        provinces.add("eIle-de-France");
        provinces.add("eCornwall");
        provinces.add("eLyonnais");
        provinces.add("eHinterpommern");

        pillaged = economicalSheetDao.getPillagedProvinces(provinces, 11L);

        Assert.assertEquals(0, pillaged.size());

        pillaged = economicalSheetDao.getPillagedProvinces(provinces, 1L);

        Assert.assertEquals(3, pillaged.size());
        Assert.assertEquals("eCornwall", pillaged.get(0));
        Assert.assertEquals("eLyonnais", pillaged.get(1));
        Assert.assertEquals("eHinterpommern", pillaged.get(2));

        provinces = new ArrayList<>(economicalSheetDao.getOwnedAndControlledProvinces("france", 1L).keySet());

        pillaged = economicalSheetDao.getPillagedProvinces(provinces, 1L);
        Collections.sort(pillaged);

        Assert.assertEquals(7, pillaged.size());
        Assert.assertEquals("eBearn", pillaged.get(0));
        Assert.assertEquals("eCornwall", pillaged.get(1));
        Assert.assertEquals("eLanguedoc", pillaged.get(2));
        Assert.assertEquals("eLyonnais", pillaged.get(3));
        Assert.assertEquals("eNormandie", pillaged.get(4));
        Assert.assertEquals("ePoitou", pillaged.get(5));
        Assert.assertEquals("eQuercy", pillaged.get(6));
    }
}
