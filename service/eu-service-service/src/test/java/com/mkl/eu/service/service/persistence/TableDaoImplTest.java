package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.enumeration.AdminActionResultEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.LimitTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.UnitActionEnum;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.persistence.tables.ITablesDao;
import org.apache.commons.lang3.StringUtils;
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
 * Test for Tables service, dao and mapping.
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
@DataSet(value = "tables.xml", columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class TableDaoImplTest {
    @Autowired
    private ITablesService tablesService;

    @Autowired
    private ITablesDao tablesDao;

    @Test
    public void testTables() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(7, tables.getPeriods().size());
        Assert.assertEquals(Period.PERIOD_III, tables.getPeriods().get(2).getName());
        Assert.assertEquals(15, tables.getPeriods().get(2).getBegin().intValue());
        Assert.assertEquals(25, tables.getPeriods().get(2).getEnd().intValue());

        Assert.assertEquals(16, tables.getTechs().size());
        Assert.assertEquals("TERCIO", tables.getTechs().get(2).getName());
        Assert.assertEquals(16, tables.getTechs().get(2).getBeginBox().intValue());
        Assert.assertEquals(6, tables.getTechs().get(2).getBeginTurn().intValue());
        Assert.assertEquals("espagne", tables.getTechs().get(2).getCountry());
        Assert.assertEquals(true, tables.getTechs().get(2).isLand());

        Assert.assertEquals(55, tables.getForeignTrades().size());

        Assert.assertEquals(45, tables.getDomesticTrades().size());

        Assert.assertEquals(145, tables.getBasicForces().size());
        Assert.assertEquals("hollande", tables.getBasicForces().get(109).getCountry());
        Assert.assertEquals(1, tables.getBasicForces().get(109).getNumber().intValue());
        Assert.assertEquals(Period.PERIOD_IV, tables.getBasicForces().get(109).getPeriod().getName());
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, tables.getBasicForces().get(109).getType());

        Assert.assertEquals(1339, tables.getUnits().size());
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, tables.getUnits().get(1162).getType());
        Assert.assertEquals("pologne", tables.getUnits().get(1162).getCountry());
        Assert.assertEquals(UnitActionEnum.MAINT_WAR, tables.getUnits().get(1162).getAction());
        Assert.assertEquals(25, tables.getUnits().get(1162).getPrice().intValue());
        Assert.assertEquals(Tech.ARQUEBUS, tables.getUnits().get(1162).getTech().getName());
        Assert.assertEquals(true, tables.getUnits().get(1162).isSpecial());

        Assert.assertEquals(1400, tables.getLimits().size());
        Limit limit = CommonUtil.findFirst(tables.getLimits().stream(),
                l -> StringUtils.equals("france", l.getCountry()) && l.getType() == LimitTypeEnum.LEADER_CONQUISTADOR
                        && StringUtils.equals(Period.PERIOD_IV, l.getPeriod().getName()));
        Assert.assertEquals(1, limit.getNumber().intValue());

        Assert.assertEquals(90, tables.getResults().size());
        Result result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == -4 && r.getDie() == 2);
        Assert.assertEquals(AdminActionResultEnum.FUMBLE, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 2 && r.getDie() == 1);
        Assert.assertEquals(AdminActionResultEnum.FAILED, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 0 && r.getDie() == 5);
        Assert.assertEquals(AdminActionResultEnum.AVERAGE, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 0 && r.getDie() == 6);
        Assert.assertEquals(AdminActionResultEnum.AVERAGE_PLUS, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == -3 && r.getDie() == 10);
        Assert.assertEquals(AdminActionResultEnum.SUCCESS, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 4 && r.getDie() == 9);
        Assert.assertEquals(AdminActionResultEnum.CRITICAL_HIT, result.getResult());
    }

    @Test
    public void testTradeIncome() {
        Assert.assertEquals(2, tablesDao.getTradeIncome(1, 2, false));
        Assert.assertEquals(4, tablesDao.getTradeIncome(1, 4, false));
        Assert.assertEquals(9, tablesDao.getTradeIncome(60, 3, false));
        Assert.assertEquals(21, tablesDao.getTradeIncome(160, 3, false));
        Assert.assertEquals(27, tablesDao.getTradeIncome(161, 3, false));
        Assert.assertEquals(60, tablesDao.getTradeIncome(251, 3, false));
        Assert.assertEquals(60, tablesDao.getTradeIncome(9999, 3, false));
        Assert.assertEquals(100, tablesDao.getTradeIncome(9999, 5, false));

        Assert.assertEquals(60, tablesDao.getTradeIncome(0, 2, true));
        Assert.assertEquals(120, tablesDao.getTradeIncome(0, 4, true));
        Assert.assertEquals(81, tablesDao.getTradeIncome(60, 3, true));
        Assert.assertEquals(54, tablesDao.getTradeIncome(299, 3, true));
        Assert.assertEquals(45, tablesDao.getTradeIncome(300, 3, true));
        Assert.assertEquals(3, tablesDao.getTradeIncome(1100, 3, true));
        Assert.assertEquals(3, tablesDao.getTradeIncome(9999, 3, true));
        Assert.assertEquals(5, tablesDao.getTradeIncome(9999, 5, true));
    }
}
