package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.military.ChooseManForSiegeRequest;
import com.mkl.eu.client.service.service.military.ChooseModeForSiegeRequest;
import com.mkl.eu.client.service.service.military.ChooseProvinceRequest;
import com.mkl.eu.client.service.service.military.SelectForcesRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.ArtillerySiege;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeCounterEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.AbstractGameServiceTest;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Test of SiegeService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class SiegeServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private SiegeServiceImpl siegeService;

    @Mock
    private ICounterDomain counterDomain;

    @Mock
    private IStatusWorkflowDomain workflowDomain;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private IOEUtil oeUtil;

    @Before
    public void init() {
        SiegeServiceImpl.TABLES = new Tables();
    }

    @Test
    public void testChooseSiegeFail() {
        Pair<Request<ChooseProvinceRequest>, GameEntity> pair = testCheckGame(siegeService::chooseSiege, "chooseSiege");
        Request<ChooseProvinceRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(0).setStatus(SiegeStatusEnum.SELECT_FORCES);
        game.getSieges().get(0).setProvince("pecs");
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(1).setStatus(SiegeStatusEnum.NEW);
        game.getSieges().get(1).setProvince("lyonnais");
        request.setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, siegeService::chooseSiege, "chooseSiege", GameStatusEnum.MILITARY_SIEGES);

        try {
            siegeService.chooseSiege(request);
            Assert.fail("Should break because chooseSiege.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseSiege.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseProvinceRequest());

        try {
            siegeService.chooseSiege(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseSiege.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("");

        try {
            siegeService.chooseSiege(request);
            Assert.fail("Should break because province is empty");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseSiege.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("idf");

        try {
            siegeService.chooseSiege(request);
            Assert.fail("Should break because another siege is in process");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_IN_PROCESS, e.getCode());
            Assert.assertEquals("chooseSiege", e.getParams()[0]);
        }

        game.getSieges().get(0).setStatus(SiegeStatusEnum.NEW);

        try {
            siegeService.chooseSiege(request);
            Assert.fail("Should break because no siege is in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("chooseSiege.request.province", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseSiegeToSelectForcesWithoutBesieged() throws FunctionalException {
        testChooseSiege(false, false);
    }

    @Test
    public void testChooseSiegeToSelectForcesWithBesieged() throws FunctionalException {
        testChooseSiege(false, true);
    }

    @Test
    public void testChooseSiegeToChooseModeWithoutBesieged() throws FunctionalException {
        testChooseSiege(true, false);
    }

    @Test
    public void testChooseSiegeToChooseModeWithBesieged() throws FunctionalException {
        testChooseSiege(true, true);
    }

    private void testChooseSiege(boolean gotoChooseMode, boolean withBesieged) throws FunctionalException {
        Pair<Request<ChooseProvinceRequest>, GameEntity> pair = testCheckGame(siegeService::chooseSiege, "chooseSiege");
        Request<ChooseProvinceRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(26L);
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(0).setId(33L);
        game.getSieges().get(0).setGame(game);
        game.getSieges().get(0).setStatus(SiegeStatusEnum.NEW);
        game.getSieges().get(0).setProvince("idf");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("idf");
        game.getStacks().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(1L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("idf");
        game.getStacks().get(1).setCountry("turquie");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(2L);
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(3L);
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(2).setId(4L);
        game.getStacks().get(1).getCounters().get(2).setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("idf");
        game.getStacks().get(2).setBesieged(withBesieged);
        game.getStacks().get(2).setCountry("espagne");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setId(5L);
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(3).setProvince("idf");
        game.getStacks().get(3).setCountry("angleterre");
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(0).setId(6L);
        game.getStacks().get(3).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(3).getCounters().add(new CounterEntity());
        game.getStacks().get(3).getCounters().get(1).setId(7L);
        game.getStacks().get(3).getCounters().get(1).setType(CounterFaceTypeEnum.LAND_DETACHMENT);


        request.setIdCountry(26L);
        request.setRequest(new ChooseProvinceRequest());
        request.getRequest().setProvince("idf");
        testCheckStatus(pair.getRight(), request, siegeService::chooseSiege, "chooseSiege", GameStatusEnum.MILITARY_SIEGES);
        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setTerrain(TerrainEnum.PLAIN);
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);

        List<String> allies = new ArrayList<>();
        allies.add("france");
        if (!gotoChooseMode) {
            allies.add("turquie");
        }
        when(oeUtil.getAllies(game.getCountries().get(0), game)).thenReturn(allies);

        simulateDiff();

        DiffResponse response = siegeService.chooseSiege(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.SIEGE, diffEntity.getTypeObject());
        Assert.assertEquals(game.getSieges().get(0).getId(), diffEntity.getIdObject());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(game.getId(), diffEntity.getIdGame());
        if (gotoChooseMode) {
            Assert.assertEquals(3 + (withBesieged ? 1 : 0), diffEntity.getAttributes().size());
            Assert.assertEquals(SiegeStatusEnum.CHOOSE_MODE.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
            Assert.assertEquals("1", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_COUNTER_ADD));
            Assert.assertEquals("0", getAttribute(diffEntity, DiffAttributeTypeEnum.LEVEL));
        } else {
            Assert.assertEquals(1 + (withBesieged ? 1 : 0), diffEntity.getAttributes().size());
            Assert.assertEquals(SiegeStatusEnum.SELECT_FORCES.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
        }
        if (withBesieged) {
            Assert.assertEquals("5", getAttribute(diffEntity, DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD));
            SiegeCounterEntity counterSpa = game.getSieges().get(0).getCounters().stream()
                    .filter(c -> c.getCounter().getId().equals(5L))
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(counterSpa);
        }

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());

        if (gotoChooseMode) {
            Assert.assertEquals(SiegeStatusEnum.CHOOSE_MODE, game.getSieges().get(0).getStatus());
            Assert.assertEquals(1 + (withBesieged ? 1 : 0), game.getSieges().get(0).getCounters().size());
            SiegeCounterEntity counterFra = game.getSieges().get(0).getCounters().stream()
                    .filter(c -> c.getCounter().getId().equals(1L))
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(counterFra);
        } else {
            Assert.assertEquals(SiegeStatusEnum.SELECT_FORCES, game.getSieges().get(0).getStatus());
        }
    }

    @Test
    public void testSelectForceFail() {
        Pair<Request<SelectForcesRequest>, GameEntity> pair = testCheckGame(siegeService::selectForces, "selectForces");
        Request<SelectForcesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(0).setStatus(SiegeStatusEnum.CHOOSE_MODE);
        game.getSieges().get(0).setProvince("pecs");
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(1).setStatus(SiegeStatusEnum.NEW);
        game.getSieges().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, siegeService::selectForces, "selectForces", GameStatusEnum.MILITARY_SIEGES);
        request.setIdCountry(12L);

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because selectForces.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request", e.getParams()[0]);
        }

        request.setRequest(new SelectForcesRequest());

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        request.getRequest().getForces().add(6L);

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because no siege is in right status");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_STATUS_NONE, e.getCode());
            Assert.assertEquals("selectForces", e.getParams()[0]);
        }

        game.getSieges().get(0).setStatus(SiegeStatusEnum.SELECT_FORCES);

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because the defender already validated its forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_SELECT_VALIDATED, e.getCode());
            Assert.assertEquals("selectForces", e.getParams()[0]);
        }

        CountryOrderEntity order = new CountryOrderEntity();
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setActive(true);
        order.setCountry(country);
        game.getOrders().add(order);

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because counter does not exist in the siege");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        when(oeUtil.getAllies(null, game)).thenReturn(Collections.singletonList("france"));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("pecs");
        game.getStacks().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(6L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MANOEUVRE);

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because counter is not an army");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(0).setCountry("pologne");

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because counter is not owned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        game.getStacks().get(0).setCountry("france");
        game.getStacks().get(0).setProvince("idf");

        try {
            siegeService.selectForces(request);
            Assert.fail("Should break because counter is not in the right province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }
    }

    @Test
    public void testSelectForcesSuccess() throws FunctionalException {
        Pair<Request<SelectForcesRequest>, GameEntity> pair = testCheckGame(siegeService::selectForces, "selectForces");
        Request<SelectForcesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(0).setGame(game);
        game.getSieges().get(0).setStatus(SiegeStatusEnum.SELECT_FORCES);
        game.getSieges().get(0).setProvince("pecs");
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(1).setStatus(SiegeStatusEnum.NEW);
        game.getSieges().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, siegeService::selectForces, "selectForces", GameStatusEnum.MILITARY_SIEGES);
        request.setIdCountry(12L);
        request.setRequest(new SelectForcesRequest());
        request.getRequest().getForces().add(6L);
        request.getRequest().getForces().add(7L);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("pecs");
        game.getStacks().get(0).setCountry(country.getName());
        CounterEntity counter = new CounterEntity();
        counter.setId(6L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.setCountry("france");
        game.getStacks().get(0).getCounters().add(counter);
        counter = new CounterEntity();
        counter.setId(7L);
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter.setCountry("france");
        game.getStacks().get(0).getCounters().add(counter);
        counter = new CounterEntity();
        counter.setId(8L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.setCountry("spain");
        game.getStacks().get(0).getCounters().add(counter);

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setTerrain(TerrainEnum.PLAIN);
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(oeUtil.getAllies(country, game)).thenReturn(Collections.singletonList(country.getName()));

        CountryOrderEntity order = new CountryOrderEntity();
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setActive(true);
        order.setCountry(country);
        game.getOrders().add(order);

        simulateDiff();

        DiffResponse response = siegeService.selectForces(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        SiegeEntity siege = game.getSieges().get(0);
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.SIEGE, diffEntity.getTypeObject());
        Assert.assertEquals(siege.getId(), diffEntity.getIdObject());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(game.getId(), diffEntity.getIdGame());
        Assert.assertTrue(diffEntity.getAttributes().size() >= 1);
        Assert.assertEquals(4, diffEntity.getAttributes().size());
        Assert.assertEquals(SiegeStatusEnum.CHOOSE_MODE.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
        Assert.assertEquals(2l, diffEntity.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_COUNTER_ADD)
                .count());
        Assert.assertEquals("0", getAttribute(diffEntity, DiffAttributeTypeEnum.LEVEL));

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testComputeBonus() {
        // Fortress alone : level of fortress is difficulty
        SiegeBonusBuilder.create().fortressLevel(2).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        // 1 canon is not enough to give a bonus
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(1).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        // 2 canons give a bonus of +1
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(2).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-1);

        // 4 canons give a bonus of +2
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(0);

        // 7 canons give a bonus of +3
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(7).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(1);

        // More than 7 canons does not change anything
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(1);

        // If it is breach: +2
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN).breach(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(3);

        // One siegework minus : +1
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN).breach(true)
                .addSiegeworkMinus()
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(4);

        // One siegework plus : +3
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN).breach(true)
                .addSiegeworkMinus().addSiegeworkPlus()
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(7);

        // a detachment besieged : +1
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN).breach(true)
                .addSiegeworkMinus().addSiegeworkPlus()
                .addDetachmentBesieged()
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(8);

        // another detachment besieged : no change
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN).breach(true)
                .addSiegeworkMinus().addSiegeworkPlus()
                .addDetachmentBesieged().addDetachmentBesieged()
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(8);

        // an army besieged : the +1 becomes +3
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(10).terrain(TerrainEnum.PLAIN).breach(true)
                .addSiegeworkMinus().addSiegeworkPlus()
                .addDetachmentBesieged().addDetachmentBesieged().addArmyBesieged()
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(10);

        // Now test terrain, lets come back to a simple base
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.PLAIN)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(0);

        // If not plain : -2
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.DENSE_FOREST)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        // but if not port : -2
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.PLAIN).port(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        // but if both : -3
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.DENSE_FOREST).port(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-3);

        // In rotw, -2 if port or not plain
        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.DENSE_FOREST).rotw(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.PLAIN).port(true).rotw(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        SiegeBonusBuilder.create().fortressLevel(2).artilleries(4).terrain(TerrainEnum.DENSE_FOREST).port(true).rotw(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(-2);

        // If fort, -1 if port or not plain
        SiegeBonusBuilder.create().fortressLevel(0).artilleries(4).terrain(TerrainEnum.DENSE_FOREST).rotw(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(+1);

        SiegeBonusBuilder.create().fortressLevel(0).artilleries(4).terrain(TerrainEnum.PLAIN).port(true).rotw(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(+1);

        SiegeBonusBuilder.create().fortressLevel(0).artilleries(4).terrain(TerrainEnum.DENSE_FOREST).port(true).rotw(true)
                .whenComputeBonus(siegeService, provinceDao, oeUtil)
                .thenExpect(+1);
    }

    private static class SiegeBonusBuilder {
        int fortressLevel;
        int artilleries;
        TerrainEnum terrain;
        boolean rotw;
        boolean port;
        boolean breach;
        List<CounterFaceTypeEnum> siegeworks = new ArrayList<>();
        List<CounterFaceTypeEnum> besieged = new ArrayList<>();
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        int bonus;

        static SiegeBonusBuilder create() {
            return new SiegeBonusBuilder();
        }

        SiegeBonusBuilder fortressLevel(int fortressLevel) {
            this.fortressLevel = fortressLevel;
            return this;
        }

        SiegeBonusBuilder artilleries(int artilleries) {
            this.artilleries = artilleries;
            return this;
        }

        SiegeBonusBuilder terrain(TerrainEnum terrain) {
            this.terrain = terrain;
            return this;
        }

        SiegeBonusBuilder rotw(boolean rotw) {
            this.rotw = rotw;
            return this;
        }

        SiegeBonusBuilder port(boolean port) {
            this.port = port;
            return this;
        }

        SiegeBonusBuilder breach(boolean breach) {
            this.breach = breach;
            return this;
        }

        SiegeBonusBuilder addSiegeworkPlus() {
            this.siegeworks.add(CounterFaceTypeEnum.SIEGEWORK_PLUS);
            return this;
        }

        SiegeBonusBuilder addSiegeworkMinus() {
            this.siegeworks.add(CounterFaceTypeEnum.SIEGEWORK_MINUS);
            return this;
        }

        SiegeBonusBuilder addArmyBesieged() {
            this.besieged.add(CounterFaceTypeEnum.ARMY_MINUS);
            return this;
        }

        SiegeBonusBuilder addDetachmentBesieged() {
            this.besieged.add(CounterFaceTypeEnum.LAND_DETACHMENT);
            return this;
        }

        SiegeBonusBuilder whenComputeBonus(SiegeServiceImpl siegeService, IProvinceDao provinceDao, IOEUtil oeUtil) {
            GameEntity game = new GameEntity();
            SiegeEntity siege = new SiegeEntity();
            siege.setGame(game);
            siege.setProvince("idf");
            siege.setBreach(breach);
            StackEntity stack = new StackEntity();
            game.getStacks().add(stack);
            stack.setProvince("idf");
            for (CounterFaceTypeEnum siegework : siegeworks) {
                CounterEntity counter = createCounter(null, null, siegework);
                stack.getCounters().add(counter);
            }
            for (CounterFaceTypeEnum defender : besieged) {
                SiegeCounterEntity siegeCounter = new SiegeCounterEntity();
                siegeCounter.setCounter(createCounter(null, null, defender));
                siege.getCounters().add(siegeCounter);
            }
            AbstractProvinceEntity idf;
            if (rotw) {
                idf = new RotwProvinceEntity();
            } else {
                idf = new EuropeanProvinceEntity();
            }
            idf.setTerrain(terrain);
            if (port) {
                SeaProvinceEntity sea = new SeaProvinceEntity();
                BorderEntity border = new BorderEntity();
                border.setProvinceFrom(idf);
                border.setProvinceTo(sea);
                idf.getBorders().add(border);
            }
            when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
            when(oeUtil.getFortressLevel(idf, game)).thenReturn(fortressLevel);
            when(oeUtil.getArtilleryBonus(any(), any(), any(), any())).thenReturn(artilleries);

            siegeService.computeSiegeBonus(siege, attributes);
            this.bonus = siege.getBonus();
            Tables tables = new Tables();
            tables.getArtillerySieges().add(createArtillerySiege(0, 4, 2));
            tables.getArtillerySieges().add(createArtillerySiege(2, 2, 1));
            tables.getArtillerySieges().add(createArtillerySiege(2, 4, 2));
            tables.getArtillerySieges().add(createArtillerySiege(2, 7, 3));
            SiegeServiceImpl.TABLES = tables;

            return this;
        }

        SiegeBonusBuilder thenExpect(int bonus) {
            Assert.assertEquals(bonus, this.bonus);
            DiffAttributesEntity attribute = attributes.stream()
                    .filter(attr -> attr.getType() == DiffAttributeTypeEnum.BONUS)
                    .findAny()
                    .orElse(null);
            if (bonus != 0) {
                Assert.assertNotNull(attribute);
                Assert.assertEquals(Integer.toString(bonus), attribute.getValue());
            } else {
                Assert.assertNull(attribute);
            }
            Assert.assertEquals(fortressLevel + "", attributes.stream()
                    .filter(attr -> attr.getType() == DiffAttributeTypeEnum.LEVEL)
                    .map(DiffAttributesEntity::getValue)
                    .findAny()
                    .orElse(null));

            return this;
        }
    }

    private static ArtillerySiege createArtillerySiege(int fortress, int artilleries, int bonus) {
        ArtillerySiege as = new ArtillerySiege();
        as.setFortress(fortress);
        as.setArtillery(artilleries);
        as.setBonus(bonus);
        return as;
    }

    @Test
    public void testChooseModeFail() {
        Pair<Request<ChooseModeForSiegeRequest>, GameEntity> pair = testCheckGame(siegeService::chooseMode, "chooseMode");
        Request<ChooseModeForSiegeRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        SiegeEntity siege = new SiegeEntity();
        siege.setFortressLevel(0);
        siege.setStatus(SiegeStatusEnum.SELECT_FORCES);
        siege.setProvince("pecs");
        game.getSieges().add(siege);
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(1).setStatus(SiegeStatusEnum.NEW);
        game.getSieges().get(1).setProvince("lyonnais");
        request.setIdCountry(12L);
        testCheckStatus(pair.getRight(), request, siegeService::chooseMode, "chooseMode", GameStatusEnum.MILITARY_SIEGES);

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because chooseMode.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseMode.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseModeForSiegeRequest());

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because mode is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseMode.request.mode", e.getParams()[0]);
        }

        request.getRequest().setMode(SiegeModeEnum.REDEPLOY);

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because provinceTo is mandatory if mode is REDEPLOY");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseMode.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("idf");

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because no siege is in right status");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_STATUS_NONE, e.getCode());
            Assert.assertEquals("chooseMode", e.getParams()[0]);
        }

        game.getSieges().get(0).setStatus(SiegeStatusEnum.CHOOSE_MODE);
        when(provinceDao.getProvinceByName(anyString())).thenReturn(new EuropeanProvinceEntity());

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because redeploy is not possible in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_CANT_REDEPLOY, e.getCode());
            Assert.assertEquals("chooseMode.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setMode(SiegeModeEnum.UNDERMINE);
        siege.setFortressLevel(2);

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because undermine is not possible with so few forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_UNDERMINE_TOO_FEW, e.getCode());
            Assert.assertEquals("chooseMode.request.mode", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseManFail() {
        Pair<Request<ChooseManForSiegeRequest>, GameEntity> pair = testCheckGame(siegeService::chooseMan, "chooseMan");
        Request<ChooseManForSiegeRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        SiegeEntity siege = new SiegeEntity();
        siege.setFortressLevel(0);
        siege.setStatus(SiegeStatusEnum.SELECT_FORCES);
        siege.setProvince("pecs");
        game.getSieges().add(siege);
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(1).setStatus(SiegeStatusEnum.NEW);
        game.getSieges().get(1).setProvince("lyonnais");
        request.setIdCountry(12L);
        testCheckStatus(pair.getRight(), request, siegeService::chooseMan, "chooseMan", GameStatusEnum.MILITARY_SIEGES);

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because chooseMan.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseMan.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseManForSiegeRequest());

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because no siege is in right status");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_STATUS_NONE, e.getCode());
            Assert.assertEquals("chooseMan", e.getParams()[0]);
        }

        siege.setStatus(SiegeStatusEnum.CHOOSE_MAN);
        request.getRequest().setMan(true);

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because chooseMan.counter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseMan.request.counter", e.getParams()[0]);
        }

        request.getRequest().setIdCounter(12l);

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because chooseMan.counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("chooseMan.request.counter", e.getParams()[0]);
        }

        SiegeCounterEntity siegeCounter = new SiegeCounterEntity();
        siegeCounter.setCounter(createCounter(12l, "france", CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        siege.getCounters().add(siegeCounter);

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because chooseMan.counter belongs to other player");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("chooseMan.request.counter", e.getParams()[0]);
        }

        siegeCounter.setPhasing(true);

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because chooseMan.counter is too small to take a loss");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_TOO_BIG, e.getCode());
            Assert.assertEquals("chooseMan.request.counter", e.getParams()[0]);
        }

        siegeCounter.getCounter().setType(CounterFaceTypeEnum.ARMY_PLUS);
        SiegeCounterEntity otherSiegeCounter = new SiegeCounterEntity();
        otherSiegeCounter.setPhasing(true);
        otherSiegeCounter.setCounter(createCounter(13l, "france", CounterFaceTypeEnum.LAND_DETACHMENT));
        siege.getCounters().add(otherSiegeCounter);
        otherSiegeCounter = new SiegeCounterEntity();
        otherSiegeCounter.setPhasing(true);
        otherSiegeCounter.setCounter(createCounter(14l, "france", CounterFaceTypeEnum.LAND_DETACHMENT));
        siege.getCounters().add(otherSiegeCounter);

        try {
            siegeService.chooseMan(request);
            Assert.fail("Should break because chooseMan.counter will lead to a stack with more than 3 counters");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_TOO_BIG, e.getCode());
            Assert.assertEquals("chooseMan.request.counter", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseUndermineNoEffect() throws FunctionalException {
        SiegeUndermineBuilder.create()
                .bonus(-5).die(3)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE));
    }

    @Test
    public void testChooseUndermineSiegeworkMinus() throws FunctionalException {
        SiegeUndermineBuilder.create()
                .bonus(0).die(4)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_MINUS)
                        .addSiegework().newStackForSiegework());

        SiegeUndermineBuilder.create()
                .bonus(-5).die(10)
                .addSiegeworkPlus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_MINUS)
                        .addSiegework());

        SiegeUndermineBuilder.create()
                .bonus(5).die(1)
                .addSiegeworkMinus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_MINUS)
                        .switchSiegework());

        SiegeUndermineBuilder.create()
                .bonus(2).die(4)
                .addSiegeworkPlus().addSiegeworkMinus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_MINUS)
                        .switchSiegework());

        SiegeUndermineBuilder.create()
                .bonus(-2).die(6)
                .addSiegeworkPlus().addSiegeworkPlus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_MINUS));
    }

    @Test
    public void testChooseUndermineSiegeworkPlus() throws FunctionalException {
        SiegeUndermineBuilder.create()
                .bonus(0).die(7)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_PLUS)
                        .addSiegework().newStackForSiegework());

        SiegeUndermineBuilder.create()
                .bonus(-1).die(10)
                .addSiegeworkPlus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_PLUS)
                        .addSiegework());

        SiegeUndermineBuilder.create()
                .bonus(5).die(3)
                .addSiegeworkMinus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_PLUS)
                        .addSiegework());

        SiegeUndermineBuilder.create()
                .bonus(2).die(7)
                .addSiegeworkPlus().addSiegeworkMinus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_PLUS)
                        .switchSiegework());

        SiegeUndermineBuilder.create()
                .bonus(-2).die(9)
                .addSiegeworkPlus().addSiegeworkPlus()
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SIEGE_WORK_PLUS));
    }

    @Test
    public void testChooseUndermineBreach() throws FunctionalException {
        SiegeUndermineBuilder.create()
                .bonus(0).die(10)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.CHOOSE_BREACH));
    }

    @Test
    public void testChooseUndermineSurrender() throws FunctionalException {
        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .fortress(3)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.CHOOSE_MAN).result(SiegeUndermineResultEnum.SURRENDER));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .fortress(2).naturalFortress(1)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls());

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .naturalFortress(2)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .naturalFortress(1)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls());

        SiegeUndermineBuilder.create()
                .bonus(4).die(10)
                .naturalFortress(1).fortress(2)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls());

        SiegeUndermineBuilder.create()
                .bonus(6).die(10)
                .fortress(1)
                .owner(Camp.ALLY).controller(Camp.ENEMY)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .removeControl().fortressFalls());

        SiegeUndermineBuilder.create()
                .bonus(8).die(10)

                .owner(Camp.NEUTRAL).controller(Camp.ENEMY)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .removeControl().fortressFalls());

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .naturalFortress(1)
                .owner(Camp.ENEMY).controller(Camp.ENEMY)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .switchControl().fortressFalls());
    }

    @Test
    public void testChooseUndermineSurrenderManned() throws FunctionalException {
        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .naturalFortress(2).fortress(4).man(true)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_3).newFortressCountry(Camp.SELF));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .naturalFortress(2).fortress(4).man(false)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls());

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .naturalFortress(2).fortress(3).man(false)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .fortress(2).man(true)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .addControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1).newFortressCountry(Camp.SELF));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .owner(Camp.SELF).controller(Camp.ENEMY)
                .fortress(2).man(true)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .removeControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1).newFortressCountry(Camp.SELF));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .owner(Camp.ENEMY).controller(Camp.ENEMY)
                .fortress(2).man(true)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .switchControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1).newFortressCountry(Camp.SELF));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .owner(Camp.ALLY).controller(Camp.ENEMY)
                .fortress(2).man(true)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .removeControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1).newFortressCountry(Camp.ALLY));

        SiegeUndermineBuilder.create()
                .bonus(3).die(10)
                .owner(Camp.NEUTRAL).controller(Camp.ENEMY)
                .fortress(2).man(true)
                .whenChooseMode(siegeService, this)
                .thenExpect(SiegeUndermineResultBuilder.create()
                        .status(SiegeStatusEnum.DONE).result(SiegeUndermineResultEnum.SURRENDER)
                        .removeControl().fortressFalls().newFortressType(CounterFaceTypeEnum.FORTRESS_1).newFortressCountry(Camp.NEUTRAL));
    }

    private static class SiegeUndermineBuilder {
        private Camp owner;
        private Camp controller;
        private int naturalFortress;
        private Integer fortress;
        private int siegeworkMinus;
        private int siegeworkPlus;
        private int bonus;
        private int die;
        private Boolean man;
        private SiegeEntity siege;
        List<DiffEntity> diffs;
        List<DiffEntity> diffsMan;

        static SiegeUndermineBuilder create() {
            return new SiegeUndermineBuilder();
        }

        SiegeUndermineBuilder owner(Camp owner) {
            this.owner = owner;
            return this;
        }

        SiegeUndermineBuilder controller(Camp controller) {
            this.controller = controller;
            return this;
        }

        SiegeUndermineBuilder naturalFortress(int naturalFortress) {
            this.naturalFortress = naturalFortress;
            return this;
        }

        SiegeUndermineBuilder fortress(Integer fortress) {
            this.fortress = fortress;
            return this;
        }

        SiegeUndermineBuilder addSiegeworkMinus() {
            this.siegeworkMinus++;
            return this;
        }

        SiegeUndermineBuilder addSiegeworkPlus() {
            this.siegeworkPlus++;
            return this;
        }

        SiegeUndermineBuilder bonus(int bonus) {
            this.bonus = bonus;
            return this;
        }

        SiegeUndermineBuilder die(int die) {
            this.die = die;
            return this;
        }

        SiegeUndermineBuilder man(Boolean man) {
            this.man = man;
            return this;
        }

        SiegeUndermineBuilder whenChooseMode(SiegeServiceImpl siegeService, SiegeServiceTest testClass) throws FunctionalException {
            Pair<Request<ChooseModeForSiegeRequest>, GameEntity> pair = testClass.testCheckGame(siegeService::chooseMode, "chooseMode");
            Request<ChooseModeForSiegeRequest> request = pair.getLeft();
            GameEntity game = pair.getRight();
            PlayableCountryEntity self = new PlayableCountryEntity();
            self.setId(12L);
            self.setName(Camp.SELF.name);
            game.getCountries().add(self);
            PlayableCountryEntity ally = new PlayableCountryEntity();
            ally.setId(13L);
            ally.setName(Camp.ALLY.name);
            game.getCountries().add(ally);
            PlayableCountryEntity neutral = new PlayableCountryEntity();
            neutral.setId(14L);
            neutral.setName(Camp.NEUTRAL.name);
            game.getCountries().add(neutral);
            PlayableCountryEntity enemy = new PlayableCountryEntity();
            enemy.setId(15L);
            enemy.setName(Camp.ENEMY.name);
            game.getCountries().add(enemy);

            StackEntity stack = new StackEntity();
            stack.setId(99L);
            stack.setProvince("pecs");
            if (fortress != null) {
                CounterFaceTypeEnum fortressType = CounterUtil.getFortressesFromLevel(fortress, false);
                stack.getCounters().add(createCounter(101L, Camp.ENEMY.name, fortressType));
            }
            if (controller != null) {
                stack.getCounters().add(createCounter(102L, controller.name, CounterFaceTypeEnum.CONTROL));
            }
            for (int i = 0; i < siegeworkMinus; i++) {
                stack.getCounters().add(createCounter(110L + i, null, CounterFaceTypeEnum.SIEGEWORK_MINUS));
            }
            for (int i = 0; i < siegeworkPlus; i++) {
                stack.getCounters().add(createCounter(120L + i, null, CounterFaceTypeEnum.SIEGEWORK_PLUS));
            }
            if (!stack.getCounters().isEmpty()) {
                game.getStacks().add(stack);
            }

            siege = new SiegeEntity();
            siege.setGame(game);
            siege.setFortressLevel(fortress != null ? fortress : naturalFortress);
            siege.setStatus(SiegeStatusEnum.CHOOSE_MODE);
            siege.setProvince("pecs");
            siege.setBonus(bonus);
            SiegeCounterEntity siegeCounter = new SiegeCounterEntity();
            siegeCounter.setSiege(siege);
            siegeCounter.setPhasing(true);
            siegeCounter.setCounter(new CounterEntity());
            siegeCounter.getCounter().setId(12l);
            siegeCounter.getCounter().setCountry(Camp.SELF.name);
            siegeCounter.getCounter().setType(CounterFaceTypeEnum.ARMY_PLUS);
            siegeCounter.getCounter().setOwner(new StackEntity());
            siegeCounter.getCounter().getOwner().setId(2l);
            siege.getCounters().add(siegeCounter);
            siegeCounter = new SiegeCounterEntity();
            siegeCounter.setSiege(siege);
            siegeCounter.setPhasing(true);
            siegeCounter.setCounter(new CounterEntity());
            siegeCounter.getCounter().setId(13l);
            siegeCounter.getCounter().setCountry(Camp.SELF.name);
            siegeCounter.getCounter().setType(CounterFaceTypeEnum.LAND_DETACHMENT);
            siegeCounter.getCounter().setOwner(new StackEntity());
            siegeCounter.getCounter().getOwner().setId(2l);
            siege.getCounters().add(siegeCounter);
            game.getSieges().add(siege);

            if (owner == null) {
                owner = Camp.ENEMY;
            }
            if (controller == null) {
                controller = owner;
            }
            AbstractProvinceEntity province = new EuropeanProvinceEntity();
            when(testClass.provinceDao.getProvinceByName("pecs")).thenReturn(province);
            when(testClass.oeUtil.getNaturalFortressLevel(province, game)).thenReturn(naturalFortress);
            when(testClass.oeUtil.getOwner(province, game)).thenReturn(owner.name);
            when(testClass.oeUtil.getController(province, game)).thenReturn(controller.name);
            when(testClass.oeUtil.rollDie(game, self)).thenReturn(die);
            when(testClass.oeUtil.getAllies(self, game)).thenReturn(Arrays.asList(Camp.SELF.name, Camp.ALLY.name));
            when(testClass.oeUtil.getEnemies(self, game)).thenReturn(Collections.singletonList(Camp.ENEMY.name));

            when(testClass.counterDomain.createStack("pecs", null, game)).thenAnswer(invocationOnMock -> {
                StackEntity newStack = new StackEntity();
                newStack.setId(1099L);
                return newStack;
            });
            when(testClass.counterDomain.switchCounter(110l, CounterFaceTypeEnum.SIEGEWORK_PLUS, null, game))
                    .thenReturn(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER, 110L));
            when(testClass.counterDomain.createCounter(any(), any(), any(), any()))
                    .thenAnswer(invocationOnMock -> {
                        CounterFaceTypeEnum face = invocationOnMock.getArgumentAt(0, CounterFaceTypeEnum.class);
                        if (face != CounterFaceTypeEnum.SIEGEWORK_MINUS && face != CounterFaceTypeEnum.SIEGEWORK_PLUS) {
                            return DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER, 1060l,
                                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK, invocationOnMock.getArgumentAt(2, Long.class)));
                        }
                        return DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER, 1010L,
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STACK, invocationOnMock.getArgumentAt(2, Long.class)));
                    });

            when(testClass.counterDomain.removeCounter(anyLong(), any()))
                    .thenAnswer(invocationOnMock -> DiffUtil.createDiff(game, DiffTypeEnum.REMOVE, DiffTypeObjectEnum.COUNTER, invocationOnMock.getArgumentAt(0, Long.class)));
            when(testClass.counterDomain.changeCounterCountry(any(), anyString(), any()))
                    .thenReturn(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER, 102L));
            when(testClass.counterDomain.createCounter(any(), any(), any(), any(), any()))
                    .thenAnswer(invocationOnMock -> DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER, invocationOnMock.getArgumentAt(0, CounterFaceTypeEnum.class) == CounterFaceTypeEnum.CONTROL ? 1020L : 1030L,
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, invocationOnMock.getArgumentAt(1, String.class), invocationOnMock.getArgumentAt(1, String.class) != null),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, invocationOnMock.getArgumentAt(0, CounterFaceTypeEnum.class))));

            when(testClass.workflowDomain.endMilitaryPhase(game)).thenReturn(Collections.singletonList(DiffUtil.createDiff(game, DiffTypeEnum.VALIDATE, DiffTypeObjectEnum.TURN_ORDER, null)));

            request.setIdCountry(self.getId());
            request.setRequest(new ChooseModeForSiegeRequest());
            request.getRequest().setProvinceTo("pecs");
            request.getRequest().setMode(SiegeModeEnum.UNDERMINE);
            testClass.testCheckStatus(pair.getRight(), request, siegeService::chooseMode, "chooseMode", GameStatusEnum.MILITARY_SIEGES);

            testClass.simulateDiff();

            siegeService.chooseMode(request);

            diffs = testClass.retrieveDiffsCreated();

            if (man != null) {
                Request<ChooseManForSiegeRequest> manRequest = new Request<>();
                manRequest.setGame(new GameInfo());
                manRequest.getGame().setIdGame(game.getId());
                manRequest.getGame().setVersionGame(VERSION_SINCE);
                manRequest.setIdCountry(self.getId());
                manRequest.setRequest(new ChooseManForSiegeRequest(man, 12l));
                siegeService.chooseMan(manRequest);

                diffsMan = testClass.retrieveDiffsCreated();
            }

            return this;
        }

        SiegeUndermineBuilder thenExpect(SiegeUndermineResultBuilder result) {
            List<DiffEntity> diffs = this.diffs;
            if (diffsMan != null) {
                DiffEntity diffSiege = diffs.stream()
                        .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.SIEGE)
                        .findAny()
                        .orElse(null);
                Assert.assertNotNull(diffSiege);

                Assert.assertEquals(SiegeUndermineResultEnum.SURRENDER.name(), getAttribute(diffSiege, DiffAttributeTypeEnum.SIEGE_UNDERMINE_RESULT));
                Assert.assertEquals(SiegeStatusEnum.CHOOSE_MAN.name(), getAttribute(diffSiege, DiffAttributeTypeEnum.STATUS));
                Assert.assertEquals(die + "", getAttribute(diffSiege, DiffAttributeTypeEnum.SIEGE_UNDERMINE_DIE));
                diffs = diffsMan;
            }

            DiffEntity diffSiege = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.SIEGE)
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(diffSiege);

            if (man == null) {
                Assert.assertEquals(result.result != null ? result.result.name() : null, getAttribute(diffSiege, DiffAttributeTypeEnum.SIEGE_UNDERMINE_RESULT));
                Assert.assertEquals(die + "", getAttribute(diffSiege, DiffAttributeTypeEnum.SIEGE_UNDERMINE_DIE));
            }
            Assert.assertEquals(result.result, siege.getUndermineResult());
            Assert.assertEquals(result.status != null ? result.status.name() : null, getAttribute(diffSiege, DiffAttributeTypeEnum.STATUS));
            Assert.assertEquals(result.status, siege.getStatus());
            Assert.assertEquals(die, siege.getUndermineDie());
            DiffEntity switchSiegework = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 110L)
                    .findAny()
                    .orElse(null);
            if (result.switchSiegework) {
                Assert.assertNotNull("Siegework minus should have changed to siegework plus but was not", switchSiegework);
            } else {
                Assert.assertNull("Siegework minus should not have changed to siegework plus but was", switchSiegework);
            }
            DiffEntity addSiegework = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 1010L)
                    .findAny()
                    .orElse(null);
            if (result.addSiegework) {
                Assert.assertNotNull("A siegework should have been added but was not.", addSiegework);
                Long id = result.newStackForSiegework ? 1099L : 99L;
                Assert.assertEquals("The new fortress belongs to the wront stack.", id.toString(), getAttribute(addSiegework, DiffAttributeTypeEnum.STACK));
            } else {
                Assert.assertNull("A siegework should not have been added but was.", addSiegework);
            }
            long removedSiegeworks = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() >= 110L && d.getIdObject() < 130L)
                    .count();
            DiffEntity removeFortress = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 101L)
                    .findAny()
                    .orElse(null);
            if (result.fortressFalls) {
                Assert.assertEquals("All siegeworks should have been removed but there are still left.", siegeworkMinus + siegeworkPlus, removedSiegeworks);
                if (fortress != null) {
                    Assert.assertNotNull("The fortress should have been removed but was not.", removeFortress);
                } else {
                    Assert.assertNull("The fortress has been removed but there was no fortress to remove.", removeFortress);
                }
            } else {
                Assert.assertEquals("Some siegeworks have been removed and it was not expected.", 0, removedSiegeworks);
                Assert.assertNull("The fortress should not have been removed but was.", removeFortress);
            }
            DiffEntity removeControl = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 102L)
                    .findAny()
                    .orElse(null);
            if (result.removeControl) {
                Assert.assertNotNull("A control counter should have been removed but was not.", removeControl);
            } else {
                Assert.assertNull("A control counter should not have been removed but was.", removeControl);
            }
            DiffEntity switchControl = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 102L)
                    .findAny()
                    .orElse(null);
            if (result.switchControl) {
                Assert.assertNotNull("A control counter should have been switched to another country but was not.", switchControl);
            } else {
                Assert.assertNull("A control counter should not have been switched to another country but was.", switchControl);
            }
            DiffEntity addControl = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 1020L)
                    .findAny()
                    .orElse(null);
            if (result.addControl) {
                Assert.assertNotNull("A control counter should have been added but was not.", addControl);
            } else {
                Assert.assertNull("A control counter should not have been added but was.", addControl);
            }
            DiffEntity addFortress = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 1030L)
                    .findAny()
                    .orElse(null);
            if (result.newFortressType != null) {
                Assert.assertNotNull("A fortress counter should have been added but was not.", addFortress);
                Assert.assertEquals("The new fortress has not the rigth face type.", result.newFortressType.name(), getAttribute(addFortress, DiffAttributeTypeEnum.TYPE));
                Assert.assertEquals("The new fortress belongs to the wrong country.", result.newFortressCountry != null ? result.newFortressCountry.name : null, getAttribute(addFortress, DiffAttributeTypeEnum.COUNTRY));
            } else {
                Assert.assertNull("A fortress counter should not have been added but was.", addFortress);
            }
            DiffEntity endSiege = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.VALIDATE && d.getTypeObject() == DiffTypeObjectEnum.TURN_ORDER)
                    .findAny()
                    .orElse(null);
            if (result.status == SiegeStatusEnum.DONE) {
                Assert.assertNotNull("The endSiege diff event has not been received while it should.", endSiege);
            } else {
                Assert.assertNull("The endSiege diff event has been received while it should not.", endSiege);
            }
            long counterAdded = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 1060l)
                    .count();
            DiffEntity removeArmy = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && d.getIdObject() == 12l)
                    .findAny()
                    .orElse(null);
            if (man != null && man) {
                Assert.assertEquals("2 counters should have been added because of manning the fortress but was not.", 2l, counterAdded);
                Assert.assertNotNull("The army counter should have been removed because of manning the fortress but was not.", removeArmy);
            } else {
                Assert.assertEquals("0 counter should have been added because of not manning the fortress but was not.", 0l, counterAdded);
                Assert.assertNull("The army counter should not have been removed because of not manning the fortress but was.", removeArmy);
            }

            return this;
        }
    }

    private static class SiegeUndermineResultBuilder {
        SiegeUndermineResultEnum result;
        SiegeStatusEnum status;
        boolean fortressFalls;
        boolean switchSiegework;
        boolean addSiegework;
        boolean newStackForSiegework;
        boolean removeControl;
        boolean switchControl;
        boolean addControl;
        CounterFaceTypeEnum newFortressType;
        Camp newFortressCountry;

        static SiegeUndermineResultBuilder create() {
            return new SiegeUndermineResultBuilder();
        }

        SiegeUndermineResultBuilder result(SiegeUndermineResultEnum result) {
            this.result = result;
            return this;
        }

        SiegeUndermineResultBuilder status(SiegeStatusEnum status) {
            this.status = status;
            return this;
        }

        SiegeUndermineResultBuilder fortressFalls() {
            this.fortressFalls = true;
            return this;
        }

        SiegeUndermineResultBuilder switchSiegework() {
            this.switchSiegework = true;
            return this;
        }

        SiegeUndermineResultBuilder addSiegework() {
            this.addSiegework = true;
            return this;
        }

        SiegeUndermineResultBuilder newStackForSiegework() {
            this.newStackForSiegework = true;
            return this;
        }

        SiegeUndermineResultBuilder removeControl() {
            this.removeControl = true;
            return this;
        }

        SiegeUndermineResultBuilder switchControl() {
            this.switchControl = true;
            return this;
        }

        SiegeUndermineResultBuilder addControl() {
            this.addControl = true;
            return this;
        }

        SiegeUndermineResultBuilder newFortressType(CounterFaceTypeEnum newFortressType) {
            this.newFortressType = newFortressType;
            return this;
        }

        SiegeUndermineResultBuilder newFortressCountry(Camp newFortressCountry) {
            this.newFortressCountry = newFortressCountry;
            return this;
        }
    }

    private enum Camp {
        SELF("espagne"),
        ALLY("austria"),
        NEUTRAL("turquie"),
        ENEMY("france");

        String name;

        private Camp(String name) {
            this.name = name;
        }
    }

}
