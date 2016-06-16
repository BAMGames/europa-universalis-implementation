package com.mkl.eu.service.service.persistence;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.eco.IEconomicalSheetDao;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
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
 * Test for EconomicalSheetDao.
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

    @After
    public void clearDb() throws Exception {
        DatabaseOperation.DELETE.execute(getConnection(), getDataSet());
        DatabaseOperation.DELETE.execute(getConnection(), getDataSetProvinces());
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

        provinces = economicalSheetDao.getOwnedAndControlledProvinces("hansia", 1L);
        Assert.assertEquals(0, provinces.size());

        provinces = economicalSheetDao.getOwnedAndControlledProvinces("hanse", 1L);
        Assert.assertEquals(4, provinces.size());
        Assert.assertEquals(20, provinces.values().stream().collect(Collectors.summingInt(value -> value)).intValue());
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

    @Test
    public void testMnu() {
        List<String> provinces = new ArrayList<>();
        Assert.assertEquals(15, economicalSheetDao.getMnuIncome("angleterre", provinces, 1L).intValue());
        Assert.assertEquals(null, economicalSheetDao.getMnuIncome("angleterre", provinces, 2L));
        Assert.assertEquals(14, economicalSheetDao.getMnuIncome("france", provinces, 1L).intValue());
        Assert.assertEquals(null, economicalSheetDao.getMnuIncome("france", provinces, 2L));
        Assert.assertEquals(null, economicalSheetDao.getMnuIncome("prusse", provinces, 1L));

        provinces.clear();
        provinces.add("eIle-de-France");
        Assert.assertEquals(null, economicalSheetDao.getMnuIncome("angleterre", provinces, 1L));
        Assert.assertEquals(14, economicalSheetDao.getMnuIncome("france", provinces, 1L).intValue());

        provinces.clear();
        provinces.add("ePoitou");
        provinces.add("eBearn");
        Assert.assertEquals(null, economicalSheetDao.getMnuIncome("france", provinces, 1L));
    }

    @Test
    public void testGold() {
        List<String> provinces = new ArrayList<>();
        Assert.assertEquals(0, economicalSheetDao.getGoldIncome(provinces, 1L).intValue());

        provinces.clear();
        provinces.add("eQuercy");
        Assert.assertEquals(20, economicalSheetDao.getGoldIncome(provinces, 1L).intValue());

        provinces.clear();
        provinces.add("eLanguedoc");
        Assert.assertEquals(50, economicalSheetDao.getGoldIncome(provinces, 1L).intValue());

        provinces.clear();
        provinces.add("eQuercy");
        provinces.add("eLanguedoc");
        Assert.assertEquals(70, economicalSheetDao.getGoldIncome(provinces, 1L).intValue());

        provinces.clear();
        provinces.add("eQuercy");
        provinces.add("eLanguedoc");
        provinces.add("eBearn");
        provinces.add("eIle-de-France");
        Assert.assertEquals(130, economicalSheetDao.getGoldIncome(provinces, 1L).intValue());
        Assert.assertEquals(70, economicalSheetDao.getGoldIncome(provinces, 2L).intValue());
    }

    @Test
    public void testFleets() {
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("france", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("angleterre", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("hollande", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("suede", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("turquie", 2L));

        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("france", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("angleterre", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("hollande", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("suede", 2L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("turquie", 2L));

        Assert.assertEquals(21, economicalSheetDao.getFleetLevelIncome("france", 1L).intValue());
        Assert.assertEquals(6, economicalSheetDao.getFleetLevelIncome("angleterre", 1L).intValue());
        Assert.assertEquals(8, economicalSheetDao.getFleetLevelIncome("hollande", 1L).intValue());
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("suede", 1L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelIncome("turquie", 1L));

        Assert.assertEquals(54, economicalSheetDao.getFleetLevelMonopoly("france", 1L).intValue());
        Assert.assertEquals(4, economicalSheetDao.getFleetLevelMonopoly("angleterre", 1L).intValue());
        Assert.assertEquals(20, economicalSheetDao.getFleetLevelMonopoly("hollande", 1L).intValue());
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("suede", 1L));
        Assert.assertEquals(null, economicalSheetDao.getFleetLevelMonopoly("turquie", 1L));
    }

    @Test
    public void testTradeCenters() {
        Map<String, List<CounterFaceTypeEnum>> centers =  economicalSheetDao.getTradeCenters(2L);

        Assert.assertEquals(0, centers.size());

        centers = economicalSheetDao.getTradeCenters(1L);

        Assert.assertEquals(2, centers.size());
        List<CounterFaceTypeEnum> franceCenters = centers.get("france");
        Collections.sort(franceCenters);
        Assert.assertEquals(2, franceCenters.size());
        Assert.assertEquals(CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN, franceCenters.get(0));
        Assert.assertEquals(CounterFaceTypeEnum.TRADE_CENTER_ATLANTIC, franceCenters.get(1));
        Assert.assertEquals(1, centers.get("hollande").size());
        Assert.assertEquals(CounterFaceTypeEnum.TRADE_CENTER_INDIAN, centers.get("hollande").get(0));
    }
}
