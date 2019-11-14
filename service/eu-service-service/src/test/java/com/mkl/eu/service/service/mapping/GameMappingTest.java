package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.service.eco.AdministrativeActionCountry;
import com.mkl.eu.client.service.service.eco.EconomicalSheetCountry;
import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Monarch;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diplo.CountryInWar;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.eco.*;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;
import com.mkl.eu.client.service.vo.military.*;
import com.mkl.eu.client.service.vo.ref.country.CountryLight;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.service.service.mapping.eco.AdministrativeActionMapping;
import com.mkl.eu.service.service.mapping.eco.EconomicalSheetMapping;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.MonarchEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.eco.*;
import com.mkl.eu.service.service.persistence.oe.event.PoliticalEventEntity;
import com.mkl.eu.service.service.persistence.oe.military.*;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test of GameMapping.
 *
 * @author MKL.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/mkl/eu/service/service/mapping/test-eu-mapping-applicationContext..xml"})
public class GameMappingTest {
    private static final PlayableCountry FRA_VO;
    private static final PlayableCountry PRU_VO;
    private static final PlayableCountryEntity FRA_OE;
    private static final PlayableCountryEntity PRU_OE;
    private static final String PECS = "Pecs";
    private static final String TYR = "Tyr";
    private static final String IDF = "IdF";
    @Autowired
    private GameMapping gameMapping;
    @Autowired
    private EconomicalSheetMapping economicalSheetMapping;
    @Autowired
    private AdministrativeActionMapping administrativeActionMapping;

    static {
        FRA_VO = new PlayableCountry();
        FRA_VO.setId(1L);
        FRA_VO.setName("FRA");
        FRA_VO.setUsername("MKL");
        FRA_VO.setDti(3);
        FRA_VO.setColonisationPenalty(3);
        FRA_VO.setReady(true);

        PRU_VO = new PlayableCountry();
        PRU_VO.setId(2L);
        PRU_VO.setName("PRU");
        PRU_VO.setUsername("Fogia");
        PRU_VO.setFti(4);
        PRU_VO.setFtiRotw(4);
        PRU_VO.setReady(false);

        FRA_OE = new PlayableCountryEntity();
        FRA_OE.setId(1L);
        FRA_OE.setName("FRA");
        FRA_OE.setUsername("MKL");
        FRA_OE.setDti(3);
        FRA_OE.setColonisationPenalty(3);
        FRA_OE.setReady(true);

        PRU_OE = new PlayableCountryEntity();
        PRU_OE.setId(2L);
        PRU_OE.setName("PRU");
        PRU_OE.setUsername("Fogia");
        PRU_OE.setFti(4);
        PRU_OE.setFtiRotw(4);
        PRU_OE.setReady(false);
    }

    @Test
    public void testVoidGameMapping() {
        Game vo = gameMapping.oeToVo(null, null);

        Assert.assertNull(vo);

        GameEntity entity = new GameEntity();

        vo = gameMapping.oeToVo(entity, null);

        ReflectionAssert.assertReflectionEquals(new Game(), vo);
    }

    @Test
    public void testFullGameMapping() {
        GameEntity entity = createGameEntity();

        Game vo = gameMapping.oeToVo(entity, null);

        // Sets are not sorted and we do not care how it is sorted, except in this test case. So we sort it.
        Collections.sort(vo.getBattles().get(0).getCounters(), (o1, o2) -> o1.getCounter().getId().compareTo(o2.getCounter().getId()));
        Collections.sort(vo.getSieges().get(0).getCounters(), (o1, o2) -> o1.getCounter().getId().compareTo(o2.getCounter().getId()));

        ReflectionAssert.assertReflectionEquals(createGameVo(), vo);

        GameLight voLight = gameMapping.oeToVoLight(entity);

        ReflectionAssert.assertReflectionEquals(createGameVoLight(), voLight);
    }

    @Test
    public void testEconomicalSheetMapping() {
        List<EconomicalSheetCountry> vos = economicalSheetMapping.oesToVosCountry(createEconomicalSheetEntities());

        List<EconomicalSheet> expected = createEconomicalSheetVos();

        Assert.assertEquals(vos.size(), expected.size());
        for (int i = 0; i < vos.size() && i < expected.size(); i++) {
            ReflectionAssert.assertReflectionEquals(vos.get(i).getSheet(), expected.get(i));
            Assert.assertEquals(vos.get(i).getIdCountry(), FRA_OE.getId());
        }
    }

    @Test
    public void testAdministrativeActionsMapping() {
        List<AdministrativeActionCountry> vos = administrativeActionMapping.oesToVosCountry(createAdministrativeActionsEntities());

        List<AdministrativeAction> expected = createAdministrativeActionsVos();

        Assert.assertEquals(vos.size(), expected.size());
        for (int i = 0; i < vos.size() && i < expected.size(); i++) {
            ReflectionAssert.assertReflectionEquals(vos.get(i).getAction(), expected.get(i));
            Assert.assertEquals(vos.get(i).getIdCountry(), FRA_OE.getId());
        }
    }

