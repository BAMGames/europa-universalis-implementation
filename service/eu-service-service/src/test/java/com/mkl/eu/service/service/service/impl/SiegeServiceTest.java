package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.military.ChooseModeForSiegeRequest;
import com.mkl.eu.client.service.service.military.ChooseProvinceRequest;
import com.mkl.eu.client.service.service.military.SelectForcesRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.ArtillerySiege;
import com.mkl.eu.client.service.vo.tables.Tables;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
            Assert.assertEquals(2 + (withBesieged ? 1 : 0), diffEntity.getAttributes().size());
            Assert.assertEquals(SiegeStatusEnum.CHOOSE_MODE.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
            Assert.assertEquals("1", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_COUNTER_ADD));
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
        Assert.assertEquals(3, diffEntity.getAttributes().size());
        Assert.assertEquals(SiegeStatusEnum.CHOOSE_MODE.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
        Assert.assertEquals(2l, diffEntity.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_COUNTER_ADD)
                .count());

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
        game.getSieges().add(new SiegeEntity());
        game.getSieges().get(0).setStatus(SiegeStatusEnum.SELECT_FORCES);
        game.getSieges().get(0).setProvince("pecs");
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
        when(oeUtil.getFortressLevel(any(), any())).thenReturn(2);

        try {
            siegeService.chooseMode(request);
            Assert.fail("Should break because undermine is not possible with so few forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.SIEGE_UNDERMINE_TOO_FEW, e.getCode());
            Assert.assertEquals("chooseMode.request.mode", e.getParams()[0]);
        }
    }
}
