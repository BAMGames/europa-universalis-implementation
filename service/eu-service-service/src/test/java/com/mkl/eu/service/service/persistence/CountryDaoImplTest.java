package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/referentiel.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class CountryDaoImplTest {

    @Autowired
    private ICountryDao countryDao;

    @Test
    public void testGetCountryByName() {
        Assert.assertEquals(null, countryDao.getCountryByName("toto"));
        Assert.assertEquals(CountryTypeEnum.MINOR, countryDao.getCountryByName("kazan").getType());
        Assert.assertEquals(12, countryDao.getCountryByName("kazan").getFidelity());
        Assert.assertEquals(ArmyClassEnum.I, countryDao.getCountryByName("kazan").getArmyClass());
        Assert.assertEquals(CultureEnum.ISLAM, countryDao.getCountryByName("kazan").getCulture());
    }
}
