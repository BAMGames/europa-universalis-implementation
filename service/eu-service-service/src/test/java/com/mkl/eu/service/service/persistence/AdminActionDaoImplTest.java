package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
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
import java.util.Collections;
import java.util.List;

/**
 * Test for AdminActionDao.
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/provinces.xml", "/com/mkl/eu/service/service/persistence/referentiel.xml", "adminAction.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class AdminActionDaoImplTest {

    @Autowired
    private IAdminActionDao adminActionDao;

    @Test
    public void testFindAdminActions() {
        List<AdministrativeActionEntity> actions;

        actions = adminActionDao.findAdminActions(null, null, null);

        Assert.assertEquals(6, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());
        Assert.assertEquals(3L, actions.get(1).getId().longValue());
        Assert.assertEquals(4L, actions.get(2).getId().longValue());
        Assert.assertEquals(5L, actions.get(3).getId().longValue());
        Assert.assertEquals(6L, actions.get(4).getId().longValue());
        Assert.assertEquals(7L, actions.get(5).getId().longValue());

        actions = adminActionDao.findAdminActions(1L, null, null);

        Assert.assertEquals(5, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());
        Assert.assertEquals(3L, actions.get(1).getId().longValue());
        Assert.assertEquals(4L, actions.get(2).getId().longValue());
        Assert.assertEquals(6L, actions.get(3).getId().longValue());
        Assert.assertEquals(7L, actions.get(4).getId().longValue());

        actions = adminActionDao.findAdminActions(2L, null, null);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(5L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(3L, null, null);

        Assert.assertEquals(0, actions.size());

        actions = adminActionDao.findAdminActions(null, 1, null);

        Assert.assertEquals(5, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());
        Assert.assertEquals(3L, actions.get(1).getId().longValue());
        Assert.assertEquals(4L, actions.get(2).getId().longValue());
        Assert.assertEquals(5L, actions.get(3).getId().longValue());
        Assert.assertEquals(7L, actions.get(4).getId().longValue());

        actions = adminActionDao.findAdminActions(null, 2, null);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(6L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(null, 3, null);

        Assert.assertEquals(0, actions.size());

        actions = adminActionDao.findAdminActions(null, null, 12L);

        Assert.assertEquals(4, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());
        Assert.assertEquals(5L, actions.get(1).getId().longValue());
        Assert.assertEquals(6L, actions.get(2).getId().longValue());
        Assert.assertEquals(7L, actions.get(3).getId().longValue());

        actions = adminActionDao.findAdminActions(null, null, 13L);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(3L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(null, null, 14L);

        Assert.assertEquals(0, actions.size());

        actions = adminActionDao.findAdminActions(null, null, null, AdminActionTypeEnum.TFI);

        Assert.assertEquals(5, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());
        Assert.assertEquals(3L, actions.get(1).getId().longValue());
        Assert.assertEquals(4L, actions.get(2).getId().longValue());
        Assert.assertEquals(5L, actions.get(3).getId().longValue());
        Assert.assertEquals(6L, actions.get(4).getId().longValue());

        actions = adminActionDao.findAdminActions(null, null, null, AdminActionTypeEnum.COL);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(7L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(null, null, null, AdminActionTypeEnum.TP);

        Assert.assertEquals(0, actions.size());

        actions = adminActionDao.findAdminActions(null, null, null, AdminActionTypeEnum.TFI, AdminActionTypeEnum.COL);

        Assert.assertEquals(6, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());
        Assert.assertEquals(3L, actions.get(1).getId().longValue());
        Assert.assertEquals(4L, actions.get(2).getId().longValue());
        Assert.assertEquals(5L, actions.get(3).getId().longValue());
        Assert.assertEquals(6L, actions.get(4).getId().longValue());
        Assert.assertEquals(7L, actions.get(5).getId().longValue());

        actions = adminActionDao.findAdminActions(1L, 1, 12L, AdminActionTypeEnum.TFI);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(1L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(1L, 1, 13L, AdminActionTypeEnum.TFI);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(3L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(2L, 1, 12L, AdminActionTypeEnum.TFI);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(5L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(1L, 2, 12L, AdminActionTypeEnum.TFI);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(6L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(1L, 1, 12L, AdminActionTypeEnum.COL);

        Assert.assertEquals(1, actions.size());
        Assert.assertEquals(7L, actions.get(0).getId().longValue());

        actions = adminActionDao.findAdminActions(2L, 2, 13L, AdminActionTypeEnum.COL);

        Assert.assertEquals(0, actions.size());
    }

    @Test
    public void testInlandAdvance() {
        List<String> countries;
        countries = adminActionDao.getCountriesInlandAdvance("rMauritanie~N", 1L);

        Assert.assertEquals(1, countries.size());
        Assert.assertEquals("espagne", countries.get(0));

        countries = adminActionDao.getCountriesInlandAdvance("rMauritanie~S", 1L);

        Assert.assertEquals(1, countries.size());
        Assert.assertEquals("espagne", countries.get(0));

        countries = adminActionDao.getCountriesInlandAdvance("rMauritanie~S", 2L);

        Assert.assertEquals(1, countries.size());
        Assert.assertEquals("maroc", countries.get(0));

        countries = adminActionDao.getCountriesInlandAdvance("rSiberie~W", 1L);
        Collections.sort(countries);

        Assert.assertEquals(4, countries.size());
        Assert.assertEquals("pologne", countries.get(0));
        Assert.assertEquals("russie", countries.get(1));
        Assert.assertEquals("steppes", countries.get(2));
        Assert.assertEquals("turquie", countries.get(3));

        countries = adminActionDao.getCountriesInlandAdvance("rSiberie~E", 1L);
        Collections.sort(countries);

        Assert.assertEquals(4, countries.size());
        Assert.assertEquals("pologne", countries.get(0));
        Assert.assertEquals("russie", countries.get(1));
        Assert.assertEquals("steppes", countries.get(2));
        Assert.assertEquals("turquie", countries.get(3));

        countries = adminActionDao.getCountriesInlandAdvance("rSiberie~W", 2L);

        Assert.assertEquals(1, countries.size());
        Assert.assertEquals("steppes", countries.get(0));

        countries = adminActionDao.getCountriesInlandAdvance("rAral~W", 1L);
        Collections.sort(countries);

        Assert.assertEquals(4, countries.size());
        Assert.assertEquals("astrakhan", countries.get(0));
        Assert.assertEquals("perse", countries.get(1));
        Assert.assertEquals("steppes", countries.get(2));
        Assert.assertEquals("turquie", countries.get(3));

        countries = adminActionDao.getCountriesInlandAdvance("rAral~E", 1L);
        Collections.sort(countries);

        Assert.assertEquals(4, countries.size());
        Assert.assertEquals("astrakhan", countries.get(0));
        Assert.assertEquals("perse", countries.get(1));
        Assert.assertEquals("steppes", countries.get(2));
        Assert.assertEquals("turquie", countries.get(3));

        countries = adminActionDao.getCountriesInlandAdvance("rAral~W", 2L);
        Collections.sort(countries);

        Assert.assertEquals(3, countries.size());
        Assert.assertEquals("astrakhan", countries.get(0));
        Assert.assertEquals("perse", countries.get(1));
        Assert.assertEquals("steppes", countries.get(2));
    }

    @Test
    public void testCountTpInRegion() {
        int tps;

        tps = adminActionDao.countOtherTpsInRegion("russie", "Venezuela", 1L);
        Assert.assertEquals(2, tps);

        tps = adminActionDao.countOtherTpsInRegion("espagne", "Venezuela", 1L);
        Assert.assertEquals(1, tps);

        tps = adminActionDao.countOtherTpsInRegion("turquie", "Venezuela", 1L);
        Assert.assertEquals(1, tps);

        tps = adminActionDao.countOtherTpsInRegion("russie", "Venezuela", 2L);
        Assert.assertEquals(0, tps);

        tps = adminActionDao.countOtherTpsInRegion("russie", "Recife", 1L);
        Assert.assertEquals(1, tps);

        tps = adminActionDao.countOtherTpsInRegion("hollande", "Recife", 1L);
        Assert.assertEquals(0, tps);
    }

    @Test
    public void testTradeFleetAccessRotw() {
        List<String> countries;

        countries = adminActionDao.getCountriesTradeFleetAccessRotw("sAmerique", 1L);
        Collections.sort(countries);

        Assert.assertEquals(2, countries.size());
        Assert.assertEquals("angleterre", countries.get(0));
        Assert.assertEquals("france", countries.get(1));

        countries = adminActionDao.getCountriesTradeFleetAccessRotw("sCaraibes", 1L);
        Collections.sort(countries);

        Assert.assertEquals(4, countries.size());
        Assert.assertEquals("espagne", countries.get(0));
        Assert.assertEquals("france", countries.get(1));
        Assert.assertEquals("hollande", countries.get(2));
        Assert.assertEquals("portugal", countries.get(3));

        countries = adminActionDao.getCountriesTradeFleetAccessRotw("sRecife", 1L);
        Collections.sort(countries);

        Assert.assertEquals(3, countries.size());
        Assert.assertEquals("angleterre", countries.get(0));
        Assert.assertEquals("hollande", countries.get(1));
        Assert.assertEquals("portugal", countries.get(2));

        countries = adminActionDao.getCountriesTradeFleetAccessRotw("sAmerique", 2L);
        Collections.sort(countries);

        Assert.assertEquals(0, countries.size());
    }

    @Test
    public void testMaxTechBox() {
        Integer box;
        List<CultureEnum> cultures = new ArrayList<>();
        cultures.add(CultureEnum.ROTW);

        box = adminActionDao.getMaxTechBox(true, cultures, 1L);
        Assert.assertNull(box);

        cultures.clear();
        cultures.add(CultureEnum.ORTHODOX);

        box = adminActionDao.getMaxTechBox(true, cultures, 2L);
        Assert.assertNull(box);

        box = adminActionDao.getMaxTechBox(true, cultures, 1L);
        Assert.assertEquals(50, box.intValue());

        box = adminActionDao.getMaxTechBox(false, cultures, 1L);
        Assert.assertEquals(28, box.intValue());

        cultures.clear();
        cultures.add(CultureEnum.LATIN);

        box = adminActionDao.getMaxTechBox(true, cultures, 1L);
        Assert.assertEquals(51, box.intValue());

        box = adminActionDao.getMaxTechBox(false, cultures, 1L);
        Assert.assertNull(box);

        cultures.add(CultureEnum.ORTHODOX);

        box = adminActionDao.getMaxTechBox(false, cultures, 1L);
        Assert.assertEquals(28, box.intValue());

        cultures.clear();
        cultures.add(CultureEnum.ISLAM);

        box = adminActionDao.getMaxTechBox(true, cultures, 1L);
        Assert.assertEquals(21, box.intValue());
    }
}
