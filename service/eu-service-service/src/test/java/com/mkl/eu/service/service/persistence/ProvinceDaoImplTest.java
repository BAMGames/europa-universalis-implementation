package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.service.service.persistence.oe.ref.province.SeaProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
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
 * Test for ProvinceDao.
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/provinces.xml", "eco.xml", "geoGroup.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class ProvinceDaoImplTest {

    @Autowired
    private IProvinceDao provinceDao;

    @Test
    public void testGetProvinceByName() {
        Assert.assertEquals(null, provinceDao.getProvinceByName("toto"));
        Assert.assertEquals(TerrainEnum.DENSE_FOREST, provinceDao.getProvinceByName("eKarelen").getTerrain());
        Assert.assertEquals(true, provinceDao.getProvinceByName("sJava") instanceof SeaProvinceEntity);
    }

    @Test
    public void testGetRegionByName() {
        Assert.assertEquals(null, provinceDao.getRegionByName("toto"));
        Assert.assertEquals(2, provinceDao.getRegionByName("Belem").getIncome());
        Assert.assertEquals(7, provinceDao.getRegionByName("Belem").getDifficulty());
        Assert.assertEquals(0, provinceDao.getRegionByName("Belem").getTolerance());
    }

    @Test
    public void testGetGoldInProvince() {
        Assert.assertEquals(null, provinceDao.getGoldInProvince("toto"));
        Assert.assertEquals(20, provinceDao.getGoldInProvince("eQuercy").getValue());
        Assert.assertEquals(50, provinceDao.getGoldInProvince("eLanguedoc").getValue());
    }

    @Test
    public void getGeoGroups() {
        List<String> geoGroups = provinceDao.getGeoGroups("toto");

        Assert.assertEquals(0, geoGroups.size());

        geoGroups = provinceDao.getGeoGroups("eSchwaben");

        Assert.assertEquals(1, geoGroups.size());
        Assert.assertTrue(geoGroups.contains("HRE"));

        geoGroups = provinceDao.getGeoGroups("eCampania");

        Assert.assertEquals(1, geoGroups.size());
        Assert.assertTrue(geoGroups.contains("ITALY"));

        geoGroups = provinceDao.getGeoGroups("rAral~W");

        Assert.assertEquals(1, geoGroups.size());
        Assert.assertTrue(geoGroups.contains("ASIA"));

        geoGroups = provinceDao.getGeoGroups("rAzteca~SE");

        Assert.assertEquals(2, geoGroups.size());
        Assert.assertTrue(geoGroups.contains("AMERICA"));
        Assert.assertTrue(geoGroups.contains("SOUTH AMERICA"));

        geoGroups = provinceDao.getGeoGroups("rAzteca~C");

        Assert.assertEquals(3, geoGroups.size());
        Assert.assertTrue(geoGroups.contains("AMERICA"));
        Assert.assertTrue(geoGroups.contains("SOUTH AMERICA"));
        Assert.assertTrue(geoGroups.contains("GOLD"));

        geoGroups = provinceDao.getGeoGroups("rAzteca~N");

        Assert.assertEquals(0, geoGroups.size());
    }
}