    private Game createGameVo() {
        Game object = new Game();

        object.setId(12L);
        object.setStatus(GameStatusEnum.ECONOMICAL_EVENT);
        object.setTurn(1);
        object.setVersion(15L);

        object.getCountries().add(FRA_VO);
        object.getCountries().add(PRU_VO);

        FRA_VO.setEconomicalSheets(createEconomicalSheetVos());
        FRA_VO.setAdministrativeActions(createAdministrativeActionsVos());
        FRA_VO.setMonarchs(createMonarchsVos());
        FRA_VO.setMonarch(createMonarchsVos().get(0));

        object.setEvents(createEventsVos());

        object.setWars(createWarsVos());

        object.setStacks(createStacksVos());

        object.setTradeFleets(createTradeFleetsVos());

        object.setCompetitions(createCompetitionsVos());

        object.setOrders(createCountryOrdersVos());

        WarLight war = new WarLight();
        war.setId(object.getWars().get(0).getId());
        war.setName(object.getWars().get(0).getName());
        war.setType(object.getWars().get(0).getType());
        object.setBattles(createBattlesVos(war));

        object.setSieges(createSiegesVos(war));

        return object;
    }

    private List<EconomicalSheet> createEconomicalSheetVos() {
        List<EconomicalSheet> objects = new ArrayList<>();

        EconomicalSheet object = new EconomicalSheet();
        object.setId(1L);
        object.setTurn(1);
        objects.add(object);

        object = new EconomicalSheet();
        object.setId(2L);
        object.setTurn(2);
        object.setActCampExpense(3);
        object.setWoodSlaves(4);
        object.setWealth(5);
        object.setVassalIncome(6);
        object.setUnitPurchExpense(7);
        object.setUnitMaintExpense(8);
        object.setTradeIncome(9);
        object.setTradeCenterLoss(10);
        object.setTradeCenterIncome(11);
        object.setTpIncome(12);
        object.setSubsidies(13);
        object.setStab(14);
        object.setSpecialIncome(15);
        object.setRtStart(16);
        object.setRtPeace(17);
        object.setRtEvents(18);
        object.setRtEnd(19);
        object.setActCampExpense(20);
        object.setAdminActExpense(21);
        object.setAdminReactExpense(22);
        object.setAdmTotalExpense(23);
        object.setColIncome(24);
        object.setDiploActions(25);
        object.setDiploReactions(26);
        object.setDomTradeIncome(27);
        object.setEventIncome(28);
        object.setExcRecruitExpense(29);
        object.setExcTaxes(30);
        object.setExcTaxesMod(31);
        object.setExoResIncome(32);
        object.setExpenses(33);
        object.setFleetLevelIncome(34);
        object.setFleetMonopIncome(35);
        object.setFortMaintExpense(36);
        object.setFortPurchExpense(37);
        object.setForTradeIncome(38);
        object.setGoldIncome(39);
        object.setGoldRotw(40);
        object.setGrossIncome(41);
        object.setIncome(42);
        object.setIndustrialIncome(43);
        object.setInflation(44);
        object.setInterBankrupt(45);
        object.setInterestExpense(46);
        object.setInterLoan(47);
        object.setInterLoanInterests(48);
        object.setInterLoanNew(49);
        object.setInterLoanRefund(50);
        object.setLandIncome(51);
        object.setLoans(52);
        object.setLostIncome(53);
        object.setMajCampExpense(54);
        object.setMandRefundExpense(55);
        object.setMaxInterLoan(56);
        object.setMaxNatLoan(57);
        object.setMilitaryExpense(58);
        object.setMissMaintExpense(59);
        object.setMnuIncome(60);
        object.setMultCampExpense(61);
        object.setNatLoan(62);
        object.setNatLoanBankrupt(63);
        object.setNatLoanEnd(64);
        object.setNatLoanInterest(65);
        object.setNatLoanNew(66);
        object.setNatLoanRefund(67);
        object.setNatLoanStart(68);
        object.setNavalRefitExpense(69);
        object.setOptRefundExpense(70);
        object.setOtherExpense(71);
        object.setPassCampExpense(72);
        object.setPeace(73);
        object.setPeriodWealth(74);
        object.setPillages(75);
        object.setPraesidioExpense(76);
        object.setPrestigeIncome(77);
        object.setPrestigeSpent(78);
        object.setPrestigeVP(79);
        object.setProvincesIncome(80);
        object.setRegularIncome(81);
        object.setRemainingExpenses(82);
        object.setRotwIncome(83);
        object.setRtAftExch(84);
        object.setRtBalance(85);
        object.setRtBefExch(86);
        object.setRtCollapse(87);
        object.setRtDiplo(88);
        object.setExchequerColumn(666);
        object.setExchequerBonus(667);
        object.setExchequerDie(668);
        object.setStabModifier(-2);
        object.setStabDie(1);
        objects.add(object);

        return objects;
    }

