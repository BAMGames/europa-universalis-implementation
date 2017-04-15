package com.mkl.eu.service.service.util.impl;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Period;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.MonarchEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
 * Unit tests for OEUtil.
 *
 * @author MKL.
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/provinces.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class OEUtilTest {
    @Autowired
    private IProvinceDao provinceDao;

    private OEUtilImpl oeUtil = new OEUtilImpl();

    @Test
    public void testAdminValue() {
        Assert.assertEquals(3, oeUtil.getAdministrativeValue(null));
        PlayableCountryEntity country = new PlayableCountryEntity();
        Assert.assertEquals(3, oeUtil.getAdministrativeValue(country));
        country.setMonarch(new MonarchEntity());
        Assert.assertEquals(3, oeUtil.getAdministrativeValue(country));
        country.getMonarch().setAdministrative(5);
        Assert.assertEquals(5, oeUtil.getAdministrativeValue(country));
        country.getMonarch().setAdministrative(9);
        Assert.assertEquals(9, oeUtil.getAdministrativeValue(country));
    }

    @Test
    public void testDiploValue() {
        Assert.assertEquals(3, oeUtil.getDiplomaticValue(null));
        PlayableCountryEntity country = new PlayableCountryEntity();
        Assert.assertEquals(3, oeUtil.getDiplomaticValue(country));
        country.setMonarch(new MonarchEntity());
        Assert.assertEquals(3, oeUtil.getDiplomaticValue(country));
        country.getMonarch().setDiplomacy(5);
        Assert.assertEquals(5, oeUtil.getDiplomaticValue(country));
        country.getMonarch().setDiplomacy(9);
        Assert.assertEquals(9, oeUtil.getDiplomaticValue(country));
    }

    @Test
    public void testMilValue() {
        Assert.assertEquals(3, oeUtil.getMilitaryValue(null));
        PlayableCountryEntity country = new PlayableCountryEntity();
        Assert.assertEquals(3, oeUtil.getMilitaryValue(country));
        country.setMonarch(new MonarchEntity());
        Assert.assertEquals(3, oeUtil.getMilitaryValue(country));
        country.getMonarch().setMilitary(5);
        Assert.assertEquals(5, oeUtil.getMilitaryValue(country));
        country.getMonarch().setMilitary(9);
        Assert.assertEquals(9, oeUtil.getMilitaryValue(country));
    }

    @Test
    public void testInitiative() {
        Assert.assertEquals(9, oeUtil.getInitiative(null));
        PlayableCountryEntity country = new PlayableCountryEntity();
        Assert.assertEquals(9, oeUtil.getInitiative(country));
        country.setMonarch(new MonarchEntity());
        Assert.assertEquals(9, oeUtil.getInitiative(country));
        country.getMonarch().setAdministrative(5);
        Assert.assertEquals(11, oeUtil.getInitiative(country));
        country.getMonarch().setDiplomacy(9);
        Assert.assertEquals(17, oeUtil.getInitiative(country));
        country.getMonarch().setMilitary(4);
        Assert.assertEquals(18, oeUtil.getInitiative(country));
    }

    @Test
    public void testStability() {
        Assert.assertEquals(0, oeUtil.getStability(null, null));
        GameEntity game = new GameEntity();
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().add(new StackEntity());
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().get(0).setProvince("B_STAB_1");
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(0, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(0, oeUtil.getStability(game, "russie"));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("B_TECH_2");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(1).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(0, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(0, oeUtil.getStability(game, "russie"));
        game.getStacks().get(1).setProvince("B_STAB_-2");
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(-2, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(0, oeUtil.getStability(game, "russie"));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(0).getCounters().get(1).setCountry("russie");
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(-2, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(1, oeUtil.getStability(game, "russie"));
    }

    @Test
    public void testTechnology() {
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(null, null, true));
        GameEntity game = new GameEntity();
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().add(new StackEntity());
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().get(0).setProvince("B_TECH_1");
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "angleterre", false));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("B_TECH_25");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(1).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(25, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "angleterre", false));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(0).getCounters().get(1).setCountry("angleterre");
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(25, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "angleterre", false));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("B_STAB_3");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(2).getCounters().get(0).setCountry("france");
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(25, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "angleterre", false));
    }

    @Test
    public void testSettleDistance() {
        Assert.assertEquals(-1, oeUtil.settleDistance(null, null, null, null, 0));
        RotwProvinceEntity panama = new RotwProvinceEntity();
        panama.setTerrain(TerrainEnum.SPARSE_FOREST);
        panama.setName("rPanama");
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, null, null, null, 0));
        List<String> discoveries = new ArrayList<>();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        discoveries.add(panama.getName());
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        List<String> sources = new ArrayList<>();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.add(panama.getName());
        Assert.assertEquals(0, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        RotwProvinceEntity mexique = new RotwProvinceEntity();
        mexique.setTerrain(TerrainEnum.SEA);
        mexique.setName("mexique");
        panama.getBorders().add(new BorderEntity());
        panama.getBorders().get(0).setProvinceFrom(panama);
        panama.getBorders().get(0).setProvinceTo(mexique);
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        discoveries.add(mexique.getName());
        Assert.assertEquals(0, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        discoveries.remove(mexique.getName());
        RotwProvinceEntity ecuador = new RotwProvinceEntity();
        ecuador.setTerrain(TerrainEnum.MOUNTAIN);
        ecuador.setName("rEcuador");
        sources.remove(panama.getName());
        sources.add(ecuador.getName());
        panama.getBorders().add(new BorderEntity());
        panama.getBorders().get(1).setProvinceFrom(panama);
        panama.getBorders().get(1).setProvinceTo(ecuador);
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        discoveries.add(ecuador.getName());
        Assert.assertEquals(6, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        RotwProvinceEntity venezuelua = new RotwProvinceEntity();
        venezuelua.setTerrain(TerrainEnum.PLAIN);
        venezuelua.setName("rVenezuela");
        sources.add(venezuelua.getName());
        panama.getBorders().add(new BorderEntity());
        panama.getBorders().get(2).setProvinceFrom(panama);
        panama.getBorders().get(2).setProvinceTo(venezuelua);
        panama.getBorders().get(2).setType(BorderEnum.RIVER);
        Assert.assertEquals(6, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.remove(ecuador.getName());
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        discoveries.add(venezuelua.getName());
        Assert.assertEquals(8, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.add(ecuador.getName());
        Assert.assertEquals(6, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.clear();
        RotwProvinceEntity guyane = new RotwProvinceEntity();
        guyane.setTerrain(TerrainEnum.PLAIN);
        guyane.setName("rGuyane");
        venezuelua.getBorders().add(new BorderEntity());
        venezuelua.getBorders().get(0).setProvinceFrom(venezuelua);
        venezuelua.getBorders().get(0).setProvinceTo(guyane);
        venezuelua.getBorders().get(0).setType(BorderEnum.PASS);
        sources.add(guyane.getName());
        discoveries.add(guyane.getName());
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        List<String> friendlies = new ArrayList<>();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        friendlies.add(venezuelua.getName());
        Assert.assertEquals(12, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        RotwProvinceEntity amazonia = new RotwProvinceEntity();
        amazonia.setTerrain(TerrainEnum.PLAIN);
        amazonia.setName("rAmazonia");
        ecuador.getBorders().add(new BorderEntity());
        ecuador.getBorders().get(0).setProvinceFrom(ecuador);
        ecuador.getBorders().get(0).setProvinceTo(amazonia);
        ecuador.getBorders().get(0).setType(BorderEnum.BERING_STRAIT);
        discoveries.add(ecuador.getName());
        Assert.assertEquals(12, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        friendlies.clear();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        Assert.assertEquals(12, oeUtil.settleDistance(ecuador, discoveries, sources, friendlies, 0));

        discoveries.add("rLena~E");
        discoveries.add("rYakoutie~W");
        discoveries.add("rLena~C");
        sources.add("rYakoutie~W");
        AbstractProvinceEntity lena = provinceDao.getProvinceByName("rLena~E");
        Assert.assertEquals(10, oeUtil.settleDistance(lena, discoveries, sources, friendlies, 0));
        Assert.assertTrue(oeUtil.canSettle(lena, discoveries, sources, friendlies));
        AbstractProvinceEntity lenaC = provinceDao.getProvinceByName("rLena~C");
        Assert.assertEquals(-1, oeUtil.settleDistance(lenaC, discoveries, sources, friendlies, 0));
        Assert.assertFalse(oeUtil.canSettle(lenaC, discoveries, sources, friendlies));
    }

    @Test
    public void testRollDie() {
        GameEntity game = new GameEntity();
        long seed = System.nanoTime();

        game.setSeed(seed);

        int die = oeUtil.rollDie(game, (String) null);

        game.setSeed(seed);

        Assert.assertEquals(die, oeUtil.rollDie(game, (PlayableCountryEntity) null));
    }

    @Test
    public void testFti() {
        GameEntity game = new GameEntity();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setFti(1);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().get(1).setFti(3);
        game.setTurn(15);

        Tables tables = new Tables();
        tables.getPeriods().add(new Period());
        tables.getPeriods().get(0).setName(Period.PERIOD_III);
        tables.getPeriods().get(0).setBegin(14);
        tables.getPeriods().get(0).setEnd(25);
        tables.getPeriods().add(new Period());
        tables.getPeriods().get(1).setName(Period.PERIOD_IV);
        tables.getPeriods().get(1).setBegin(25);
        tables.getPeriods().get(1).setEnd(30);

        Assert.assertEquals(0, oeUtil.getFti(game, tables, null));
        Assert.assertEquals(1, oeUtil.getFti(game, tables, "france"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "angleterre"));
        Assert.assertEquals(2, oeUtil.getFti(game, tables, "sabaudia"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "danemark"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "genes"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "hollande"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "portugal"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "provincesne"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "suede"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "venise"));

        game.setTurn(27);

        Assert.assertEquals(0, oeUtil.getFti(game, tables, null));
        Assert.assertEquals(1, oeUtil.getFti(game, tables, "france"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "angleterre"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "sabaudia"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "danemark"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "genes"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "hollande"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "portugal"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "provincesne"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "suede"));
        Assert.assertEquals(4, oeUtil.getFti(game, tables, "venise"));

        game.setTurn(35);

        Assert.assertEquals(0, oeUtil.getFti(game, tables, null));
        Assert.assertEquals(1, oeUtil.getFti(game, tables, "france"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "angleterre"));
        Assert.assertEquals(2, oeUtil.getFti(game, tables, "sabaudia"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "danemark"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "genes"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "hollande"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "portugal"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "provincesne"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "suede"));
        Assert.assertEquals(3, oeUtil.getFti(game, tables, "venise"));
    }

    @Test
    public void testDti() {
        GameEntity game = new GameEntity();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setDti(3);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().get(1).setDti(1);
        game.setTurn(15);

        Tables tables = new Tables();
        tables.getPeriods().add(new Period());
        tables.getPeriods().get(0).setName(Period.PERIOD_III);
        tables.getPeriods().get(0).setBegin(14);
        tables.getPeriods().get(0).setEnd(25);
        tables.getPeriods().add(new Period());
        tables.getPeriods().get(1).setName(Period.PERIOD_IV);
        tables.getPeriods().get(1).setBegin(25);
        tables.getPeriods().get(1).setEnd(30);

        Assert.assertEquals(0, oeUtil.getDti(game, tables, null));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "france"));
        Assert.assertEquals(1, oeUtil.getDti(game, tables, "angleterre"));
        Assert.assertEquals(2, oeUtil.getDti(game, tables, "sabaudia"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "danemark"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "genes"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "hollande"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "portugal"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "provincesne"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "suede"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "venise"));

        game.setTurn(27);

        Assert.assertEquals(0, oeUtil.getDti(game, tables, null));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "france"));
        Assert.assertEquals(1, oeUtil.getDti(game, tables, "angleterre"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "sabaudia"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "danemark"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "genes"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "hollande"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "portugal"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "provincesne"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "suede"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "venise"));

        game.setTurn(35);

        Assert.assertEquals(0, oeUtil.getDti(game, tables, null));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "france"));
        Assert.assertEquals(1, oeUtil.getDti(game, tables, "angleterre"));
        Assert.assertEquals(2, oeUtil.getDti(game, tables, "sabaudia"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "danemark"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "genes"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "hollande"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "portugal"));
        Assert.assertEquals(4, oeUtil.getDti(game, tables, "provincesne"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "suede"));
        Assert.assertEquals(3, oeUtil.getDti(game, tables, "venise"));
    }

    @Test
    public void testGetStacksOnProvince() {
        GameEntity game = new GameEntity();
        StackEntity idf = new StackEntity();
        idf.setId(1L);
        idf.setProvince("idf");
        game.getStacks().add(idf);
        StackEntity lyonnais = new StackEntity();
        lyonnais.setId(2L);
        lyonnais.setProvince("lyonnais");
        game.getStacks().add(lyonnais);
        StackEntity provence1 = new StackEntity();
        provence1.setId(3L);
        provence1.setProvince("provence");
        game.getStacks().add(provence1);
        StackEntity provence2 = new StackEntity();
        provence2.setId(4L);
        provence2.setProvince("provence");
        game.getStacks().add(provence2);

        List<StackEntity> stacks = oeUtil.getStacksOnProvince(game, "idf");

        Assert.assertEquals(1, stacks.size());
        Assert.assertEquals(idf, stacks.get(0));

        stacks = oeUtil.getStacksOnProvince(game, "lyonnais");

        Assert.assertEquals(1, stacks.size());
        Assert.assertEquals(lyonnais, stacks.get(0));

        stacks = oeUtil.getStacksOnProvince(game, "province");

        Assert.assertEquals(0, stacks.size());

        stacks = oeUtil.getStacksOnProvince(game, "provence");

        Assert.assertEquals(2, stacks.size());
        Assert.assertEquals(provence1, stacks.get(0));
        Assert.assertEquals(provence2, stacks.get(1));
    }

    @Test
    public void testMobile() {
        Assert.assertEquals(false, oeUtil.isMobile(null));
        StackEntity stack = new StackEntity();

        Assert.assertEquals(false, oeUtil.isMobile(stack));

        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_PLUS));

        Assert.assertEquals(true, oeUtil.isMobile(stack));

        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FORTRESS_2));

        Assert.assertEquals(false, oeUtil.isMobile(stack));

        stack.getCounters().clear();
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FORTRESS_2));

        Assert.assertEquals(false, oeUtil.isMobile(stack));

        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_PLUS));

        Assert.assertEquals(false, oeUtil.isMobile(stack));

        stack.getCounters().clear();
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_PLUS));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));

        Assert.assertEquals(true, oeUtil.isMobile(stack));

        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FORTRESS_2));

        Assert.assertEquals(false, oeUtil.isMobile(stack));

        stack.getCounters().clear();
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_PLUS));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FORTRESS_2));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));

        Assert.assertEquals(false, oeUtil.isMobile(stack));

        stack.getCounters().clear();
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_PLUS));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));

        Assert.assertEquals(true, oeUtil.isMobile(stack));

        stack.setBesieged(true);

        Assert.assertEquals(false, oeUtil.isMobile(stack));
    }

    private CounterEntity createCounter(CounterFaceTypeEnum face) {
        CounterEntity counter = new CounterEntity();
        counter.setType(face);
        return counter;
    }

    @Test
    public void testWarStatus() {
        GameEntity game = new GameEntity();
        PlayableCountryEntity country = new PlayableCountryEntity();

        Assert.assertEquals(null, oeUtil.getWarStatus(null, null));
        Assert.assertEquals(null, oeUtil.getWarStatus(game, null));
        Assert.assertEquals(null, oeUtil.getWarStatus(null, country));
        Assert.assertEquals(WarStatusEnum.PEACE, oeUtil.getWarStatus(game, country));

        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setWar(game.getWars().get(0));
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName("espagne");
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setWar(game.getWars().get(0));
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName("france");
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setWar(game.getWars().get(0));
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName("pologne");
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FOREIGN);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(3).setWar(game.getWars().get(0));
        game.getWars().get(0).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(3).getCountry().setName("turquie");
        game.getWars().get(0).getCountries().get(3).setImplication(WarImplicationEnum.LIMITED);

        game.getWars().add(new WarEntity());
        game.getWars().get(1).setType(WarTypeEnum.CIVIL_WAR);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(0).setWar(game.getWars().get(1));
        game.getWars().get(1).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(0).getCountry().setName("espagne");
        game.getWars().get(1).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(0).setOffensive(true);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(1).setWar(game.getWars().get(1));
        game.getWars().get(1).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(1).getCountry().setName("france");
        game.getWars().get(1).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(2).setWar(game.getWars().get(1));
        game.getWars().get(1).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(2).getCountry().setName("angleterre");
        game.getWars().get(1).getCountries().get(2).setImplication(WarImplicationEnum.FOREIGN);
        game.getWars().get(1).getCountries().get(2).setOffensive(true);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(3).setWar(game.getWars().get(1));
        game.getWars().get(1).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(3).getCountry().setName("pologne");
        game.getWars().get(1).getCountries().get(3).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(4).setWar(game.getWars().get(1));
        game.getWars().get(1).getCountries().get(4).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(4).getCountry().setName("turquie");
        game.getWars().get(1).getCountries().get(4).setImplication(WarImplicationEnum.FOREIGN);

        game.getWars().add(new WarEntity());
        game.getWars().get(2).setType(WarTypeEnum.RELIGIOUS_WAR);
        game.getWars().get(2).getCountries().add(new CountryInWarEntity());
        game.getWars().get(2).getCountries().get(0).setWar(game.getWars().get(2));
        game.getWars().get(2).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(2).getCountries().get(0).getCountry().setName("espagne");
        game.getWars().get(2).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(2).getCountries().get(0).setOffensive(true);
        game.getWars().get(2).getCountries().add(new CountryInWarEntity());
        game.getWars().get(2).getCountries().get(1).setWar(game.getWars().get(2));
        game.getWars().get(2).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(2).getCountries().get(1).getCountry().setName("france");
        game.getWars().get(2).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(2).getCountries().add(new CountryInWarEntity());
        game.getWars().get(2).getCountries().get(2).setWar(game.getWars().get(2));
        game.getWars().get(2).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(2).getCountries().get(2).getCountry().setName("angleterre");
        game.getWars().get(2).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(2).getCountries().add(new CountryInWarEntity());
        game.getWars().get(2).getCountries().get(3).setWar(game.getWars().get(2));
        game.getWars().get(2).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(2).getCountries().get(3).getCountry().setName("suede");
        game.getWars().get(2).getCountries().get(3).setImplication(WarImplicationEnum.FOREIGN);
        game.getWars().get(2).getCountries().get(3).setOffensive(true);

        country.setName("espagne");

        Assert.assertEquals(WarStatusEnum.CLASSIC_WAR, oeUtil.getWarStatus(game, country));

        country.setName("france");

        Assert.assertEquals(WarStatusEnum.CIVIL_WAR, oeUtil.getWarStatus(game, country));

        country.setName("angleterre");

        Assert.assertEquals(WarStatusEnum.RELIGIOUS_WAR, oeUtil.getWarStatus(game, country));

        country.setName("pologne");

        Assert.assertEquals(WarStatusEnum.LIMITED_INTERVENTION, oeUtil.getWarStatus(game, country));

        country.setName("turquie");

        Assert.assertEquals(WarStatusEnum.LIMITED_INTERVENTION, oeUtil.getWarStatus(game, country));

        country.setName("suede");

        Assert.assertEquals(WarStatusEnum.FOREIGN_INTERVENTION, oeUtil.getWarStatus(game, country));

        country.setName("russie");

        Assert.assertEquals(WarStatusEnum.PEACE, oeUtil.getWarStatus(game, country));

        Assert.assertEquals(0, oeUtil.getEnemies(null, null, true).size());
        Assert.assertEquals(0, oeUtil.getEnemies(game, null, true).size());
        Assert.assertEquals(0, oeUtil.getEnemies(null, country, true).size());

        List<String> enemies;
        country.setName("espagne");
        enemies = oeUtil.getEnemies(game, country, true);
        Collections.sort(enemies);

        Assert.assertEquals(4, enemies.size());
        Assert.assertEquals("angleterre", enemies.get(0));
        Assert.assertEquals("france", enemies.get(1));
        Assert.assertEquals("pologne", enemies.get(2));
        Assert.assertEquals("turquie", enemies.get(3));

        enemies = oeUtil.getEnemies(game, country, false);
        Collections.sort(enemies);

        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals("angleterre", enemies.get(0));
        Assert.assertEquals("france", enemies.get(1));

        country.setName("france");
        enemies = oeUtil.getEnemies(game, country, true);
        Collections.sort(enemies);

        Assert.assertEquals(3, enemies.size());
        Assert.assertEquals("angleterre", enemies.get(0));
        Assert.assertEquals("espagne", enemies.get(1));
        Assert.assertEquals("suede", enemies.get(2));

        enemies = oeUtil.getEnemies(game, country, false);
        Collections.sort(enemies);

        Assert.assertEquals(1, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));

        country.setName("angleterre");
        enemies = oeUtil.getEnemies(game, country, true);
        Collections.sort(enemies);

        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));
        Assert.assertEquals("suede", enemies.get(1));

        enemies = oeUtil.getEnemies(game, country, false);
        Collections.sort(enemies);

        Assert.assertEquals(1, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));

        country.setName("pologne");
        enemies = oeUtil.getEnemies(game, country, true);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());

        enemies = oeUtil.getEnemies(game, country, false);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());

        country.setName("russie");
        enemies = oeUtil.getEnemies(game, country, true);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());

        enemies = oeUtil.getEnemies(game, country, false);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());
    }

    @Test
    public void testGetMovePoints() {
        AbstractProvinceEntity from = new EuropeanProvinceEntity();
        AbstractProvinceEntity to = from;

        Assert.assertEquals(0, oeUtil.getMovePoints(from, to, false));
        Assert.assertEquals(0, oeUtil.getMovePoints(from, to, true));

        to = new EuropeanProvinceEntity();

        Assert.assertEquals(-1, oeUtil.getMovePoints(from, to, false));
        Assert.assertEquals(-1, oeUtil.getMovePoints(from, to, true));

        Pair<AbstractProvinceEntity, AbstractProvinceEntity> pair = createProvinces(TerrainEnum.PLAIN, false, TerrainEnum.PLAIN, false, null);

        Assert.assertEquals(2, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(1, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.PLAIN, true, TerrainEnum.PLAIN, false, null);

        Assert.assertEquals(2, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(1, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.PLAIN, false, TerrainEnum.PLAIN, true, null);

        Assert.assertEquals(4, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(2, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.PLAIN, true, TerrainEnum.PLAIN, true, null);

        Assert.assertEquals(4, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(2, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.PLAIN, true, TerrainEnum.PLAIN, true, BorderEnum.RIVER);

        Assert.assertEquals(6, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(4, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.PLAIN, true, TerrainEnum.PLAIN, false, BorderEnum.RIVER);

        Assert.assertEquals(3, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(2, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.SWAMP, true, TerrainEnum.DESERT, false, BorderEnum.STRAIT);

        Assert.assertEquals(5, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(5, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.DENSE_FOREST, true, TerrainEnum.SWAMP, false, BorderEnum.PASS);

        Assert.assertEquals(4, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(4, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.DENSE_FOREST, true, TerrainEnum.SPARSE_FOREST, true, BorderEnum.PASS);

        Assert.assertEquals(8, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(8, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.SWAMP, true, TerrainEnum.DENSE_FOREST, true, BorderEnum.PASS);

        Assert.assertEquals(10, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(10, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.MOUNTAIN, false, TerrainEnum.MOUNTAIN, false, BorderEnum.PASS);

        Assert.assertEquals(4, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(3, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.MOUNTAIN, true, TerrainEnum.MOUNTAIN, true, BorderEnum.PASS);

        Assert.assertEquals(8, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(8, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.SWAMP, true, TerrainEnum.SWAMP, true, BorderEnum.RIVER);

        Assert.assertEquals(12, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(12, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.SWAMP, true, TerrainEnum.SWAMP, true, BorderEnum.BERING_STRAIT);

        Assert.assertEquals(12, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(12, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));

        pair = createProvinces(TerrainEnum.PLAIN, true, TerrainEnum.PLAIN, true, BorderEnum.BERING_STRAIT);

        Assert.assertEquals(12, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), false));
        Assert.assertEquals(12, oeUtil.getMovePoints(pair.getLeft(), pair.getRight(), true));
    }

    private Pair<AbstractProvinceEntity, AbstractProvinceEntity> createProvinces(TerrainEnum terrainFrom, boolean fromRotw, TerrainEnum terrainTo, boolean toRotw, BorderEnum borderType) {
        AbstractProvinceEntity from;
        if (fromRotw) {
            from = new RotwProvinceEntity();
        } else {
            from = new EuropeanProvinceEntity();
        }
        from.setTerrain(terrainFrom);

        AbstractProvinceEntity to;
        if (toRotw) {
            to = new RotwProvinceEntity();
        } else {
            to = new EuropeanProvinceEntity();
        }
        to.setTerrain(terrainTo);

        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(from);
        border.setProvinceTo(new EuropeanProvinceEntity());
        from.getBorders().add(border);
        border = new BorderEntity();
        border.setProvinceFrom(from);
        border.setProvinceTo(to);
        border.setType(borderType);
        from.getBorders().add(border);
        border = new BorderEntity();
        border.setProvinceFrom(from);
        border.setProvinceTo(new RotwProvinceEntity());
        border.setType(BorderEnum.RIVER);
        from.getBorders().add(border);

        return new ImmutablePair<>(from, to);
    }
}
