package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.country.Relation;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.country.RelationEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.event.PoliticalEventEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Test of GameMapping.
 *
 * @author MKL.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/mkl/eu/service/service/eu-service-service-applicationContext-test.xml"})
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

    static {
        FRA_VO = new PlayableCountry();
        FRA_VO.setId(1L);
        FRA_VO.setName("FRA");
        FRA_VO.setUsername("MKL");
        FRA_VO.setDti(3);
        FRA_VO.setDtiMax(5);

        PRU_VO = new PlayableCountry();
        PRU_VO.setId(2L);
        PRU_VO.setName("PRU");
        PRU_VO.setUsername("Fogia");
        PRU_VO.setFti(4);
        PRU_VO.setFtiMax(4);

        FRA_OE = new PlayableCountryEntity();
        FRA_OE.setId(1L);
        FRA_OE.setName("FRA");
        FRA_OE.setUsername("MKL");
        FRA_OE.setDti(3);
        FRA_OE.setDtiMax(5);

        PRU_OE = new PlayableCountryEntity();
        PRU_OE.setId(2L);
        PRU_OE.setName("PRU");
        PRU_OE.setUsername("Fogia");
        PRU_OE.setFti(4);
        PRU_OE.setFtiMax(4);
    }

    @Test
    public void testVoidGameMapping() {
        Game vo = gameMapping.oeToVo(null);

        Assert.assertNull(vo);

        GameEntity entity = new GameEntity();

        vo = gameMapping.oeToVo(entity);

        ReflectionAssert.assertReflectionEquals(new Game(), vo);
    }

    @Test
    public void testFullGameMapping() {
        GameEntity entity = createGameEntity();

        Game vo = gameMapping.oeToVo(entity);

        ReflectionAssert.assertReflectionEquals(createGameVo(), vo);

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

        object.setEvents(createEventsVos());

        object.setRelations(createRelationsVos(object.getCountries().get(0), object.getCountries().get(0), object.getCountries().get(1)));

        object.setStacks(createStacksVos());

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

    private List<Relation> createRelationsVos(PlayableCountry first, PlayableCountry second, PlayableCountry third) {
        List<Relation> objects = new ArrayList<>();

        Relation object = new Relation();
        object.setId(1L);
        object.setType(RelationTypeEnum.ALLIANCE);
        object.setFirst(first);
        object.setSecond(second);
        objects.add(object);
        object = new Relation();
        object.setId(2L);
        object.setType(RelationTypeEnum.WAR);
        object.setFirst(second);
        object.setSecond(third);
        objects.add(object);

        return objects;
    }

    private List<Stack> createStacksVos() {
        List<Stack> objects = new ArrayList<>();

        Stack object = new Stack();
        object.setId(1L);
        object.setProvince(PECS);
        List<Counter> subObjects = new ArrayList<>();
        Counter subObject = new Counter();
        subObject.setId(1L);
        subObject.setType(CounterFaceTypeEnum.ARMY_MINUS);
        subObject.setCountry("FRA");
        subObject.setOwner(object);
        subObjects.add(subObject);
        subObject = new Counter();
        subObject.setId(2L);
        subObject.setType(CounterFaceTypeEnum.ARMY_PLUS);
        subObject.setCountry("PRU");
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new Stack();
        object.setId(2L);
        object.setProvince(TYR);
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

        object.setEvents(createEventsEntities());


        object.setRelations(createRelationsEntities(object.getCountries().get(0), object.getCountries().get(0), object.getCountries().get(1)));

        object.setStacks(createStacksEntities());

        return object;
    }

    private List<EconomicalSheetEntity> createEconomicalSheetEntities() {
        List<EconomicalSheetEntity> objects = new ArrayList<>();

        EconomicalSheetEntity object = new EconomicalSheetEntity();
        object.setId(1L);
        object.setTurn(1);
        objects.add(object);

        object = new EconomicalSheetEntity();
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

    private List<RelationEntity> createRelationsEntities(PlayableCountryEntity first, PlayableCountryEntity second, PlayableCountryEntity third) {
        List<RelationEntity> objects = new ArrayList<>();

        RelationEntity object = new RelationEntity();
        object.setId(1L);
        object.setType(RelationTypeEnum.ALLIANCE);
        object.setFirst(first);
        object.setSecond(second);
        objects.add(object);
        object = new RelationEntity();
        object.setId(2L);
        object.setType(RelationTypeEnum.WAR);
        object.setFirst(second);
        object.setSecond(third);
        objects.add(object);

        return objects;
    }

    private List<StackEntity> createStacksEntities() {
        List<StackEntity> objects = new ArrayList<>();

        StackEntity object = new StackEntity();
        object.setId(1L);
        object.setProvince(PECS);
        List<CounterEntity> subObjects = new ArrayList<>();
        CounterEntity subObject = new CounterEntity();
        subObject.setId(1L);
        subObject.setType(CounterFaceTypeEnum.ARMY_MINUS);
        subObject.setCountry("FRA");
        subObject.setOwner(object);
        subObjects.add(subObject);
        subObject = new CounterEntity();
        subObject.setId(2L);
        subObject.setType(CounterFaceTypeEnum.ARMY_PLUS);
        subObject.setCountry("PRU");
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new StackEntity();
        object.setId(2L);
        object.setProvince(TYR);
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
        objects.add(object);

        return objects;
    }
}
