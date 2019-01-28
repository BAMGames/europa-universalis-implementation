package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.ChooseBattleRequest;
import com.mkl.eu.client.service.service.military.SelectForceRequest;
import com.mkl.eu.client.service.service.military.WithdrawBeforeBattleRequest;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.BattleTech;
import com.mkl.eu.client.service.vo.tables.CombatResult;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleCounterEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.AbstractGameServiceTest;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import java.util.*;

import static com.mkl.eu.client.common.util.CommonUtil.THIRD;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Test of MilitaryService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class MilitaryServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private MilitaryServiceImpl militaryService;

    @Mock
    private IOEUtil oeUtil;

    @Mock
    private ICounterDomain counterDomain;

    @Mock
    private IProvinceDao provinceDao;

    @Test
    public void testChooseBattleFail() {
        Pair<Request<ChooseBattleRequest>, GameEntity> pair = testCheckGame(militaryService::chooseBattle, "chooseBattle");
        Request<ChooseBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        request.setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, militaryService::chooseBattle, "chooseBattle", GameStatusEnum.MILITARY_BATTLES);

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because chooseBattle.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseBattleRequest());

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("");

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because province is empty");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("idf");

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because another battle is in process");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_IN_PROCESS, e.getCode());
            Assert.assertEquals("chooseBattle", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.NEW);

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because no battle is in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseBattleToSelectForces() throws FunctionalException {
        testChooseBattle(false);
    }

    @Test
    public void testChooseBattleToWithdrawBeforeBattle() throws FunctionalException {
        testChooseBattle(true);
    }

    private void testChooseBattle(boolean gotoWithdraw) throws FunctionalException {
        Pair<Request<ChooseBattleRequest>, GameEntity> pair = testCheckGame(militaryService::chooseBattle, "chooseBattle");
        Request<ChooseBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(26L);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setId(33L);
        game.getBattles().get(0).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(0).setProvince("idf");
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
        request.setRequest(new ChooseBattleRequest());
        request.getRequest().setProvince("idf");
        testCheckStatus(pair.getRight(), request, militaryService::chooseBattle, "chooseBattle", GameStatusEnum.MILITARY_BATTLES);

        List<String> allies = new ArrayList<>();
        allies.add("france");
        List<String> enemies = new ArrayList<>();
        enemies.add("espagne");
        if (!gotoWithdraw) {
            allies.add("turquie");
            enemies.add("angleterre");
        }
        when(oeUtil.getAllies(game.getCountries().get(0), game)).thenReturn(allies);
        when(oeUtil.getEnemies(game.getCountries().get(0), game)).thenReturn(enemies);

        simulateDiff();

        DiffResponse response = militaryService.chooseBattle(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.BATTLE, diffEntity.getTypeObject());
        Assert.assertEquals(game.getBattles().get(0).getId(), diffEntity.getIdObject());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(game.getId(), diffEntity.getIdGame());
        if (gotoWithdraw) {
            Assert.assertEquals(5, diffEntity.getAttributes().size());
            Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffEntity.getAttributes().get(0).getType());
            Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE.name(), diffEntity.getAttributes().get(0).getValue());
            Assert.assertEquals(DiffAttributeTypeEnum.PHASING_READY, diffEntity.getAttributes().get(1).getType());
            Assert.assertEquals("true", diffEntity.getAttributes().get(1).getValue());
            Assert.assertEquals(DiffAttributeTypeEnum.PHASING_COUNTER_ADD, diffEntity.getAttributes().get(2).getType());
            Assert.assertEquals("1", diffEntity.getAttributes().get(2).getValue());
            Assert.assertEquals(DiffAttributeTypeEnum.NON_PHASING_READY, diffEntity.getAttributes().get(3).getType());
            Assert.assertEquals("true", diffEntity.getAttributes().get(3).getValue());
            Assert.assertEquals(DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD, diffEntity.getAttributes().get(4).getType());
            Assert.assertEquals("5", diffEntity.getAttributes().get(4).getValue());
        } else {
            Assert.assertEquals(1, diffEntity.getAttributes().size());
            Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffEntity.getAttributes().get(0).getType());
            Assert.assertEquals(BattleStatusEnum.SELECT_FORCES.name(), diffEntity.getAttributes().get(0).getValue());
        }

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());

        if (gotoWithdraw) {
            Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE, game.getBattles().get(0).getStatus());
            Assert.assertEquals(2, game.getBattles().get(0).getCounters().size());
            BattleCounterEntity counterFra = game.getBattles().get(0).getCounters().stream()
                    .filter(c -> c.getCounter().getId().equals(1L))
                    .findAny()
                    .orElse(null);
            Assert.assertEquals(true, counterFra.isPhasing());
            BattleCounterEntity counterEsp = game.getBattles().get(0).getCounters().stream()
                    .filter(c -> c.getCounter().getId().equals(5L))
                    .findAny()
                    .orElse(null);
            Assert.assertEquals(false, counterEsp.isPhasing());
        } else {
            Assert.assertEquals(BattleStatusEnum.SELECT_FORCES, game.getBattles().get(0).getStatus());
        }
    }

    @Test
    public void testSelectForceFail() {
        Pair<Request<SelectForceRequest>, GameEntity> pair = testCheckGame(militaryService::selectForce, "selectForce");
        Request<SelectForceRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, militaryService::selectForce, "selectForce", GameStatusEnum.MILITARY_BATTLES);
        request.setIdCountry(26L);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because selectForce.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request", e.getParams()[0]);
        }

        request.setRequest(new SelectForceRequest());
        request.setIdCountry(12L);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request.idCounter", e.getParams()[0]);
        }

        request.getRequest().setIdCounter(6L);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because no battle is in right status");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("selectForce", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because counter does not exist in the battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request.idCounter", e.getParams()[0]);
        }

        BattleCounterEntity battleCounter = new BattleCounterEntity();
        battleCounter.setCounter(new CounterEntity());
        battleCounter.getCounter().setId(6L);
        game.getBattles().get(0).getCounters().add(battleCounter);

        try {
            militaryService.selectForce(request);
        } catch (FunctionalException e) {
            Assert.fail("Should not break " + e.getMessage());
        }

        request.getRequest().setAdd(true);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request.idCounter", e.getParams()[0]);
        }

        when(oeUtil.getAllies(null, game)).thenReturn(Collections.singletonList("france"));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("pecs");
        game.getStacks().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(6L);
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);

        try {
            militaryService.selectForce(request);
        } catch (FunctionalException e) {
            Assert.fail("Should not break " + e.getMessage());
        }

        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MANOEUVRE);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because counter is not an army");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request.idCounter", e.getParams()[0]);
        }

        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(0).setCountry("pologne");

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because counter is not owned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request.idCounter", e.getParams()[0]);
        }

        game.getStacks().get(0).setCountry("france");
        game.getStacks().get(0).setProvince("idf");

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because counter is not in the right province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForce.request.idCounter", e.getParams()[0]);
        }

        game.getStacks().get(0).setProvince("pecs");
        game.getBattles().get(0).getNonPhasing().setForces(true);

        try {
            militaryService.selectForce(request);
            Assert.fail("Should break because the attacker already validated its forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_SELECT_VALIDATED, e.getCode());
            Assert.assertEquals("selectForce", e.getParams()[0]);
        }
    }

    @Test
    public void testSelectForceAddAttacker() throws FunctionalException {
        testSelectForceSuccess(true, true);
    }

    @Test
    public void testSelectForceRemoveAttacker() throws FunctionalException {
        testSelectForceSuccess(false, true);
    }

    @Test
    public void testSelectForceAddDefender() throws FunctionalException {
        testSelectForceSuccess(true, false);
    }

    @Test
    public void testSelectForceRemoveDefender() throws FunctionalException {
        testSelectForceSuccess(false, false);
    }

    private void testSelectForceSuccess(boolean add, boolean attacker) throws FunctionalException {
        Pair<Request<SelectForceRequest>, GameEntity> pair = testCheckGame(militaryService::selectForce, "selectForce");
        Request<SelectForceRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("pecs");
        game.getStacks().get(0).setCountry(country.getName());
        CounterEntity counter = new CounterEntity();
        counter.setId(6L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(0).getCounters().add(counter);

        testCheckStatus(pair.getRight(), request, militaryService::selectForce, "selectForce", GameStatusEnum.MILITARY_BATTLES);
        request.setIdCountry(26L);
        request.setRequest(new SelectForceRequest());
        request.setIdCountry(12L);
        request.getRequest().setIdCounter(6L);
        request.getRequest().setAdd(add);

        when(oeUtil.getAllies(country, game)).thenReturn(Collections.singletonList(country.getName()));

        if (!add) {
            BattleCounterEntity battleCounter = new BattleCounterEntity();
            battleCounter.setCounter(new CounterEntity());
            battleCounter.getCounter().setId(6L);
            game.getBattles().get(0).getCounters().add(battleCounter);
        }

        if (attacker) {
            CountryOrderEntity order = new CountryOrderEntity();
            order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
            order.setActive(true);
            order.setCountry(country);
            game.getOrders().add(order);
        }

        simulateDiff();

        DiffResponse response = militaryService.selectForce(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        BattleEntity battle = game.getBattles().get(0);

        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.BATTLE, diffEntity.getTypeObject());
        Assert.assertEquals(game.getBattles().get(0).getId(), diffEntity.getIdObject());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(game.getId(), diffEntity.getIdGame());
        Assert.assertEquals(1, diffEntity.getAttributes().size());
        DiffAttributeTypeEnum diffStatus;
        if (attacker) {
            if (add) {
                diffStatus = DiffAttributeTypeEnum.PHASING_COUNTER_ADD;

                Assert.assertEquals(1, battle.getCounters().size());
                BattleCounterEntity battleCounter = battle.getCounters().iterator().next();
                Assert.assertEquals(battle, battleCounter.getBattle());
                Assert.assertEquals(counter, battleCounter.getCounter());
                Assert.assertEquals(true, battleCounter.isPhasing());
            } else {
                diffStatus = DiffAttributeTypeEnum.PHASING_COUNTER_REMOVE;

                Assert.assertEquals(0, battle.getCounters().size());
            }
        } else {
            if (add) {
                diffStatus = DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD;

                Assert.assertEquals(1, battle.getCounters().size());
                BattleCounterEntity battleCounter = battle.getCounters().iterator().next();
                Assert.assertEquals(battle, battleCounter.getBattle());
                Assert.assertEquals(counter, battleCounter.getCounter());
                Assert.assertEquals(false, battleCounter.isPhasing());
            } else {
                diffStatus = DiffAttributeTypeEnum.NON_PHASING_COUNTER_REMOVE;

                Assert.assertEquals(0, battle.getCounters().size());
            }
        }
        Assert.assertEquals(diffStatus, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("6", diffEntity.getAttributes().get(0).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testValidateForcesFail() {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(militaryService::validateForces, "validateForces");
        Request<ValidateRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        game.getBattles().get(0).getNonPhasing().setForces(true);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, militaryService::validateForces, "validateForces", GameStatusEnum.MILITARY_BATTLES);
        request.setIdCountry(26L);

        try {
            militaryService.validateForces(request);
            Assert.fail("Should break because validateForces.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateForces.request", e.getParams()[0]);
        }

        request.setRequest(new ValidateRequest());
        request.setIdCountry(12L);

        try {
            militaryService.validateForces(request);
            Assert.fail("Should break because no battle is in right status");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("validateForces", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);

        try {
            militaryService.validateForces(request);
            Assert.fail("Should break because invalidate is impossible if no other counter exists");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_INVALIDATE_NO_FORCE, e.getCode());
            Assert.assertEquals("validateForces.request.validate", e.getParams()[0]);
        }

        request.getRequest().setValidate(true);
        game.getBattles().get(0).getNonPhasing().setForces(false);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("pecs");
        game.getStacks().get(0).setCountry(country.getName());
        CounterEntity counter = new CounterEntity();
        counter.setId(6L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(0).getCounters().add(counter);
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setCounter(counter);
        game.getBattles().get(0).getCounters().add(bc);
        counter = new CounterEntity();
        counter.setId(7L);
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(0).getCounters().add(counter);
        bc = new BattleCounterEntity();
        bc.setCounter(counter);
        game.getBattles().get(0).getCounters().add(bc);
        counter = new CounterEntity();
        counter.setId(8L);
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(0).getCounters().add(counter);

        when(oeUtil.getAllies(country, game)).thenReturn(Collections.singletonList(country.getName()));

        try {
            militaryService.validateForces(request);
            Assert.fail("Should break because validate is impossible if other counter could be selected");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_VALIDATE_OTHER_FORCE, e.getCode());
            Assert.assertEquals("validateForces.request.validate", e.getParams()[0]);
        }
    }

    @Test
    public void testValidateForcesValidatePhasingSuccess() throws FunctionalException {
        testValidateForcesSuccess(true, true, false);
    }

    @Test
    public void testValidateForcesInvalidatePhasingSuccess() throws FunctionalException {
        testValidateForcesSuccess(true, false, true);
    }

    @Test
    public void testValidateForcesValidateNonPhasingSuccess() throws FunctionalException {
        testValidateForcesSuccess(false, true, false);
    }

    @Test
    public void testValidateForcesInvalidateNonPhasingSuccess() throws FunctionalException {
        testValidateForcesSuccess(false, false, true);
    }

    @Test
    public void testValidateForcesValidatePhasingNothing() throws FunctionalException {
        testValidateForcesSuccess(true, true, true);
    }

    @Test
    public void testValidateForcesInvalidatePhasingNothing() throws FunctionalException {
        testValidateForcesSuccess(true, false, false);
    }

    @Test
    public void testValidateForcesValidateNonPhasingNothing() throws FunctionalException {
        testValidateForcesSuccess(false, true, true);
    }

    @Test
    public void testValidateForcesInvalidateNonPhasingNothing() throws FunctionalException {
        testValidateForcesSuccess(false, false, false);
    }

    private void testValidateForcesSuccess(boolean phasing, boolean validate, boolean before) throws FunctionalException {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(militaryService::validateForces, "validateForces");
        Request<ValidateRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);
        game.getBattles().get(0).getNonPhasing().setForces(phasing || before);
        game.getBattles().get(0).getPhasing().setForces(!phasing || before);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, militaryService::validateForces, "validateForces", GameStatusEnum.MILITARY_BATTLES);
        request.setIdCountry(12L);
        request.setRequest(new ValidateRequest());
        request.getRequest().setValidate(validate);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setProvince("pecs");
        game.getStacks().get(0).setCountry(country.getName());
        CounterEntity counter = new CounterEntity();
        counter.setId(6L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(0).getCounters().add(counter);
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(phasing);
        bc.setCounter(counter);
        game.getBattles().get(0).getCounters().add(bc);
        counter = new CounterEntity();
        counter.setId(7L);
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        game.getStacks().get(0).getCounters().add(counter);
        bc = new BattleCounterEntity();
        bc.setPhasing(phasing);
        bc.setCounter(counter);
        game.getBattles().get(0).getCounters().add(bc);
        counter = new CounterEntity();
        counter.setId(8L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        game.getStacks().get(0).getCounters().add(counter);

        when(oeUtil.getAllies(country, game)).thenReturn(Collections.singletonList(country.getName()));

        if (phasing) {
            CountryOrderEntity order = new CountryOrderEntity();
            order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
            order.setActive(true);
            order.setCountry(country);
            game.getOrders().add(order);
        }

        simulateDiff();

        DiffResponse response = militaryService.validateForces(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        BattleEntity battle = game.getBattles().get(0);
        if (validate != before) {
            Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
            Assert.assertEquals(DiffTypeObjectEnum.BATTLE, diffEntity.getTypeObject());
            Assert.assertEquals(game.getBattles().get(0).getId(), diffEntity.getIdObject());
            Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
            Assert.assertEquals(game.getId(), diffEntity.getIdGame());
            Assert.assertTrue(diffEntity.getAttributes().size() >= 1);
            DiffAttributeTypeEnum diffStatus;
            if (phasing) {
                diffStatus = DiffAttributeTypeEnum.PHASING_READY;

                Assert.assertEquals(validate, battle.getPhasing().isForces());
            } else {
                diffStatus = DiffAttributeTypeEnum.NON_PHASING_READY;

                Assert.assertEquals(validate, battle.getNonPhasing().isForces());
            }
            Assert.assertEquals(diffStatus, diffEntity.getAttributes().get(0).getType());
            Assert.assertEquals(Boolean.toString(validate), diffEntity.getAttributes().get(0).getValue());

            DiffAttributesEntity statusChanged = diffEntity.getAttributes().stream()
                    .filter(attribute -> attribute.getType() == DiffAttributeTypeEnum.STATUS)
                    .findAny()
                    .orElse(null);
            if (battle.getPhasing().isForces() && battle.getNonPhasing().isForces()) {
                Assert.assertNotNull(statusChanged);
                Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE.name(), statusChanged.getValue());
                Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE, battle.getStatus());
            } else {
                Assert.assertNull(statusChanged);
                Assert.assertEquals(BattleStatusEnum.SELECT_FORCES, battle.getStatus());
            }
        } else {
            Assert.assertNull(diffEntity);
        }

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testFillBattleModifiers() {
        BattleEntity battle = new BattleEntity();

        BattleCounterEntity battleCounter = new BattleCounterEntity();
        battleCounter.setPhasing(true);
        CounterEntity phasingCounter = new CounterEntity();
        phasingCounter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        phasingCounter.setCountry(PlayableCountry.FRANCE);
        battleCounter.setCounter(phasingCounter);
        battle.getCounters().add(battleCounter);
        battleCounter = new BattleCounterEntity();
        battleCounter.setPhasing(false);
        CounterEntity nonPhasingCounter = new CounterEntity();
        nonPhasingCounter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        nonPhasingCounter.setCountry(PlayableCountry.SPAIN);
        battleCounter.setCounter(nonPhasingCounter);
        battle.getCounters().add(battleCounter);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setTerrain(TerrainEnum.PLAIN);
        EuropeanProvinceEntity morbihan = new EuropeanProvinceEntity();
        morbihan.setTerrain(TerrainEnum.DENSE_FOREST);
        EuropeanProvinceEntity lyonnais = new EuropeanProvinceEntity();
        lyonnais.setTerrain(TerrainEnum.SPARSE_FOREST);
        EuropeanProvinceEntity limoges = new EuropeanProvinceEntity();
        limoges.setTerrain(TerrainEnum.DESERT);
        EuropeanProvinceEntity neva = new EuropeanProvinceEntity();
        neva.setTerrain(TerrainEnum.SWAMP);
        EuropeanProvinceEntity tyrol = new EuropeanProvinceEntity();
        tyrol.setTerrain(TerrainEnum.MOUNTAIN);
        MilitaryServiceImpl.TABLES = new Tables();
        fillBatleTechTables(MilitaryServiceImpl.TABLES);

        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        when(provinceDao.getProvinceByName("morbihan")).thenReturn(morbihan);
        when(provinceDao.getProvinceByName("lyonnais")).thenReturn(lyonnais);
        when(provinceDao.getProvinceByName("limoges")).thenReturn(limoges);
        when(provinceDao.getProvinceByName("neva")).thenReturn(neva);
        when(provinceDao.getProvinceByName("tyrol")).thenReturn(tyrol);

        when(oeUtil.getTechnology(Collections.singletonList(phasingCounter), true, militaryService.getReferential(), militaryService.getTables(), battle.getGame()))
                .thenReturn(Tech.ARQUEBUS);
        when(oeUtil.getTechnology(Collections.singletonList(nonPhasingCounter), true, militaryService.getReferential(), militaryService.getTables(), battle.getGame()))
                .thenReturn(Tech.RENAISSANCE);
        List<ArmyInfo> armyPhasing = new ArrayList<>();
        armyPhasing.add(new ArmyInfo());
        armyPhasing.get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        armyPhasing.get(0).setCountry(PlayableCountry.FRANCE);
        armyPhasing.get(0).setArmyClass(ArmyClassEnum.IVM);
        List<ArmyInfo> armyNonPhasing = new ArrayList<>();
        armyNonPhasing.add(new ArmyInfo());
        armyNonPhasing.get(0).setType(CounterFaceTypeEnum.ARMY_MINUS);
        armyNonPhasing.get(0).setCountry(PlayableCountry.SPAIN);
        armyNonPhasing.get(0).setArmyClass(ArmyClassEnum.IV);
        when(oeUtil.getArmyInfo(Collections.singletonList(phasingCounter), militaryService.getReferential())).thenReturn(armyPhasing);
        when(oeUtil.getArmyInfo(Collections.singletonList(nonPhasingCounter), militaryService.getReferential())).thenReturn(armyNonPhasing);

        battle.setProvince("idf");
        checkModifiers(battle, Modifiers.init(0));
        Assert.assertEquals(Tech.ARQUEBUS, battle.getPhasing().getTech());
        Assert.assertEquals(Tech.RENAISSANCE, battle.getNonPhasing().getTech());
        Assert.assertEquals("C", battle.getPhasing().getFireColumn());
        Assert.assertEquals("A", battle.getPhasing().getShockColumn());
        Assert.assertEquals(2, battle.getPhasing().getMoral().intValue());
        Assert.assertEquals("C", battle.getNonPhasing().getFireColumn());
        Assert.assertEquals("B", battle.getNonPhasing().getShockColumn());
        Assert.assertEquals(2, battle.getNonPhasing().getMoral().intValue());

        battle.setProvince("morbihan");
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("lyonnais");
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("limoges");
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("neva");
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("tyrol");
        checkModifiers(battle, Modifiers.init(-1)
                .addFireNonPhasingFirstDay(1)
                .addShockNonPhasingFirstDay(1)
                .addFireNonPhasingSecondDay(1)
                .addShockNonPhasingSecondDay(1));

        when(oeUtil.getArtilleryBonus(armyPhasing, militaryService.getTables(), battle.getGame())).thenReturn(6);
        when(oeUtil.getArtilleryBonus(armyNonPhasing, militaryService.getTables(), battle.getGame())).thenReturn(5);

        battle.setProvince("idf");
        checkModifiers(battle, Modifiers.init(0)
                .addFirePhasingFirstDay(1)
                .addFirePhasingSecondDay(1));

        when(oeUtil.getArtilleryBonus(armyNonPhasing, militaryService.getTables(), battle.getGame())).thenReturn(7);
        checkModifiers(battle, Modifiers.init(0)
                .addFirePhasingFirstDay(1)
                .addFirePhasingSecondDay(1)
                .addFireNonPhasingFirstDay(1)
                .addFireNonPhasingSecondDay(1));


        when(oeUtil.getArtilleryBonus(armyPhasing, militaryService.getTables(), battle.getGame())).thenReturn(0);
        when(oeUtil.getArtilleryBonus(armyNonPhasing, militaryService.getTables(), battle.getGame())).thenReturn(1);
        when(oeUtil.getCavalryBonus(armyPhasing, TerrainEnum.PLAIN, militaryService.getTables(), battle.getGame())).thenReturn(true);
        when(oeUtil.getCavalryBonus(armyNonPhasing, TerrainEnum.PLAIN, militaryService.getTables(), battle.getGame())).thenReturn(false);
        checkModifiers(battle, Modifiers.init(0)
                .addShockPhasingFirstDay(1)
                .addShockPhasingSecondDay(1));

        when(oeUtil.getCavalryBonus(armyPhasing, TerrainEnum.PLAIN, militaryService.getTables(), battle.getGame())).thenReturn(false);
        when(oeUtil.getCavalryBonus(armyNonPhasing, TerrainEnum.PLAIN, militaryService.getTables(), battle.getGame())).thenReturn(true);
        checkModifiers(battle, Modifiers.init(0)
                .addShockNonPhasingFirstDay(1)
                .addShockNonPhasingSecondDay(1));

        nonPhasingCounter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        checkModifiers(battle, Modifiers.init(0)
                .addShockPhasingFirstDay(1)
                .addShockPhasingSecondDay(1)
                .addShockNonPhasingFirstDay(1)
                .addShockNonPhasingSecondDay(1)
                .addFireNonPhasingFirstDay(-1)
                .addFireNonPhasingSecondDay(-1));

        battle.setProvince("lyonnais");
        when(oeUtil.getArtilleryBonus(armyPhasing, militaryService.getTables(), battle.getGame())).thenReturn(10);
        when(oeUtil.getArtilleryBonus(armyNonPhasing, militaryService.getTables(), battle.getGame())).thenReturn(1);
        when(oeUtil.getCavalryBonus(armyPhasing, TerrainEnum.SPARSE_FOREST, militaryService.getTables(), battle.getGame())).thenReturn(true);
        when(oeUtil.getCavalryBonus(armyNonPhasing, TerrainEnum.SPARSE_FOREST, militaryService.getTables(), battle.getGame())).thenReturn(false);
        checkModifiers(battle, Modifiers.init(-1)
                .addFirePhasingFirstDay(1)
                .addFirePhasingSecondDay(1)
                .addShockPhasingFirstDay(2)
                .addShockPhasingSecondDay(2)
                .addFireNonPhasingFirstDay(-1)
                .addFireNonPhasingSecondDay(-1));
    }

    private void checkModifiers(BattleEntity battle, Modifiers modifiers) {
        militaryService.fillBattleModifiers(battle);

        Assert.assertEquals(modifiers.firePF, battle.getPhasing().getFirstDay().getFireMod());
        Assert.assertEquals(modifiers.shockPF, battle.getPhasing().getFirstDay().getShockMod());
        Assert.assertEquals(modifiers.firePS, battle.getPhasing().getSecondDay().getFireMod());
        Assert.assertEquals(modifiers.shockPS, battle.getPhasing().getSecondDay().getShockMod());
        Assert.assertEquals(modifiers.pursuitP, battle.getPhasing().getPursuitMod());

        Assert.assertEquals(modifiers.fireNPF, battle.getNonPhasing().getFirstDay().getFireMod());
        Assert.assertEquals(modifiers.shockNPF, battle.getNonPhasing().getFirstDay().getShockMod());
        Assert.assertEquals(modifiers.fireNPS, battle.getNonPhasing().getSecondDay().getFireMod());
        Assert.assertEquals(modifiers.shockNPS, battle.getNonPhasing().getSecondDay().getShockMod());
        Assert.assertEquals(modifiers.pursuitNP, battle.getNonPhasing().getPursuitMod());
    }

    private static class Modifiers {
        /** Modifiers Phasing First day. */
        private int firePF;
        private int shockPF;
        /** Modifiers Phasing Second day. */
        private int firePS;
        private int shockPS;
        private int pursuitP;
        /** Modifiers Non Phasing First day. */
        private int fireNPF;
        private int shockNPF;
        /** Modifiers Non Phasing Second day. */
        private int fireNPS;
        private int shockNPS;
        private int pursuitNP;

        static Modifiers init(int init) {
            Modifiers modifiers = new Modifiers();

            modifiers.firePF = init;
            modifiers.shockPF = init;
            modifiers.firePS = init - 1;
            modifiers.shockPS = init - 1;
            modifiers.fireNPF = init;
            modifiers.shockNPF = init;
            modifiers.fireNPS = init - 1;
            modifiers.shockNPS = init - 1;
            modifiers.pursuitP = init;
            modifiers.pursuitNP = init;

            return modifiers;
        }

        Modifiers addFirePhasingFirstDay(int firePF) {
            this.firePF += firePF;

            return this;
        }

        Modifiers addShockPhasingFirstDay(int shockPF) {
            this.shockPF += shockPF;

            return this;
        }

        Modifiers addFirePhasingSecondDay(int firePS) {
            this.firePS += firePS;

            return this;
        }

        Modifiers addShockPhasingSecondDay(int shockPS) {
            this.shockPS += shockPS;

            return this;
        }

        Modifiers addFireNonPhasingFirstDay(int fireNPF) {
            this.fireNPF += fireNPF;

            return this;
        }

        Modifiers addShockNonPhasingFirstDay(int shockNPF) {
            this.shockNPF += shockNPF;

            return this;
        }

        Modifiers addFireNonPhasingSecondDay(int fireNPS) {
            this.fireNPS += fireNPS;

            return this;
        }

        Modifiers addShockNonPhasingSecondDay(int shockNPS) {
            this.shockNPS += shockNPS;

            return this;
        }
    }

    @Test
    public void testWithdrawBeforeBattleFail() {
        Pair<Request<WithdrawBeforeBattleRequest>, GameEntity> pair = testCheckGame(militaryService::withdrawBeforeBattle, "withdrawBeforeBattle");
        Request<WithdrawBeforeBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);
        game.getBattles().get(0).setProvince("idf");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setCountry(new PlayableCountryEntity());
        order.getCountry().setId(26L);
        game.getOrders().add(order);
        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(1L);
        idf.setName("idf");
        EuropeanProvinceEntity orleans = new EuropeanProvinceEntity();
        orleans.setId(2L);
        orleans.setName("orleans");
        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(idf);
        border.setProvinceTo(orleans);
        idf.getBorders().add(border);
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        testCheckStatus(pair.getRight(), request, militaryService::withdrawBeforeBattle, "withdrawBeforeBattle", GameStatusEnum.MILITARY_BATTLES);
        request.setIdCountry(26L);

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because withdrawBeforeBattle.idCountry is the phasing player");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_ONLY_NON_PHASING_CAN_WITHDRAW, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(27L);

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because withdrawBeforeBattle.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request", e.getParams()[0]);
        }

        request.setRequest(new WithdrawBeforeBattleRequest());
        request.getRequest().setWithdraw(true);

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because battle is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("");

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province is empty");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("toto");

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(new EuropeanProvinceEntity());

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province is not next to battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("orleans");
        when(provinceDao.getProvinceByName("orleans")).thenReturn(orleans);

        try {
            militaryService.withdrawBeforeBattle(request);
            Assert.fail("Should break because cannot retreat in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_CANT_WITHDRAW, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }
    }

    @Test
    public void testWithdrawBeforeBattleSuccess() throws FunctionalException {
        Pair<Request<WithdrawBeforeBattleRequest>, GameEntity> pair = testCheckGame(militaryService::withdrawBeforeBattle, "withdrawBeforeBattle");
        Request<WithdrawBeforeBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        StackEntity stack1 = new StackEntity();
        stack1.setId(1l);
        stack1.setProvince("idf");
        stack1.setCountry("france");
        stack1.getCounters().add(createCounter(1l, "france", CounterFaceTypeEnum.ARMY_PLUS));
        stack1.getCounters().add(createCounter(2l, "france", CounterFaceTypeEnum.LAND_DETACHMENT));
        game.getStacks().add(stack1);
        StackEntity stack2 = new StackEntity();
        stack2.setId(2l);
        stack2.setProvince("idf");
        stack2.setCountry("spain");
        stack2.getCounters().add(createCounter(3l, "spain", CounterFaceTypeEnum.ARMY_MINUS));
        game.getStacks().add(stack2);
        StackEntity stack3 = new StackEntity();
        stack3.setId(3l);
        stack3.setProvince("idf");
        stack3.setCountry("savoie");
        stack3.getCounters().add(createCounter(3l, "savoie", CounterFaceTypeEnum.LAND_DETACHMENT));
        game.getStacks().add(stack3);

        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        game.getBattles().get(0).setProvince("idf");
        stack1.getCounters().forEach(counter -> {
            BattleCounterEntity bc = new BattleCounterEntity();
            bc.setPhasing(false);
            bc.setCounter(counter);
        });
        stack2.getCounters().forEach(counter -> {
            BattleCounterEntity bc = new BattleCounterEntity();
            bc.setPhasing(true);
            bc.setCounter(counter);
        });
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(27L);
        country.setName("france");
        game.getCountries().add(country);
        when(oeUtil.getAllies(country, game)).thenReturn(Arrays.asList("france", "savoie"));
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setCountry(new PlayableCountryEntity());
        order.getCountry().setId(26L);
        game.getOrders().add(order);
        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(1L);
        idf.setName("idf");
        idf.setTerrain(TerrainEnum.PLAIN);
        EuropeanProvinceEntity orleans = new EuropeanProvinceEntity();
        orleans.setId(2L);
        orleans.setName("orleans");
        orleans.setTerrain(TerrainEnum.PLAIN);
        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(idf);
        border.setProvinceTo(orleans);
        idf.getBorders().add(border);
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        when(provinceDao.getProvinceByName("orleans")).thenReturn(orleans);
        when(oeUtil.canRetreat(any(), anyBoolean(), anyInt(), any(), any())).thenReturn(true);
        when(oeUtil.lossesMitigation(anyDouble(), anyBoolean(), any())).thenReturn(AbstractWithLossEntity.create(0));
        when(oeUtil.lossModificationSize(any(), anyInt())).thenReturn(AbstractWithLossEntity.create(0));
        when(oeUtil.retreat(anyInt())).thenReturn(AbstractWithLossEntity.create(0));
        testCheckStatus(pair.getRight(), request, militaryService::withdrawBeforeBattle, "withdrawBeforeBattle", GameStatusEnum.MILITARY_BATTLES);
        request.setIdCountry(27L);
        request.setRequest(new WithdrawBeforeBattleRequest());
        request.getRequest().setWithdraw(true);
        request.getRequest().setProvinceTo("orleans");
        BattleEntity battle = game.getBattles().get(0);
        MilitaryServiceImpl.TABLES = new Tables();
        MilitaryServiceImpl.TABLES.getBattleTechs().add(new BattleTech());
        simulateDiff();

        when(oeUtil.rollDie(game, country)).thenReturn(5);

        militaryService.withdrawBeforeBattle(request);

        Assert.assertTrue(battle.getEnd() != BattleEndEnum.WITHDRAW_BEFORE_BATTLE);

        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        request.getRequest().setProvinceTo("idf");

        militaryService.withdrawBeforeBattle(request);

        Assert.assertTrue(battle.getEnd() == BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
        Assert.assertTrue(battle.getStatus() == BattleStatusEnum.DONE);
        Assert.assertTrue(battle.getWinner() == BattleWinnerEnum.NONE);
        List<DiffEntity> diffs = retrieveDiffsCreated();
        Assert.assertEquals(3, diffs.size());
        DiffEntity diffBattle = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffBattle);
        Assert.assertEquals(battle.getId(), diffBattle.getIdObject());
        Assert.assertEquals(BattleStatusEnum.DONE.name(), getAttribute(diffBattle, DiffAttributeTypeEnum.STATUS));
        Assert.assertEquals(BattleEndEnum.WITHDRAW_BEFORE_BATTLE.name(), getAttribute(diffBattle, DiffAttributeTypeEnum.END));
        Assert.assertEquals(BattleWinnerEnum.NONE.name(), getAttribute(diffBattle, DiffAttributeTypeEnum.WINNER));
        DiffEntity diffStack1 = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(diff.getIdObject(), stack1.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffStack1);
        Assert.assertEquals("idf", getAttribute(diffStack1, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("idf", getAttribute(diffStack1, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(diffStack1, DiffAttributeTypeEnum.MOVE_PHASE));
        Assert.assertEquals(Boolean.TRUE.toString(), getAttribute(diffStack1, DiffAttributeTypeEnum.BESIEGED));
        DiffEntity diffStack3 = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(diff.getIdObject(), stack3.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffStack3);
        Assert.assertEquals("idf", getAttribute(diffStack3, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("idf", getAttribute(diffStack3, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(diffStack3, DiffAttributeTypeEnum.MOVE_PHASE));
        Assert.assertEquals(Boolean.TRUE.toString(), getAttribute(diffStack3, DiffAttributeTypeEnum.BESIEGED));

        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        request.getRequest().setProvinceTo("orleans");
        when(oeUtil.rollDie(game, country)).thenReturn(8);

        militaryService.withdrawBeforeBattle(request);

        Assert.assertTrue(battle.getEnd() == BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
        Assert.assertTrue(battle.getStatus() == BattleStatusEnum.DONE);
        Assert.assertTrue(battle.getWinner() == BattleWinnerEnum.NONE);
        diffs = retrieveDiffsCreated();
        Assert.assertEquals(3, diffs.size());
        diffBattle = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffBattle);
        Assert.assertEquals(battle.getId(), diffBattle.getIdObject());
        Assert.assertEquals(BattleStatusEnum.DONE.name(), getAttribute(diffBattle, DiffAttributeTypeEnum.STATUS));
        Assert.assertEquals(BattleEndEnum.WITHDRAW_BEFORE_BATTLE.name(), getAttribute(diffBattle, DiffAttributeTypeEnum.END));
        Assert.assertEquals(BattleWinnerEnum.NONE.name(), getAttribute(diffBattle, DiffAttributeTypeEnum.WINNER));
        diffStack1 = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(diff.getIdObject(), stack1.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffStack1);
        Assert.assertEquals("idf", getAttribute(diffStack1, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("orleans", getAttribute(diffStack1, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(diffStack1, DiffAttributeTypeEnum.MOVE_PHASE));
        Assert.assertNull(getAttribute(diffStack1, DiffAttributeTypeEnum.BESIEGED));
        diffStack3 = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(diff.getIdObject(), stack3.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffStack3);
        Assert.assertEquals("idf", getAttribute(diffStack3, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("orleans", getAttribute(diffStack3, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(diffStack3, DiffAttributeTypeEnum.MOVE_PHASE));
        Assert.assertNull(getAttribute(diffStack3, DiffAttributeTypeEnum.BESIEGED));
    }

    @Test
    public void testLossGreaterThanArmy() {
        Assert.assertTrue(AbstractWithLossEntity.create(1).isGreaterThanSize(THIRD));
        Assert.assertTrue(AbstractWithLossEntity.create(1).isGreaterThanSize(0.3333334));
        Assert.assertFalse(AbstractWithLossEntity.create(1).isGreaterThanSize(2 * THIRD));
        Assert.assertTrue(AbstractWithLossEntity.create(2).isGreaterThanSize(2 * THIRD));
        Assert.assertTrue(AbstractWithLossEntity.create(6).isGreaterThanSize(1 + 2 * THIRD));
        Assert.assertFalse(AbstractWithLossEntity.create(6).isGreaterThanSize(2 + 1 * THIRD));
    }

    @Test
    public void testRoutedFirstFire() throws FunctionalException {
        // Non phasing routed. Non phasing no fire because medieval
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .sizeReductionDamage(2)
                        .tech(Tech.RENAISSANCE)
                        .firstFire(8)
                        .pursuit(5))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .sizeReductionDamage(2)
                        .tech(Tech.MEDIEVAL)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(0)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(1));

        // Non phasing routed. Both side no fire damage
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .sizeReductionDamage(2)
                        .tech(Tech.RENAISSANCE)
                        .firstFire(10)
                        .pursuit(8))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .sizeReductionDamage(2)
                        .tech(Tech.RENAISSANCE)
                        .firstFire(10)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(2)
                        .nonPhasingThirdLosses(3)
                        .nonPhasingMoralLosses(3));

        // Phasing routed. Both side half fire damage
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .sizeReductionDamage(2)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(8)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .sizeReductionDamage(2)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10)
                        .pursuit(5))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(3)
                        .phasingMoralLosses(2)
                        .nonPhasingThirdLosses(0)
                        .nonPhasingMoralLosses(1));

        // Both side routed. Both side half fire damage
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .sizeReductionDamage(2)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10)
                        .retreat(4)
                        .retreatLosses(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .sizeReductionDamage(2)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10)
                        .retreat(5)
                        .retreatLosses(2))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(2)
                        .nonPhasingThirdLosses(3)
                        .nonPhasingMoralLosses(2));
    }

    @Test
    public void testRoutedAndAnnihilatedFirstFire() throws FunctionalException {
        // Phasing routed and annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(9)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10)
                        .pursuit(5))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(1)
                        .nonPhasingAnnihilated(false));

        // Non phasing routed and annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10)
                        .pursuit(5))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(9)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(1)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(true));

        // Phasing routed, non phasing annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(8))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(11)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(1)
                        .nonPhasingAnnihilated(true));

        // Non phasing routed, phasing annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(11)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(8))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(1)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(false));

        // Both routed, phasing annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(11)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(false));

        // Both routed, non phasing annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(10))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(11)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(true));

        // Both routed and annihilated.
        BattleBuilder.create()
                .rotw(true)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(11)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(11)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_FIRE)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(1)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(1)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(true));
    }

    @Test
    public void testRoutedFirstShock() throws FunctionalException {
        // Everyone full damage fire.
        // Phasing routed. Non phasing annihilated after battle so still win the battle.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(7)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(7)
                        .pursuit(2))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_SHOCK)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(3)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(true));

        // Phasing routed. Non phasing annihilated.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(7)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_SHOCK)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(3)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(true));

        // Both side routed. No fire because Renaissance without army.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .sizeReductionDamage(6)
                        .veteran(false)
                        .tech(Tech.RENAISSANCE)
                        .firstShock(10)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .sizeReductionDamage(6)
                        .veteran(false)
                        .tech(Tech.RENAISSANCE)
                        .firstShock(10)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_FIRST_SHOCK)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(0)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(false));

    }

    @Test
    public void testAnnihilatedFirstDay() throws FunctionalException {
        // Phasing annihilated.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .pursuit(2))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ANNIHILATED_AT_FIRST_DAY)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(3)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(false));

        // Non phasing annihilated.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .pursuit(2))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ANNIHILATED_AT_FIRST_DAY)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(3)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(true));

        // Both annihilated.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreat(1))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ANNIHILATED_AT_FIRST_DAY)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(3)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(true));
    }

    @Test
    public void testNoWinnerFirstDay() throws FunctionalException {
        // Phasing annihilated.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8))
                .whenBattle(militaryService, this)
                .thenExpect(BattleResultBuilder.create()
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(3)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(false));
    }

    private static class BattleBuilder {
        BattleEntity battle;
        List<DiffEntity> diffs;
        boolean rotw;
        BattleSideBuilder phasing;
        BattleSideBuilder nonPhasing;

        static BattleBuilder create() {
            return new BattleBuilder();
        }

        BattleBuilder rotw(boolean rotw) {
            this.rotw = rotw;
            return this;
        }

        BattleBuilder phasing(BattleSideBuilder phasing) {
            this.phasing = phasing;
            return this;
        }

        BattleBuilder nonPhasing(BattleSideBuilder nonPhasing) {
            this.nonPhasing = nonPhasing;
            return this;
        }

        BattleBuilder whenBattle(MilitaryServiceImpl militaryService, MilitaryServiceTest testClass) throws FunctionalException {
            Pair<Request<WithdrawBeforeBattleRequest>, GameEntity> pair = testClass.testCheckGame(militaryService::withdrawBeforeBattle, "withdrawBeforeBattle");
            GameEntity game = pair.getRight();
            game.setStatus(GameStatusEnum.MILITARY_BATTLES);
            PlayableCountryEntity phasingCountry = new PlayableCountryEntity();
            phasingCountry.setName("france");
            phasingCountry.setId(1L);
            phasingCountry.setGame(game);
            phasingCountry.setLandTech(phasing.tech);
            game.getCountries().add(phasingCountry);
            PlayableCountryEntity nonPhasingCountry = new PlayableCountryEntity();
            nonPhasingCountry.setName("spain");
            nonPhasingCountry.setId(2L);
            nonPhasingCountry.setGame(game);
            nonPhasingCountry.setLandTech(nonPhasing.tech);
            game.getCountries().add(nonPhasingCountry);
            battle = new BattleEntity();
            battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
            battle.setGame(game);
            battle.setProvince("idf");
            AbstractProvinceEntity province;
            if (rotw) {
                province = new RotwProvinceEntity();
            } else {
                province = new EuropeanProvinceEntity();
            }
            province.setName("idf");
            province.setTerrain(TerrainEnum.PLAIN);
            when(testClass.provinceDao.getProvinceByName("idf")).thenReturn(province);
            battle.setTurn(1);
            long counterId = 72;
            for (CounterEntity counter : phasing.counters) {
                BattleCounterEntity bc = new BattleCounterEntity();
                counter.setId(counterId++);
                counter.setCountry(phasingCountry.getName());
                bc.setCounter(counter);
                bc.setBattle(battle);
                bc.setPhasing(true);
                battle.getCounters().add(bc);
            }
            for (CounterEntity counter : nonPhasing.counters) {
                BattleCounterEntity bc = new BattleCounterEntity();
                counter.setId(counterId++);
                counter.setCountry(nonPhasingCountry.getName());
                bc.setCounter(counter);
                bc.setBattle(battle);
                bc.setPhasing(false);
                battle.getCounters().add(bc);
            }
            game.getBattles().add(battle);
            Request<WithdrawBeforeBattleRequest> request = pair.getLeft();
            request.setRequest(new WithdrawBeforeBattleRequest());
            request.getRequest().setWithdraw(false);
            request.setIdCountry(2L);

            when(testClass.oeUtil.getTechnology(any(), anyBoolean(), any(), any(), any())).thenReturn(phasing.tech, nonPhasing.tech);
            when(testClass.oeUtil.isStackVeteran(any())).thenReturn(phasing.veteran, nonPhasing.veteran);
            when(testClass.oeUtil.lossesMitigation(anyDouble(), anyBoolean(), any())).thenReturn(AbstractWithLossEntity.create(phasing.sizeReductionDamage), AbstractWithLossEntity.create(nonPhasing.sizeReductionDamage));
            when(testClass.oeUtil.lossModificationSize(any(), anyInt())).thenAnswer(answer -> answer.getArgumentAt(0, AbstractWithLossEntity.class));
            OngoingStubbing<Integer> dice = when(testClass.oeUtil.rollDie(game));
            if (phasing.firstFire != null) {
                dice = dice.thenReturn(phasing.firstFire);
            }
            if (nonPhasing.firstFire != null) {
                dice = dice.thenReturn(nonPhasing.firstFire);
            }
            if (phasing.firstShock != null) {
                dice = dice.thenReturn(phasing.firstShock);
            }
            if (nonPhasing.firstShock != null) {
                dice = dice.thenReturn(nonPhasing.firstShock);
            }
            if (phasing.pursuit != null) {
                dice = dice.thenReturn(phasing.pursuit);
            }
            if (nonPhasing.pursuit != null) {
                dice = dice.thenReturn(nonPhasing.pursuit);
            }
            if (phasing.retreat != null) {
                dice = dice.thenReturn(phasing.retreat);
            }
            if (nonPhasing.retreat != null) {
                dice.thenReturn(nonPhasing.retreat);
            }
            if (Objects.equals(phasing.retreat, nonPhasing.retreat)) {
                when(testClass.oeUtil.retreat(phasing.retreat)).thenReturn(AbstractWithLossEntity.create(phasing.retreatLosses), AbstractWithLossEntity.create(nonPhasing.retreatLosses));
            } else {
                when(testClass.oeUtil.retreat(phasing.retreat)).thenReturn(AbstractWithLossEntity.create(phasing.retreatLosses));
                when(testClass.oeUtil.retreat(nonPhasing.retreat)).thenReturn(AbstractWithLossEntity.create(nonPhasing.retreatLosses));
            }

            when(testClass.counterDomain.removeCounter(anyLong(), any())).thenAnswer(invocation -> {
                DiffEntity diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                diff.setType(DiffTypeEnum.REMOVE);
                diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
                diff.setIdObject(invocation.getArgumentAt(0, Long.class));
                return diff;
            });

            Tables tables = new Tables();
            testClass.fillBatleTechTables(tables);

            CombatResult result = new CombatResult();
            result.setColumn("A");
            result.setDice(10);
            result.setRoundLoss(2);
            result.setThirdLoss(1);
            result.setMoraleLoss(2);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("B");
            result.setDice(7);
            result.setRoundLoss(1);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("B");
            result.setDice(8);
            result.setRoundLoss(1);
            result.setThirdLoss(1);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("B");
            result.setDice(10);
            result.setRoundLoss(2);
            result.setMoraleLoss(2);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(10);
            result.setRoundLoss(1);
            result.setThirdLoss(2);
            result.setMoraleLoss(2);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(8);
            result.setRoundLoss(1);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("E");
            result.setDice(5);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("E");
            result.setDice(8);
            result.setRoundLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("E");
            result.setDice(11);
            result.setRoundLoss(1);
            result.setThirdLoss(2);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            MilitaryServiceImpl.TABLES = tables;

            testClass.simulateDiff();

            militaryService.withdrawBeforeBattle(request);

            diffs = testClass.retrieveDiffsCreated();

            return this;
        }

        BattleBuilder thenExpect(BattleResultBuilder result) {
            Assert.assertEquals(result.end, battle.getEnd());
            Assert.assertEquals(result.winner, battle.getWinner());
            Assert.assertEquals(result.phasingThirdLosses, battle.getPhasing().getLosses().getTotalThird());
            Assert.assertEquals(result.phasingMoralLosses, CommonUtil.add(0, battle.getPhasing().getLosses().getMoraleLoss()).intValue());
            Assert.assertEquals(result.nonPhasingThirdLosses, battle.getNonPhasing().getLosses().getTotalThird());
            Assert.assertEquals(result.nonPhasingMoralLosses, CommonUtil.add(0, battle.getNonPhasing().getLosses().getMoraleLoss()).intValue());

            Assert.assertEquals(phasing.firstFire, battle.getPhasing().getFirstDay().getFire());
            Assert.assertEquals(phasing.firstShock, battle.getPhasing().getFirstDay().getShock());
            Assert.assertEquals(phasing.pursuit, battle.getPhasing().getPursuit());
            Assert.assertEquals(phasing.retreat, battle.getPhasing().getRetreat());
            Assert.assertEquals(nonPhasing.firstFire, battle.getNonPhasing().getFirstDay().getFire());
            Assert.assertEquals(nonPhasing.firstShock, battle.getNonPhasing().getFirstDay().getShock());
            Assert.assertEquals(nonPhasing.pursuit, battle.getNonPhasing().getPursuit());
            Assert.assertEquals(nonPhasing.retreat, battle.getNonPhasing().getRetreat());

            int diffsSize = 1;
            if (result.phasingAnnihilated) {
                diffsSize += phasing.counters.size();
            }
            if (result.nonPhasingAnnihilated) {
                diffsSize += nonPhasing.counters.size();
            }
            Assert.assertEquals(diffsSize, diffs.size());

            DiffEntity diff = diffs.stream().filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(diff);
            Assert.assertEquals(toString(result.end), getAttribute(diff, DiffAttributeTypeEnum.END));
            Assert.assertEquals(toString(result.winner), getAttribute(diff, DiffAttributeTypeEnum.WINNER));
            Assert.assertEquals(battle.getPhasing().getLosses().getRoundLoss() + "", getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS));
            Assert.assertEquals(battle.getPhasing().getLosses().getThirdLoss() + "", getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS));
            Assert.assertEquals(result.phasingMoralLosses == 0 ? "" : result.phasingMoralLosses + "", getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS));
            Assert.assertEquals(battle.getNonPhasing().getLosses().getRoundLoss() + "", getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS));
            Assert.assertEquals(battle.getNonPhasing().getLosses().getThirdLoss() + "", getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS));
            Assert.assertEquals(result.nonPhasingMoralLosses == 0 ? "" : result.nonPhasingMoralLosses + "", getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS));

            Assert.assertEquals(toString(phasing.firstFire), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE));
            Assert.assertEquals(toString(phasing.firstShock), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK));
            Assert.assertEquals(toString(phasing.pursuit), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT));
            Assert.assertEquals(toString(phasing.retreat), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_RETREAT));
            Assert.assertEquals(toString(nonPhasing.firstFire), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE));
            Assert.assertEquals(toString(nonPhasing.firstShock), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK));
            Assert.assertEquals(toString(nonPhasing.pursuit), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT));
            Assert.assertEquals(toString(nonPhasing.retreat), getAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_RETREAT));

            if (result.phasingAnnihilated) {
                for (CounterEntity counter : phasing.counters) {
                    Assert.assertTrue(diffs.stream()
                            .anyMatch(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                                    Objects.equals(d.getIdObject(), counter.getId())));
                }
            }
            if (result.nonPhasingAnnihilated) {
                for (CounterEntity counter : nonPhasing.counters) {
                    Assert.assertTrue(diffs.stream()
                            .anyMatch(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                                    Objects.equals(d.getIdObject(), counter.getId())));
                }
            }

            if (result.end != null) {
                boolean phasingLossesAuto = result.phasingAnnihilated || result.phasingThirdLosses == 0;
                boolean nonPhasingLossesAuto = result.nonPhasingAnnihilated || result.nonPhasingThirdLosses == 0;
                boolean phasingRetreatAuto = result.winner == BattleWinnerEnum.PHASING || result.phasingAnnihilated;
                boolean nonPhasingRetreatAuto = result.winner == BattleWinnerEnum.NON_PHASING || result.nonPhasingAnnihilated;
                if (!phasingLossesAuto || !nonPhasingLossesAuto) {
                    Assert.assertEquals(BattleStatusEnum.CHOOSE_LOSS, battle.getStatus());
                    Assert.assertEquals(BattleStatusEnum.CHOOSE_LOSS.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));
                    Assert.assertEquals(phasingLossesAuto, battle.getPhasing().isLossesSelected());
                    Assert.assertEquals(toString(phasingLossesAuto), getAttribute(diff, DiffAttributeTypeEnum.PHASING_READY));
                    Assert.assertEquals(nonPhasingLossesAuto, battle.getNonPhasing().isLossesSelected());
                    Assert.assertEquals(toString(nonPhasingLossesAuto), getAttribute(diff, DiffAttributeTypeEnum.NON_PHASING_READY));
                } else if (!phasingRetreatAuto || !nonPhasingRetreatAuto) {
                    Assert.assertEquals(BattleStatusEnum.RETREAT, battle.getStatus());
                    Assert.assertTrue(battle.getPhasing().isLossesSelected());
                    Assert.assertTrue(battle.getNonPhasing().isLossesSelected());
                    Assert.assertEquals(BattleStatusEnum.RETREAT.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));
                    Assert.assertEquals(phasingRetreatAuto, battle.getPhasing().isRetreatSelected());
                    Assert.assertEquals(toString(phasingRetreatAuto), getAttribute(diff, DiffAttributeTypeEnum.PHASING_READY));
                    Assert.assertEquals(nonPhasingRetreatAuto, battle.getNonPhasing().isRetreatSelected());
                    Assert.assertEquals(toString(nonPhasingRetreatAuto), getAttribute(diff, DiffAttributeTypeEnum.NON_PHASING_READY));
                } else {
                    Assert.assertEquals(BattleStatusEnum.DONE, battle.getStatus());
                    Assert.assertEquals(BattleStatusEnum.DONE.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));
                    Assert.assertEquals(0, battle.getCounters().size());
                }
            } else {
                Assert.assertEquals(BattleStatusEnum.WITHDRAW_AFTER_FIRST_DAY_ATT, battle.getStatus());
                Assert.assertEquals(BattleStatusEnum.WITHDRAW_AFTER_FIRST_DAY_ATT.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));
            }

            return this;
        }

        private String toString(Integer i) {
            return i == null ? null : i.toString();
        }

        private String toString(boolean i) {
            return i ? Boolean.toString(i) : null;
        }

        private <T extends Enum<T>> String toString(Enum<T> e) {
            return e == null ? null : e.name();
        }
    }

    private static class BattleSideBuilder {
        List<CounterEntity> counters = new ArrayList<>();
        boolean veteran;
        String tech;
        Integer firstFire;
        Integer firstShock;
        Integer pursuit;
        Integer retreat;
        int retreatLosses;
        int sizeReductionDamage;

        static BattleSideBuilder create() {
            return new BattleSideBuilder();
        }

        BattleSideBuilder addCounters(CounterEntity... counters) {
            this.counters.addAll(Arrays.asList(counters));
            return this;
        }

        BattleSideBuilder veteran(boolean veteran) {
            this.veteran = veteran;
            return this;
        }

        BattleSideBuilder tech(String tech) {
            this.tech = tech;
            return this;
        }

        BattleSideBuilder firstFire(Integer firstFire) {
            this.firstFire = firstFire;
            return this;
        }

        BattleSideBuilder firstShock(Integer firstShock) {
            this.firstShock = firstShock;
            return this;
        }

        BattleSideBuilder pursuit(Integer pursuit) {
            this.pursuit = pursuit;
            return this;
        }

        BattleSideBuilder retreat(Integer retreat) {
            this.retreat = retreat;
            return this;
        }

        BattleSideBuilder retreatLosses(int retreatLosses) {
            this.retreatLosses = retreatLosses;
            return this;
        }

        BattleSideBuilder sizeReductionDamage(int sizeReductionDamage) {
            this.sizeReductionDamage = sizeReductionDamage;
            return this;
        }
    }

    private static class ArmyBuilder {
        CounterFaceTypeEnum type;

        static ArmyBuilder create() {
            return new ArmyBuilder();
        }

        ArmyBuilder type(CounterFaceTypeEnum type) {
            this.type = type;
            return this;
        }

        CounterEntity toCounter() {
            CounterEntity counter = new CounterEntity();
            counter.setType(type);
            return counter;
        }
    }

    private static class BattleResultBuilder {
        BattleEndEnum end;
        BattleWinnerEnum winner;
        int phasingThirdLosses;
        int phasingMoralLosses;
        boolean phasingAnnihilated;
        int nonPhasingThirdLosses;
        int nonPhasingMoralLosses;
        boolean nonPhasingAnnihilated;

        static BattleResultBuilder create() {
            return new BattleResultBuilder();
        }

        BattleResultBuilder end(BattleEndEnum end) {
            this.end = end;
            return this;
        }

        BattleResultBuilder winner(BattleWinnerEnum winner) {
            this.winner = winner;
            return this;
        }

        BattleResultBuilder phasingThirdLosses(int phasingThirdLosses) {
            this.phasingThirdLosses = phasingThirdLosses;
            return this;
        }

        BattleResultBuilder phasingMoralLosses(int phasingMoralLosses) {
            this.phasingMoralLosses = phasingMoralLosses;
            return this;
        }

        BattleResultBuilder phasingAnnihilated(boolean phasingAnnihilated) {
            this.phasingAnnihilated = phasingAnnihilated;
            return this;
        }

        BattleResultBuilder nonPhasingThirdLosses(int nonPhasingThirdLosses) {
            this.nonPhasingThirdLosses = nonPhasingThirdLosses;
            return this;
        }

        BattleResultBuilder nonPhasingMoralLosses(int nonPhasingMoralLosses) {
            this.nonPhasingMoralLosses = nonPhasingMoralLosses;
            return this;
        }

        BattleResultBuilder nonPhasingAnnihilated(boolean nonPhasingAnnihilated) {
            this.nonPhasingAnnihilated = nonPhasingAnnihilated;
            return this;
        }
    }
}
