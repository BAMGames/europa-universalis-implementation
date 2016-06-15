package com.mkl.eu.service.service.persistence;

import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.LimitTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.UnitActionEnum;
import com.mkl.eu.client.service.vo.tables.Period;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.client.service.vo.tables.Tech;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
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
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;

/**
 * Description of the class.
 *
 * @author MKL
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mkl/eu/service/service/eu-service-service-applicationContext.xml", "classpath:com/mkl/eu/service/service/test-database-applicationContext.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TableDaoImplTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private ITablesService tablesService;

    @Before
    public void initDb() throws Exception{
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
    }

    private IDataSet getDataSet() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader()
                                      .getResourceAsStream("com/mkl/eu/service/service/persistence/tables.xml");
        InputSource xmlSource = new InputSource(inputStream);
        FlatXmlProducer flatXmlProducer = new FlatXmlProducer(xmlSource);
        ReplacementDataSet dataSet= new ReplacementDataSet(new FlatXmlDataSet(flatXmlProducer));
        dataSet.addReplacementObject("[null]", null);
        return dataSet;

    }

    private IDatabaseConnection getConnection() throws Exception {
        Connection jdbcConnection = dataSource.getConnection();
        return new DatabaseConnection(jdbcConnection);
    }

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
        Assert.assertEquals(2, tables.getForeignTrades().get(11).getCountryValue());
        Assert.assertEquals(null, tables.getForeignTrades().get(11).getMinValue());
        Assert.assertEquals(49, tables.getForeignTrades().get(11).getMaxValue().intValue());
        Assert.assertEquals(true, tables.getForeignTrades().get(11).isForeignTrade());
        Assert.assertEquals(60, tables.getForeignTrades().get(11).getValue());

        Assert.assertEquals(45, tables.getDomesticTrades().size());
        Assert.assertEquals(1, tables.getDomesticTrades().get(8).getCountryValue());
        Assert.assertEquals(251, tables.getDomesticTrades().get(8).getMinValue().intValue());
        Assert.assertEquals(null, tables.getDomesticTrades().get(8).getMaxValue());
        Assert.assertEquals(false, tables.getDomesticTrades().get(8).isForeignTrade());
        Assert.assertEquals(20, tables.getDomesticTrades().get(8).getValue());

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

        Assert.assertEquals(639, tables.getLimits().size());
        Assert.assertEquals("france", tables.getLimits().get(258).getCountry());
        Assert.assertEquals(LimitTypeEnum.LEADER_CONQUISTADOR, tables.getLimits().get(258).getType());
        Assert.assertEquals(1, tables.getLimits().get(258).getNumber().intValue());
        Assert.assertEquals(Period.PERIOD_IV, tables.getLimits().get(258).getPeriod().getName());
    }
}
