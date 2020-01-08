package com.mkl.eu.service.service.util.impl;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.MonarchEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.tables.CombatResultEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.AbstractGameServiceTest;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;
import static com.mkl.eu.client.common.util.CommonUtil.THIRD;

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
    private static int count = 0;
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

        game.setSeed(seed);

        Assert.assertEquals(die, oeUtil.rollDie(game));
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

    private CounterEntity createCounter(CounterFaceTypeEnum face, String country) {
        CounterEntity counter = new CounterEntity();
        counter.setType(face);
        counter.setCountry(country);
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
        Assert.assertEquals(0, oeUtil.getEnemies(null, game, true).size());
        Assert.assertEquals(0, oeUtil.getEnemies(country, null, true).size());

        List<String> enemies;
        country.setName("espagne");
        enemies = oeUtil.getEnemies(country, game, true);
        Collections.sort(enemies);

        Assert.assertEquals(4, enemies.size());
        Assert.assertEquals("angleterre", enemies.get(0));
        Assert.assertEquals("france", enemies.get(1));
        Assert.assertEquals("pologne", enemies.get(2));
        Assert.assertEquals("turquie", enemies.get(3));

        enemies = oeUtil.getEnemies(country, game, false);
        Collections.sort(enemies);

        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals("angleterre", enemies.get(0));
        Assert.assertEquals("france", enemies.get(1));

        country.setName("france");
        enemies = oeUtil.getEnemies(country, game, true);
        Collections.sort(enemies);

        Assert.assertEquals(3, enemies.size());
        Assert.assertEquals("angleterre", enemies.get(0));
        Assert.assertEquals("espagne", enemies.get(1));
        Assert.assertEquals("suede", enemies.get(2));

        enemies = oeUtil.getEnemies(country, game, false);
        Collections.sort(enemies);

        Assert.assertEquals(1, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));

        country.setName("angleterre");
        enemies = oeUtil.getEnemies(country, game, true);
        Collections.sort(enemies);

        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));
        Assert.assertEquals("suede", enemies.get(1));

        enemies = oeUtil.getEnemies(country, game, false);
        Collections.sort(enemies);

        Assert.assertEquals(1, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));

        country.setName("pologne");
        enemies = oeUtil.getEnemies(country, game, true);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());

        enemies = oeUtil.getEnemies(country, game, false);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());

        country.setName("russie");
        enemies = oeUtil.getEnemies(country, game, true);
        Collections.sort(enemies);

        Assert.assertEquals(0, enemies.size());

        enemies = oeUtil.getEnemies(country, game, false);
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

    @Test
    public void testGetController() {
        GameEntity game = new GameEntity();
        EuropeanProvinceEntity provinceEu = new EuropeanProvinceEntity();

        Assert.assertEquals(null, oeUtil.getOwner(provinceEu, game));
        Assert.assertEquals(null, oeUtil.getController(provinceEu, game));

        provinceEu.setDefaultOwner("france");

        Assert.assertEquals("france", oeUtil.getOwner(provinceEu, game));
        Assert.assertEquals("france", oeUtil.getController(provinceEu, game));

        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("eArtois");
        CounterEntity counter = new CounterEntity();
        counter.setType(CounterFaceTypeEnum.OWN);
        counter.setCountry("espagne");
        game.getStacks().get(0).getCounters().add(counter);

        Assert.assertEquals("france", oeUtil.getOwner(provinceEu, game));
        Assert.assertEquals("france", oeUtil.getController(provinceEu, game));

        provinceEu.setName("eArtois");

        Assert.assertEquals("espagne", oeUtil.getOwner(provinceEu, game));
        Assert.assertEquals("espagne", oeUtil.getController(provinceEu, game));

        counter = new CounterEntity();
        counter.setType(CounterFaceTypeEnum.CONTROL);
        counter.setCountry("hollande");
        game.getStacks().get(0).getCounters().add(counter);

        Assert.assertEquals("espagne", oeUtil.getOwner(provinceEu, game));
        Assert.assertEquals("hollande", oeUtil.getController(provinceEu, game));

        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("rAral");

        RotwProvinceEntity provinceRotw = new RotwProvinceEntity();
        provinceRotw.setName("rAral");

        Assert.assertEquals(null, oeUtil.getOwner(provinceRotw, game));
        Assert.assertEquals(null, oeUtil.getController(provinceRotw, game));

        counter = new CounterEntity();
        counter.setType(CounterFaceTypeEnum.COLONY_PLUS);
        counter.setCountry("russie");
        game.getStacks().get(1).getCounters().add(counter);

        Assert.assertEquals("russie", oeUtil.getOwner(provinceRotw, game));
        Assert.assertEquals("russie", oeUtil.getController(provinceRotw, game));

        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("rAral");
        counter = new CounterEntity();
        counter.setType(CounterFaceTypeEnum.CONTROL);
        counter.setCountry("turquie");
        game.getStacks().get(2).getCounters().add(counter);

        Assert.assertEquals("russie", oeUtil.getOwner(provinceRotw, game));
        Assert.assertEquals("turquie", oeUtil.getController(provinceRotw, game));
    }

    @Test
    public void getAlliesEnemies() {
        GameEntity game = new GameEntity();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");

        List<String> allies;
        List<String> enemies;

        allies = oeUtil.getAllies(country, game);
        enemies = oeUtil.getEnemies(country, game);

        Assert.assertEquals(1, allies.size());
        Assert.assertEquals("france", allies.get(0));
        Assert.assertEquals(0, enemies.size());

        WarEntity war1 = new WarEntity();
        war1.setType(WarTypeEnum.CLASSIC_WAR);
        CountryInWarEntity countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("france");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        countryWar.setWar(war1);
        war1.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("espagne");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        countryWar.setWar(war1);
        war1.getCountries().add(countryWar);
        game.getWars().add(war1);

        allies = oeUtil.getAllies(country, game);
        enemies = oeUtil.getEnemies(country, game);

        Assert.assertEquals(1, allies.size());
        Assert.assertEquals("france", allies.get(0));
        Assert.assertEquals(1, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));

        WarEntity war2 = new WarEntity();
        war2.setType(WarTypeEnum.RELIGIOUS_WAR);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("espagne");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        countryWar.setWar(war2);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("portugal");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        countryWar.setWar(war2);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("angleterre");
        countryWar.setImplication(WarImplicationEnum.LIMITED);
        countryWar.setOffensive(true);
        countryWar.setWar(war2);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("france");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        countryWar.setWar(war2);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("turquie");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        countryWar.setWar(war2);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("hollande");
        countryWar.setImplication(WarImplicationEnum.FOREIGN);
        countryWar.setOffensive(false);
        countryWar.setWar(war2);
        war2.getCountries().add(countryWar);
        game.getWars().add(war2);

        allies = oeUtil.getAllies(country, game);
        enemies = oeUtil.getEnemies(country, game);

        Assert.assertEquals(2, allies.size());
        Assert.assertEquals("france", allies.get(0));
        Assert.assertEquals("turquie", allies.get(1));
        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));
        Assert.assertEquals("portugal", enemies.get(1));

        WarEntity war3 = new WarEntity();
        war3.setType(WarTypeEnum.CIVIL_WAR);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("habsbourg");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        countryWar.setWar(war3);
        war3.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("france");
        countryWar.setImplication(WarImplicationEnum.LIMITED);
        countryWar.setOffensive(true);
        countryWar.setWar(war3);
        war3.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName("pologne");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        countryWar.setWar(war3);
        war3.getCountries().add(countryWar);
        game.getWars().add(war3);

        allies = oeUtil.getAllies(country, game);
        enemies = oeUtil.getEnemies(country, game);

        Assert.assertEquals(2, allies.size());
        Assert.assertEquals("france", allies.get(0));
        Assert.assertEquals("turquie", allies.get(1));
        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals("espagne", enemies.get(0));
        Assert.assertEquals("portugal", enemies.get(1));
    }

    @Test
    public void testLevelFortress() {
        EuropeanProvinceEntity provinceEu = new EuropeanProvinceEntity();
        provinceEu.setName("idf");
        GameEntity game = new GameEntity();

        Assert.assertEquals(0, oeUtil.getFortressLevel(provinceEu, game));

        provinceEu.setFortress(2);

        Assert.assertEquals(2, oeUtil.getFortressLevel(provinceEu, game));

        StackEntity stack = new StackEntity();
        stack.setProvince("idf");
        stack.getCounters().add(new CounterEntity());
        stack.getCounters().get(0).setType(CounterFaceTypeEnum.FORTRESS_1);
        stack.getCounters().get(0).setCountry("france");
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("pecs");
        stack.getCounters().add(new CounterEntity());
        stack.getCounters().get(0).setType(CounterFaceTypeEnum.FORTRESS_1);
        game.getStacks().add(stack);

        Assert.assertEquals(2, oeUtil.getFortressLevel(provinceEu, game));

        provinceEu.setDefaultOwner("france");

        Assert.assertEquals(1, oeUtil.getFortressLevel(provinceEu, game));

        RotwProvinceEntity provinceRotw = new RotwProvinceEntity();

        Assert.assertEquals(0, oeUtil.getFortressLevel(provinceRotw, game));

        provinceRotw.setName("idf");

        Assert.assertEquals(0, oeUtil.getFortressLevel(provinceRotw, game));

        provinceRotw.setFortress(3);

        Assert.assertEquals(3, oeUtil.getFortressLevel(provinceRotw, game));

        stack = new StackEntity();
        stack.setProvince("idf");
        stack.getCounters().add(new CounterEntity());
        stack.getCounters().get(0).setType(CounterFaceTypeEnum.TRADING_POST_MINUS);
        stack.getCounters().get(0).setCountry("france");
        game.getStacks().add(stack);

        Assert.assertEquals(1, oeUtil.getFortressLevel(provinceRotw, game));

        provinceEu.setName("pecs");
        provinceRotw.setName("pecs");

        Assert.assertEquals(1, oeUtil.getFortressLevel(provinceEu, game));
        Assert.assertEquals(1, oeUtil.getFortressLevel(provinceRotw, game));
    }

    @Test
    public void testGetTechnology() {
        Referential referential = new Referential();
        GameEntity game = new GameEntity();

        Assert.assertEquals(null, oeUtil.getTechnology("france", true, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("france", false, referential, game));

        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setLandTech(Tech.MANOEUVRE);
        france.setNavalTech(Tech.SEVENTY_FOUR);
        game.getCountries().add(france);

        Assert.assertEquals(Tech.MANOEUVRE, oeUtil.getTechnology("france", true, referential, game));
        Assert.assertEquals(Tech.SEVENTY_FOUR, oeUtil.getTechnology("france", false, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("angleterre", true, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("angleterre", false, referential, game));

        PlayableCountryEntity angleterre = new PlayableCountryEntity();
        angleterre.setName("angleterre");
        angleterre.setLandTech(Tech.ARQUEBUS);
        angleterre.setNavalTech(Tech.NAE_GALEON);
        game.getCountries().add(angleterre);

        Assert.assertEquals(Tech.ARQUEBUS, oeUtil.getTechnology("angleterre", true, referential, game));
        Assert.assertEquals(Tech.NAE_GALEON, oeUtil.getTechnology("angleterre", false, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("genoa", true, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("genoa", false, referential, game));

        CountryReferential genoa = new CountryReferential();
        genoa.setName("genoa");
        genoa.setCulture(CultureEnum.LATIN);
        referential.getCountries().add(genoa);

        Assert.assertEquals(null, oeUtil.getTechnology("genoa", true, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("genoa", false, referential, game));

        game.getMinorLandTechnologies().put(CultureEnum.LATIN, Tech.MUSKET);
        game.getMinorNavalTechnologies().put(CultureEnum.LATIN, Tech.BATTERY);

        Assert.assertEquals(Tech.MUSKET, oeUtil.getTechnology("genoa", true, referential, game));
        Assert.assertEquals(Tech.BATTERY, oeUtil.getTechnology("genoa", false, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("tunisia", true, referential, game));
        Assert.assertEquals(null, oeUtil.getTechnology("tunisia", false, referential, game));

        CountryReferential tunisia = new CountryReferential();
        tunisia.setName("tunisia");
        tunisia.setCulture(CultureEnum.ISLAM);
        referential.getCountries().add(tunisia);
        game.getMinorLandTechnologies().put(CultureEnum.ISLAM, Tech.BAROQUE);
        game.getMinorNavalTechnologies().put(CultureEnum.ISLAM, Tech.VESSEL);

        Assert.assertEquals(Tech.BAROQUE, oeUtil.getTechnology("tunisia", true, referential, game));
        Assert.assertEquals(Tech.VESSEL, oeUtil.getTechnology("tunisia", false, referential, game));
    }

    @Test
    public void testTechnologyStack() {
        GameEntity game = new GameEntity();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setLandTech(Tech.ARQUEBUS);
        france.setNavalTech(Tech.GALLEON_FLUYT);
        game.getCountries().add(france);

        PlayableCountryEntity angleterre = new PlayableCountryEntity();
        angleterre.setName("angleterre");
        angleterre.setLandTech(Tech.RENAISSANCE);
        angleterre.setNavalTech(Tech.NAE_GALEON);
        game.getCountries().add(angleterre);

        Referential referential = new Referential();
        CountryReferential genoa = new CountryReferential();
        genoa.setName("genoa");
        genoa.setCulture(CultureEnum.LATIN);
        referential.getCountries().add(genoa);
        game.getMinorLandTechnologies().put(CultureEnum.LATIN, Tech.MEDIEVAL);
        game.getMinorNavalTechnologies().put(CultureEnum.LATIN, Tech.CARRACK);

        Tables tables = new Tables();
        Tech medieval = new Tech();
        medieval.setName(Tech.MEDIEVAL);
        medieval.setBeginTurn(0);
        tables.getTechs().add(medieval);
        Tech renaissance = new Tech();
        renaissance.setName(Tech.RENAISSANCE);
        renaissance.setBeginTurn(7);
        tables.getTechs().add(renaissance);
        Tech arquebus = new Tech();
        arquebus.setName(Tech.ARQUEBUS);
        arquebus.setBeginTurn(15);
        tables.getTechs().add(arquebus);
        Tech carrack = new Tech();
        carrack.setName(Tech.CARRACK);
        carrack.setBeginTurn(0);
        tables.getTechs().add(carrack);
        Tech nae = new Tech();
        nae.setName(Tech.NAE_GALEON);
        nae.setBeginTurn(12);
        tables.getTechs().add(nae);
        Tech galleon = new Tech();
        galleon.setName(Tech.GALLEON_FLUYT);
        galleon.setBeginTurn(14);
        tables.getTechs().add(galleon);

        List<CounterEntity> counters = new ArrayList<>();

        Assert.assertEquals(null, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(null, oeUtil.getTechnology(counters, false, referential, tables, game));

        CounterEntity counter = new CounterEntity();
        counter.setCountry("france");
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counters.add(counter);

        Assert.assertEquals(Tech.ARQUEBUS, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(Tech.GALLEON_FLUYT, oeUtil.getTechnology(counters, false, referential, tables, game));

        counter = new CounterEntity();
        counter.setCountry("angleterre");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        counters.add(counter);

        Assert.assertEquals(Tech.ARQUEBUS, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(Tech.GALLEON_FLUYT, oeUtil.getTechnology(counters, false, referential, tables, game));

        counter = new CounterEntity();
        counter.setCountry("genoa");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        counters.add(counter);

        Assert.assertEquals(Tech.RENAISSANCE, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(Tech.NAE_GALEON, oeUtil.getTechnology(counters, false, referential, tables, game));

        counter = new CounterEntity();
        counter.setCountry("france");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        counters.add(counter);

        Assert.assertEquals(Tech.ARQUEBUS, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(Tech.GALLEON_FLUYT, oeUtil.getTechnology(counters, false, referential, tables, game));

        counter = new CounterEntity();
        counter.setCountry("genoa");
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counters.add(counter);

        Assert.assertEquals(Tech.RENAISSANCE, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(Tech.NAE_GALEON, oeUtil.getTechnology(counters, false, referential, tables, game));

        counter = new CounterEntity();
        counter.setCountry("genoa");
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        counters.add(counter);

        Assert.assertEquals(Tech.MEDIEVAL, oeUtil.getTechnology(counters, true, referential, tables, game));
        Assert.assertEquals(Tech.CARRACK, oeUtil.getTechnology(counters, false, referential, tables, game));
    }

    @Test
    public void testArtilleryBonus() {
        Referential referential = new Referential();
        CountryReferential france = new CountryReferential();
        france.setName("france");
        france.setArmyClass(ArmyClassEnum.IV);
        referential.getCountries().add(france);
        CountryReferential turquie = new CountryReferential();
        turquie.setName("turquie");
        turquie.setArmyClass(ArmyClassEnum.IM);
        referential.getCountries().add(turquie);
        CountryReferential genoa = new CountryReferential();
        genoa.setName("genoa");
        genoa.setArmyClass(ArmyClassEnum.IV);
        referential.getCountries().add(genoa);

        Tables tables = new Tables();
        ArmyArtillery franceI = new ArmyArtillery();
        franceI.setCountry("france");
        franceI.setPeriod("I");
        franceI.setArtillery(4);
        tables.getArmyArtilleries().add(franceI);
        ArmyArtillery turquieI = new ArmyArtillery();
        turquieI.setCountry("turquie");
        turquieI.setPeriod("I");
        turquieI.setArtillery(3);
        tables.getArmyArtilleries().add(turquieI);
        ArmyArtillery genoaI = new ArmyArtillery();
        genoaI.setArmyClass(ArmyClassEnum.IV);
        genoaI.setPeriod("I");
        genoaI.setArtillery(1);
        tables.getArmyArtilleries().add(genoaI);
        ArmyArtillery franceIV = new ArmyArtillery();
        franceIV.setCountry("france");
        franceIV.setPeriod("IV");
        franceIV.setArtillery(6);
        tables.getArmyArtilleries().add(franceIV);
        ArmyArtillery turquieIV = new ArmyArtillery();
        turquieIV.setCountry("turquie");
        turquieIV.setPeriod("IV");
        turquieIV.setArtillery(5);
        tables.getArmyArtilleries().add(turquieIV);
        ArmyArtillery genoaIV = new ArmyArtillery();
        genoaIV.setArmyClass(ArmyClassEnum.IV);
        genoaIV.setPeriod("IV");
        genoaIV.setArtillery(4);
        tables.getArmyArtilleries().add(genoaIV);

        Period periodI = new Period();
        periodI.setName("I");
        periodI.setBegin(1);
        periodI.setEnd(6);
        tables.getPeriods().add(periodI);
        Period periodIV = new Period();
        periodIV.setName("IV");
        periodIV.setBegin(25);
        periodIV.setEnd(36);
        tables.getPeriods().add(periodIV);

        GameEntity game = new GameEntity();
        game.setTurn(30);

        List<CounterEntity> counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, "france"));

        Assert.assertEquals(0, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"));

        Assert.assertEquals(6, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_MINUS, "france"));

        Assert.assertEquals(3, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "turquie"));

        Assert.assertEquals(5, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_MINUS, "turquie"));

        Assert.assertEquals(2, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "genoa"));

        Assert.assertEquals(4, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_MINUS, "genoa"));

        Assert.assertEquals(2, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        game.setTurn(3);

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"));

        Assert.assertEquals(4, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_MINUS, "france"));

        Assert.assertEquals(2, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "turquie"));

        Assert.assertEquals(3, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_MINUS, "turquie"));

        Assert.assertEquals(1, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "genoa"));

        Assert.assertEquals(1, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Collections.singletonList(createCounter(CounterFaceTypeEnum.ARMY_MINUS, "genoa"));

        Assert.assertEquals(0, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Arrays.asList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"),
                createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"));

        Assert.assertEquals(6, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Arrays.asList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"),
                createCounter(CounterFaceTypeEnum.ARMY_MINUS, "france"));

        Assert.assertEquals(6, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Arrays.asList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"),
                createCounter(CounterFaceTypeEnum.ARMY_MINUS, "turquie"));

        Assert.assertEquals(5, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Arrays.asList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"),
                createCounter(CounterFaceTypeEnum.ARMY_MINUS, "genoa"));

        Assert.assertEquals(4, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Arrays.asList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "france"),
                createCounter(CounterFaceTypeEnum.ARMY_MINUS, "genoa"),
                createCounter(CounterFaceTypeEnum.ARMY_MINUS, "turquie"));

        Assert.assertEquals(5, oeUtil.getArtilleryBonus(counters, referential, tables, game));

        counters = Arrays.asList(createCounter(CounterFaceTypeEnum.ARMY_PLUS, "genoa"),
                createCounter(CounterFaceTypeEnum.ARMY_PLUS, "genoa"),
                createCounter(CounterFaceTypeEnum.ARMY_PLUS, "genoa"));

        Assert.assertEquals(2, oeUtil.getArtilleryBonus(counters, referential, tables, game));
    }

    @Test
    public void testCavalryBonus() {
        Tables tables = new Tables();
        Period periodI = new Period();
        periodI.setName("I");
        periodI.setBegin(1);
        periodI.setEnd(6);
        tables.getPeriods().add(periodI);
        Period periodII = new Period();
        periodII.setName("II");
        periodII.setBegin(7);
        periodII.setEnd(14);
        tables.getPeriods().add(periodII);
        Period periodIII = new Period();
        periodIII.setName("III");
        periodIII.setBegin(15);
        periodIII.setEnd(25);
        tables.getPeriods().add(periodIII);
        Period periodIV = new Period();
        periodIV.setName("IV");
        periodIV.setBegin(26);
        periodIV.setEnd(34);
        tables.getPeriods().add(periodIV);
        Period periodV = new Period();
        periodV.setName("V");
        periodV.setBegin(35);
        periodV.setEnd(42);
        tables.getPeriods().add(periodV);
        Period periodVI = new Period();
        periodVI.setName("VI");
        periodVI.setBegin(43);
        periodVI.setEnd(52);
        tables.getPeriods().add(periodVI);
        Period periodVII = new Period();
        periodVII.setName("VII");
        periodVII.setBegin(53);
        periodVII.setEnd(62);
        tables.getPeriods().add(periodVII);

        checkTerrains(Collections.emptyList(), Collections.emptyList(), periodI.getBegin(), tables);

        checkTerrains(Collections.singletonList(createDetachment(ArmyClassEnum.IV)), Collections.emptyList(), periodI.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IV)), Collections.singletonList(TerrainEnum.PLAIN), periodIII.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IV)), Collections.singletonList(TerrainEnum.PLAIN), 20, tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IV)), Collections.singletonList(TerrainEnum.PLAIN), periodV.getEnd(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IV)), Collections.emptyList(), periodVI.getBegin(), tables);

        checkTerrains(Collections.singletonList(createDetachment(ArmyClassEnum.IIIM)), Collections.emptyList(), periodI.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIIM)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DENSE_FOREST), periodIV.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIIM)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DENSE_FOREST), 40, tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIIM)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DENSE_FOREST), periodV.getEnd(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIIM)), Collections.emptyList(), periodVI.getBegin(), tables);

        checkTerrains(Collections.singletonList(createDetachment(PlayableCountry.SWEDEN)), Collections.emptyList(), periodI.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(PlayableCountry.SWEDEN)), Collections.singletonList(TerrainEnum.DENSE_FOREST), periodIII.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(PlayableCountry.SWEDEN)), Collections.singletonList(TerrainEnum.DENSE_FOREST), 20, tables);
        checkTerrains(Collections.singletonList(createArmy(PlayableCountry.SWEDEN)), Collections.singletonList(TerrainEnum.DENSE_FOREST), periodVI.getEnd(), tables);
        checkTerrains(Collections.singletonList(createArmy(PlayableCountry.SWEDEN)), Collections.emptyList(), periodVII.getBegin(), tables);

        checkTerrains(Collections.singletonList(createDetachment(ArmyClassEnum.IIM)), Collections.emptyList(), periodI.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIM)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.SPARSE_FOREST), periodI.getBegin(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIM)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.SPARSE_FOREST), 20, tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIM)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.SPARSE_FOREST), periodIV.getEnd(), tables);
        checkTerrains(Collections.singletonList(createArmy(ArmyClassEnum.IIM)), Collections.emptyList(), periodV.getBegin(), tables);

        checkTerrains(Collections.singletonList(createDetachment(PlayableCountry.TURKEY)), Collections.emptyList(), 1, tables);
        checkTerrains(Collections.singletonList(createArmy(PlayableCountry.TURKEY)), Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT), 100, tables);

        List<ArmyInfo> allArmies = Arrays.asList(createArmy(ArmyClassEnum.IV), createArmy(ArmyClassEnum.IIIM), createArmy(PlayableCountry.SWEDEN),
                createArmy(ArmyClassEnum.IIM), createArmy(PlayableCountry.TURKEY));
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST), periodI.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST), periodI.getEnd(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST), periodII.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST), periodII.getEnd(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST, TerrainEnum.DENSE_FOREST), periodIII.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST, TerrainEnum.DENSE_FOREST), periodIII.getEnd(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST, TerrainEnum.DENSE_FOREST), periodIV.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.SPARSE_FOREST, TerrainEnum.DENSE_FOREST), periodIV.getEnd(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.DENSE_FOREST), periodV.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.DENSE_FOREST), periodV.getEnd(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.DENSE_FOREST), periodVI.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT, TerrainEnum.DENSE_FOREST), periodVI.getEnd(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT), periodVII.getBegin(), tables);
        checkTerrains(allArmies, Arrays.asList(TerrainEnum.PLAIN, TerrainEnum.DESERT), periodVII.getEnd(), tables);

        checkAssault(false, periodI.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.FRANCE));
        checkAssault(false, periodI.getBegin(), tables, createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.POLAND));
        checkAssault(true, periodI.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.POLAND));
        checkAssault(true, periodI.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.RUSSIA));
        checkAssault(true, periodI.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.TURKEY));
        checkAssault(false, periodI.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, PlayableCountry.TURKEY));

        checkAssault(false, periodI.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_PLUS, PlayableCountry.FRANCE));
        checkAssault(false, periodI.getEnd(), tables, createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.POLAND));
        checkAssault(true, periodI.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_PLUS, PlayableCountry.POLAND));
        checkAssault(true, periodI.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_PLUS, PlayableCountry.RUSSIA));
        checkAssault(true, periodI.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_PLUS, PlayableCountry.TURKEY));
        checkAssault(false, periodI.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, PlayableCountry.TURKEY));

        checkAssault(false, periodII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.FRANCE));
        checkAssault(false, periodII.getBegin(), tables, createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.POLAND));
        checkAssault(true, periodII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.POLAND));
        checkAssault(true, periodII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.RUSSIA));
        checkAssault(true, periodII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.TURKEY));
        checkAssault(false, periodII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, PlayableCountry.TURKEY));

        checkAssault(false, periodII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.FRANCE));
        checkAssault(false, periodII.getEnd(), tables, createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.POLAND));
        checkAssault(true, periodII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.POLAND));
        checkAssault(true, periodII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.RUSSIA));
        checkAssault(true, periodII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.TURKEY));
        checkAssault(false, periodII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, PlayableCountry.TURKEY));

        checkAssault(false, periodIII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.FRANCE));
        checkAssault(false, periodIII.getBegin(), tables, createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.POLAND));
        checkAssault(false, periodIII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.POLAND));
        checkAssault(true, periodIII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.RUSSIA));
        checkAssault(true, periodIII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.TURKEY));
        checkAssault(false, periodIII.getBegin(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, PlayableCountry.TURKEY));

        checkAssault(false, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.FRANCE));
        checkAssault(false, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.POLAND));
        checkAssault(false, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.POLAND));
        checkAssault(true, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.RUSSIA));
        checkAssault(true, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_MINUS, PlayableCountry.TURKEY));
        checkAssault(false, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, PlayableCountry.TURKEY));

        checkAssault(true, periodIII.getEnd(), tables, createCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, PlayableCountry.TURKEY),
                createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, PlayableCountry.TURKEY),
                createCounter(CounterFaceTypeEnum.ARMY_PLUS, PlayableCountry.TURKEY));
    }

    private void checkTerrains(List<ArmyInfo> armies, List<TerrainEnum> terrains, int turn, Tables tables) {
        GameEntity game = new GameEntity();
        game.setTurn(turn);
        for (TerrainEnum terrain : TerrainEnum.values()) {
            Assert.assertEquals("Expected " + (terrains.contains(terrain) ? "" : "no ") + "bonus for terrain " + terrain + " but it was not the case.",
                    terrains.contains(terrain), oeUtil.getCavalryBonus(armies, terrain, tables, game));
        }
    }

    private void checkAssault(boolean hasBonus, int turn, Tables tables, CounterEntity... counters) {
        GameEntity game = new GameEntity();
        game.setTurn(turn);
        Assert.assertEquals(hasBonus, oeUtil.getAssaultBonus(Arrays.asList(counters), tables, game));
    }

    private ArmyInfo createArmy(String country) {
        ArmyInfo army = new ArmyInfo();
        army.setType(getArmyType());
        army.setCountry(country);
        return army;
    }

    private ArmyInfo createArmy(ArmyClassEnum armyClass) {
        ArmyInfo army = new ArmyInfo();
        army.setType(getArmyType());
        army.setArmyClass(armyClass);
        return army;
    }

    private ArmyInfo createArmyPlus(ArmyClassEnum armyClass) {
        ArmyInfo army = new ArmyInfo();
        army.setType(CounterFaceTypeEnum.ARMY_PLUS);
        army.setArmyClass(armyClass);
        return army;
    }

    private ArmyInfo createDetachment(String country) {
        ArmyInfo army = new ArmyInfo();
        army.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        army.setCountry(country);
        return army;
    }

    private ArmyInfo createDetachment(ArmyClassEnum armyClass) {
        ArmyInfo army = new ArmyInfo();
        army.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        army.setArmyClass(armyClass);
        return army;
    }

    private CounterFaceTypeEnum getArmyType() {
        CounterFaceTypeEnum type;
        if (count % 4 == 0) {
            type = CounterFaceTypeEnum.ARMY_MINUS;
        } else if (count % 4 == 1) {
            type = CounterFaceTypeEnum.ARMY_TIMAR_MINUS;
        } else if (count % 4 == 2) {
            type = CounterFaceTypeEnum.ARMY_PLUS;
        } else {
            type = CounterFaceTypeEnum.ARMY_TIMAR_PLUS;
        }
        count++;

        return type;
    }

    @Test
    public void testCanRetreat() {
        GameEntity game = new GameEntity();
        EuropeanProvinceEntity province = new EuropeanProvinceEntity();
        province.setName("idf");
        province.setFortress(3);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        game.getCountries().add(espagne);

        Assert.assertFalse(oeUtil.canRetreat(province, false, 2, france, game));

        WarEntity war = new WarEntity();
        war.setType(WarTypeEnum.CLASSIC_WAR);
        CountryInWarEntity countryWar = new CountryInWarEntity();
        countryWar.setWar(war);
        countryWar.setOffensive(true);
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName(france.getName());
        war.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setWar(war);
        countryWar.setOffensive(false);
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName(espagne.getName());
        war.getCountries().add(countryWar);
        game.getWars().add(war);

        province.setDefaultOwner("suisse");

        Assert.assertFalse(oeUtil.canRetreat(province, false, 2, france, game));

        StackEntity stack = new StackEntity();
        stack.setProvince(province.getName());
        stack.setCountry(espagne.getName());
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.OWN, espagne.getName()));
        game.getStacks().add(stack);

        Assert.assertFalse(oeUtil.canRetreat(province, false, 2, france, game));

        stack = new StackEntity();
        stack.setProvince(province.getName());
        stack.setCountry(france.getName());
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.CONTROL, france.getName()));
        game.getStacks().add(stack);

        Assert.assertTrue(oeUtil.canRetreat(province, false, 2, france, game));

        stack = new StackEntity();
        stack.setProvince(province.getName());
        stack.setCountry(espagne.getName());
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS, espagne.getName()));
        game.getStacks().add(stack);

        Assert.assertFalse(oeUtil.canRetreat(province, false, 2, france, game));

        Assert.assertTrue(oeUtil.canRetreat(province, true, 2, france, game));

        Assert.assertFalse(oeUtil.canRetreat(province, true, 4, france, game));
    }

    @Test
    public void testVeteran() {
        List<CounterEntity> counters = new ArrayList<>();
        Assert.assertFalse(oeUtil.isStackVeteran(counters));

        CounterEntity army = new CounterEntity();
        army.setType(CounterFaceTypeEnum.ARMY_PLUS);
        counters.add(army);

        Assert.assertFalse(oeUtil.isStackVeteran(counters));

        army.setVeterans(4d);

        Assert.assertTrue(oeUtil.isStackVeteran(counters));

        army.setVeterans(2d);

        Assert.assertFalse(oeUtil.isStackVeteran(counters));

        army.setVeterans(3d);

        Assert.assertTrue(oeUtil.isStackVeteran(counters));

        CounterEntity detachment1 = new CounterEntity();
        detachment1.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        counters.add(detachment1);

        Assert.assertTrue(oeUtil.isStackVeteran(counters));

        CounterEntity detachment2 = new CounterEntity();
        detachment2.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        counters.add(detachment2);

        Assert.assertFalse(oeUtil.isStackVeteran(counters));

        detachment1.setVeterans(1d);

        Assert.assertTrue(oeUtil.isStackVeteran(counters));

        detachment1.setVeterans(null);

        Assert.assertFalse(oeUtil.isStackVeteran(counters));

        CounterEntity explo = new CounterEntity();
        explo.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        explo.setVeterans(THIRD);
        counters.add(explo);

        Assert.assertTrue(oeUtil.isStackVeteran(counters));

        counters.clear();
        counters.add(army);
        army.setVeterans(4d);

        Assert.assertTrue(oeUtil.isStackVeteran(counters));

        counters.add(createCounter(CounterFaceTypeEnum.PACHA_1));

        Assert.assertFalse(oeUtil.isStackVeteran(counters));
    }

    @Test
    public void testLossModificationSizeFail() {
        try {
            oeUtil.lossModificationSize(null, -3);
            Assert.fail("Size diff of -3 does not exist.");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("lossModificationSize", e.getParams()[0]);
            Assert.assertEquals("sizeDiff", e.getParams()[1]);
            Assert.assertEquals(-3, e.getParams()[2]);
        }

        try {
            oeUtil.lossModificationSize(null, 4);
            Assert.fail("Size diff of -3 does not exist.");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("lossModificationSize", e.getParams()[0]);
            Assert.assertEquals("sizeDiff", e.getParams()[1]);
            Assert.assertEquals(4, e.getParams()[2]);
        }
    }

    @Test
    public void testLossModificationSizeMinusTwo() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(-2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());
    }

    @Test
    public void testLossModificationSizeMinusOne() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(-1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());
    }

    @Test
    public void testLossModificationSizeZero() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(0)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());
    }

    public void testLossModificationSizeOneInitial() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(2).toEntity());
    }

    @Test
    public void testLossModificationSizeOne() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(1)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).toEntity());
    }

    @Test
    public void testLossModificationSizeTwo() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(9).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(2)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(9).third(1).toEntity());
    }

    public void testLossModificationSizeThreeAlternative() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(9).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(9).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(10).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(10).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(11).third(1).toEntity());
    }

    @Test
    public void testLossModificationSizeThree() {
        LossModBuilder.create()
                .losses(LossBuilder.create().toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(1).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(2).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(1).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(3).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(4).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(2).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(5).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(3).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(6).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(7).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(4).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(8).third(2).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(9).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(5).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(10).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(10).third(1).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(1).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(11).toEntity());

        LossModBuilder.create()
                .losses(LossBuilder.create().round(6).third(2).toEntity())
                .size(3)
                .when(oeUtil)
                .thenExpect(LossBuilder.create().round(11).third(2).toEntity());
    }

    private static class LossModBuilder {
        AbstractWithLossEntity losses;
        Integer size;
        AbstractWithLossEntity result;

        static LossModBuilder create() {
            return new LossModBuilder();
        }

        LossModBuilder losses(AbstractWithLossEntity losses) {
            this.losses = losses;
            return this;
        }

        LossModBuilder size(Integer size) {
            this.size = size;
            return this;
        }

        LossModBuilder when(OEUtilImpl oeUtil) {
            result = oeUtil.lossModificationSize(losses, size);
            return this;
        }

        LossModBuilder thenExpect(AbstractWithLossEntity expected) {
            Assert.assertEquals("The round losses is incorrect for losses " + losses + " and size diff " + size, zeroIfNull(expected.getRoundLoss()), zeroIfNull(result.getRoundLoss()));
            Assert.assertEquals("The third losses is incorrect for losses " + losses + " and size diff " + size, zeroIfNull(expected.getThirdLoss()), zeroIfNull(result.getThirdLoss()));
            return this;
        }
    }

    private static Integer zeroIfNull(Integer input) {
        return input == null ? 0 : input;
    }

    private static class LossBuilder {
        Integer round;
        Integer third;

        static LossBuilder create() {
            return new LossBuilder();
        }

        LossBuilder round(Integer round) {
            this.round = round;
            return this;
        }

        LossBuilder third(Integer third) {
            this.third = third;
            return this;
        }

        AbstractWithLossEntity toEntity() {
            AbstractWithLossEntity entity = new CombatResultEntity();
            entity.setRoundLoss(round);
            entity.setThirdLoss(third);
            return entity;
        }
    }

    @Test
    public void testGetArmySize() {
        GameEntity game = new GameEntity();
        game.setTurn(35);
        Period period = new Period();
        period.setName(Period.PERIOD_V);
        period.setBegin(0);
        period.setEnd(666);
        Tables tables = new Tables();
        tables.getPeriods().add(period);
        tables.getArmyClasses().add(createArmyClasse(ArmyClassEnum.A, 7));
        tables.getArmyClasses().add(createArmyClasse(ArmyClassEnum.I, 4));
        tables.getArmyClasses().add(createArmyClasse(ArmyClassEnum.IV, 3));
        tables.getArmyClasses().add(createArmyClasse(ArmyClassEnum.IIIM, 2));
        tables.getArmyClasses().add(createArmyClasse(ArmyClassEnum.III, 0));

        Assert.assertEquals(7, oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game).intValue());
        Assert.assertEquals(4, oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.I), createDetachment(ArmyClassEnum.I)), tables, game).intValue());
        Assert.assertEquals(3, oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IV), createDetachment(ArmyClassEnum.IV)), tables, game).intValue());
        Assert.assertEquals(2, oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IIIM), createDetachment(ArmyClassEnum.IIIM)), tables, game).intValue());
        Assert.assertEquals(0, oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.III), createDetachment(ArmyClassEnum.III)), tables, game).intValue());

        Assert.assertEquals(3, oeUtil.getArmySize(Arrays.asList(createArmyPlus(ArmyClassEnum.I), createArmyPlus(ArmyClassEnum.IIIM)), tables, game).intValue());
        Assert.assertEquals(1, oeUtil.getArmySize(Arrays.asList(createDetachment(ArmyClassEnum.A), createArmyPlus(ArmyClassEnum.III)), tables, game).intValue());
        Assert.assertEquals(5, oeUtil.getArmySize(Arrays.asList(createArmyPlus(ArmyClassEnum.A), createDetachment(ArmyClassEnum.III)), tables, game).intValue());

        Assert.assertEquals(1, oeUtil.getArmySize(Arrays.asList(createDetachment(ArmyClassEnum.A), createDetachment(ArmyClassEnum.I),
                createArmyPlus(ArmyClassEnum.III)), tables, game).intValue());
        Assert.assertEquals(2, oeUtil.getArmySize(Arrays.asList(createDetachment(ArmyClassEnum.A), createDetachment(ArmyClassEnum.I),
                createDetachment(ArmyClassEnum.IV), createArmyPlus(ArmyClassEnum.III)), tables, game).intValue());

        // 7 vs 4 => +1 / -1
        Assert.assertEquals(1, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.I), createDetachment(ArmyClassEnum.I)), tables, game)), EPSILON);
        Assert.assertEquals(-1, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.I), createDetachment(ArmyClassEnum.I)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game)), EPSILON);
        // 7 vs 2 => +2 / -2
        Assert.assertEquals(2, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IIIM), createDetachment(ArmyClassEnum.IIIM)), tables, game)), EPSILON);
        Assert.assertEquals(-2, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IIIM), createDetachment(ArmyClassEnum.IIIM)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game)), EPSILON);
        // 7 vs 0 => +2 / -2
        Assert.assertEquals(2, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.III), createDetachment(ArmyClassEnum.III)), tables, game)), EPSILON);
        Assert.assertEquals(-2, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.III), createDetachment(ArmyClassEnum.III)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game)), EPSILON);
        // 3 vs 2 => 0 / 0
        Assert.assertEquals(0, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IV), createDetachment(ArmyClassEnum.IV)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IIIM), createDetachment(ArmyClassEnum.IIIM)), tables, game)), EPSILON);
        Assert.assertEquals(0, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IIIM), createDetachment(ArmyClassEnum.IIIM)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.IV), createDetachment(ArmyClassEnum.IV)), tables, game)), EPSILON);
        //7 vs 2.5 => +1 / -1
        Assert.assertEquals(1, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createDetachment(ArmyClassEnum.IV), createDetachment(ArmyClassEnum.IIIM)), tables, game)), EPSILON);
        Assert.assertEquals(-1, oeUtil.getSizeDiff(oeUtil.getArmySize(Arrays.asList(createDetachment(ArmyClassEnum.IV), createDetachment(ArmyClassEnum.IIIM)), tables, game),
                oeUtil.getArmySize(Arrays.asList(createArmy(ArmyClassEnum.A), createDetachment(ArmyClassEnum.A)), tables, game)), EPSILON);
    }

    private static ArmyClasse createArmyClasse(ArmyClassEnum armyClass, int size) {
        ArmyClasse armyClasse = new ArmyClasse();
        armyClasse.setPeriod(Period.PERIOD_V);
        armyClasse.setArmyClass(armyClass);
        armyClasse.setSize(size);
        return armyClasse;
    }

    @Test
    public void testLossesMitigation() {
        Assert.assertEquals(9, oeUtil.lossesMitigation(THIRD, true, null).getTotalThird());
        Assert.assertEquals(7, oeUtil.lossesMitigation(2 * THIRD, true, null).getTotalThird());
        Assert.assertEquals(6, oeUtil.lossesMitigation(1d, true, null).getTotalThird());
        Assert.assertEquals(5, oeUtil.lossesMitigation(1 + THIRD, true, null).getTotalThird());
        Assert.assertEquals(5, oeUtil.lossesMitigation(1.33333333, true, null).getTotalThird());
        Assert.assertEquals(5, oeUtil.lossesMitigation(1 + 2 * THIRD, true, null).getTotalThird());
        Assert.assertEquals(4, oeUtil.lossesMitigation(2d, true, null).getTotalThird());
        Assert.assertEquals(4, oeUtil.lossesMitigation(2 + THIRD, true, null).getTotalThird());
        Assert.assertEquals(4, oeUtil.lossesMitigation(2 + 2 * THIRD, true, null).getTotalThird());
        Assert.assertEquals(3, oeUtil.lossesMitigation(3d, true, null).getTotalThird());
        Assert.assertEquals(3, oeUtil.lossesMitigation(3 + THIRD, true, null).getTotalThird());
        Assert.assertEquals(3, oeUtil.lossesMitigation(3 + 2 * THIRD, true, null).getTotalThird());
        Assert.assertEquals(2, oeUtil.lossesMitigation(4d, true, null).getTotalThird());
        Assert.assertEquals(2, oeUtil.lossesMitigation(4 + THIRD, true, null).getTotalThird());
        Assert.assertEquals(2, oeUtil.lossesMitigation(5d, true, null).getTotalThird());
        Assert.assertEquals(2, oeUtil.lossesMitigation(5 + 2 * THIRD, true, null).getTotalThird());
        Assert.assertEquals(1, oeUtil.lossesMitigation(6d, true, null).getTotalThird());
        Assert.assertEquals(1, oeUtil.lossesMitigation(6 + 2 * THIRD, true, null).getTotalThird());
        Assert.assertEquals(1, oeUtil.lossesMitigation(7d, true, null).getTotalThird());
        Assert.assertEquals(1, oeUtil.lossesMitigation(7 + THIRD, true, null).getTotalThird());
        Assert.assertEquals(1, oeUtil.lossesMitigation(7d, true, () -> 2).getTotalThird());
        Assert.assertEquals(1, oeUtil.lossesMitigation(7 + THIRD, true, () -> 10).getTotalThird());
        Assert.assertEquals(0, oeUtil.lossesMitigation(7d, true, () -> 1).getTotalThird());
        Assert.assertEquals(0, oeUtil.lossesMitigation(7 + THIRD, true, () -> 9).getTotalThird());
        Assert.assertEquals(0, oeUtil.lossesMitigation(8d, true, null).getTotalThird());
    }

    @Test
    public void testRetreat() {
        Assert.assertEquals(0, oeUtil.retreat(-1).getTotalThird());
        Assert.assertEquals(0, oeUtil.retreat(0).getTotalThird());
        Assert.assertEquals(0, oeUtil.retreat(1).getTotalThird());
        Assert.assertEquals(0, oeUtil.retreat(2).getTotalThird());
        Assert.assertEquals(1, oeUtil.retreat(3).getTotalThird());
        Assert.assertEquals(1, oeUtil.retreat(4).getTotalThird());
        Assert.assertEquals(2, oeUtil.retreat(5).getTotalThird());
        Assert.assertEquals(2, oeUtil.retreat(6).getTotalThird());
        Assert.assertEquals(3, oeUtil.retreat(7).getTotalThird());
        Assert.assertEquals(3, oeUtil.retreat(8).getTotalThird());
        Assert.assertEquals(3, oeUtil.retreat(9).getTotalThird());
        Assert.assertEquals(3, oeUtil.retreat(10).getTotalThird());
        Assert.assertEquals(3, oeUtil.retreat(11).getTotalThird());
    }

    @Test
    public void testWarFromBattle() {
        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("spain", false)
                .whenSearchWar(oeUtil)
                .thenExpect(1L, true);

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("spain", true)
                .whenSearchWar(oeUtil)
                .thenExpectNothing();

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", false)
                .addCounter("spain", true)
                .whenSearchWar(oeUtil)
                .thenExpect(1L, false);

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .addCountry("austria", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("spain", false)
                .whenSearchWar(oeUtil)
                .thenExpect(1L, true);

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .addCountry("austria", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("spain", false)
                .addCounter("austria", false)
                .whenSearchWar(oeUtil)
                .thenExpect(1L, true);

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("spain", false)
                .addCounter("austria", false)
                .whenSearchWar(oeUtil)
                .thenExpectNothing();

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .addCountry("austria", false, WarImplicationEnum.FULL)
                        .toWar())
                .addWar(WarBuilder.create().id(2L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("poland", true, WarImplicationEnum.FULL)
                        .addCountry("spain", true, WarImplicationEnum.FULL)
                        .addCountry("turkey", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("turkey", false)
                .whenSearchWar(oeUtil)
                .thenExpectNothing();

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .addCountry("austria", false, WarImplicationEnum.FULL)
                        .toWar())
                .addWar(WarBuilder.create().id(2L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("poland", true, WarImplicationEnum.FULL)
                        .addCountry("spain", true, WarImplicationEnum.FULL)
                        .addCountry("turkey", false, WarImplicationEnum.FULL)
                        .toWar())
                .addWar(WarBuilder.create().id(3L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("turkey", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("france", true)
                .addCounter("turkey", false)
                .whenSearchWar(oeUtil)
                .thenExpect(3L, true);

        SearchWarBuilder.create()
                .addWar(WarBuilder.create().id(1L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("spain", false, WarImplicationEnum.FULL)
                        .addCountry("austria", false, WarImplicationEnum.FULL)
                        .toWar())
                .addWar(WarBuilder.create().id(2L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("poland", true, WarImplicationEnum.FULL)
                        .addCountry("spain", true, WarImplicationEnum.FULL)
                        .addCountry("turkey", false, WarImplicationEnum.FULL)
                        .toWar())
                .addWar(WarBuilder.create().id(3L).type(WarTypeEnum.CLASSIC_WAR)
                        .addCountry("france", true, WarImplicationEnum.FULL)
                        .addCountry("turkey", false, WarImplicationEnum.FULL)
                        .toWar())
                .addCounter("turkey", true)
                .addCounter("france", false)
                .whenSearchWar(oeUtil)
                .thenExpect(3L, false);
    }

    private static class SearchWarBuilder {
        List<WarEntity> wars = new ArrayList<>();
        List<CounterEntity> phasingCounters = new ArrayList<>();
        List<CounterEntity> nonPhasingCounters = new ArrayList<>();
        Pair<WarEntity, Boolean> war;

        static SearchWarBuilder create() {
            return new SearchWarBuilder();
        }

        SearchWarBuilder addWar(WarEntity war) {
            wars.add(war);
            return this;
        }

        SearchWarBuilder addCounter(String country, boolean phasing) {
            CounterEntity counter = new CounterEntity();
            counter.setCountry(country);
            if (phasing) {
                phasingCounters.add(counter);
            } else {
                nonPhasingCounters.add(counter);
            }
            return this;
        }

        SearchWarBuilder whenSearchWar(IOEUtil oeUtil) {
            GameEntity game = new GameEntity();
            game.getWars().addAll(wars);
            war = oeUtil.searchWar(phasingCounters, nonPhasingCounters, game);
            return this;
        }

        SearchWarBuilder thenExpect(Long id, boolean phasingOffensive) {
            if (id == null) {
                Assert.assertNull("No war was fitting this battle, but a war was found.", war);
            } else {
                Assert.assertNotNull("A war was fitting this battle, but was not found.", war);
                Assert.assertEquals("The wrong war was retrieved.", id, war.getLeft().getId());
                Assert.assertEquals("The wrong role (phasing = offensive) was retrieved.", phasingOffensive, war.getRight());
            }
            return this;
        }

        SearchWarBuilder thenExpectNothing() {
            return thenExpect(null, true);
        }
    }

    private static class WarBuilder<T extends WarBuilder> {
        Long id;
        WarTypeEnum type;
        List<CountryInWarEntity> countries = new ArrayList<>();

        static WarBuilder create() {
            return new WarBuilder();
        }

        T id(Long id) {
            this.id = id;
            return (T) this;
        }

        T type(WarTypeEnum type) {
            this.type = type;
            return (T) this;
        }

        T addCountry(String country, boolean offensive, WarImplicationEnum implication) {
            CountryInWarEntity countryInWarEntity = new CountryInWarEntity();
            countryInWarEntity.setCountry(new CountryEntity());
            countryInWarEntity.getCountry().setName(country);
            countryInWarEntity.setOffensive(offensive);
            countryInWarEntity.setImplication(implication);
            countries.add(countryInWarEntity);
            return (T) this;
        }

        WarEntity toWar() {
            WarEntity war = new WarEntity();
            war.setId(id);
            war.setType(type);
            war.getCountries().addAll(countries);
            war.getCountries().forEach(country -> country.setWar(war));
            return war;
        }
    }

    @Test
    public void isWarAlly() {
        WarAllyBuilder.create()
                .country("france").isOffensive()
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(true);
        WarAllyBuilder.create()
                .country("spain").isOffensive()
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(false);
        WarAllyBuilder.create()
                .country("austria").isOffensive()
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(false);
        WarAllyBuilder.create()
                .country("turkey").isOffensive()
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(false);
        WarAllyBuilder.create()
                .country("france")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(false);
        WarAllyBuilder.create()
                .country("spain")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(true);
        WarAllyBuilder.create()
                .country("austria")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(true);
        WarAllyBuilder.create()
                .country("turkey")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenIsWarAlly(oeUtil)
                .thenExpect(false);
    }

    @Test
    public void testGetWarAlliesEnemies() {
        WarAllyBuilder.create()
                .country("france")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenGetFaction(oeUtil, true)
                .thenExpect("france");
        WarAllyBuilder.create()
                .country("france")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenGetFaction(oeUtil, false)
                .thenExpect("spain", "austria");
        WarAllyBuilder.create()
                .country("spain")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenGetFaction(oeUtil, false)
                .thenExpect("austria", "spain");
        WarAllyBuilder.create()
                .country("spain")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenGetFaction(oeUtil, true)
                .thenExpect("france");

        WarAllyBuilder.create()
                .country("france")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("savoie", true, WarImplicationEnum.LIMITED)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenGetFaction(oeUtil, true)
                .thenExpect("france");
        WarAllyBuilder.create()
                .country("spain")
                .id(1L).type(WarTypeEnum.CLASSIC_WAR)
                .addCountry("france", true, WarImplicationEnum.FULL)
                .addCountry("savoie", true, WarImplicationEnum.LIMITED)
                .addCountry("spain", false, WarImplicationEnum.FULL)
                .addCountry("austria", false, WarImplicationEnum.FULL)
                .whenGetFaction(oeUtil, true)
                .thenExpect("france");
    }

    private static class WarAllyBuilder extends WarBuilder<WarAllyBuilder> {
        String country;
        boolean offensive;
        boolean ally;
        List<String> countries;

        static WarAllyBuilder create() {
            return new WarAllyBuilder();
        }

        WarAllyBuilder country(String country) {
            this.country = country;
            return this;
        }

        WarAllyBuilder isOffensive() {
            this.offensive = true;
            return this;
        }

        WarAllyBuilder whenIsWarAlly(IOEUtil oeUtil) {
            PlayableCountryEntity country = new PlayableCountryEntity();
            country.setName(this.country);
            ally = oeUtil.isWarAlly(country, toWar(), offensive);
            return this;
        }

        WarAllyBuilder thenExpect(boolean ally) {
            Assert.assertEquals(ally, this.ally);
            return this;
        }

        WarAllyBuilder whenGetFaction(IOEUtil ioeUtil, boolean offensive) {
            countries = ioeUtil.getWarFaction(toWar(), offensive);
            return this;
        }

        WarAllyBuilder thenExpect(String... expectedCountries) {
            Assert.assertEquals("Size between expected countries and retrieved ones does not match.", expectedCountries.length, countries.size());
            for (String expectedCountry : expectedCountries) {
                Assert.assertTrue("The country " + expectedCountry + " should have been retrieved but was not.", countries.contains(expectedCountry));
            }
            return this;
        }
    }

    @Test
    public void testProsperity() {
        ProsperityBuilder.create()
                .antePenultGrossIncome(100).previousGrossIncome(110)
                .whenProsperity(oeUtil)
                .thenExpect(0);
        ProsperityBuilder.create()
                .antePenultGrossIncome(100).grossIncome(120)
                .whenProsperity(oeUtil)
                .thenExpect(0);
        ProsperityBuilder.create()
                .previousGrossIncome(110).grossIncome(120)
                .whenProsperity(oeUtil)
                .thenExpect(0);

        ProsperityBuilder.create()
                .antePenultGrossIncome(100).previousGrossIncome(110).grossIncome(120)
                .whenProsperity(oeUtil)
                .thenExpect(1);
        ProsperityBuilder.create()
                .antePenultGrossIncome(100).previousGrossIncome(100).grossIncome(101)
                .whenProsperity(oeUtil)
                .thenExpect(1);
        ProsperityBuilder.create()
                .antePenultGrossIncome(100).previousGrossIncome(110).grossIncome(109)
                .whenProsperity(oeUtil)
                .thenExpect(0);
        ProsperityBuilder.create()
                .antePenultGrossIncome(100).previousGrossIncome(99).grossIncome(98)
                .whenProsperity(oeUtil)
                .thenExpect(-1);
        ProsperityBuilder.create()
                .antePenultGrossIncome(100).previousGrossIncome(99).grossIncome(99)
                .whenProsperity(oeUtil)
                .thenExpect(0);
    }

    static class ProsperityBuilder {
        Integer grossIncome;
        Integer previousGrossIncome;
        Integer antePenultGrossIncome;
        int prosperity;

        static ProsperityBuilder create() {
            return new ProsperityBuilder();
        }

        ProsperityBuilder grossIncome(Integer grossIncome) {
            this.grossIncome = grossIncome;
            return this;
        }

        ProsperityBuilder previousGrossIncome(Integer previousGrossIncome) {
            this.previousGrossIncome = previousGrossIncome;
            return this;
        }

        ProsperityBuilder antePenultGrossIncome(Integer antePenultGrossIncome) {
            this.antePenultGrossIncome = antePenultGrossIncome;
            return this;
        }

        ProsperityBuilder whenProsperity(IOEUtil oeUtil) {
            GameEntity game = new GameEntity();
            game.setTurn(10);

            PlayableCountryEntity country = new PlayableCountryEntity();
            EconomicalSheetEntity sheet = new EconomicalSheetEntity();
            sheet.setTurn(10);
            sheet.setGrossIncome(grossIncome);
            country.getEconomicalSheets().add(sheet);
            sheet = new EconomicalSheetEntity();
            sheet.setTurn(9);
            sheet.setGrossIncome(previousGrossIncome);
            country.getEconomicalSheets().add(sheet);
            sheet = new EconomicalSheetEntity();
            sheet.setTurn(8);
            sheet.setGrossIncome(antePenultGrossIncome);
            country.getEconomicalSheets().add(sheet);

            prosperity = oeUtil.getProsperity(country, game);

            return this;
        }

        ProsperityBuilder thenExpect(int expected) {
            Assert.assertEquals(expected, prosperity);
            return this;
        }
    }

    @Test
    public void testGetInflationBox() {
        GameEntity game = new GameEntity();
        StackEntity stack = new StackEntity();
        stack.setProvince("idf");
        stack.getCounters().add(AbstractGameServiceTest.createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stack));
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("B_PB_2D");
        stack.getCounters().add(AbstractGameServiceTest.createCounter(1L, "null", CounterFaceTypeEnum.INFLATION, stack));
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("pecs");
        stack.getCounters().add(AbstractGameServiceTest.createCounter(1L, "france", CounterFaceTypeEnum.CONTROL, stack));
        game.getStacks().add(stack);

        Assert.assertEquals("B_PB_2D", oeUtil.getInflationBox(game));
    }

    @Test
    public void testGetMinimalInflation() {
        Tables tables = new Tables();
        Period periodI = new Period();
        periodI.setName("I");
        periodI.setBegin(1);
        periodI.setEnd(6);
        tables.getPeriods().add(periodI);
        Period periodII = new Period();
        periodII.setName("II");
        periodII.setBegin(7);
        periodII.setEnd(14);
        tables.getPeriods().add(periodII);
        Period periodIII = new Period();
        periodIII.setName("III");
        periodIII.setBegin(15);
        periodIII.setEnd(25);
        tables.getPeriods().add(periodIII);
        Period periodIV = new Period();
        periodIV.setName("IV");
        periodIV.setBegin(26);
        periodIV.setEnd(34);
        tables.getPeriods().add(periodIV);
        Period periodV = new Period();
        periodV.setName("V");
        periodV.setBegin(35);
        periodV.setEnd(42);
        tables.getPeriods().add(periodV);
        Period periodVI = new Period();
        periodVI.setName("VI");
        periodVI.setBegin(43);
        periodVI.setEnd(52);
        tables.getPeriods().add(periodVI);
        Period periodVII = new Period();
        periodVII.setName("VII");
        periodVII.setBegin(53);
        periodVII.setEnd(62);
        tables.getPeriods().add(periodVII);

        GameEntity game = new GameEntity();
        game.setTurn(1);

        Assert.assertEquals(5, oeUtil.getMinimalInflation(5, PlayableCountry.SPAIN, tables, game));
        Assert.assertEquals(3, oeUtil.getMinimalInflation(5, PlayableCountry.POLAND, tables, game));
        Assert.assertEquals(3, oeUtil.getMinimalInflation(5, PlayableCountry.RUSSIA, tables, game));
        Assert.assertEquals(5, oeUtil.getMinimalInflation(5, PlayableCountry.SWEDEN, tables, game));

        game.setTurn(25);

        Assert.assertEquals(10, oeUtil.getMinimalInflation(10, PlayableCountry.SPAIN, tables, game));
        Assert.assertEquals(5, oeUtil.getMinimalInflation(10, PlayableCountry.POLAND, tables, game));
        Assert.assertEquals(5, oeUtil.getMinimalInflation(10, PlayableCountry.RUSSIA, tables, game));
        Assert.assertEquals(5, oeUtil.getMinimalInflation(10, PlayableCountry.SWEDEN, tables, game));

        game.setTurn(42);

        Assert.assertEquals(15, oeUtil.getMinimalInflation(15, PlayableCountry.SPAIN, tables, game));
        Assert.assertEquals(8, oeUtil.getMinimalInflation(15, PlayableCountry.POLAND, tables, game));
        Assert.assertEquals(8, oeUtil.getMinimalInflation(15, PlayableCountry.RUSSIA, tables, game));
        Assert.assertEquals(8, oeUtil.getMinimalInflation(15, PlayableCountry.SWEDEN, tables, game));

        game.setTurn(43);

        Assert.assertEquals(15, oeUtil.getMinimalInflation(15, PlayableCountry.SPAIN, tables, game));
        Assert.assertEquals(8, oeUtil.getMinimalInflation(15, PlayableCountry.POLAND, tables, game));
        Assert.assertEquals(15, oeUtil.getMinimalInflation(15, PlayableCountry.RUSSIA, tables, game));
        Assert.assertEquals(15, oeUtil.getMinimalInflation(15, PlayableCountry.SWEDEN, tables, game));
    }

    @Test
    public void testGetStackController() {
        StackControllerBuilder.create()
                .whenGetController(oeUtil)
                .thenExpect(null);
        StackControllerBuilder.create()
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .whenGetController(oeUtil)
                .thenExpect("france");
        StackControllerBuilder.create()
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("france");
        StackControllerBuilder.create().controller("france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("france");
        StackControllerBuilder.create().controller("espagne")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("espagne");
        StackControllerBuilder.create().controller("france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("espagne");
        StackControllerBuilder.create().controller("espagne")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("france");
        StackControllerBuilder.create().controller("france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("france");
        StackControllerBuilder.create().controller("espagne")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "france")
                .addCounter(CounterFaceTypeEnum.ARMY_MINUS, "espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "espagne")
                .whenGetController(oeUtil)
                .thenExpect("espagne");
        StackControllerBuilder.create().controller("france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "angleterre")
                .whenGetController(oeUtil)
                .thenExpect("france");
        StackControllerBuilder.create().controller("espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "angleterre")
                .whenGetController(oeUtil)
                .thenExpect("espagne");
        StackControllerBuilder.create().controller("angleterre")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "france")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "espagne")
                .addCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, "angleterre")
                .whenGetController(oeUtil)
                .thenExpect("angleterre");
    }

    static class StackControllerBuilder {
        List<CounterEntity> counters = new ArrayList<>();
        String controller;
        String result;

        static StackControllerBuilder create() {
            return new StackControllerBuilder();
        }

        StackControllerBuilder addCounter(CounterFaceTypeEnum face, String country) {
            CounterEntity counterEntity = new CounterEntity();
            counterEntity.setType(face);
            counterEntity.setCountry(country);
            counters.add(counterEntity);
            return this;
        }

        StackControllerBuilder controller(String controller) {
            this.controller = controller;
            return this;
        }

        StackControllerBuilder whenGetController(IOEUtil oeUtil) {
            StackEntity stack = new StackEntity();
            stack.setCountry(controller);
            stack.getCounters().addAll(counters);
            counters.forEach(counter -> counter.setOwner(stack));

            result = oeUtil.getController(stack);

            return this;
        }

        StackControllerBuilder thenExpect(String expected) {
            Assert.assertEquals("Wrong controller returned.", expected, result);

            return this;
        }
    }

    @Test
    public void testGetStackLeader() {
        StacksBuilder.create()
                .addStack().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeader(null);
        StacksBuilder.create()
                .addStack()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeader(null);
        StacksBuilder.create()
                .addStack()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Natto").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeader(null);
        StacksBuilder.create()
                .addStack().leader("Edward")
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Natto").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.LEADER).leader("Edward").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeader(null);
        StacksBuilder.create()
                .addStack().leader("Edward")
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Natto").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Tourette").rank("C").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.LEADER).leader("Edward").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeader("Tourette");
        StacksBuilder.create()
                .addStack().leader("Edward")
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Natto").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Tourette").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.LEADER).leader("Edward").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeader("Edward");
        StacksBuilder.create()
                .addStack()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Natto").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Tourette").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.LEADER).leader("Edward").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL && StringUtils.equals(leader.getCountry(), "angleterre"))
                .thenExpectLeader("Edward");
        StacksBuilder.create()
                .addStack()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Natto").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Tourette").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.LEADER).leader("Edward").rank("B").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("angleterre").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeader(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL && StringUtils.equals(leader.getCountry(), "france"))
                .thenExpectLeader("Tourette");
    }

    @Test
    public void testLeadingCountries() {
        StacksBuilder.create()
                .addStack().country("france").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders();

        StacksBuilder.create()
                .addStack().country("france").leader("Napo").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Napo").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Napo");

        StacksBuilder.create()
                .addStack().country("france").leader("Napo").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Napo").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .addStack().country("france").leader("Turennes").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Turennes").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Napo", "Turennes");

        StacksBuilder.create()
                .addStack().country("france").leader("Napo").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Napo").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .addStack().country("espagne").leader("Draco").addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("espagne").face(CounterFaceTypeEnum.LEADER).leader("Draco").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france", "espagne")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Napo", "Draco");

        StacksBuilder.create()
                .addStack().country("france").leader("Napo").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LAND_DETACHMENT).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Napo").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .addStack().country("espagne").leader("Draco").addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("espagne").face(CounterFaceTypeEnum.LEADER).leader("Draco").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Napo");

        StacksBuilder.create()
                .addStack().country("france").leader("Napo").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Napo").rank("A").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("espagne").face(CounterFaceTypeEnum.LEADER).leader("Draco").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Napo");

        StacksBuilder.create()
                .addStack().country("france").leader("Napo").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Napo").rank("A").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .addStack().country("france").leader("Nabo").addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LEADER).leader("Nabo").rank("Z").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Napo");

        StacksBuilder.create()
                .addStack().country("france").leader("Pacha1").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("turquie").face(CounterFaceTypeEnum.PACHA_1).leader("Pacha1").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .addStack().country("espagne").addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_MINUS).toParent()
                .addCounter().country("turquie").face(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toParent()
                .addCounter().country("turquie").face(CounterFaceTypeEnum.PACHA_3).leader("Pacha3").rank("A").type(LeaderTypeEnum.ADMIRAL).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("turquie")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Pacha1");

        StacksBuilder.create()
                .addStack().country("france").leader("Pacha1").addCounter().country("france").face(CounterFaceTypeEnum.ARMY_PLUS).toParent()
                .addCounter().country("france").face(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toParent()
                .addCounter().country("turquie").face(CounterFaceTypeEnum.PACHA_1).leader("Pacha1").rank("A").type(LeaderTypeEnum.GENERAL).toParent()
                .addCounter().country("turquie").face(CounterFaceTypeEnum.PACHA_3).leader("Pacha3").rank("A").type(LeaderTypeEnum.GENERAL).toParent().toParent()
                .addStack().country("espagne").addCounter().country("espagne").face(CounterFaceTypeEnum.ARMY_PLUS).toParent()
                .addCounter().country("turquie").face(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toParent().toParent()
                .whenGetLeadingCountry(oeUtil)
                .thenExpectLeadingCountry("turquie", "france")
                .whenGetLeaders(oeUtil, leader -> leader.getType() == LeaderTypeEnum.GENERAL)
                .thenExpectLeaders("Pacha1");
    }

    static class StacksBuilder {
        List<StackBuilder> stacks = new ArrayList<>();
        List<String> leadingCountries;
        List<Leader> leaders;
        String leader;

        static StacksBuilder create() {
            return new StacksBuilder();
        }

        StackBuilder addStack() {
            StackBuilder stack = new StackBuilder(this);
            stacks.add(stack);
            return stack;
        }

        StacksBuilder whenGetLeadingCountry(IOEUtil oeUtil) {
            List<StackEntity> entities = stacks.stream().map(StackBuilder::toEntity).collect(Collectors.toList());
            List<CounterEntity> counters = entities.stream().flatMap(s -> s.getCounters().stream()).collect(Collectors.toList());

            leadingCountries = oeUtil.getLeadingCountries(counters);

            return this;
        }

        StacksBuilder whenGetLeaders(IOEUtil oeUtil, Predicate<Leader> conditions) {
            List<StackEntity> entities = stacks.stream().map(StackBuilder::toEntity).collect(Collectors.toList());
            List<CounterEntity> counters = entities.stream().flatMap(s -> s.getCounters().stream()).collect(Collectors.toList());
            Tables tables = new Tables();
            tables.getLeaders().addAll(stacks.stream().flatMap(s -> s.counters.stream()).map(CounterBuilder::toLeader).filter(leader -> leader != null).collect(Collectors.toList()));

            leaders = oeUtil.getLeaders(counters, tables, conditions);

            return this;
        }

        StacksBuilder whenGetLeader(IOEUtil oeUtil, Predicate<Leader> conditions) {
            List<StackEntity> entities = stacks.stream().map(StackBuilder::toEntity).collect(Collectors.toList());
            Tables tables = new Tables();
            tables.getLeaders().addAll(stacks.stream().flatMap(s -> s.counters.stream()).map(CounterBuilder::toLeader).filter(leader -> leader != null).collect(Collectors.toList()));

            leader = oeUtil.getLeader(entities.get(0), tables, conditions);

            return this;
        }

        StacksBuilder thenExpectLeadingCountry(String... countries) {
            if (countries == null) {
                Assert.assertEquals("Number of different eligible leading countries mismatch.", 0, leadingCountries.size());
            } else {
                Assert.assertEquals("Number of different eligible leading countries mismatch.", countries.length, leadingCountries.size());
                for (String country : countries) {
                    Assert.assertTrue("Country was supposed to be an eligible leading country but was not.", leadingCountries.contains(country));
                }
            }

            return this;
        }

        StacksBuilder thenExpectLeaders(String... leaders) {
            if (leaders == null) {
                Assert.assertEquals("Number of different eligible leaders mismatch.", 0, leadingCountries.size());
            } else {
                Assert.assertEquals("Number of different eligible leaders mismatch.", leaders.length, this.leaders.size());
                for (String leader : leaders) {
                    Assert.assertTrue("Leaders was supposed to be an eligible leaders but was not.", this.leaders.stream().anyMatch(l -> StringUtils.equals(l.getCode(), leader)));
                }
            }

            return this;
        }

        StacksBuilder thenExpectLeader(String leader) {
            Assert.assertEquals("Leader was supposed to control the stack but was not.", leader, this.leader);

            return this;
        }

        class StackBuilder {
            StacksBuilder parent;
            String country;
            String leader;
            List<CounterBuilder> counters = new ArrayList<>();

            StackBuilder(StacksBuilder parent) {
                this.parent = parent;
            }

            StackBuilder country(String country) {
                this.country = country;
                return this;
            }

            StackBuilder leader(String leader) {
                this.leader = leader;
                return this;
            }

            CounterBuilder addCounter() {
                CounterBuilder counter = new CounterBuilder(this);
                counters.add(counter);
                return counter;
            }

            StacksBuilder toParent() {
                return parent;
            }

            StackEntity toEntity() {
                StackEntity stack = new StackEntity();
                stack.setCountry(country);
                stack.setLeader(leader);
                stack.getCounters().addAll(counters.stream().map(CounterBuilder::toEntity).collect(Collectors.toList()));
                stack.getCounters().forEach(counter -> counter.setOwner(stack));
                return stack;
            }
        }

        class CounterBuilder {
            StackBuilder parent;
            CounterFaceTypeEnum face;
            String country;
            String leader;
            String rank;
            LeaderTypeEnum type;

            CounterBuilder(StackBuilder parent) {
                this.parent = parent;
            }

            CounterBuilder face(CounterFaceTypeEnum face) {
                this.face = face;
                return this;
            }

            CounterBuilder country(String country) {
                this.country = country;
                return this;
            }

            CounterBuilder leader(String leader) {
                this.leader = leader;
                return this;
            }

            CounterBuilder rank(String rank) {
                this.rank = rank;
                return this;
            }

            CounterBuilder type(LeaderTypeEnum type) {
                this.type = type;
                return this;
            }

            StackBuilder toParent() {
                return parent;
            }

            CounterEntity toEntity() {
                CounterEntity counter = new CounterEntity();
                counter.setType(face);
                counter.setCountry(country);
                counter.setCode(leader);
                return counter;
            }

            Leader toLeader() {
                if (StringUtils.isEmpty(this.leader)) {
                    return null;
                }
                Leader leader = new Leader();
                leader.setCode(this.leader);
                leader.setCountry(country);
                leader.setType(type);
                leader.setRank(rank);
                return leader;
            }
        }
    }
}
