package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.Referential;
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
    private ITablesService tablesService;

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

    @Test
    public void testReferential() {
        tablesService.refresh();
        Referential referential = tablesService.getReferential();

        Assert.assertEquals(119, referential.getCountries().size());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getName());
        Assert.assertEquals(CountryTypeEnum.MINORMAJOR, referential.getCountries().get(5).getType());
        Assert.assertEquals(ReligionEnum.PROTESTANT, referential.getCountries().get(5).getReligion());
        Assert.assertEquals(20, referential.getCountries().get(5).getRoyalMarriage().intValue());
        Assert.assertEquals(10, referential.getCountries().get(5).getSubsidies().intValue());
        Assert.assertEquals(3, referential.getCountries().get(5).getMilitaryAlliance().intValue());
        Assert.assertEquals(5, referential.getCountries().get(5).getExpCorps().intValue());
        Assert.assertEquals(9, referential.getCountries().get(5).getEntryInWar().intValue());
        Assert.assertEquals(null, referential.getCountries().get(5).getVassal());
        Assert.assertEquals(null, referential.getCountries().get(5).getAnnexion());
        Assert.assertEquals(12, referential.getCountries().get(5).getFidelity());
        Assert.assertEquals(ArmyClassEnum.III, referential.getCountries().get(5).getArmyClass());
        Assert.assertEquals(CultureEnum.LATIN, referential.getCountries().get(5).getCulture());
        Assert.assertEquals(null, referential.getCountries().get(5).isHre());
        Assert.assertEquals(null, referential.getCountries().get(5).isElector());
        Assert.assertEquals(null, referential.getCountries().get(5).getPreference());
        Assert.assertEquals(null, referential.getCountries().get(5).getPreferenceBonus());
        Assert.assertEquals(3, referential.getCountries().get(5).getBasicForces().size());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getBasicForces().get(0).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(5).getBasicForces().get(0).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, referential.getCountries().get(5).getBasicForces().get(0).getType());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getBasicForces().get(1).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(5).getBasicForces().get(1).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.FLEET_PLUS, referential.getCountries().get(5).getBasicForces().get(1).getType());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getBasicForces().get(2).getCountry());
        Assert.assertEquals(3, referential.getCountries().get(5).getBasicForces().get(2).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.LD, referential.getCountries().get(5).getBasicForces().get(2).getType());
        Assert.assertEquals(25, referential.getCountries().get(5).getLimits().size());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getLimits().get(0).getCountry());
        Assert.assertEquals(3, referential.getCountries().get(5).getLimits().get(0).getNumber().intValue());
        Assert.assertEquals(CounterTypeEnum.ARMY, referential.getCountries().get(5).getLimits().get(0).getType());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getLimits().get(10).getCountry());
        Assert.assertEquals(2, referential.getCountries().get(5).getLimits().get(10).getNumber().intValue());
        Assert.assertEquals(CounterTypeEnum.FORT45, referential.getCountries().get(5).getLimits().get(10).getType());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getLimits().get(20).getCountry());
        Assert.assertEquals(2, referential.getCountries().get(5).getLimits().get(20).getNumber().intValue());
        Assert.assertEquals(CounterTypeEnum.MNU_ART, referential.getCountries().get(5).getLimits().get(20).getType());
        Assert.assertEquals(2, referential.getCountries().get(5).getReinforcements().size());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getReinforcements().get(0).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(5).getReinforcements().get(0).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, referential.getCountries().get(5).getReinforcements().get(0).getType());
        Assert.assertEquals("hollande", referential.getCountries().get(5).getReinforcements().get(1).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(5).getReinforcements().get(1).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.FLEET_PLUS, referential.getCountries().get(5).getReinforcements().get(1).getType());

        Assert.assertEquals("boheme", referential.getCountries().get(85).getName());
        Assert.assertEquals(CountryTypeEnum.MINOR, referential.getCountries().get(85).getType());
        Assert.assertEquals(ReligionEnum.PROTESTANT, referential.getCountries().get(85).getReligion());
        Assert.assertEquals(15, referential.getCountries().get(85).getRoyalMarriage().intValue());
        Assert.assertEquals(20, referential.getCountries().get(85).getSubsidies().intValue());
        Assert.assertEquals(1, referential.getCountries().get(85).getMilitaryAlliance().intValue());
        Assert.assertEquals(3, referential.getCountries().get(85).getExpCorps().intValue());
        Assert.assertEquals(3, referential.getCountries().get(85).getEntryInWar().intValue());
        Assert.assertEquals(5, referential.getCountries().get(85).getVassal().intValue());
        Assert.assertEquals(15, referential.getCountries().get(85).getAnnexion().intValue());
        Assert.assertEquals(15, referential.getCountries().get(85).getFidelity());
        Assert.assertEquals(ArmyClassEnum.III, referential.getCountries().get(85).getArmyClass());
        Assert.assertEquals(CultureEnum.LATIN, referential.getCountries().get(85).getCulture());
        Assert.assertEquals(true, referential.getCountries().get(85).isHre());
        Assert.assertEquals(true, referential.getCountries().get(85).isElector());
        Assert.assertEquals(null, referential.getCountries().get(85).getPreference());
        Assert.assertEquals(null, referential.getCountries().get(85).getPreferenceBonus());
        Assert.assertEquals(1, referential.getCountries().get(85).getBasicForces().size());
        Assert.assertEquals("boheme", referential.getCountries().get(85).getBasicForces().get(0).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(85).getBasicForces().get(0).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, referential.getCountries().get(85).getBasicForces().get(0).getType());
        Assert.assertEquals(2, referential.getCountries().get(85).getLimits().size());
        Assert.assertEquals("boheme", referential.getCountries().get(85).getLimits().get(0).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(85).getLimits().get(0).getNumber().intValue());
        Assert.assertEquals(CounterTypeEnum.ARMY, referential.getCountries().get(85).getLimits().get(0).getType());
        Assert.assertEquals("boheme", referential.getCountries().get(85).getLimits().get(1).getCountry());
        Assert.assertEquals(2, referential.getCountries().get(85).getLimits().get(1).getNumber().intValue());
        Assert.assertEquals(CounterTypeEnum.LD, referential.getCountries().get(85).getLimits().get(1).getType());
        Assert.assertEquals(1, referential.getCountries().get(85).getReinforcements().size());
        Assert.assertEquals("boheme", referential.getCountries().get(85).getReinforcements().get(0).getCountry());
        Assert.assertEquals(1, referential.getCountries().get(85).getReinforcements().get(0).getNumber().intValue());
        Assert.assertEquals(ForceTypeEnum.LD, referential.getCountries().get(85).getReinforcements().get(0).getType());

        Assert.assertEquals("pologne", referential.getCountries().get(8).getName());
        Assert.assertEquals(CountryTypeEnum.MINORMAJOR, referential.getCountries().get(8).getType());
        Assert.assertEquals(ReligionEnum.CATHOLIC, referential.getCountries().get(8).getReligion());
        Assert.assertEquals("FRA", referential.getCountries().get(8).getPreference());
        Assert.assertEquals(1, referential.getCountries().get(8).getPreferenceBonus().intValue());
    }
}