    private List<AdministrativeAction> createAdministrativeActionsVos() {
        List<AdministrativeAction> objects = new ArrayList<>();

        AdministrativeAction object = new AdministrativeAction();
        object.setId(1L);
        object.setTurn(2);
        object.setProvince("province");
        object.setBonus(6);
        object.setColumn(-2);
        object.setCost(30);
        object.setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);
        object.setDie(9);
        object.setIdObject(12L);
        object.setResult(ResultEnum.AVERAGE_PLUS);
        object.setSecondaryDie(6);
        object.setSecondaryResult(false);
        object.setStatus(AdminActionStatusEnum.DONE);
        object.setType(AdminActionTypeEnum.COL);
        objects.add(object);

        return objects;
    }

    private List<PoliticalEvent> createEventsVos() {
        List<PoliticalEvent> objects = new ArrayList<>();

        PoliticalEvent object = new PoliticalEvent();
        object.setId(1L);
        object.setTurn(1);
        objects.add(object);
        object = new PoliticalEvent();
        object.setId(2L);
        object.setTurn(3);
        objects.add(object);
        object = new PoliticalEvent();
        object.setId(3L);
        object.setTurn(5);
        objects.add(object);

        return objects;
    }

    private List<War> createWarsVos() {
        List<War> objects = new ArrayList<>();

        War object = new War();
        object.setId(1L);
        object.setName("france vs persia");
        object.setType(WarTypeEnum.CLASSIC_WAR);
        CountryInWar country = new CountryInWar();
        country.setOffensive(true);
        country.setImplication(WarImplicationEnum.FULL);
        country.setCountry(new CountryLight());
        country.getCountry().setId(101L);
        country.getCountry().setName("france");
        country.getCountry().setArmyClass(ArmyClassEnum.IV);
        country.getCountry().setCulture(CultureEnum.LATIN);
        country.getCountry().setReligion(ReligionEnum.CATHOLIC);
        country.getCountry().setType(CountryTypeEnum.MAJOR);
        object.getCountries().add(country);
        country = new CountryInWar();
        country.setOffensive(false);
        country.setImplication(WarImplicationEnum.LIMITED);
        country.setCountry(new CountryLight());
        country.getCountry().setId(102L);
        country.getCountry().setName("persia");
        country.getCountry().setArmyClass(ArmyClassEnum.IIM);
        country.getCountry().setCulture(CultureEnum.ISLAM);
        country.getCountry().setReligion(ReligionEnum.SHIITE);
        country.getCountry().setType(CountryTypeEnum.MINOR);
        object.getCountries().add(country);
        objects.add(object);
        object = new War();
        object.setId(2L);
        object.setName("N/A");
        object.setType(WarTypeEnum.CIVIL_WAR);
        objects.add(object);

        return objects;
    }

    private List<Stack> createStacksVos() {
        List<Stack> objects = new ArrayList<>();

        Stack object = new Stack();
        object.setId(1L);
        object.setProvince(PECS);
        object.setMovePhase(MovePhaseEnum.IS_MOVING);
        object.setCountry("FRA");
        object.setMove(6);
        List<Counter> subObjects = new ArrayList<>();
        Counter subObject = new Counter();
        subObject.setId(1L);
        subObject.setType(CounterFaceTypeEnum.ARMY_MINUS);
        subObject.setCountry("FRA");
        subObject.setVeterans(2d);
        subObject.setOwner(object);
        subObjects.add(subObject);
        subObject = new Counter();
        subObject.setId(2L);
        subObject.setType(CounterFaceTypeEnum.ARMY_PLUS);
        subObject.setCountry("PRU");
        subObject.setVeterans(0d);
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new Stack();
        object.setId(2L);
        object.setProvince(TYR);
        object.setMovePhase(MovePhaseEnum.MOVED);
        object.setBesieged(true);
        object.setCountry("FRA");
        object.setMove(8);
        subObjects = new ArrayList<>();
        subObject = new Counter();
        subObject.setId(3L);
        subObject.setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        subObject.setCountry("FRA");
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new Stack();
        object.setId(3L);
        object.setProvince(IDF);
        object.setMovePhase(MovePhaseEnum.NOT_MOVED);
        object.setBesieged(false);
        objects.add(object);

        return objects;
    }

    private List<TradeFleet> createTradeFleetsVos() {
        List<TradeFleet> objects = new ArrayList<>();

        TradeFleet object = new TradeFleet();
        object.setId(1L);
        object.setCountry("france");
        object.setProvince("ZPFrance");
        object.setLevel(4);
        objects.add(object);
        object = new TradeFleet();
        objects.add(object);
        object.setId(2L);
        object.setCountry("angleterre");
        object.setProvince("ZPFrance");

        return objects;
    }

    private List<Monarch> createMonarchsVos() {
        List<Monarch> objects = new ArrayList<>();

        Monarch object = new Monarch();
        object.setId(1L);
        object.setBegin(1);
        object.setEnd(12);
        object.setAdministrative(9);
        object.setDiplomacy(9);
        object.setMilitary(9);
        object.setMilitaryAverage(5);
        objects.add(object);

        object = new Monarch();
        object.setId(2L);
        object.setBegin(13);
        object.setEnd(63);
        object.setAdministrative(3);
        object.setDiplomacy(3);
        object.setMilitary(3);
        object.setMilitaryAverage(0);
        objects.add(object);

        return objects;
    }

    private List<Competition> createCompetitionsVos() {
        List<Competition> objects = new ArrayList<>();

        Competition object = new Competition();
        object.setId(1L);
        object.setTurn(3);
        object.setType(CompetitionTypeEnum.TF_4);
        object.setProvince("ZPFrance");
        object.getRounds().add(new CompetitionRound());
        object.getRounds().get(0).setId(1L);
        object.getRounds().get(0).setRound(1);
        object.getRounds().get(0).setCountry("france");
        object.getRounds().get(0).setColumn(1);
        object.getRounds().get(0).setDie(6);
        object.getRounds().get(0).setResult(ResultEnum.AVERAGE_PLUS);
        object.getRounds().get(0).setSecondaryDie(5);
        object.getRounds().get(0).setSecondaryResult(false);
        objects.add(object);

        object = new Competition();
        object.setId(2L);
        object.setTurn(5);
        object.setType(CompetitionTypeEnum.ESTABLISHMENT);
        object.setProvince("rGuyana~E");
        object.getRounds().add(new CompetitionRound());
        object.getRounds().add(new CompetitionRound());
        object.getRounds().get(1).setId(3L);
        object.getRounds().get(1).setRound(2);
        object.getRounds().get(1).setCountry("espagne");
        object.getRounds().get(1).setColumn(-1);
        object.getRounds().get(1).setDie(9);
        object.getRounds().get(1).setResult(ResultEnum.CRITICAL_HIT);
        objects.add(object);

        return objects;
    }

    private List<CountryOrder> createCountryOrdersVos() {
        List<CountryOrder> objects = new ArrayList<>();

        CountryOrder object = new CountryOrder();
        object.setCountry(FRA_VO);
        object.setPosition(0);
        object.setActive(true);
        object.setReady(true);
        objects.add(object);

        object = new CountryOrder();
        object.setCountry(PRU_VO);
        object.setPosition(0);
        object.setActive(false);
        object.setReady(false);
        objects.add(object);

        return objects;
    }

    private GameLight createGameVoLight() {
        GameLight game = new GameLight();

        game.setId(12L);
        game.setStatus(GameStatusEnum.ECONOMICAL_EVENT);
        game.setTurn(1);

        return game;
    }

    private List<Battle> createBattlesVos(WarLight war) {
        List<Battle> objects = new ArrayList<>();

        Battle object = new Battle();
        object.setId(1L);
        object.setWinner(BattleWinnerEnum.NONE);
        object.setStatus(BattleStatusEnum.DONE);
        object.setPhasingOffensive(true);
        object.setProvince("idf");
        object.setWar(war);
        object.setEnd(BattleEndEnum.END_OF_SECOND_DAY);
        object.setTurn(12);
        BattleCounter counter = new BattleCounter();
        counter.setPhasing(true);
        counter.setCounter(new Counter());
        counter.getCounter().setId(101L);
        counter.getCounter().setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter.getCounter().setCountry("france");
        object.getCounters().add(counter);
        counter = new BattleCounter();
        counter.setPhasing(false);
        counter.setCounter(new Counter());
        counter.getCounter().setId(102L);
        counter.getCounter().setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.getCounter().setCountry("persia");
        object.getCounters().add(counter);
        object.getPhasing().setSize(2d);
        object.getPhasing().setTech(Tech.ARQUEBUS);
        object.getPhasing().setSizeDiff(-2);
        object.getPhasing().setMoral(3);
        object.getPhasing().setFireColumn("C");
        object.getPhasing().setShockColumn("B");
        object.getPhasing().setForces(true);
        object.getPhasing().setPursuit(5);
        object.getPhasing().setPursuitMod(3);
        object.getPhasing().setLossesSelected(true);
        object.getPhasing().setRetreatSelected(true);
        object.getPhasing().getLosses().setRoundLoss(1);
        object.getPhasing().getLosses().setThirdLoss(2);
        object.getPhasing().getLosses().setRoundLoss(2);
        object.getPhasing().getFirstDay().setFireMod(1);
        object.getPhasing().getFirstDay().setFire(5);
        object.getPhasing().getFirstDay().setShockMod(2);
        object.getPhasing().getFirstDay().setShock(9);
        object.getNonPhasing().setSize(2d);
        object.getNonPhasing().setTech(Tech.MEDIEVAL);
        object.getNonPhasing().setSizeDiff(2);
        object.getNonPhasing().setMoral(2);
        object.getNonPhasing().setFireColumn("D");
        object.getNonPhasing().setShockColumn("D");
        object.getNonPhasing().setPursuitMod(1);
        object.getNonPhasing().setRetreat(4);
        objects.add(object);
        object = new Battle();
        object.setId(2L);
        objects.add(object);

        return objects;
    }

    private List<Siege> createSiegesVos(WarLight war) {
        List<Siege> objects = new ArrayList<>();

        Siege object = new Siege();
        object.setId(1L);
        object.setFortressFalls(true);
        object.setStatus(SiegeStatusEnum.DONE);
        object.setBesiegingOffensive(true);
        object.setProvince("idf");
        object.setWar(war);
        object.setTurn(12);
        object.setFortressLevel(2);
        object.setBreach(true);
        object.setBonus(5);
        object.setUndermineDie(2);
        object.setUndermineResult(SiegeUndermineResultEnum.SIEGE_WORK_PLUS);
        SiegeCounter counter = new SiegeCounter();
        counter.setPhasing(true);
        counter.setCounter(new Counter());
        counter.getCounter().setId(101L);
        counter.getCounter().setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter.getCounter().setCountry("france");
        object.getCounters().add(counter);
        counter = new SiegeCounter();
        counter.setPhasing(false);
        counter.setCounter(new Counter());
        counter.getCounter().setId(102L);
        counter.getCounter().setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.getCounter().setCountry("persia");
        object.getCounters().add(counter);
        object.setPhasing(new SiegeSide());
        object.getPhasing().setSize(2d);
        object.getPhasing().setTech(Tech.ARQUEBUS);
        object.getPhasing().setMoral(3);
        object.getPhasing().setLossesSelected(true);
        object.getPhasing().setLosses(new AbstractWithLoss());
        object.getPhasing().getLosses().setRoundLoss(1);
        object.getPhasing().getLosses().setThirdLoss(2);
        object.getPhasing().getLosses().setRoundLoss(2);
        object.getPhasing().setModifiers(new BattleDay());
        object.getPhasing().getModifiers().setFireMod(1);
        object.getPhasing().getModifiers().setFire(5);
        object.getPhasing().getModifiers().setShockMod(2);
        object.getPhasing().getModifiers().setShock(9);
        object.setNonPhasing(new SiegeSide());
        object.getNonPhasing().setSize(2d);
        object.getNonPhasing().setTech(Tech.MEDIEVAL);
        object.getNonPhasing().setMoral(2);
        object.getNonPhasing().setLosses(new AbstractWithLoss());
        object.getNonPhasing().setModifiers(new BattleDay());
        objects.add(object);
        object = new Siege();
        object.setId(2L);
        object.setPhasing(new SiegeSide());
        object.getPhasing().setLosses(new AbstractWithLoss());
        object.getPhasing().setModifiers(new BattleDay());
        object.setNonPhasing(new SiegeSide());
        object.getNonPhasing().setLosses(new AbstractWithLoss());
        object.getNonPhasing().setModifiers(new BattleDay());
        objects.add(object);

        return objects;
    }

    private GameEntity createGameEntity() {
        GameEntity object = new GameEntity();

        object.setId(12L);
        object.setStatus(GameStatusEnum.ECONOMICAL_EVENT);
        object.setTurn(1);
        object.setVersion(15L);

        object.getCountries().add(FRA_OE);
        object.getCountries().add(PRU_OE);

        FRA_OE.setEconomicalSheets(createEconomicalSheetEntities());
        FRA_OE.setAdministrativeActions(createAdministrativeActionsEntities());
        FRA_OE.setMonarchs(createMonarchsEntities());
        FRA_OE.setMonarch(createMonarchsEntities().get(0));

        object.setEvents(createEventsEntities());

        object.setWars(createWarsEntities());

        object.setStacks(createStacksEntities());

        object.setTradeFleets(createTradeFleetsEntities());

        object.setCompetitions(createCompetitionsEntities());

        object.setOrders(createCountryOrdersEntities());

        object.setBattles(createBattlesEntities(object.getWars().get(0)));

        object.setSieges(createSiegesEntities(object.getWars().get(0)));

        return object;
    }

    private List<EconomicalSheetEntity> createEconomicalSheetEntities() {
        List<EconomicalSheetEntity> objects = new ArrayList<>();

        EconomicalSheetEntity object = new EconomicalSheetEntity();
        object.setId(1L);
        object.setCountry(FRA_OE);
        object.setTurn(1);
        objects.add(object);

        object = new EconomicalSheetEntity();
        object.setId(2L);
        object.setCountry(FRA_OE);
        object.setTurn(2);
        object.setActCampExpense(3);
        object.setWoodSlaves(4);
        object.setWealth(5);
        object.setVassalIncome(6);
        object.setUnitPurchExpense(7);
        object.setUnitMaintExpense(8);
        object.setTradeIncome(9);
        object.setTradeCenterLoss(10);
        object.setTradeCenterIncome(11);
        object.setTpIncome(12);
        object.setSubsidies(13);
        object.setStab(14);
        object.setSpecialIncome(15);
        object.setRtStart(16);
        object.setRtPeace(17);
        object.setRtEvents(18);
        object.setRtEnd(19);
        object.setActCampExpense(20);
        object.setAdminActExpense(21);
        object.setAdminReactExpense(22);
        object.setAdmTotalExpense(23);
        object.setColIncome(24);
        object.setDiploActions(25);
        object.setDiploReactions(26);
        object.setDomTradeIncome(27);
        object.setEventIncome(28);
        object.setExcRecruitExpense(29);
        object.setExcTaxes(30);
        object.setExcTaxesMod(31);
        object.setExoResIncome(32);
        object.setExpenses(33);
        object.setFleetLevelIncome(34);
        object.setFleetMonopIncome(35);
        object.setFortMaintExpense(36);
        object.setFortPurchExpense(37);
        object.setForTradeIncome(38);
        object.setGoldIncome(39);
        object.setGoldRotw(40);
        object.setGrossIncome(41);
        object.setIncome(42);
        object.setIndustrialIncome(43);
        object.setInflation(44);
        object.setInterBankrupt(45);
        object.setInterestExpense(46);
        object.setInterLoan(47);
        object.setInterLoanInterests(48);
        object.setInterLoanNew(49);
        object.setInterLoanRefund(50);
        object.setLandIncome(51);
        object.setLoans(52);
        object.setLostIncome(53);
        object.setMajCampExpense(54);
        object.setMandRefundExpense(55);
        object.setMaxInterLoan(56);
        object.setMaxNatLoan(57);
        object.setMilitaryExpense(58);
        object.setMissMaintExpense(59);
        object.setMnuIncome(60);
        object.setMultCampExpense(61);
        object.setNatLoan(62);
        object.setNatLoanBankrupt(63);
        object.setNatLoanEnd(64);
        object.setNatLoanInterest(65);
        object.setNatLoanNew(66);
        object.setNatLoanRefund(67);
        object.setNatLoanStart(68);
        object.setNavalRefitExpense(69);
        object.setOptRefundExpense(70);
        object.setOtherExpense(71);
        object.setPassCampExpense(72);
        object.setPeace(73);
        object.setPeriodWealth(74);
        object.setPillages(75);
        object.setPraesidioExpense(76);
        object.setPrestigeIncome(77);
        object.setPrestigeSpent(78);
        object.setPrestigeVP(79);
        object.setProvincesIncome(80);
        object.setRegularIncome(81);
        object.setRemainingExpenses(82);
        object.setRotwIncome(83);
        object.setRtAftExch(84);
        object.setRtBalance(85);
        object.setRtBefExch(86);
        object.setRtCollapse(87);
        object.setRtDiplo(88);
        object.setExchequerColumn(666);
        object.setExchequerBonus(667);
        object.setExchequerDie(668);
        object.setStabModifier(-2);
        object.setStabDie(1);
        objects.add(object);

        return objects;
    }

    private List<AdministrativeActionEntity> createAdministrativeActionsEntities() {
        List<AdministrativeActionEntity> objects = new ArrayList<>();

        AdministrativeActionEntity object = new AdministrativeActionEntity();
        object.setId(1L);
        object.setCountry(FRA_OE);
        object.setTurn(2);
        object.setProvince("province");
        object.setBonus(6);
        object.setColumn(-2);
        object.setCost(30);
        object.setCounterFaceType(CounterFaceTypeEnum.ARMY_MINUS);
        object.setDie(9);
        object.setIdObject(12L);
        object.setResult(ResultEnum.AVERAGE_PLUS);
        object.setSecondaryDie(6);
        object.setSecondaryResult(false);
        object.setStatus(AdminActionStatusEnum.DONE);
        object.setType(AdminActionTypeEnum.COL);
        objects.add(object);

        return objects;
    }

    private List<PoliticalEventEntity> createEventsEntities() {
        List<PoliticalEventEntity> objects = new ArrayList<>();

        PoliticalEventEntity object = new PoliticalEventEntity();
        object.setId(1L);
        object.setTurn(1);
        objects.add(object);
        object = new PoliticalEventEntity();
        object.setId(2L);
        object.setTurn(3);
        objects.add(object);
        object = new PoliticalEventEntity();
        object.setId(3L);
        object.setTurn(5);
        objects.add(object);

        return objects;
    }

    private List<WarEntity> createWarsEntities() {
        List<WarEntity> objects = new ArrayList<>();

        WarEntity object = new WarEntity();
        object.setId(1L);
        object.setName("france vs persia");
        object.setType(WarTypeEnum.CLASSIC_WAR);
        CountryInWarEntity country = new CountryInWarEntity();
        country.setOffensive(true);
        country.setImplication(WarImplicationEnum.FULL);
        country.setCountry(new CountryEntity());
        country.getCountry().setId(101L);
        country.getCountry().setName("france");
        country.getCountry().setArmyClass(ArmyClassEnum.IV);
        country.getCountry().setCulture(CultureEnum.LATIN);
        country.getCountry().setReligion(ReligionEnum.CATHOLIC);
        country.getCountry().setType(CountryTypeEnum.MAJOR);
        object.getCountries().add(country);
        country = new CountryInWarEntity();
        country.setOffensive(false);
        country.setImplication(WarImplicationEnum.LIMITED);
        country.setCountry(new CountryEntity());
        country.getCountry().setId(102L);
        country.getCountry().setName("persia");
        country.getCountry().setArmyClass(ArmyClassEnum.IIM);
        country.getCountry().setCulture(CultureEnum.ISLAM);
        country.getCountry().setReligion(ReligionEnum.SHIITE);
        country.getCountry().setType(CountryTypeEnum.MINOR);
        object.getCountries().add(country);
        objects.add(object);
        object = new WarEntity();
        object.setId(2L);
        object.setName("N/A");
        object.setType(WarTypeEnum.CIVIL_WAR);
        objects.add(object);

        return objects;
    }

    private List<StackEntity> createStacksEntities() {
        List<StackEntity> objects = new ArrayList<>();

        StackEntity object = new StackEntity();
        object.setId(1L);
        object.setProvince(PECS);
        object.setMovePhase(MovePhaseEnum.IS_MOVING);
        object.setCountry("FRA");
        object.setMove(6);
        List<CounterEntity> subObjects = new ArrayList<>();
        CounterEntity subObject = new CounterEntity();
        subObject.setId(1L);
        subObject.setType(CounterFaceTypeEnum.ARMY_MINUS);
        subObject.setCountry("FRA");
        subObject.setVeterans(2d);
        subObject.setOwner(object);
        subObjects.add(subObject);
        subObject = new CounterEntity();
        subObject.setId(2L);
        subObject.setType(CounterFaceTypeEnum.ARMY_PLUS);
        subObject.setCountry("PRU");
        subObject.setVeterans(0d);
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new StackEntity();
        object.setId(2L);
        object.setProvince(TYR);
        object.setMovePhase(MovePhaseEnum.MOVED);
        object.setBesieged(true);
        object.setCountry("FRA");
        object.setMove(8);
        subObjects = new ArrayList<>();
        subObject = new CounterEntity();
        subObject.setId(3L);
        subObject.setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        subObject.setCountry("FRA");
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new StackEntity();
        object.setId(3L);
        object.setProvince(IDF);
        object.setMovePhase(MovePhaseEnum.NOT_MOVED);
        object.setBesieged(false);
        objects.add(object);

        return objects;
    }

    private List<TradeFleetEntity> createTradeFleetsEntities() {
        List<TradeFleetEntity> objects = new ArrayList<>();

        TradeFleetEntity object = new TradeFleetEntity();
        object.setId(1L);
        object.setCountry("france");
        object.setProvince("ZPFrance");
        object.setLevel(4);
        objects.add(object);
        object = new TradeFleetEntity();
        objects.add(object);
        object.setId(2L);
        object.setCountry("angleterre");
        object.setProvince("ZPFrance");

        return objects;
    }

    private List<MonarchEntity> createMonarchsEntities() {
        List<MonarchEntity> objects = new ArrayList<>();

        MonarchEntity object = new MonarchEntity();
        object.setId(1L);
        object.setBegin(1);
        object.setEnd(12);
        object.setAdministrative(9);
        object.setDiplomacy(9);
        object.setMilitary(9);
        object.setMilitaryAverage(5);
        objects.add(object);

        object = new MonarchEntity();
        object.setId(2L);
        object.setBegin(13);
        object.setEnd(63);
        object.setAdministrative(3);
        object.setDiplomacy(3);
        object.setMilitary(3);
        object.setMilitaryAverage(0);
        objects.add(object);

        return objects;
    }

    private List<CompetitionEntity> createCompetitionsEntities() {
        List<CompetitionEntity> objects = new ArrayList<>();

        CompetitionEntity object = new CompetitionEntity();
        object.setId(1L);
        object.setTurn(3);
        object.setType(CompetitionTypeEnum.TF_4);
        object.setProvince("ZPFrance");
        object.getRounds().add(new CompetitionRoundEntity());
        object.getRounds().get(0).setId(1L);
        object.getRounds().get(0).setRound(1);
        object.getRounds().get(0).setCountry("france");
        object.getRounds().get(0).setColumn(1);
        object.getRounds().get(0).setDie(6);
        object.getRounds().get(0).setResult(ResultEnum.AVERAGE_PLUS);
        object.getRounds().get(0).setSecondaryDie(5);
        object.getRounds().get(0).setSecondaryResult(false);
        objects.add(object);

        object = new CompetitionEntity();
        object.setId(2L);
        object.setTurn(5);
        object.setType(CompetitionTypeEnum.ESTABLISHMENT);
        object.setProvince("rGuyana~E");
        object.getRounds().add(new CompetitionRoundEntity());
        object.getRounds().add(new CompetitionRoundEntity());
        object.getRounds().get(1).setId(3L);
        object.getRounds().get(1).setRound(2);
        object.getRounds().get(1).setCountry("espagne");
        object.getRounds().get(1).setColumn(-1);
        object.getRounds().get(1).setDie(9);
        object.getRounds().get(1).setResult(ResultEnum.CRITICAL_HIT);
        objects.add(object);

        return objects;
    }

    private List<CountryOrderEntity> createCountryOrdersEntities() {
        List<CountryOrderEntity> objects = new ArrayList<>();

        CountryOrderEntity object = new CountryOrderEntity();
        object.setCountry(FRA_OE);
        object.setPosition(0);
        object.setActive(true);
        object.setReady(true);
        objects.add(object);

        object = new CountryOrderEntity();
        object.setCountry(PRU_OE);
        object.setPosition(0);
        object.setActive(false);
        object.setReady(false);
        objects.add(object);

        return objects;
    }

    private List<BattleEntity> createBattlesEntities(WarEntity war) {
        List<BattleEntity> objects = new ArrayList<>();

        BattleEntity object = new BattleEntity();
        object.setId(1L);
        object.setWinner(BattleWinnerEnum.NONE);
        object.setStatus(BattleStatusEnum.DONE);
        object.setPhasingOffensive(true);
        object.setProvince("idf");
        object.setWar(war);
        object.setEnd(BattleEndEnum.END_OF_SECOND_DAY);
        object.setTurn(12);
        BattleCounterEntity counter = new BattleCounterEntity();
        counter.setPhasing(true);
        counter.setCounter(101L);
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter.setCountry("france");
        object.getCounters().add(counter);
        counter = new BattleCounterEntity();
        counter.setPhasing(false);
        counter.setCounter(102L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.setCountry("persia");
        object.getCounters().add(counter);
        object.getPhasing().setSize(2d);
        object.getPhasing().setTech(Tech.ARQUEBUS);
        object.getPhasing().setSizeDiff(-2);
        object.getPhasing().setMoral(3);
        object.getPhasing().setFireColumn("C");
        object.getPhasing().setShockColumn("B");
        object.getPhasing().setForces(true);
        object.getPhasing().setPursuit(5);
        object.getPhasing().setPursuitMod(3);
        object.getPhasing().setLossesSelected(true);
        object.getPhasing().setRetreatSelected(true);
        object.getPhasing().getLosses().setRoundLoss(1);
        object.getPhasing().getLosses().setThirdLoss(2);
        object.getPhasing().getLosses().setRoundLoss(2);
        object.getPhasing().getFirstDay().setFireMod(1);
        object.getPhasing().getFirstDay().setFire(5);
        object.getPhasing().getFirstDay().setShockMod(2);
        object.getPhasing().getFirstDay().setShock(9);
        object.getNonPhasing().setSize(2d);
        object.getNonPhasing().setTech(Tech.MEDIEVAL);
        object.getNonPhasing().setSizeDiff(2);
        object.getNonPhasing().setMoral(2);
        object.getNonPhasing().setFireColumn("D");
        object.getNonPhasing().setShockColumn("D");
        object.getNonPhasing().setPursuitMod(1);
        object.getNonPhasing().setRetreat(4);
        objects.add(object);
        object = new BattleEntity();
        object.setId(2L);
        objects.add(object);

        return objects;
    }

    private List<SiegeEntity> createSiegesEntities(WarEntity war) {
        List<SiegeEntity> objects = new ArrayList<>();

        SiegeEntity object = new SiegeEntity();
        object.setId(1L);
        object.setFortressFalls(true);
        object.setStatus(SiegeStatusEnum.DONE);
        object.setBesiegingOffensive(true);
        object.setProvince("idf");
        object.setWar(war);
        object.setTurn(12);
        object.setFortressLevel(2);
        object.setBreach(true);
        object.setBonus(5);
        object.setUndermineDie(2);
        object.setUndermineResult(SiegeUndermineResultEnum.SIEGE_WORK_PLUS);
        SiegeCounterEntity counter = new SiegeCounterEntity();
        counter.setPhasing(true);
        counter.setCounter(101L);
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter.setCountry("france");
        object.getCounters().add(counter);
        counter = new SiegeCounterEntity();
        counter.setPhasing(false);
        counter.setCounter(102L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.setCountry("persia");
        object.getCounters().add(counter);
        object.setPhasing(new SiegeSideEntity());
        object.getPhasing().setSize(2d);
        object.getPhasing().setTech(Tech.ARQUEBUS);
        object.getPhasing().setMoral(3);
        object.getPhasing().setLossesSelected(true);
        object.getPhasing().setLosses(new BattleLossesEntity());
        object.getPhasing().getLosses().setRoundLoss(1);
        object.getPhasing().getLosses().setThirdLoss(2);
        object.getPhasing().getLosses().setRoundLoss(2);
        object.getPhasing().setModifiers(new BattleDayEntity());
        object.getPhasing().getModifiers().setFireMod(1);
        object.getPhasing().getModifiers().setFire(5);
        object.getPhasing().getModifiers().setShockMod(2);
        object.getPhasing().getModifiers().setShock(9);
        object.setNonPhasing(new SiegeSideEntity());
        object.getNonPhasing().setSize(2d);
        object.getNonPhasing().setTech(Tech.MEDIEVAL);
        object.getNonPhasing().setMoral(2);
        object.getNonPhasing().setLosses(new BattleLossesEntity());
        object.getNonPhasing().setModifiers(new BattleDayEntity());
        objects.add(object);
        object = new SiegeEntity();
        object.setId(2L);
        object.setPhasing(new SiegeSideEntity());
        object.getPhasing().setLosses(new BattleLossesEntity());
        object.getPhasing().setModifiers(new BattleDayEntity());
        object.setNonPhasing(new SiegeSideEntity());
        object.getNonPhasing().setLosses(new BattleLossesEntity());
        object.getNonPhasing().setModifiers(new BattleDayEntity());
        objects.add(object);

        return objects;
    }
}
