package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.enumeration.*;
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

import java.util.List;

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
        Assert.assertEquals(ResultEnum.FUMBLE, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 2 && r.getDie() == 1);
        Assert.assertEquals(ResultEnum.FAILED, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 0 && r.getDie() == 5);
        Assert.assertEquals(ResultEnum.AVERAGE, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 0 && r.getDie() == 6);
        Assert.assertEquals(ResultEnum.AVERAGE_PLUS, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == -3 && r.getDie() == 10);
        Assert.assertEquals(ResultEnum.SUCCESS, result.getResult());
        result = CommonUtil.findFirst(tables.getResults().stream(),
                r -> r.getColumn() == 4 && r.getDie() == 9);
        Assert.assertEquals(ResultEnum.CRITICAL_HIT, result.getResult());
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

    @Test
    public void testBattleTech() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(113, tables.getBattleTechs().size());

        BattleTech arqRen = tables.getBattleTechs().stream()
                .filter(bt -> StringUtils.equals(bt.getTechnologyFor(), Tech.ARQUEBUS) &&
                        StringUtils.equals(bt.getTechnologyAgainst(), Tech.RENAISSANCE))
                .findAny()
                .orElse(null);

        Assert.assertNotNull(arqRen);
        Assert.assertEquals(true, arqRen.isLand());
        Assert.assertEquals("C", arqRen.getColumnFire());
        Assert.assertEquals("A", arqRen.getColumnShock());
        Assert.assertEquals(2, arqRen.getMoral());
        Assert.assertEquals(true, arqRen.isMoralBonusVeteran());

        BattleTech car74 = tables.getBattleTechs().stream()
                .filter(bt -> StringUtils.equals(bt.getTechnologyFor(), Tech.CARRACK) &&
                        StringUtils.equals(bt.getTechnologyAgainst(), Tech.SEVENTY_FOUR))
                .findAny()
                .orElse(null);

        Assert.assertNotNull(car74);
        Assert.assertEquals(false, car74.isLand());
        Assert.assertEquals(null, car74.getColumnFire());
        Assert.assertEquals("E", car74.getColumnShock());
        Assert.assertEquals(1, car74.getMoral());
        Assert.assertEquals(false, car74.isMoralBonusVeteran());
    }

    @Test
    public void testCombatResult() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(60, tables.getCombatResults().size());

        CombatResult a5 = tables.getCombatResults().stream()
                .filter(cr -> StringUtils.equals("A", cr.getColumn()) && cr.getDice() == 5)
                .findAny()
                .orElse(null);

        Assert.assertNotNull(a5);
        Assert.assertEquals(0, a5.getRoundLoss().intValue());
        Assert.assertEquals(2, a5.getThirdLoss().intValue());
        Assert.assertEquals(0, a5.getMoraleLoss().intValue());

        CombatResult d12 = tables.getCombatResults().stream()
                .filter(cr -> StringUtils.equals("D", cr.getColumn()) && cr.getDice() == 12)
                .findAny()
                .orElse(null);

        Assert.assertNotNull(d12);
        Assert.assertEquals(2, d12.getRoundLoss().intValue());
        Assert.assertEquals(0, d12.getThirdLoss().intValue());
        Assert.assertEquals(2, d12.getMoraleLoss().intValue());
    }

    @Test
    public void testArmyClasse() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(63, tables.getArmyClasses().size());

        ArmyClasse russieI = tables.getArmyClasses().stream()
                .filter(ac -> StringUtils.equals(Period.PERIOD_I, ac.getPeriod()) && ac.getArmyClass() == ArmyClassEnum.I)
                .findAny()
                .orElse(null);

        Assert.assertNotNull(russieI);
        Assert.assertEquals(7, russieI.getSize().intValue());

        ArmyClasse fraV = tables.getArmyClasses().stream()
                .filter(ac -> StringUtils.equals(Period.PERIOD_V, ac.getPeriod()) && ac.getArmyClass() == ArmyClassEnum.IV)
                .findAny()
                .orElse(null);

        Assert.assertNotNull(fraV);
        Assert.assertEquals(3, fraV.getSize().intValue());
    }

    @Test
    public void testArmyArtillery() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(133, tables.getArmyArtilleries().size());

        ArmyArtillery russieI = tables.getArmyArtilleries().stream()
                .filter(ac -> StringUtils.equals(Period.PERIOD_I, ac.getPeriod()) && StringUtils.equals(PlayableCountry.RUSSIA, ac.getCountry()))
                .findAny()
                .orElse(null);

        Assert.assertNotNull(russieI);
        Assert.assertEquals(1, russieI.getArtillery().intValue());

        ArmyArtillery fraV = tables.getArmyArtilleries().stream()
                .filter(ac -> StringUtils.equals(Period.PERIOD_V, ac.getPeriod()) && StringUtils.equals(PlayableCountry.FRANCE, ac.getCountry()))
                .findAny()
                .orElse(null);

        Assert.assertNotNull(fraV);
        Assert.assertEquals(5, fraV.getArtillery().intValue());

        ArmyArtillery IIMIII = tables.getArmyArtilleries().stream()
                .filter(ac -> StringUtils.equals(Period.PERIOD_III, ac.getPeriod()) && ac.getArmyClass() == ArmyClassEnum.IIM)
                .findAny()
                .orElse(null);

        Assert.assertNotNull(IIMIII);
        Assert.assertEquals(3, IIMIII.getArtillery().intValue());
    }

    @Test
    public void testArtillerySiege() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(18, tables.getArtillerySieges().size());

        ArtillerySiege artillerySiege = tables.getArtillerySieges().stream()
                .filter(as -> as.getFortress() == 0 && as.getArtillery() == 1)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(artillerySiege);
        Assert.assertEquals(1, artillerySiege.getBonus().intValue());

        artillerySiege = tables.getArtillerySieges().stream()
                .filter(as -> as.getFortress() == 0 && as.getArtillery() == 3)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(artillerySiege);
        Assert.assertEquals(2, artillerySiege.getBonus().intValue());

        artillerySiege = tables.getArtillerySieges().stream()
                .filter(as -> as.getFortress() == 0 && as.getArtillery() == 5)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(artillerySiege);
        Assert.assertEquals(3, artillerySiege.getBonus().intValue());
    }

    @Test
    public void testFortressResistance() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(12, tables.getFortressResistances().size());

        FortressResistance fortressResistance = tables.getFortressResistances().stream()
                .filter(fr -> fr.getFortress() == 4 && !fr.isBreach())
                .findAny()
                .orElse(null);
        Assert.assertNotNull(fortressResistance);
        Assert.assertEquals(3, fortressResistance.getRound().intValue());
        Assert.assertEquals(0, fortressResistance.getThird().intValue());

        fortressResistance = tables.getFortressResistances().stream()
                .filter(fr -> fr.getFortress() == 5 && !fr.isBreach())
                .findAny()
                .orElse(null);
        Assert.assertNotNull(fortressResistance);
        Assert.assertEquals(3, fortressResistance.getRound().intValue());
        Assert.assertEquals(0, fortressResistance.getThird().intValue());

        fortressResistance = tables.getFortressResistances().stream()
                .filter(fr -> fr.getFortress() == 0 && fr.isBreach())
                .findAny()
                .orElse(null);
        Assert.assertNotNull(fortressResistance);
        Assert.assertEquals(0, fortressResistance.getRound().intValue());
        Assert.assertEquals(1, fortressResistance.getThird().intValue());
    }

    @Test
    public void testAssaultResult() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();

        Assert.assertEquals(60, tables.getAssaultResults().size());

        AssaultResult assaultResult = tables.getAssaultResults().stream()
                .filter(ar -> ar.getDice() == 6 && ar.isFire() && !ar.isBreach() && !ar.isBesieger())
                .findAny()
                .orElse(null);
        Assert.assertNotNull(assaultResult);
        Assert.assertEquals(0, assaultResult.getRoundLoss().intValue());
        Assert.assertEquals(2, assaultResult.getThirdLoss().intValue());
        Assert.assertEquals(1, assaultResult.getMoraleLoss().intValue());

        assaultResult = tables.getAssaultResults().stream()
                .filter(ar -> ar.getDice() == 7 && ar.isFire() && !ar.isBreach() && ar.isBesieger())
                .findAny()
                .orElse(null);
        Assert.assertNotNull(assaultResult);
        Assert.assertEquals(1, assaultResult.getRoundLoss().intValue());
        Assert.assertEquals(0, assaultResult.getThirdLoss().intValue());
        Assert.assertEquals(1, assaultResult.getMoraleLoss().intValue());

        assaultResult = tables.getAssaultResults().stream()
                .filter(ar -> ar.getDice() == 10 && !ar.isFire() && ar.isBreach() && !ar.isBesieger())
                .findAny()
                .orElse(null);
        Assert.assertNotNull(assaultResult);
        Assert.assertEquals(1, assaultResult.getRoundLoss().intValue());
        Assert.assertEquals(0, assaultResult.getThirdLoss().intValue());
        Assert.assertEquals(3, assaultResult.getMoraleLoss().intValue());
    }

    @Test
    public void testExchequer() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();
        List<Exchequer> exchequers = tables.getExchequers();

        Assert.assertEquals(6, exchequers.size());
        Exchequer exchequer = exchequers.stream()
                .filter(exc -> exc.getResult() == ResultEnum.FUMBLE)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(exchequer);
        Assert.assertEquals(30, exchequer.getRegular().intValue());
        Assert.assertEquals(0, exchequer.getPrestige().intValue());
        Assert.assertEquals(40, exchequer.getNatLoan().intValue());
        Assert.assertEquals(20, exchequer.getInterLoan().intValue());

        exchequer = exchequers.stream()
                .filter(exc -> exc.getResult() == ResultEnum.CRITICAL_HIT)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(exchequer);
        Assert.assertEquals(60, exchequer.getRegular().intValue());
        Assert.assertEquals(40, exchequer.getPrestige().intValue());
        Assert.assertEquals(20, exchequer.getNatLoan().intValue());
        Assert.assertEquals(100, exchequer.getInterLoan().intValue());
    }

    @Test
    public void testLeaders() {
        tablesService.refresh();
        Tables tables = tablesService.getTables();
        List<Leader> leaders = tables.getLeaders();

        Assert.assertEquals(620, leaders.size());

        Leader leader = leaders.stream()
                .filter(l -> StringUtils.equals(l.getCode(), "Bonaparte"))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(leader);
        Assert.assertEquals("Bonaparte", leader.getName());
        Assert.assertEquals("revolutionnaires", leader.getCountry());
        Assert.assertEquals("VII-5(2)", leader.getEvent());
        Assert.assertEquals(null, leader.getBegin());
        Assert.assertEquals(null, leader.getEnd());
        Assert.assertEquals("B", leader.getRank());
        Assert.assertEquals(6, leader.getManoeuvre());
        Assert.assertEquals(6, leader.getFire());
        Assert.assertEquals(6, leader.getShock());
        Assert.assertEquals(3, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.GENERAL, leader.getType());
        Assert.assertEquals(false, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(false, leader.isMediterranee());
        Assert.assertEquals(false, leader.isPrivateer());
        Assert.assertEquals(false, leader.isAnonymous());
        Assert.assertEquals(false, leader.isMain());
        Assert.assertEquals(null, leader.getSize());
        Assert.assertNull(leader.getOtherSide());

        leader = leaders.stream()
                .filter(l -> StringUtils.equals(l.getCode(), "Albuquerque"))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(leader);
        Assert.assertEquals("Albuquerque", leader.getName());
        Assert.assertEquals("porviceroy", leader.getCountry());
        Assert.assertEquals(null, leader.getEvent());
        Assert.assertEquals(5, leader.getBegin().intValue());
        Assert.assertEquals(9, leader.getEnd().intValue());
        Assert.assertEquals("B", leader.getRank());
        Assert.assertEquals(5, leader.getManoeuvre());
        Assert.assertEquals(5, leader.getFire());
        Assert.assertEquals(5, leader.getShock());
        Assert.assertEquals(3, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.CONQUISTADOR, leader.getType());
        Assert.assertEquals(true, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(false, leader.isMediterranee());
        Assert.assertEquals(false, leader.isPrivateer());
        Assert.assertEquals(false, leader.isAnonymous());
        Assert.assertEquals(true, leader.isMain());
        Assert.assertEquals(null, leader.getSize());
        Assert.assertNotNull(leader.getOtherSide());
        leader = leader.getOtherSide();
        Assert.assertEquals("Albuquerque", leader.getName());
        Assert.assertEquals("porviceroy", leader.getCountry());
        Assert.assertEquals(null, leader.getEvent());
        Assert.assertEquals(5, leader.getBegin().intValue());
        Assert.assertEquals(9, leader.getEnd().intValue());
        Assert.assertEquals("B", leader.getRank());
        Assert.assertEquals(5, leader.getManoeuvre());
        Assert.assertEquals(5, leader.getFire());
        Assert.assertEquals(5, leader.getShock());
        Assert.assertEquals(0, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.EXPLORER, leader.getType());
        Assert.assertEquals(true, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(false, leader.isMediterranee());
        Assert.assertEquals(true, leader.isPrivateer());
        Assert.assertEquals(false, leader.isAnonymous());
        Assert.assertEquals(false, leader.isMain());
        Assert.assertEquals(null, leader.getSize());
        Assert.assertNotNull(leader.getOtherSide());

        leader = leaders.stream()
                .filter(l -> StringUtils.equals(l.getCode(), "Barbaros2"))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(leader);
        Assert.assertEquals("Barbaros2", leader.getName());
        Assert.assertEquals("algerie", leader.getCountry());
        Assert.assertEquals(null, leader.getEvent());
        Assert.assertEquals(5, leader.getBegin().intValue());
        Assert.assertEquals(11, leader.getEnd().intValue());
        Assert.assertEquals("A", leader.getRank());
        Assert.assertEquals(5, leader.getManoeuvre());
        Assert.assertEquals(4, leader.getFire());
        Assert.assertEquals(5, leader.getShock());
        Assert.assertEquals(0, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.ADMIRAL, leader.getType());
        Assert.assertEquals(false, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(true, leader.isMediterranee());
        Assert.assertEquals(true, leader.isPrivateer());
        Assert.assertEquals(false, leader.isAnonymous());
        Assert.assertEquals(true, leader.isMain());
        Assert.assertEquals(null, leader.getSize());
        Assert.assertNotNull(leader.getOtherSide());
        leader = leader.getOtherSide();
        Assert.assertEquals("Barbaros2", leader.getName());
        Assert.assertEquals("turquie", leader.getCountry());
        Assert.assertEquals(null, leader.getEvent());
        Assert.assertEquals(5, leader.getBegin().intValue());
        Assert.assertEquals(11, leader.getEnd().intValue());
        Assert.assertEquals("A", leader.getRank());
        Assert.assertEquals(5, leader.getManoeuvre());
        Assert.assertEquals(4, leader.getFire());
        Assert.assertEquals(5, leader.getShock());
        Assert.assertEquals(0, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.ADMIRAL, leader.getType());
        Assert.assertEquals(false, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(true, leader.isMediterranee());
        Assert.assertEquals(true, leader.isPrivateer());
        Assert.assertEquals(false, leader.isAnonymous());
        Assert.assertEquals(false, leader.isMain());
        Assert.assertEquals(null, leader.getSize());
        Assert.assertNotNull(leader.getOtherSide());

        leader = leaders.stream()
                .filter(l -> StringUtils.equals(l.getCode(), "timar_Israf"))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(leader);
        Assert.assertEquals("timar_Israf", leader.getName());
        Assert.assertEquals("turquie", leader.getCountry());
        Assert.assertEquals(null, leader.getEvent());
        Assert.assertEquals(null, leader.getBegin());
        Assert.assertEquals(null, leader.getEnd());
        Assert.assertEquals("S", leader.getRank());
        Assert.assertEquals(4, leader.getManoeuvre());
        Assert.assertEquals(4, leader.getFire());
        Assert.assertEquals(4, leader.getShock());
        Assert.assertEquals(0, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.PACHA, leader.getType());
        Assert.assertEquals(false, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(false, leader.isMediterranee());
        Assert.assertEquals(false, leader.isPrivateer());
        Assert.assertEquals(true, leader.isAnonymous());
        Assert.assertEquals(true, leader.isMain());
        Assert.assertEquals(2, leader.getSize().intValue());
        Assert.assertNotNull(leader.getOtherSide());
        leader = leader.getOtherSide();
        Assert.assertEquals("timar_Israf", leader.getName());
        Assert.assertEquals("turquie", leader.getCountry());
        Assert.assertEquals(null, leader.getEvent());
        Assert.assertEquals(null, leader.getBegin());
        Assert.assertEquals(null, leader.getEnd());
        Assert.assertEquals(null, leader.getRank());
        Assert.assertEquals(0, leader.getManoeuvre());
        Assert.assertEquals(0, leader.getFire());
        Assert.assertEquals(0, leader.getShock());
        Assert.assertEquals(0, leader.getSiege());
        Assert.assertEquals(LeaderTypeEnum.PACHA, leader.getType());
        Assert.assertEquals(false, leader.isRotw());
        Assert.assertEquals(false, leader.isAmerica());
        Assert.assertEquals(false, leader.isAsia());
        Assert.assertEquals(false, leader.isMediterranee());
        Assert.assertEquals(false, leader.isPrivateer());
        Assert.assertEquals(true, leader.isAnonymous());
        Assert.assertEquals(false, leader.isMain());
        Assert.assertEquals(null, leader.getSize());
        Assert.assertNotNull(leader.getOtherSide());
    }
}
