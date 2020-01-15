package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.IReferentielConstants;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
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
import com.mkl.eu.service.service.persistence.oe.military.BattleLossesEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.*;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.AbstractGameServiceTest;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import java.util.*;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.THIRD;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Test of MilitaryService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class BattleServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private BattleServiceImpl battleService;

    @Mock
    private IOEUtil oeUtil;

    @Mock
    private ICounterDomain counterDomain;

    @Mock
    private IStatusWorkflowDomain statusWorkflowDomain;

    @Mock
    private IProvinceDao provinceDao;

    @Test
    public void testChooseBattleFail() {
        Pair<Request<ChooseProvinceRequest>, GameEntity> pair = testCheckGame(battleService::chooseBattle, "chooseBattle");
        Request<ChooseProvinceRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, battleService::chooseBattle, "chooseBattle", GameStatusEnum.MILITARY_BATTLES);

        try {
            battleService.chooseBattle(request);
            Assert.fail("Should break because chooseBattle.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseProvinceRequest());

        try {
            battleService.chooseBattle(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("");

        try {
            battleService.chooseBattle(request);
            Assert.fail("Should break because province is empty");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("idf");

        try {
            battleService.chooseBattle(request);
            Assert.fail("Should break because another battle is in process");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_IN_PROCESS, e.getCode());
            Assert.assertEquals("chooseBattle", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.NEW);

        try {
            battleService.chooseBattle(request);
            Assert.fail("Should break because no battle is in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseBattleTooMuchForces() throws FunctionalException {
        testChooseBattle(true, false, false, false);
    }

    @Test
    public void testChooseBattleTooMuchLeadingCountries() throws FunctionalException {
        testChooseBattle(false, true, false, false);
    }

    @Test
    public void testChooseBattleTooMuchLeaders() throws FunctionalException {
        testChooseBattle(false, false, true, false);
    }

    @Test
    public void testChooseBattleTooMuchEverything() throws FunctionalException {
        testChooseBattle(true, true, true, false);
    }

    @Test
    public void testChooseBattleToWithdrawBeforeBattle() throws FunctionalException {
        testChooseBattle(false, false, false, true);
    }

    private void testChooseBattle(boolean tooMuchforces, boolean tooMuchLeadingCountries, boolean tooMuchLeaders, boolean forcesSelected) throws FunctionalException {
        Pair<Request<ChooseProvinceRequest>, GameEntity> pair = testCheckGame(battleService::chooseBattle, "chooseBattle");
        Request<ChooseProvinceRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(26L);
        BattleEntity battle = new BattleEntity();
        battle.setId(33L);
        battle.setStatus(BattleStatusEnum.NEW);
        battle.setProvince("idf");
        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        game.getBattles().add(battle);
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


        request.getGame().setIdCountry(26L);
        request.setRequest(new ChooseProvinceRequest());
        request.getRequest().setProvince("idf");
        testCheckStatus(pair.getRight(), request, battleService::chooseBattle, "chooseBattle", GameStatusEnum.MILITARY_BATTLES);

        List<String> allies = new ArrayList<>();
        allies.add("france");
        List<String> enemies = new ArrayList<>();
        enemies.add("espagne");
        if (tooMuchforces) {
            allies.add("turquie");
            enemies.add("angleterre");
        }
        List<String> leadingCountries = new ArrayList<>();
        leadingCountries.add("france");
        if (tooMuchLeadingCountries) {
            leadingCountries.add("turquie");
        }
        List<Leader> leaders = new ArrayList<>();
        Leader leader = new Leader();
        leader.setCode("Napo");
        leaders.add(leader);
        if (tooMuchLeaders) {
            leaders.add(null);
        }
        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(allies);
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(enemies);
        when(oeUtil.getLeadingCountries(any())).thenReturn(leadingCountries);
        when(oeUtil.getLeaders(any(), any(), any())).thenReturn(leaders);

        simulateDiff();

        DiffResponse response = battleService.chooseBattle(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.BATTLE, diffEntity.getTypeObject());
        Assert.assertEquals(game.getBattles().get(0).getId(), diffEntity.getIdObject());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(game.getId(), diffEntity.getIdGame());
        if (forcesSelected) {
            Assert.assertEquals(9, diffEntity.getAttributes().size());
            Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
            Assert.assertEquals("true", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_READY));
            Assert.assertEquals("france", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_COUNTRY));
            Assert.assertEquals("france", battle.getPhasing().getCountry());
            Assert.assertEquals("Napo", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_LEADER));
            Assert.assertEquals("Napo", battle.getPhasing().getLeader());
            Assert.assertEquals("1", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_COUNTER_ADD));
            Assert.assertEquals("true", getAttribute(diffEntity, DiffAttributeTypeEnum.NON_PHASING_READY));
            Assert.assertEquals("france", getAttribute(diffEntity, DiffAttributeTypeEnum.NON_PHASING_COUNTRY));
            Assert.assertEquals("france", battle.getNonPhasing().getCountry());
            Assert.assertEquals("Napo", getAttribute(diffEntity, DiffAttributeTypeEnum.NON_PHASING_LEADER));
            Assert.assertEquals("Napo", battle.getNonPhasing().getLeader());
            Assert.assertEquals("5", getAttribute(diffEntity, DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD));
        } else {
            Assert.assertEquals(1, diffEntity.getAttributes().size());
            Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffEntity.getAttributes().get(0).getType());
            Assert.assertEquals(BattleStatusEnum.SELECT_FORCES.name(), diffEntity.getAttributes().get(0).getValue());
        }

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());

        if (forcesSelected) {
            Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE, game.getBattles().get(0).getStatus());
            Assert.assertEquals(2, game.getBattles().get(0).getCounters().size());
            BattleCounterEntity counterFra = game.getBattles().get(0).getCounters().stream()
                    .filter(c -> c.getCounter().equals(1L))
                    .findAny()
                    .orElse(null);
            Assert.assertEquals(true, counterFra.isPhasing());
            BattleCounterEntity counterEsp = game.getBattles().get(0).getCounters().stream()
                    .filter(c -> c.getCounter().equals(5L))
                    .findAny()
                    .orElse(null);
            Assert.assertEquals(false, counterEsp.isPhasing());
        } else {
            Assert.assertEquals(BattleStatusEnum.SELECT_FORCES, game.getBattles().get(0).getStatus());
        }
    }

    @Test
    public void testSelectForceFail() {
        Pair<Request<SelectForcesRequest>, GameEntity> pair = testCheckGame(battleService::selectForces, "selectForces");
        Request<SelectForcesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        BattleEntity battle = new BattleEntity();
        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        battle.setProvince("pecs");
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        game.getBattles().add(battle);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, battleService::selectForces, "selectForces", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(26L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because selectForces.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request", e.getParams()[0]);
        }

        request.setRequest(new SelectForcesRequest());
        request.getGame().setIdCountry(12L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        request.getRequest().getForces().add(6L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because no battle is in right status");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("selectForces", e.getParams()[0]);
        }

        battle.setStatus(BattleStatusEnum.SELECT_FORCES);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because counter does not exist in the battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Collections.singletonList("france"));
        StackEntity stack = new StackEntity();
        game.getStacks().add(stack);
        stack.setProvince("pecs");
        stack.setCountry("france");
        stack.getCounters().add(new CounterEntity());
        stack.getCounters().get(0).setId(6L);
        stack.getCounters().get(0).setType(CounterFaceTypeEnum.TECH_MANOEUVRE);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because counter is not an army");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        stack.getCounters().get(0).setType(CounterFaceTypeEnum.ARMY_PLUS);
        stack.setCountry("pologne");

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because counter is not owned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        stack.setCountry("france");
        stack.setProvince("idf");

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because counter is not in the right province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        stack.setProvince("pecs");
        battle.getNonPhasing().setForces(true);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because the attacker already validated its forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_SELECT_VALIDATED, e.getCode());
            Assert.assertEquals("selectForces", e.getParams()[0]);
        }

        battle.getNonPhasing().setForces(false);
        stack.getCounters().add(createCounter(7L, "pologne", CounterFaceTypeEnum.ARMY_PLUS));
        stack.getCounters().add(createCounter(8L, "pologne", CounterFaceTypeEnum.LAND_DETACHMENT));
        stack.getCounters().add(createCounter(9L, "pologne", CounterFaceTypeEnum.LAND_DETACHMENT));
        stack.getCounters().add(createCounter(10L, "pologne", CounterFaceTypeEnum.LAND_DETACHMENT));
        request.getRequest().getForces().add(8L);
        request.getRequest().getForces().add(9L);
        request.getRequest().getForces().add(10L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because you cannot select 4 counters in the battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_TOO_BIG, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        request.getRequest().getForces().clear();
        battle.getCounters().clear();
        request.getRequest().getForces().add(6L);
        request.getRequest().getForces().add(7L);
        request.getRequest().getForces().add(8L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because you cannot counters of size 9 in the battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_TOO_BIG, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        request.getRequest().getForces().clear();
        battle.getCounters().clear();
        request.getRequest().getForces().add(6L);
        request.getRequest().getForces().add(7L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because leading country is ambiguous");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_LEADING_COUNTRY_AMBIGUOUS, e.getCode());
            Assert.assertEquals("selectForces.request.country", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().setCountry("espagne");
        when(oeUtil.getLeadingCountries(any())).thenReturn(Arrays.asList("france", "pologne"));

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because selected country cannot lead the battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_LEADING_COUNTRY_AMBIGUOUS, e.getCode());
            Assert.assertEquals("selectForces.request.country", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().setCountry("france");
        Tables tables = new Tables();
        AbstractBack.TABLES = tables;
        stack.getCounters().add(createLeader(21L, "france", CounterFaceTypeEnum.LEADER, "Napo", LeaderTypeEnum.GENERAL, "A 666 -1", tables, stack));
        stack.getCounters().add(createLeader(22L, "france", CounterFaceTypeEnum.LEADER, "Nabo", LeaderTypeEnum.GENERAL, "Z 111", tables, stack));
        stack.getCounters().add(createLeader(23L, "espagne", CounterFaceTypeEnum.LEADER, "Infante", LeaderTypeEnum.GENERAL, "B 333", tables, stack));
        stack.getCounters().add(createLeader(24L, "pologne", CounterFaceTypeEnum.LEADER, "Sibierski", LeaderTypeEnum.GENERAL, "D 434", tables, stack));
        stack.getCounters().add(createLeader(25L, "pologne", CounterFaceTypeEnum.LEADER, "Sibierluge", LeaderTypeEnum.ADMIRAL, "C 122", tables, stack));

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because no leader selected while some are eligible");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_INVALID_LEADER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().getForces().add(22L);
        request.getRequest().getForces().add(24L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because only one leader can lead this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_TOO_MANY_LEADERS, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().getForces().remove(22L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because the leader can not lead this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_NOT_SUITABLE_LEADER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().getForces().remove(24L);
        request.getRequest().getForces().add(22L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because there is a higher rank eligible leader than the one selected");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_INVALID_LEADER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().setCountry("pologne");
        request.getRequest().getForces().remove(22L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because no leader selected while some are eligible");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_INVALID_LEADER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }

        battle.getCounters().clear();
        request.getRequest().getForces().add(25L);

        try {
            battleService.selectForces(request);
            Assert.fail("Should break because the leader can not lead this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_FORCES_NOT_SUITABLE_LEADER, e.getCode());
            Assert.assertEquals("selectForces.request.forces", e.getParams()[0]);
        }
    }

    @Test
    public void testSelectForcesValidatePhasingSuccess() throws FunctionalException {
        testSelectForcesSuccess(true);
    }

    @Test
    public void testSelectForcesValidateNonPhasingSuccess() throws FunctionalException {
        testSelectForcesSuccess(false);
    }

    private void testSelectForcesSuccess(boolean phasing) throws FunctionalException {
        Pair<Request<SelectForcesRequest>, GameEntity> pair = testCheckGame(battleService::selectForces, "selectForces");
        Request<SelectForcesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(12L);
        country.setName("france");
        game.getCountries().add(country);
        BattleEntity battle = new BattleEntity();
        battle.setStatus(BattleStatusEnum.SELECT_FORCES);
        battle.getNonPhasing().setForces(phasing);
        battle.getPhasing().setForces(!phasing);
        battle.setProvince("pecs");
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        game.getBattles().add(battle);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        testCheckStatus(pair.getRight(), request, battleService::selectForces, "selectForces", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(12L);
        request.setRequest(new SelectForcesRequest());
        request.getRequest().getForces().add(5L);
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
        counter.setId(5L);
        counter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        counter.setCountry("france");
        game.getStacks().get(0).getCounters().add(counter);
        counter = new CounterEntity();
        counter.setId(8L);
        counter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        counter.setCountry("spain");
        game.getStacks().get(0).getCounters().add(counter);

        when(oeUtil.getWarFaction(battle.getWar(), phasing ? battle.isPhasingOffensive() : !battle.isPhasingOffensive()))
                .thenReturn(Collections.singletonList(country.getName()));

        Tables tables = new Tables();
        if (phasing) {
            CountryOrderEntity order = new CountryOrderEntity();
            order.setActive(true);
            order.setCountry(country);
            game.getOrders().add(order);

            CounterEntity counterLeader = new CounterEntity();
            counterLeader.setId(9L);
            counterLeader.setCountry(country.getName());
            counterLeader.setType(CounterFaceTypeEnum.LEADER);
            counterLeader.setCode("Napo");
            game.getStacks().get(0).getCounters().add(counterLeader);

            request.getRequest().getForces().add(9L);

            Leader leader = new Leader();
            leader.setCode("Napo");
            leader.setCountry("france");
            leader.setType(LeaderTypeEnum.GENERAL);
            leader.setRank("A");
            tables.getLeaders().add(leader);
        }
        when(oeUtil.getLeadingCountries(any())).thenReturn(Collections.singletonList("france"));
        AbstractBack.TABLES = tables;

        simulateDiff();

        DiffResponse response = battleService.selectForces(request);

        DiffEntity diffEntity = retrieveDiffCreated();

        Assert.assertEquals(DiffTypeEnum.MODIFY, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.BATTLE, diffEntity.getTypeObject());
        Assert.assertEquals(battle.getId(), diffEntity.getIdObject());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(game.getId(), diffEntity.getIdGame());
        Assert.assertTrue(diffEntity.getAttributes().size() >= 1);
        DiffAttributeTypeEnum diffStatus;
        if (phasing) {
            diffStatus = DiffAttributeTypeEnum.PHASING_READY;

            Assert.assertEquals(true, battle.getPhasing().isForces());
            Assert.assertEquals("france", battle.getPhasing().getCountry());
            Assert.assertEquals("Napo", battle.getPhasing().getLeader());
            Assert.assertEquals("Napo", getAttribute(diffEntity, DiffAttributeTypeEnum.PHASING_LEADER));
        } else {
            diffStatus = DiffAttributeTypeEnum.NON_PHASING_READY;

            Assert.assertEquals(true, battle.getNonPhasing().isForces());
            Assert.assertEquals("france", battle.getNonPhasing().getCountry());
            Assert.assertEquals(null, battle.getNonPhasing().getLeader());
        }
        Assert.assertEquals(phasing ? 8 : 7, diffEntity.getAttributes().size());
        Assert.assertEquals(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE.name(), getAttribute(diffEntity, DiffAttributeTypeEnum.STATUS));
        Assert.assertEquals("true", getAttribute(diffEntity, diffStatus));
        Assert.assertEquals("france", getAttribute(diffEntity, phasing ? DiffAttributeTypeEnum.PHASING_COUNTRY : DiffAttributeTypeEnum.NON_PHASING_COUNTRY));
        Assert.assertEquals(phasing ? 4l : 3l, diffEntity.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_COUNTER_ADD && phasing || attr.getType() == DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD)
                .count());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testFillBattleModifiers() {
        BattleEntity battle = new BattleEntity();
        battle.setProvince("idf");

        BattleCounterEntity battleCounter = new BattleCounterEntity();
        battleCounter.setPhasing(true);
        battleCounter.setCounter(1L);
        battleCounter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        battleCounter.setCountry(PlayableCountry.FRANCE);
        battle.getCounters().add(battleCounter);
        battleCounter = new BattleCounterEntity();
        battleCounter.setPhasing(false);
        battleCounter.setCounter(2L);
        battleCounter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battleCounter.setCountry(PlayableCountry.SPAIN);
        battle.getCounters().add(battleCounter);

        StackEntity stack = new StackEntity();
        stack.setProvince(battle.getProvince());
        battle.setGame(new GameEntity());
        battle.getGame().getStacks().add(stack);
        CounterEntity phasingCounter = new CounterEntity();
        phasingCounter.setId(1L);
        phasingCounter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        phasingCounter.setCountry(PlayableCountry.FRANCE);
        stack.getCounters().add(phasingCounter);
        CounterEntity nonPhasingCounter = new CounterEntity();
        nonPhasingCounter.setId(2L);
        nonPhasingCounter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        nonPhasingCounter.setCountry(PlayableCountry.SPAIN);
        stack.getCounters().add(nonPhasingCounter);

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
        BattleServiceImpl.TABLES = new Tables();
        fillBatleTechTables(BattleServiceImpl.TABLES);

        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        when(provinceDao.getProvinceByName("morbihan")).thenReturn(morbihan);
        when(provinceDao.getProvinceByName("lyonnais")).thenReturn(lyonnais);
        when(provinceDao.getProvinceByName("limoges")).thenReturn(limoges);
        when(provinceDao.getProvinceByName("neva")).thenReturn(neva);
        when(provinceDao.getProvinceByName("tyrol")).thenReturn(tyrol);

        when(oeUtil.getTechnology(Collections.singletonList(phasingCounter), true, battleService.getReferential(), battleService.getTables(), battle.getGame()))
                .thenReturn(Tech.ARQUEBUS);
        when(oeUtil.getTechnology(Collections.singletonList(nonPhasingCounter), true, battleService.getReferential(), battleService.getTables(), battle.getGame()))
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
        battle.getPhasing().setLeader("phasing");
        battle.getNonPhasing().setLeader("notPhasing");
        Leader phasingLeader = new Leader();
        phasingLeader.setCode("phasing");
        BattleServiceImpl.TABLES.getLeaders().add(phasingLeader);
        Leader notPhasingLeader = new Leader();
        notPhasingLeader.setCode("notPhasing");
        BattleServiceImpl.TABLES.getLeaders().add(notPhasingLeader);
        when(oeUtil.getArmyInfo(Collections.singletonList(phasingCounter), battleService.getReferential())).thenReturn(armyPhasing);
        when(oeUtil.getArmyInfo(Collections.singletonList(nonPhasingCounter), battleService.getReferential())).thenReturn(armyNonPhasing);

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
        stack.setProvince(battle.getProvince());
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("lyonnais");
        stack.setProvince(battle.getProvince());
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("limoges");
        stack.setProvince(battle.getProvince());
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("neva");
        stack.setProvince(battle.getProvince());
        checkModifiers(battle, Modifiers.init(-1));

        battle.setProvince("tyrol");
        stack.setProvince(battle.getProvince());
        checkModifiers(battle, Modifiers.init(-1)
                .addFireNonPhasingFirstDay(1)
                .addShockNonPhasingFirstDay(1)
                .addFireNonPhasingSecondDay(1)
                .addShockNonPhasingSecondDay(1));

        when(oeUtil.getArtilleryBonus(armyPhasing, battleService.getTables(), battle.getGame())).thenReturn(6);
        when(oeUtil.getArtilleryBonus(armyNonPhasing, battleService.getTables(), battle.getGame())).thenReturn(5);

        battle.setProvince("idf");
        stack.setProvince(battle.getProvince());
        checkModifiers(battle, Modifiers.init(0)
                .addFirePhasingFirstDay(1)
                .addFirePhasingSecondDay(1));

        when(oeUtil.getArtilleryBonus(armyNonPhasing, battleService.getTables(), battle.getGame())).thenReturn(7);
        checkModifiers(battle, Modifiers.init(0)
                .addFirePhasingFirstDay(1)
                .addFirePhasingSecondDay(1)
                .addFireNonPhasingFirstDay(1)
                .addFireNonPhasingSecondDay(1));


        when(oeUtil.getArtilleryBonus(armyPhasing, battleService.getTables(), battle.getGame())).thenReturn(0);
        when(oeUtil.getArtilleryBonus(armyNonPhasing, battleService.getTables(), battle.getGame())).thenReturn(1);
        when(oeUtil.getCavalryBonus(armyPhasing, TerrainEnum.PLAIN, battleService.getTables(), battle.getGame())).thenReturn(true);
        when(oeUtil.getCavalryBonus(armyNonPhasing, TerrainEnum.PLAIN, battleService.getTables(), battle.getGame())).thenReturn(false);
        checkModifiers(battle, Modifiers.init(0)
                .addShockPhasingFirstDay(1)
                .addShockPhasingSecondDay(1));

        when(oeUtil.getCavalryBonus(armyPhasing, TerrainEnum.PLAIN, battleService.getTables(), battle.getGame())).thenReturn(false);
        when(oeUtil.getCavalryBonus(armyNonPhasing, TerrainEnum.PLAIN, battleService.getTables(), battle.getGame())).thenReturn(true);
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
        stack.setProvince(battle.getProvince());
        when(oeUtil.getArtilleryBonus(armyPhasing, battleService.getTables(), battle.getGame())).thenReturn(10);
        when(oeUtil.getArtilleryBonus(armyNonPhasing, battleService.getTables(), battle.getGame())).thenReturn(1);
        when(oeUtil.getCavalryBonus(armyPhasing, TerrainEnum.SPARSE_FOREST, battleService.getTables(), battle.getGame())).thenReturn(true);
        when(oeUtil.getCavalryBonus(armyNonPhasing, TerrainEnum.SPARSE_FOREST, battleService.getTables(), battle.getGame())).thenReturn(false);
        checkModifiers(battle, Modifiers.init(-1)
                .addFirePhasingFirstDay(1)
                .addFirePhasingSecondDay(1)
                .addShockPhasingFirstDay(2)
                .addShockPhasingSecondDay(2)
                .addFireNonPhasingFirstDay(-1)
                .addFireNonPhasingSecondDay(-1));

        phasingCounter.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        nonPhasingCounter.setType(CounterFaceTypeEnum.ARMY_PLUS);
        checkModifiers(battle, Modifiers.init(-1)
                .addShockPhasingFirstDay(1)
                .addShockPhasingSecondDay(1)
                .addShockNonPhasingFirstDay(1)
                .addShockNonPhasingSecondDay(1));

        phasingLeader.setFire(5);
        phasingLeader.setShock(3);
        notPhasingLeader.setFire(2);
        notPhasingLeader.setShock(4);
        checkModifiers(battle, Modifiers.init(-1)
                .addFirePhasingFirstDay(3)
                .addFirePhasingSecondDay(3)
                .addFireNonPhasingFirstDay(-3)
                .addFireNonPhasingSecondDay(-3)
                .addShockPhasingFirstDay(0)
                .addShockPhasingSecondDay(0)
                .addShockNonPhasingFirstDay(2)
                .addShockNonPhasingSecondDay(2));
    }

    private void checkModifiers(BattleEntity battle, Modifiers modifiers) {
        battleService.fillBattleModifiers(battle);

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
        Pair<Request<WithdrawBeforeBattleRequest>, GameEntity> pair = testCheckGame(battleService::withdrawBeforeBattle, "withdrawBeforeBattle");
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
        testCheckStatus(pair.getRight(), request, battleService::withdrawBeforeBattle, "withdrawBeforeBattle", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(26L);

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because withdrawBeforeBattle.idCountry is the phasing player");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_ONLY_NON_PHASING_CAN_WITHDRAW, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(27L);

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because withdrawBeforeBattle.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request", e.getParams()[0]);
        }

        request.setRequest(new WithdrawBeforeBattleRequest());
        request.getRequest().setWithdraw(true);

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because battle is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("");

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province is empty");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("toto");

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(new EuropeanProvinceEntity());

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because province is not next to battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("orleans");
        when(provinceDao.getProvinceByName("orleans")).thenReturn(orleans);

        try {
            battleService.withdrawBeforeBattle(request);
            Assert.fail("Should break because cannot retreat in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_CANT_WITHDRAW, e.getCode());
            Assert.assertEquals("withdrawBeforeBattle.request.provinceTo", e.getParams()[0]);
        }
    }

    @Test
    public void testWithdrawBeforeBattleSuccess() throws FunctionalException {
        Pair<Request<WithdrawBeforeBattleRequest>, GameEntity> pair = testCheckGame(battleService::withdrawBeforeBattle, "withdrawBeforeBattle");
        Request<WithdrawBeforeBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        StackEntity stack1 = new StackEntity();
        stack1.setId(1l);
        stack1.setProvince("idf");
        stack1.setCountry("france");
        stack1.getCounters().add(createCounter(1l, "france", CounterFaceTypeEnum.ARMY_PLUS, stack1));
        stack1.getCounters().add(createCounter(11l, "france", CounterFaceTypeEnum.LAND_DETACHMENT, stack1));
        game.getStacks().add(stack1);
        StackEntity stack2 = new StackEntity();
        stack2.setId(2l);
        stack2.setProvince("idf");
        stack2.setCountry("spain");
        stack2.getCounters().add(createCounter(2l, "spain", CounterFaceTypeEnum.ARMY_MINUS, stack2));
        game.getStacks().add(stack2);
        StackEntity stack3 = new StackEntity();
        stack3.setId(3l);
        stack3.setProvince("idf");
        stack3.setCountry("savoie");
        stack3.getCounters().add(createCounter(3l, "savoie", CounterFaceTypeEnum.LAND_DETACHMENT, stack3));
        game.getStacks().add(stack3);
        StackEntity stack4 = new StackEntity();
        stack4.setId(3l);
        stack4.setProvince("idf");
        stack4.setCountry("france");
        stack4.getCounters().add(createCounter(4l, "france", CounterFaceTypeEnum.MNU_ART_MINUS, stack4));
        game.getStacks().add(stack4);

        BattleEntity battle = new BattleEntity();
        battle.setGame(game);
        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        battle.setProvince("idf");
        game.getBattles().add(battle);
        stack1.getCounters().forEach(counter -> {
            BattleCounterEntity bc = new BattleCounterEntity();
            bc.setPhasing(false);
            bc.setCounter(counter.getId());
            bc.setCountry(counter.getCountry());
            bc.setType(counter.getType());
            battle.getCounters().add(bc);
        });
        stack2.getCounters().forEach(counter -> {
            BattleCounterEntity bc = new BattleCounterEntity();
            bc.setPhasing(true);
            bc.setCounter(counter.getId());
            bc.setCountry(counter.getCountry());
            bc.setType(counter.getType());
            battle.getCounters().add(bc);
        });
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(27L);
        country.setName("france");
        game.getCountries().add(country);
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
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
        when(oeUtil.getController(stack1)).thenReturn("france");
        when(oeUtil.getController(stack2)).thenReturn("spain");
        testCheckStatus(pair.getRight(), request, battleService::withdrawBeforeBattle, "withdrawBeforeBattle", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);
        request.setRequest(new WithdrawBeforeBattleRequest());
        request.getRequest().setWithdraw(true);
        request.getRequest().setProvinceTo("orleans");
        BattleServiceImpl.TABLES = new Tables();
        BattleServiceImpl.TABLES.getBattleTechs().add(new BattleTech());
        simulateDiff();

        when(oeUtil.rollDie(game, country)).thenReturn(5);
        when(oeUtil.rollDie(game)).thenReturn(3);
        Leader napo = new Leader();
        napo.setCode("Napo");
        napo.setManoeuvre(3);
        Leader nabo = new Leader();
        nabo.setCode("Nabo");
        nabo.setManoeuvre(3);
        AbstractBack.TABLES.getLeaders().add(napo);
        AbstractBack.TABLES.getLeaders().add(nabo);
        when(oeUtil.isMobile(stack1)).thenReturn(true);
        when(oeUtil.isMobile(stack2)).thenReturn(true);
        when(oeUtil.isMobile(stack3)).thenReturn(true);
        Referential ref = new Referential();
        CountryReferential france = new CountryReferential();
        france.setType(CountryTypeEnum.MAJOR);
        france.setName("france");
        ref.getCountries().add(france);
        CountryReferential hollande = new CountryReferential();
        hollande.setType(CountryTypeEnum.MINORMAJOR);
        hollande.setName("hollande");
        ref.getCountries().add(hollande);
        CountryReferential savoie = new CountryReferential();
        savoie.setType(CountryTypeEnum.MINOR);
        savoie.setName("savoie");
        ref.getCountries().add(savoie);
        AbstractBack.REFERENTIAL = ref;
        Leader leader = new Leader();
        leader.setCode("france-general-3");
        AbstractBack.TABLES.getLeaders().add(leader);
        leader = new Leader();
        leader.setCode("hollande-general-3");
        AbstractBack.TABLES.getLeaders().add(leader);
        leader = new Leader();
        leader.setCode("minor-general-3");
        AbstractBack.TABLES.getLeaders().add(leader);
        leader = new Leader();
        leader.setCode("natives-general-3");
        AbstractBack.TABLES.getLeaders().add(leader);
        battle.getPhasing().setCountry("france");
        battle.getNonPhasing().setCountry("hollande");

        battleService.withdrawBeforeBattle(request);

        Assert.assertTrue(battle.getEnd() != BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
        Assert.assertEquals("france-general-3", battle.getPhasing().getLeader());
        Assert.assertEquals("hollande-general-3", battle.getNonPhasing().getLeader());

        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        battle.getNonPhasing().setLeader(null);
        battle.getPhasing().setLeader(null);
        battle.getPhasing().setCountry("savoie");
        battle.getNonPhasing().setCountry(null);

        battleService.withdrawBeforeBattle(request);

        Assert.assertTrue(battle.getEnd() != BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
        Assert.assertEquals("minor-general-3", battle.getPhasing().getLeader());
        Assert.assertEquals("natives-general-3", battle.getNonPhasing().getLeader());

        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        battle.getNonPhasing().setLeader("Napo");
        battle.getPhasing().setLeader("Nabo");

        battleService.withdrawBeforeBattle(request);

        Assert.assertTrue(battle.getEnd() != BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
        Assert.assertEquals("Nabo", battle.getPhasing().getLeader());
        Assert.assertEquals("Napo", battle.getNonPhasing().getLeader());

        battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
        request.getRequest().setProvinceTo("idf");

        battleService.withdrawBeforeBattle(request);

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
        napo.setManoeuvre(6);

        battleService.withdrawBeforeBattle(request);

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
        Assert.assertNull(getAttributeFull(diffStack1, DiffAttributeTypeEnum.BESIEGED));
        diffStack3 = diffs.stream()
                .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.STACK
                        && Objects.equals(diff.getIdObject(), stack3.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diffStack3);
        Assert.assertEquals("idf", getAttribute(diffStack3, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("orleans", getAttribute(diffStack3, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(diffStack3, DiffAttributeTypeEnum.MOVE_PHASE));
        Assert.assertNull(getAttributeFull(diffStack3, DiffAttributeTypeEnum.BESIEGED));
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
    public void testRetreatFirstDayFail() {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(battleService::retreatFirstDay, "retreatFirstDay");
        Request<ValidateRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        BattleEntity battle = new BattleEntity();
        battle.setStatus(BattleStatusEnum.SELECT_FORCES);
        battle.setProvince("idf");
        game.getBattles().add(battle);
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(1L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(1L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
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
        testCheckStatus(pair.getRight(), request, battleService::retreatFirstDay, "retreatFirstDay", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);

        try {
            battleService.retreatFirstDay(request);
            Assert.fail("Should break because retreatFirstDay.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("retreatFirstDay.request", e.getParams()[0]);
        }

        request.setRequest(new ValidateRequest());

        try {
            battleService.retreatFirstDay(request);
            Assert.fail("Should break because battle is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("retreatFirstDay", e.getParams()[0]);
        }

        battle.setStatus(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT);

        try {
            battleService.retreatFirstDay(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatFirstDay.request.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));

        try {
            battleService.retreatFirstDay(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatFirstDay.request.idCountry", e.getParams()[0]);
        }

        battle.setStatus(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF);
        request.getGame().setIdCountry(26L);

        try {
            battleService.retreatFirstDay(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatFirstDay.request.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));

        try {
            battleService.retreatFirstDay(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatFirstDay.request.idCountry", e.getParams()[0]);
        }
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
                .whenBattle(battleService, this)
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
    public void testRoutedSecondFire() throws FunctionalException {
        // Phasing routed.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreatFirstDayResult(false)
                        .secondFire(6)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreatFirstDayResult(false)
                        .secondFire(7)
                        .pursuit(2))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_SECOND_FIRE)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(12)
                        .phasingMoralLosses(4)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(false));

        // Phasing routed but non phasing annihilated.
        // Non passive has only 3 detachments so its losses are capped.
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(8)
                        .firstShock(8)
                        .retreatFirstDayResult(false)
                        .secondFire(8))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_MINUS).toCounter(),
                                ArmyBuilder.create().type(CounterFaceTypeEnum.LAND_DETACHMENT).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreatFirstDayResult(false)
                        .secondFire(7)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_SECOND_FIRE)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(4)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(true));
    }

    @Test
    public void testMissedRetreatFirstDay() throws FunctionalException {
        // without failed retreat, no annihilation and no route
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(8)
                        .firstShock(8)
                        .secondFire(7)
                        .secondShock(7)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(8)
                        .firstShock(8)
                        .secondFire(7)
                        .secondShock(7)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.END_OF_SECOND_DAY)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(9)
                        .phasingMoralLosses(2)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(false));

        // with failed retreat, double annihilation and double route
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(8)
                        .firstShock(8)
                        .retreatFirstDayResult(false)
                        .secondFire(7)
                        .secondShock(7)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(8)
                        .firstShock(8)
                        .retreatFirstDayResult(false)
                        .secondFire(7)
                        .secondShock(7)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.ROUTED_AT_SECOND_SHOCK)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(12)
                        .phasingMoralLosses(4)
                        .phasingAnnihilated(true)
                        .nonPhasingThirdLosses(12)
                        .nonPhasingMoralLosses(4)
                        .nonPhasingAnnihilated(true));
    }

    @Test
    public void testPasseddRetreatFirstDay() throws FunctionalException {
        // Phasing retreated the first day while winning
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .retreatFirstDayResult(true)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(6)
                        .firstShock(7)
                        .retreatFirstDayResult(false)
                        .pursuit(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.RETREAT_AT_FIRST_DAY)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(3)
                        .phasingMoralLosses(1)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(false));

        // Non phasing retreated the first day while loosing
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .pursuit(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(6)
                        .firstShock(7)
                        .retreatFirstDayResult(true)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.RETREAT_AT_FIRST_DAY)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(3)
                        .phasingMoralLosses(1)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(false));

        // Non phasing retreated the first day but is annihilated at pursuit
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(10)
                        .firstShock(8)
                        .pursuit(6))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(6)
                        .firstShock(7)
                        .retreatFirstDayResult(true)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.RETREAT_AT_FIRST_DAY)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(3)
                        .phasingMoralLosses(1)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(12)
                        .nonPhasingMoralLosses(3)
                        .nonPhasingAnnihilated(true));
    }

    @Test
    public void testTwoDayBattle() throws FunctionalException {
        // No casualty, same moral, tie
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(4)
                        .firstShock(4)
                        .secondFire(4)
                        .secondShock(4)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(4)
                        .firstShock(4)
                        .secondFire(4)
                        .secondShock(4)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.END_OF_SECOND_DAY)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(0)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(0)
                        .nonPhasingMoralLosses(0)
                        .nonPhasingAnnihilated(false));

        // No casualty, more moral at start of battle wins
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(4)
                        .firstShock(4)
                        .secondFire(4)
                        .secondShock(4)
                        .pursuit(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .tech(Tech.MUSKET)
                        .firstFire(4)
                        .firstShock(4)
                        .secondFire(4)
                        .secondShock(4)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.END_OF_SECOND_DAY)
                        .winner(BattleWinnerEnum.PHASING)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(0)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(0)
                        .nonPhasingMoralLosses(0)
                        .nonPhasingAnnihilated(false));

        // Phasing doing morale damage, but with lower starting moral still loses the battle
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(8)
                        .firstShock(6)
                        .secondFire(7)
                        .secondShock(7)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(4)
                        .firstShock(4)
                        .secondFire(4)
                        .secondShock(4)
                        .pursuit(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.END_OF_SECOND_DAY)
                        .winner(BattleWinnerEnum.NON_PHASING)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(0)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(6)
                        .nonPhasingMoralLosses(1)
                        .nonPhasingAnnihilated(false));

        // Phasing doing big morale damage, but with lower starting moral will do a tie - remember arquebus do half damage at fire
        BattleBuilder.create()
                .rotw(false)
                .phasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(false)
                        .tech(Tech.ARQUEBUS)
                        .firstFire(9)
                        .firstShock(6)
                        .secondFire(10)
                        .secondShock(7)
                        .retreat(1))
                .nonPhasing(BattleSideBuilder.create()
                        .addCounters(ArmyBuilder.create().type(CounterFaceTypeEnum.ARMY_PLUS).toCounter())
                        .veteran(true)
                        .tech(Tech.MUSKET)
                        .firstFire(4)
                        .firstShock(4)
                        .secondFire(4)
                        .secondShock(4)
                        .retreat(1))
                .whenBattle(battleService, this)
                .thenExpect(BattleResultBuilder.create()
                        .end(BattleEndEnum.END_OF_SECOND_DAY)
                        .winner(BattleWinnerEnum.NONE)
                        .phasingThirdLosses(0)
                        .phasingMoralLosses(0)
                        .phasingAnnihilated(false)
                        .nonPhasingThirdLosses(9)
                        .nonPhasingMoralLosses(2)
                        .nonPhasingAnnihilated(false));
    }

    private static class BattleBuilder {
        BattleEntity battle;
        List<DiffEntity> diffsFirstDay;
        List<DiffEntity> diffsFirstRetreat;
        List<DiffEntity> diffsSecondDay;
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

        BattleBuilder whenBattle(BattleServiceImpl militaryService, BattleServiceTest testClass) throws FunctionalException {
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
            CountryOrderEntity order = new CountryOrderEntity();
            order.setActive(true);
            order.setCountry(new PlayableCountryEntity());
            order.getCountry().setId(phasingCountry.getId());
            game.getOrders().add(order);
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
            StackEntity stackPhasing = new StackEntity();
            stackPhasing.setId(1L);
            stackPhasing.setGame(game);
            stackPhasing.setProvince(battle.getProvince());
            stackPhasing.setCountry(phasingCountry.getName());
            stackPhasing.setMovePhase(MovePhaseEnum.FIGHTING);
            for (CounterEntity counter : phasing.counters) {
                BattleCounterEntity bc = new BattleCounterEntity();
                counter.setId(counterId++);
                counter.setCountry(phasingCountry.getName());
                bc.setCounter(counter.getId());
                bc.setCountry(counter.getCountry());
                bc.setType(counter.getType());
                bc.setBattle(battle);
                bc.setPhasing(true);
                battle.getCounters().add(bc);
                stackPhasing.getCounters().add(counter);
                counter.setOwner(stackPhasing);
            }
            StackEntity stackNotPhasing = new StackEntity();
            stackNotPhasing.setId(2L);
            stackNotPhasing.setGame(game);
            stackNotPhasing.setProvince(battle.getProvince());
            stackNotPhasing.setCountry(nonPhasingCountry.getName());
            for (CounterEntity counter : nonPhasing.counters) {
                BattleCounterEntity bc = new BattleCounterEntity();
                counter.setId(counterId++);
                counter.setCountry(nonPhasingCountry.getName());
                bc.setCounter(counter.getId());
                bc.setCountry(counter.getCountry());
                bc.setType(counter.getType());
                bc.setBattle(battle);
                bc.setPhasing(false);
                battle.getCounters().add(bc);
                stackNotPhasing.getCounters().add(counter);
                counter.setOwner(stackNotPhasing);
            }
            game.getBattles().add(battle);
            game.getStacks().add(stackPhasing);
            game.getStacks().add(stackNotPhasing);
            Request<WithdrawBeforeBattleRequest> request = pair.getLeft();
            request.setRequest(new WithdrawBeforeBattleRequest());
            request.getRequest().setWithdraw(false);
            request.getGame().setIdCountry(nonPhasingCountry.getId());

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
            if (nonPhasing.retreatFirstDayAttempt) {
                int retreatDie = nonPhasing.retreatFirstDayResult ? 1 : 10;
                dice = dice.thenReturn(retreatDie);
            }
            if (phasing.retreatFirstDayAttempt) {
                int retreatDie = phasing.retreatFirstDayResult ? 1 : 10;
                dice = dice.thenReturn(retreatDie);
            }
            if (phasing.secondFire != null) {
                dice = dice.thenReturn(phasing.secondFire);
            }
            if (nonPhasing.secondFire != null) {
                dice = dice.thenReturn(nonPhasing.secondFire);
            }
            if (phasing.secondShock != null) {
                dice = dice.thenReturn(phasing.secondShock);
            }
            if (nonPhasing.secondShock != null) {
                dice = dice.thenReturn(nonPhasing.secondShock);
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

            when(testClass.counterDomain.removeCounter(any())).thenAnswer(removeCounterAnswer());
            when(testClass.oeUtil.getController(stackPhasing)).thenReturn(rotw ? nonPhasingCountry.getName() : phasingCountry.getName());
            when(testClass.oeUtil.getController(stackNotPhasing)).thenReturn(rotw ? phasingCountry.getName() : nonPhasingCountry.getName());
            when(testClass.oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Collections.singletonList(phasingCountry.getName()));
            when(testClass.oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Collections.singletonList(nonPhasingCountry.getName()));
            DiffEntity endDiff = new DiffEntity();
            endDiff.setType(DiffTypeEnum.VALIDATE);
            endDiff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
            when(testClass.statusWorkflowDomain.endMilitaryPhase(game)).thenReturn(Collections.singletonList(endDiff));

            Tables tables = new Tables();
            battle.getPhasing().setLeader("phasing");
            battle.getNonPhasing().setLeader("notPhasing");
            Leader leader = new Leader();
            leader.setCode("phasing");
            tables.getLeaders().add(leader);
            leader = new Leader();
            leader.setCode("notPhasing");
            tables.getLeaders().add(leader);
            testClass.fillBatleTechTables(tables);

            CombatResult result = new CombatResult();
            result.setColumn("A");
            result.setDice(3);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("A");
            result.setDice(10);
            result.setRoundLoss(2);
            result.setThirdLoss(1);
            result.setMoraleLoss(2);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("B");
            result.setDice(4);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("B");
            result.setDice(6);
            result.setThirdLoss(2);
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
            result.setDice(4);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(6);
            result.setThirdLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(7);
            result.setThirdLoss(2);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(8);
            result.setRoundLoss(1);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(9);
            result.setRoundLoss(1);
            result.setThirdLoss(1);
            result.setMoraleLoss(1);
            tables.getCombatResults().add(result);
            result = new CombatResult();
            result.setColumn("C");
            result.setDice(10);
            result.setRoundLoss(1);
            result.setThirdLoss(2);
            result.setMoraleLoss(2);
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
            BattleServiceImpl.TABLES = tables;

            testClass.simulateDiff();

            militaryService.withdrawBeforeBattle(request);

            diffsFirstDay = testClass.retrieveDiffsCreated();

            if (battle.getWinner() == null) {
                Assert.assertEquals(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF, battle.getStatus());

                Request<ValidateRequest> retreatRequest = new Request<>();
                retreatRequest.setGame(new GameInfo());
                retreatRequest.getGame().setIdGame(game.getId());
                retreatRequest.getGame().setVersionGame(VERSION_SINCE);
                retreatRequest.getGame().setIdCountry(nonPhasingCountry.getId());
                retreatRequest.setRequest(new ValidateRequest());
                retreatRequest.getRequest().setValidate(nonPhasing.retreatFirstDayAttempt);

                militaryService.retreatFirstDay(retreatRequest);

                diffsFirstRetreat = testClass.retrieveDiffsCreated();

                if (!nonPhasing.retreatFirstDayResult) {
                    Assert.assertEquals(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT, battle.getStatus());

                    retreatRequest.getGame().setIdCountry(phasingCountry.getId());
                    retreatRequest.getRequest().setValidate(phasing.retreatFirstDayAttempt);

                    militaryService.retreatFirstDay(retreatRequest);

                    diffsSecondDay = testClass.retrieveDiffsCreated();
                }
            }

            return this;
        }

        BattleBuilder thenExpect(BattleResultBuilder result) {
            Assert.assertEquals(result.end, battle.getEnd());
            Assert.assertEquals(result.winner, battle.getWinner());

            Assert.assertEquals(phasing.firstFire, battle.getPhasing().getFirstDay().getFire());
            Assert.assertEquals(phasing.firstShock, battle.getPhasing().getFirstDay().getShock());
            Assert.assertEquals(phasing.pursuit, battle.getPhasing().getPursuit());
            Assert.assertEquals(phasing.retreat, battle.getPhasing().getRetreat());
            Assert.assertEquals(nonPhasing.firstFire, battle.getNonPhasing().getFirstDay().getFire());
            Assert.assertEquals(nonPhasing.firstShock, battle.getNonPhasing().getFirstDay().getShock());
            Assert.assertEquals(nonPhasing.pursuit, battle.getNonPhasing().getPursuit());
            Assert.assertEquals(nonPhasing.retreat, battle.getNonPhasing().getRetreat());

            Assert.assertEquals(result.phasingThirdLosses, battle.getPhasing().getLosses().getTotalThird());
            Assert.assertEquals(result.phasingMoralLosses, CommonUtil.add(0, battle.getPhasing().getLosses().getMoraleLoss()).intValue());
            Assert.assertEquals(result.nonPhasingThirdLosses, battle.getNonPhasing().getLosses().getTotalThird());
            Assert.assertEquals(result.nonPhasingMoralLosses, CommonUtil.add(0, battle.getNonPhasing().getLosses().getMoraleLoss()).intValue());

            boolean onlyOneDayBattle = result.end == BattleEndEnum.ROUTED_AT_FIRST_FIRE || result.end == BattleEndEnum.ROUTED_AT_FIRST_SHOCK || result.end == BattleEndEnum.ANNIHILATED_AT_FIRST_DAY;

            DiffEntity diffFirstDay = diffsFirstDay.stream().filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE && Objects.equals(d.getIdObject(), battle.getId()))
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull(diffFirstDay);
            DiffEntity diffLastDay;
            List<DiffEntity> diffsLastDay;
            if (onlyOneDayBattle) {
                Assert.assertNull(diffsFirstRetreat);
                Assert.assertNull(diffsSecondDay);
                diffLastDay = diffFirstDay;
                diffsLastDay = diffsFirstDay;
            } else {
                Assert.assertEquals(1, diffsFirstDay.size());
                Assert.assertNull(getAttributeFull(diffFirstDay, DiffAttributeTypeEnum.END));
                Assert.assertNull(getAttributeFull(diffFirstDay, DiffAttributeTypeEnum.WINNER));
                Assert.assertEquals(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF.name(), getAttribute(diffFirstDay, DiffAttributeTypeEnum.STATUS));
                if (nonPhasing.retreatFirstDayResult) {
                    diffLastDay = diffsFirstRetreat.stream().filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE && Objects.equals(d.getIdObject(), battle.getId()))
                            .findAny()
                            .orElse(null);
                    diffsLastDay = diffsFirstRetreat;
                } else {
                    Assert.assertEquals(1, diffsFirstRetreat.size());
                    DiffEntity diffRetreat = diffsFirstRetreat.get(0);
                    Assert.assertEquals(DiffTypeEnum.MODIFY, diffRetreat.getType());
                    Assert.assertEquals(DiffTypeObjectEnum.BATTLE, diffRetreat.getTypeObject());
                    Assert.assertEquals(battle.getId(), diffRetreat.getIdObject());
                    Assert.assertEquals(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT.name(), getAttribute(diffRetreat, DiffAttributeTypeEnum.STATUS));
                    if (nonPhasing.retreatFirstDayAttempt && !nonPhasing.retreatFirstDayResult) {
                        Assert.assertEquals(toString(battle.getNonPhasing().getSecondDay().getFireMod()), getAttribute(diffRetreat, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD));
                        Assert.assertEquals(toString(battle.getNonPhasing().getSecondDay().getShockMod()), getAttribute(diffRetreat, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD));
                    } else {
                        Assert.assertNull(getAttributeFull(diffRetreat, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD));
                        Assert.assertNull(getAttributeFull(diffRetreat, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD));
                    }

                    diffLastDay = diffsSecondDay.stream().filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE && Objects.equals(d.getIdObject(), battle.getId()))
                            .findAny()
                            .orElse(null);
                    diffsLastDay = diffsSecondDay;
                }
                Assert.assertNotNull(diffLastDay);
                if (phasing.retreatFirstDayAttempt && !phasing.retreatFirstDayResult) {
                    Assert.assertEquals(toString(battle.getPhasing().getSecondDay().getFireMod()), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE_MOD));
                    Assert.assertEquals(toString(battle.getPhasing().getSecondDay().getShockMod()), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK_MOD));
                } else {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE_MOD));
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK_MOD));
                }
            }

            Assert.assertEquals(toString(result.end), getAttribute(diffLastDay, DiffAttributeTypeEnum.END));
            Assert.assertEquals(toString(result.winner), getAttribute(diffLastDay, DiffAttributeTypeEnum.WINNER));
            Assert.assertEquals(battle.getPhasing().getLosses().getRoundLoss() + "", getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS));
            Assert.assertEquals(battle.getPhasing().getLosses().getThirdLoss() + "", getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS));
            Assert.assertEquals(result.phasingMoralLosses == 0 ? "" : result.phasingMoralLosses + "", getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS));
            Assert.assertEquals(battle.getNonPhasing().getLosses().getRoundLoss() + "", getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS));
            Assert.assertEquals(battle.getNonPhasing().getLosses().getThirdLoss() + "", getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS));
            Assert.assertEquals(result.nonPhasingMoralLosses == 0 ? "" : result.nonPhasingMoralLosses + "", getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS));

            if (phasing.firstFire == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE));
            } else {
                Assert.assertEquals(toString(phasing.firstFire), getAttribute(diffFirstDay, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE));
            }
            if (phasing.firstShock == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK));
            } else {
                Assert.assertEquals(toString(phasing.firstShock), getAttribute(diffFirstDay, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK));
            }
            if (nonPhasing.firstFire == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE));
            } else {
                Assert.assertEquals(toString(nonPhasing.firstFire), getAttribute(diffFirstDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE));
            }
            if (nonPhasing.firstShock == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK));
            } else {
                Assert.assertEquals(toString(nonPhasing.firstShock), getAttribute(diffFirstDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK));
            }

            if (!onlyOneDayBattle) {
                if (phasing.secondFire == null) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE));
                } else {
                    Assert.assertEquals(toString(phasing.secondFire), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE));
                }
                if (phasing.secondShock == null) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK));
                } else {
                    Assert.assertEquals(toString(phasing.secondShock), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK));
                }
                if (nonPhasing.secondFire == null) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE));
                } else {
                    Assert.assertEquals(toString(nonPhasing.secondFire), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE));
                }
                if (nonPhasing.secondShock == null) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK));
                } else {
                    Assert.assertEquals(toString(nonPhasing.secondShock), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK));
                }
            }
            if (phasing.pursuit == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT));
            } else {
                Assert.assertEquals(toString(phasing.pursuit), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT));
            }
            if (phasing.retreat == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_RETREAT));
            } else {
                Assert.assertEquals(toString(phasing.retreat), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_PHASING_RETREAT));
            }
            if (nonPhasing.pursuit == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT));
            } else {
                Assert.assertEquals(toString(nonPhasing.pursuit), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT));
            }
            if (nonPhasing.retreat == null) {
                Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_RETREAT));
            } else {
                Assert.assertEquals(toString(nonPhasing.retreat), getAttribute(diffLastDay, DiffAttributeTypeEnum.BATTLE_NON_PHASING_RETREAT));
            }

            if (result.phasingAnnihilated) {
                for (CounterEntity counter : phasing.counters) {
                    Assert.assertTrue(diffsLastDay.stream()
                            .anyMatch(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                                    Objects.equals(d.getIdObject(), counter.getId())));
                }
            }
            if (result.nonPhasingAnnihilated) {
                for (CounterEntity counter : nonPhasing.counters) {
                    Assert.assertTrue(diffsLastDay.stream()
                            .anyMatch(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                                    Objects.equals(d.getIdObject(), counter.getId())));
                }
            }


            int diffsSize = 1;
            if (result.phasingAnnihilated) {
                diffsSize += phasing.counters.size();
            }
            if (result.nonPhasingAnnihilated) {
                diffsSize += nonPhasing.counters.size();
            }
            boolean phasingLossesAuto = result.phasingAnnihilated || result.phasingThirdLosses == 0;
            boolean nonPhasingLossesAuto = result.nonPhasingAnnihilated || result.nonPhasingThirdLosses == 0;
            boolean phasingRetreatAuto = result.winner == BattleWinnerEnum.PHASING || result.phasingAnnihilated;
            boolean nonPhasingRetreatAuto = result.winner == BattleWinnerEnum.NON_PHASING || result.nonPhasingAnnihilated;
            if (!phasingLossesAuto || !nonPhasingLossesAuto) {
                Assert.assertEquals(BattleStatusEnum.CHOOSE_LOSS, battle.getStatus());
                Assert.assertEquals(BattleStatusEnum.CHOOSE_LOSS.name(), getAttribute(diffLastDay, DiffAttributeTypeEnum.STATUS));
                Assert.assertEquals(phasingLossesAuto, battle.getPhasing().isLossesSelected());
                if (!phasingLossesAuto) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.PHASING_READY));
                } else {
                    Assert.assertEquals("true", getAttribute(diffLastDay, DiffAttributeTypeEnum.PHASING_READY));
                }
                Assert.assertEquals(nonPhasingLossesAuto, battle.getNonPhasing().isLossesSelected());
                if (!nonPhasingLossesAuto) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.NON_PHASING_READY));
                } else {
                    Assert.assertEquals("true", getAttribute(diffLastDay, DiffAttributeTypeEnum.NON_PHASING_READY));
                }
            } else if (!phasingRetreatAuto || !nonPhasingRetreatAuto) {
                Assert.assertEquals(BattleStatusEnum.RETREAT, battle.getStatus());
                Assert.assertTrue(battle.getPhasing().isLossesSelected());
                Assert.assertTrue(battle.getNonPhasing().isLossesSelected());
                Assert.assertEquals(BattleStatusEnum.RETREAT.name(), getAttribute(diffLastDay, DiffAttributeTypeEnum.STATUS));
                Assert.assertEquals(phasingRetreatAuto, battle.getPhasing().isRetreatSelected());
                if (!phasingRetreatAuto) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.PHASING_READY));
                } else {
                    Assert.assertEquals("true", getAttribute(diffLastDay, DiffAttributeTypeEnum.PHASING_READY));
                }
                Assert.assertEquals(nonPhasingRetreatAuto, battle.getNonPhasing().isRetreatSelected());
                if (!nonPhasingRetreatAuto) {
                    Assert.assertNull(getAttributeFull(diffLastDay, DiffAttributeTypeEnum.NON_PHASING_READY));
                } else {
                    Assert.assertEquals("true", getAttribute(diffLastDay, DiffAttributeTypeEnum.NON_PHASING_READY));
                }
            } else {
                Assert.assertEquals(BattleStatusEnum.DONE, battle.getStatus());
                Assert.assertEquals(BattleStatusEnum.DONE.name(), getAttribute(diffLastDay, DiffAttributeTypeEnum.STATUS));
                Assert.assertTrue(diffsLastDay.stream()
                        .anyMatch(d -> d.getType() == DiffTypeEnum.VALIDATE && d.getTypeObject() == DiffTypeObjectEnum.TURN_ORDER));
                diffsSize++;
                DiffEntity stackPhasingkMovePhase = diffsLastDay.stream()
                        .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.STACK &&
                                Objects.equals(diff.getIdObject(), 1L))
                        .findAny()
                        .orElse(null);
                if (result.phasingAnnihilated) {
                    Assert.assertNull(stackPhasingkMovePhase);
                } else {
                    Assert.assertNotNull(stackPhasingkMovePhase);
                    Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(stackPhasingkMovePhase, DiffAttributeTypeEnum.MOVE_PHASE));
                    if (rotw) {
                        Assert.assertEquals("spain", getAttribute(stackPhasingkMovePhase, DiffAttributeTypeEnum.COUNTRY));
                    } else {
                        Assert.assertFalse(stackPhasingkMovePhase.getAttributes().stream()
                                .anyMatch(attr -> attr.getType() == DiffAttributeTypeEnum.COUNTRY));
                    }
                    diffsSize++;
                }
                DiffEntity stackNotPhasingkMovePhase = diffsLastDay.stream()
                        .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.STACK &&
                                Objects.equals(diff.getIdObject(), 2L))
                        .findAny()
                        .orElse(null);
                if (rotw && !result.nonPhasingAnnihilated) {
                    Assert.assertNotNull(stackNotPhasingkMovePhase);
                    Assert.assertFalse(stackNotPhasingkMovePhase.getAttributes().stream()
                            .anyMatch(attr -> attr.getType() == DiffAttributeTypeEnum.MOVE_PHASE));
                    Assert.assertEquals("france", getAttribute(stackNotPhasingkMovePhase, DiffAttributeTypeEnum.COUNTRY));
                    diffsSize++;
                } else {
                    Assert.assertNull(stackNotPhasingkMovePhase);
                }
            }
            Assert.assertEquals(diffsSize, diffsLastDay.size());

            return this;
        }

        private String toString(Integer i) {
            return i == null ? null : i.toString();
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
        Integer secondFire;
        Integer secondShock;
        Integer pursuit;
        Integer retreat;
        int retreatLosses;
        int sizeReductionDamage;
        boolean retreatFirstDayAttempt;
        boolean retreatFirstDayResult;

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

        BattleSideBuilder secondFire(Integer secondFire) {
            this.secondFire = secondFire;
            return this;
        }

        BattleSideBuilder secondShock(Integer secondShock) {
            this.secondShock = secondShock;
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

        BattleSideBuilder retreatFirstDayResult(boolean retreatFirstDayResult) {
            this.retreatFirstDayAttempt = true;
            this.retreatFirstDayResult = retreatFirstDayResult;
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

    @Test
    public void testChooseLossesFail() {
        Pair<Request<ChooseLossesRequest>, GameEntity> pair = testCheckGame(battleService::chooseLossesFromBattle, "chooseLosses");
        Request<ChooseLossesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setGame(game);
        battle.setStatus(BattleStatusEnum.SELECT_FORCES);
        battle.setProvince("idf");
        StackEntity stack = new StackEntity();
        stack.setProvince(battle.getProvince());
        game.getStacks().add(stack);
        stack.getCounters().add(createCounter(1l, "france", CounterFaceTypeEnum.ARMY_MINUS, stack));
        stack.getCounters().add(createCounter(2l, "savoie", CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, stack));
        stack.getCounters().add(createCounter(3l, "spain", CounterFaceTypeEnum.ARMY_MINUS, stack));
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
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
        testCheckStatus(pair.getRight(), request, battleService::chooseLossesFromBattle, "chooseLosses", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because chooseLosses.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseLosses.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseLossesRequest());

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because battle is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("chooseLosses", e.getParams()[0]);
        }

        battle.setStatus(BattleStatusEnum.CHOOSE_LOSS);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("chooseLosses.request.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("chooseLosses.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(26L);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("chooseLosses.request.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("chooseLosses.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(27L);
        when(oeUtil.isWarAlly(france, battle.getWar(), false)).thenReturn(true);
        battle.getPhasing().setLossesSelected(true);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because losses has already been chosen by this side");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_DONE, e.getCode());
            Assert.assertEquals("chooseLosses.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(26L);
        when(oeUtil.isWarAlly(spain, battle.getWar(), true)).thenReturn(true);
        battle.getNonPhasing().setLossesSelected(true);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because losses has already been chosen by this side");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_DONE, e.getCode());
            Assert.assertEquals("chooseLosses.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(27L);
        battle.getPhasing().setLossesSelected(false);
        battle.getNonPhasing().setLossesSelected(false);
        battle.getPhasing().getLosses().setRoundLoss(1);
        battle.getPhasing().getLosses().setThirdLoss(1);
        ChooseLossesRequest.UnitLoss loss = new ChooseLossesRequest.UnitLoss();
        loss.setIdCounter(1L);
        loss.setRoundLosses(1);
        request.getRequest().getLosses().add(loss);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because losses are bigger than the one sent");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_MISMATCH, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }

        loss = new ChooseLossesRequest.UnitLoss();
        loss.setIdCounter(2L);
        loss.setThirdLosses(2);
        request.getRequest().getLosses().add(loss);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because losses are smaller than the one sent");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_MISMATCH, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }

        battle.getPhasing().getLosses().setThirdLoss(2);
        when(provinceDao.getProvinceByName("idf")).thenReturn(new EuropeanProvinceEntity());

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because no third loss on european province can be taken");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_NO_THIRD, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }

        when(provinceDao.getProvinceByName("idf")).thenReturn(new RotwProvinceEntity());
        loss.setIdCounter(666L);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because counter outside of battle cannot take loss");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }

        loss.setIdCounter(3L);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because counter not owned cannot take loss");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }

        loss.setIdCounter(2L);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because counter cannot take that many loss");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_TOO_BIG, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseLossesComplexFail() {
        Pair<Request<ChooseLossesRequest>, GameEntity> pair = testCheckGame(battleService::chooseLossesFromBattle, "chooseLosses");
        Request<ChooseLossesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setStatus(BattleStatusEnum.CHOOSE_LOSS);
        battle.setProvince("idf");
        StackEntity stack = new StackEntity();
        stack.setProvince(battle.getProvince());
        game.getStacks().add(stack);
        stack.getCounters().add(createCounter(2l, "savoie", CounterFaceTypeEnum.ARMY_MINUS, 10L));
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
        game.getOrders().add(order);
        testCheckStatus(pair.getRight(), request, battleService::chooseLossesFromBattle, "chooseLosses", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        when(oeUtil.isWarAlly(france, battle.getWar(), false)).thenReturn(true);

        battle.getPhasing().getLosses().setThirdLoss(1);
        ChooseLossesRequest.UnitLoss loss = new ChooseLossesRequest.UnitLoss();
        loss.setIdCounter(2L);
        loss.setThirdLosses(1);
        request.setRequest(new ChooseLossesRequest());
        request.getRequest().getLosses().add(loss);

        try {
            battleService.chooseLossesFromBattle(request);
            Assert.fail("Should break because it would result to too many thirds");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_LOSSES_TOO_MANY_THIRD, e.getCode());
            Assert.assertEquals("chooseLosses.request.losses", e.getParams()[0]);
        }
    }

    @Test
    public void testChooseLossesSuccess() throws FunctionalException {
        Pair<Request<ChooseLossesRequest>, GameEntity> pair = testCheckGame(battleService::chooseLossesFromBattle, "chooseLosses");
        Request<ChooseLossesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setStatus(BattleStatusEnum.CHOOSE_LOSS);
        battle.setProvince("idf");
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
        game.getOrders().add(order);
        testCheckStatus(pair.getRight(), request, battleService::chooseLossesFromBattle, "chooseLosses", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);
        StackEntity stack = new StackEntity();
        stack.setId(10L);
        stack.setGame(game);
        stack.setProvince(battle.getProvince());
        stack.getCounters().add(createCounter(1l, "france", CounterFaceTypeEnum.LAND_DETACHMENT, stack));
        stack.getCounters().add(createCounter(2l, "savoie", CounterFaceTypeEnum.ARMY_MINUS, stack));
        game.getStacks().add(stack);

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        when(oeUtil.isWarAlly(france, battle.getWar(), false)).thenReturn(true);
        when(counterDomain.removeCounter(any())).thenAnswer(removeCounterAnswer());
        when(counterDomain.createCounter(any(), any(), anyLong(), any())).thenAnswer(invocation -> {
            DiffEntity diff = new DiffEntity();
            diff.setIdObject(invocation.getArgumentAt(2, Long.class));
            diff.setType(DiffTypeEnum.ADD);
            diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
            diff.getAttributes().add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, invocation.getArgumentAt(0, CounterFaceTypeEnum.class)));
            diff.getAttributes().add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, invocation.getArgumentAt(1, String.class)));
            diff.setVersionGame(VERSION_SINCE);
            diff.setIdGame(GAME_ID);
            return diff;
        });

        battle.getPhasing().getLosses().setRoundLoss(1);
        battle.getPhasing().getLosses().setThirdLoss(1);
        ChooseLossesRequest.UnitLoss loss = new ChooseLossesRequest.UnitLoss();
        loss.setIdCounter(1L);
        loss.setRoundLosses(1);
        request.setRequest(new ChooseLossesRequest());
        request.getRequest().getLosses().add(loss);
        loss = new ChooseLossesRequest.UnitLoss();
        loss.setIdCounter(2L);
        loss.setThirdLosses(1);
        request.getRequest().getLosses().add(loss);


        simulateDiff();

        battleService.chooseLossesFromBattle(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(6, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE && Objects.equals(d.getIdObject(), battle.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.PHASING_READY));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 1L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 2L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        List<DiffEntity> diffsCreate = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 10L))
                .collect(Collectors.toList());
        Assert.assertEquals(3, diffsCreate.size());
        List<String> faces = diffsCreate.stream()
                .flatMap(d -> d.getAttributes().stream())
                .filter(a -> a.getType() == DiffAttributeTypeEnum.COUNTER_FACE_TYPE)
                .map(DiffAttributesEntity::getValue)
                .collect(Collectors.toList());
        Assert.assertEquals(1L, faces.stream()
                .filter(s -> StringUtils.equals(s, CounterFaceTypeEnum.LAND_DETACHMENT.name()))
                .count());
        Assert.assertEquals(2L, faces.stream()
                .filter(s -> StringUtils.equals(s, CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION.name()))
                .count());
    }

    @Test
    public void testChooseLossesSuccess2() throws FunctionalException {
        Pair<Request<ChooseLossesRequest>, GameEntity> pair = testCheckGame(battleService::chooseLossesFromBattle, "chooseLosses");
        Request<ChooseLossesRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setGame(game);
        battle.setStatus(BattleStatusEnum.CHOOSE_LOSS);
        battle.setProvince("idf");
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_PLUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
        game.getOrders().add(order);
        testCheckStatus(pair.getRight(), request, battleService::chooseLossesFromBattle, "chooseLosses", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(26L);
        StackEntity stack = new StackEntity();
        stack.setId(20L);
        stack.setGame(game);
        stack.setProvince(battle.getProvince());
        for (BattleCounterEntity battleCounter : battle.getCounters()) {
            stack.getCounters().add(createCounter(battleCounter.getCounter(), battleCounter.getCountry(), battleCounter.getType(), stack));
        }
        game.getStacks().add(stack);

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        when(oeUtil.isWarAlly(spain, battle.getWar(), true)).thenReturn(true);
        when(counterDomain.removeCounter(any())).thenAnswer(removeCounterAnswer());
        when(counterDomain.createCounter(any(), any(), anyLong(), any())).thenAnswer(invocation -> {
            DiffEntity diff = new DiffEntity();
            diff.setIdObject(invocation.getArgumentAt(2, Long.class));
            diff.setType(DiffTypeEnum.ADD);
            diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
            diff.getAttributes().add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTER_FACE_TYPE, invocation.getArgumentAt(0, CounterFaceTypeEnum.class)));
            diff.getAttributes().add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, invocation.getArgumentAt(1, String.class)));
            diff.setVersionGame(VERSION_SINCE);
            diff.setIdGame(GAME_ID);
            return diff;
        });

        battle.getNonPhasing().getLosses().setRoundLoss(1);
        battle.getNonPhasing().setSize(6d);
        battle.getPhasing().setLossesSelected(true);
        battle.setWinner(BattleWinnerEnum.PHASING);
        ChooseLossesRequest.UnitLoss loss = new ChooseLossesRequest.UnitLoss();
        loss.setIdCounter(3L);
        loss.setThirdLosses(3);
        request.setRequest(new ChooseLossesRequest());
        request.getRequest().getLosses().add(loss);


        simulateDiff();

        battleService.chooseLossesFromBattle(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(4, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE && Objects.equals(d.getIdObject(), battle.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(BattleStatusEnum.RETREAT.name(), getAttribute(diff, DiffAttributeTypeEnum.STATUS));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 3L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        List<DiffEntity> diffsCreate = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 20L))
                .collect(Collectors.toList());
        Assert.assertEquals(2, diffsCreate.size());
        List<String> faces = diffsCreate.stream()
                .flatMap(d -> d.getAttributes().stream())
                .filter(a -> a.getType() == DiffAttributeTypeEnum.COUNTER_FACE_TYPE)
                .map(DiffAttributesEntity::getValue)
                .collect(Collectors.toList());
        Assert.assertEquals(1L, faces.stream()
                .filter(s -> StringUtils.equals(s, CounterFaceTypeEnum.LAND_DETACHMENT.name()))
                .count());
        Assert.assertEquals(1L, faces.stream()
                .filter(s -> StringUtils.equals(s, CounterFaceTypeEnum.ARMY_MINUS.name()))
                .count());
    }

    @Test
    public void testRetreatAfterBattleFail() {
        Pair<Request<RetreatAfterBattleRequest>, GameEntity> pair = testCheckGame(battleService::retreatAfterBattle, "retreatAfterBattle");
        Request<RetreatAfterBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        StackEntity stackPhasing = new StackEntity();
        stackPhasing.setCountry("france");
        stackPhasing.setProvince("idf");
        stackPhasing.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        game.getStacks().add(stackPhasing);
        StackEntity stackNonPhasing = new StackEntity();
        stackNonPhasing.setCountry("spain");
        stackNonPhasing.setProvince("idf");
        stackNonPhasing.getCounters().add(createCounter(3L, "spain", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        stackNonPhasing.getCounters().add(createCounter(4l, "austria", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        game.getStacks().add(stackNonPhasing);
        StackEntity stackNotMobile = new StackEntity();
        stackNotMobile.setProvince("idf");
        stackNotMobile.setCountry("france");
        stackNotMobile.getCounters().add(createCounter(5L, "france", CounterFaceTypeEnum.MNU_ART_MINUS, stackNotMobile));
        game.getStacks().add(stackNotMobile);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setStatus(BattleStatusEnum.SELECT_FORCES);
        battle.setProvince("idf");
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
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
        testCheckStatus(pair.getRight(), request, battleService::retreatAfterBattle, "retreatAfterBattle", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);
        when(counterDomain.createStack(any(), any(), any())).thenReturn(new StackEntity());
        when(oeUtil.isMobile(stackPhasing)).thenReturn(true);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreatAfterBattle.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request", e.getParams()[0]);
        }

        request.setRequest(new RetreatAfterBattleRequest());

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because battle is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_STATUS_NONE, e.getCode());
            Assert.assertEquals("retreatAfterBattle", e.getParams()[0]);
        }

        battle.setStatus(BattleStatusEnum.RETREAT);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(26L);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.idCountry", e.getParams()[0]);
        }

        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because country has no right to decide a retreat in this battle");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(27L);
        when(oeUtil.isWarAlly(france, battle.getWar(), false)).thenReturn(true);
        battle.getPhasing().setRetreatSelected(true);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat has already been done by this side");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_DONE, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(26L);
        when(oeUtil.isWarAlly(spain, battle.getWar(), true)).thenReturn(true);
        battle.getNonPhasing().setRetreatSelected(true);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat has already been done by this side");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_DONE, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(27L);
        battle.getPhasing().setRetreatSelected(false);
        battle.getNonPhasing().setRetreatSelected(false);

        request.getRequest().getRetreatInFortress().add(2L);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat involves a non existing counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_RETREAT_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.retreatInFortress", e.getParams()[0]);
        }

        CounterEntity counter = createCounter(2L, "austria", CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, stackPhasing.getId());
        stackPhasing.getCounters().add(counter);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat involves an enemy counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_RETREAT_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.retreatInFortress", e.getParams()[0]);
        }

        counter.setCountry("savoie");
        request.getRequest().getRetreatInFortress().add(5L);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat involves a non mobile counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_RETREAT_INVALID_COUNTER, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.retreatInFortress", e.getParams()[0]);
        }

        request.getRequest().getRetreatInFortress().remove(5L);
        when(oeUtil.canRetreat(idf, true, THIRD, france, game)).thenReturn(false);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat is impossible in fortress");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_CANT_RETREAT, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.retreatInFortress", e.getParams()[0]);
        }

        when(oeUtil.canRetreat(idf, true, THIRD, france, game)).thenReturn(true);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat did not tell which province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_RETREAT_NEEDED, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("orleans");

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.provinceTo", e.getParams()[0]);
        }

        when(provinceDao.getProvinceByName("orleans")).thenReturn(orleans);

        try {
            battleService.retreatAfterBattle(request);
            Assert.fail("Should break because retreat is impossible in province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_CANT_RETREAT, e.getCode());
            Assert.assertEquals("retreatAfterBattle.request.provinceTo", e.getParams()[0]);
        }
    }

    @Test
    public void testRetreatAfterBattleInFortressSuccess() throws FunctionalException {
        Pair<Request<RetreatAfterBattleRequest>, GameEntity> pair = testCheckGame(battleService::retreatAfterBattle, "retreatAfterBattle");
        Request<RetreatAfterBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        StackEntity stackPhasing = new StackEntity();
        stackPhasing.setCountry("france");
        stackPhasing.setProvince("idf");
        stackPhasing.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        stackPhasing.getCounters().add(createCounter(2L, "savoie", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        game.getStacks().add(stackPhasing);
        StackEntity stackNonPhasing = new StackEntity();
        stackNonPhasing.setCountry("spain");
        stackNonPhasing.setProvince("idf");
        stackNonPhasing.getCounters().add(createCounter(3L, "spain", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        stackNonPhasing.getCounters().add(createCounter(4l, "austria", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        game.getStacks().add(stackNonPhasing);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setStatus(BattleStatusEnum.RETREAT);
        battle.setProvince("idf");
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
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
        testCheckStatus(pair.getRight(), request, battleService::retreatAfterBattle, "retreatAfterBattle", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(27L);
        when(counterDomain.createStack(any(), any(), any())).thenReturn(new StackEntity());
        when(oeUtil.isMobile(stackPhasing)).thenReturn(true);
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));
        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        when(oeUtil.isWarAlly(france, battle.getWar(), false)).thenReturn(true);

        request.setRequest(new RetreatAfterBattleRequest());
        request.getRequest().getRetreatInFortress().add(1L);
        request.getRequest().getRetreatInFortress().add(2L);
        when(oeUtil.canRetreat(idf, true, 4, france, game)).thenReturn(true);
        when(counterDomain.changeCounterOwner(any(), any(), any())).thenAnswer(invocation -> {
            CounterEntity counter = invocation.getArgumentAt(0, CounterEntity.class);
            DiffEntity diff = new DiffEntity();
            diff.setIdObject(counter.getId());
            diff.setType(DiffTypeEnum.MOVE);
            diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
            diff.setVersionGame(VERSION_SINCE);
            diff.setIdGame(GAME_ID);
            StackEntity stack = counter.getOwner();
            stack.getCounters().remove(counter);
            if (stack.getCounters().isEmpty()) {
                game.getStacks().remove(stack);
            }
            return diff;
        });

        simulateDiff();

        battleService.retreatAfterBattle(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(4, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(1, diff.getAttributes().size());
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.PHASING_READY));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.STACK)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(4, diff.getAttributes().size());
        Assert.assertEquals("idf", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE));
        Assert.assertEquals("france", getAttribute(diff, DiffAttributeTypeEnum.COUNTRY));
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.BESIEGED));
        Assert.assertEquals("MOVED", getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 1L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 2L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
    }

    @Test
    public void testRetreatAfterBattleInFortressAndProvinceSuccess() throws FunctionalException {
        Pair<Request<RetreatAfterBattleRequest>, GameEntity> pair = testCheckGame(battleService::retreatAfterBattle, "retreatAfterBattle");
        Request<RetreatAfterBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        StackEntity stackPhasing = new StackEntity();
        stackPhasing.setId(1L);
        stackPhasing.setCountry("france");
        stackPhasing.setProvince("idf");
        stackPhasing.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        stackPhasing.getCounters().add(createCounter(2L, "savoie", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        game.getStacks().add(stackPhasing);
        StackEntity stackNonPhasing = new StackEntity();
        stackNonPhasing.setId(2L);
        stackNonPhasing.setCountry("spain");
        stackNonPhasing.setProvince("idf");
        stackNonPhasing.getCounters().add(createCounter(3L, "spain", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        stackNonPhasing.getCounters().add(createCounter(4l, "austria", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        game.getStacks().add(stackNonPhasing);
        StackEntity stackNonPhasingNonMoving = new StackEntity();
        stackNonPhasingNonMoving.setId(3L);
        stackNonPhasingNonMoving.setCountry("spain");
        stackNonPhasingNonMoving.setProvince("idf");
        stackNonPhasingNonMoving.getCounters().add(createCounter(5L, "spain", CounterFaceTypeEnum.MNU_ART_MINUS, stackNonPhasingNonMoving));
        game.getStacks().add(stackNonPhasingNonMoving);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setGame(game);
        battle.setStatus(BattleStatusEnum.RETREAT);
        battle.setProvince("idf");
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
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
        when(provinceDao.getProvinceByName("orleans")).thenReturn(orleans);
        testCheckStatus(pair.getRight(), request, battleService::retreatAfterBattle, "retreatAfterBattle", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(26L);
        when(counterDomain.createStack(any(), any(), any())).thenReturn(new StackEntity());
        when(oeUtil.isMobile(stackNonPhasing)).thenReturn(true);
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));
        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        when(oeUtil.isWarAlly(spain, battle.getWar(), true)).thenReturn(true);
        when(oeUtil.getController(stackNonPhasing)).thenReturn("savoie");

        request.setRequest(new RetreatAfterBattleRequest());
        request.getRequest().getRetreatInFortress().add(3L);
        request.getRequest().setProvinceTo("orleans");
        when(oeUtil.canRetreat(idf, true, 2, spain, game)).thenReturn(true);
        when(oeUtil.canRetreat(orleans, false, 0, spain, game)).thenReturn(true);
        when(counterDomain.changeCounterOwner(any(), any(), any())).thenAnswer(invocation -> {
            CounterEntity counter = invocation.getArgumentAt(0, CounterEntity.class);
            DiffEntity diff = new DiffEntity();
            diff.setIdObject(counter.getId());
            diff.setType(DiffTypeEnum.MOVE);
            diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
            diff.setVersionGame(VERSION_SINCE);
            diff.setIdGame(GAME_ID);
            StackEntity stack = counter.getOwner();
            stack.getCounters().remove(counter);
            if (stack.getCounters().isEmpty()) {
                game.getStacks().remove(stack);
            }
            return diff;
        });

        simulateDiff();

        battleService.retreatAfterBattle(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(4, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(1, diff.getAttributes().size());
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.NON_PHASING_READY));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.STACK)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(4, diff.getAttributes().size());
        Assert.assertEquals("idf", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE));
        Assert.assertEquals("spain", getAttribute(diff, DiffAttributeTypeEnum.COUNTRY));
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.BESIEGED));
        Assert.assertEquals("MOVED", getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 3L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.STACK && Objects.equals(d.getIdObject(), stackNonPhasing.getId()))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(4, diff.getAttributes().size());
        Assert.assertEquals("idf", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("orleans", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals("MOVED", getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));
        Assert.assertEquals("savoie", getAttribute(diff, DiffAttributeTypeEnum.COUNTRY));
    }

    @Test
    public void testRetreatAfterBattleInFortressAndDisbandSuccess() throws FunctionalException {
        Pair<Request<RetreatAfterBattleRequest>, GameEntity> pair = testCheckGame(battleService::retreatAfterBattle, "retreatAfterBattle");
        Request<RetreatAfterBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setId(27L);
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setId(26L);
        spain.setName("spain");
        game.getCountries().add(spain);
        StackEntity stackPhasing = new StackEntity();
        stackPhasing.setId(1L);
        stackPhasing.setGame(game);
        stackPhasing.setCountry("france");
        stackPhasing.setProvince("idf");
        stackPhasing.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        stackPhasing.getCounters().add(createCounter(2L, "savoie", CounterFaceTypeEnum.ARMY_MINUS, stackPhasing));
        game.getStacks().add(stackPhasing);
        StackEntity stackNonPhasing = new StackEntity();
        stackNonPhasing.setId(2L);
        stackNonPhasing.setGame(game);
        stackNonPhasing.setCountry("spain");
        stackNonPhasing.setProvince("idf");
        stackNonPhasing.getCounters().add(createCounter(3L, "spain", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        stackNonPhasing.getCounters().add(createCounter(4l, "austria", CounterFaceTypeEnum.ARMY_MINUS, stackNonPhasing));
        game.getStacks().add(stackNonPhasing);
        StackEntity stackNonPhasingNonMoving = new StackEntity();
        stackNonPhasingNonMoving.setId(3L);
        stackNonPhasingNonMoving.setCountry("spain");
        stackNonPhasingNonMoving.setProvince("idf");
        stackNonPhasingNonMoving.getCounters().add(createCounter(5L, "spain", CounterFaceTypeEnum.MNU_ART_MINUS, stackNonPhasingNonMoving));
        game.getStacks().add(stackNonPhasingNonMoving);
        game.getBattles().add(new BattleEntity());
        BattleEntity battle = game.getBattles().get(0);
        battle.setGame(game);
        battle.setStatus(BattleStatusEnum.RETREAT);
        battle.setProvince("idf");
        BattleCounterEntity bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(1L);
        bc.setCountry("france");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(true);
        bc.setCounter(2L);
        bc.setCountry("savoie");
        bc.setType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(3L);
        bc.setCountry("spain");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        bc = new BattleCounterEntity();
        bc.setPhasing(false);
        bc.setCounter(4L);
        bc.setCountry("austria");
        bc.setType(CounterFaceTypeEnum.ARMY_MINUS);
        battle.getCounters().add(bc);
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(france);
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
        when(provinceDao.getProvinceByName("orleans")).thenReturn(orleans);
        testCheckStatus(pair.getRight(), request, battleService::retreatAfterBattle, "retreatAfterBattle", GameStatusEnum.MILITARY_BATTLES);
        request.getGame().setIdCountry(26L);
        when(counterDomain.createStack(any(), any(), any())).thenReturn(new StackEntity());
        when(oeUtil.isMobile(stackNonPhasing)).thenReturn(true);
        when(oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive())).thenReturn(Arrays.asList("spain", "austria"));
        when(oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive())).thenReturn(Arrays.asList("france", "savoie"));
        when(oeUtil.isWarAlly(spain, battle.getWar(), true)).thenReturn(true);

        request.setRequest(new RetreatAfterBattleRequest());
        request.getRequest().getRetreatInFortress().add(3L);
        request.getRequest().setDisbandRemaining(true);
        when(oeUtil.canRetreat(idf, true, 2, spain, game)).thenReturn(true);
        when(counterDomain.removeCounter(any())).thenAnswer(removeCounterAnswer());
        when(counterDomain.changeCounterOwner(any(), any(), any())).thenAnswer(invocation -> {
            CounterEntity counter = invocation.getArgumentAt(0, CounterEntity.class);
            StackEntity stack = counter.getOwner();
            stack.getCounters().remove(counter);
            if (stack.getCounters().isEmpty()) {
                game.getStacks().remove(stack);
            }
            return DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER, counter.getId());
        });

        simulateDiff();

        battleService.retreatAfterBattle(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(4, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(1, diff.getAttributes().size());
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.NON_PHASING_READY));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.STACK)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(4, diff.getAttributes().size());
        Assert.assertEquals("idf", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE));
        Assert.assertEquals("spain", getAttribute(diff, DiffAttributeTypeEnum.COUNTRY));
        Assert.assertEquals("true", getAttribute(diff, DiffAttributeTypeEnum.BESIEGED));
        Assert.assertEquals("MOVED", getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 3L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(d.getIdObject(), 4L))
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
    }

    @Test
    public void testGetLeaderConditions() {
        SeaProvinceEntity north = new SeaProvinceEntity();
        north.setName("north");
        when(provinceDao.getProvinceByName("north")).thenReturn(north);
        when(provinceDao.getGeoGroups("north")).thenReturn(Collections.singletonList(IReferentielConstants.EUROPE));
        SeaProvinceEntity lion = new SeaProvinceEntity();
        lion.setName("lion");
        when(provinceDao.getProvinceByName("lion")).thenReturn(lion);
        when(provinceDao.getGeoGroups("lion")).thenReturn(Arrays.asList(IReferentielConstants.EUROPE, IReferentielConstants.MEDITERRANEAN_SEA));
        SeaProvinceEntity tempetes = new SeaProvinceEntity();
        tempetes.setName("tempetes");
        when(provinceDao.getProvinceByName("tempetes")).thenReturn(tempetes);
        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        RotwProvinceEntity aral = new RotwProvinceEntity();
        aral.setName("aral");
        when(provinceDao.getProvinceByName("aral")).thenReturn(aral);
        when(provinceDao.getGeoGroups("aral")).thenReturn(Collections.singletonList(IReferentielConstants.ASIA));
        RotwProvinceEntity siberie = new RotwProvinceEntity();
        siberie.setName("siberie");
        when(provinceDao.getProvinceByName("siberie")).thenReturn(siberie);
        RotwProvinceEntity oregon = new RotwProvinceEntity();
        oregon.setName("oregon");
        when(provinceDao.getProvinceByName("oregon")).thenReturn(oregon);
        when(provinceDao.getGeoGroups("oregon")).thenReturn(Collections.singletonList(IReferentielConstants.AMERICA));

        Assert.assertEquals(Leader.navalEurope, battleService.getLeaderConditions("north"));
        Assert.assertEquals(Leader.navalEurope, battleService.getLeaderConditions(north));

        Assert.assertEquals(Leader.navalEuropeMed, battleService.getLeaderConditions("lion"));
        Assert.assertEquals(Leader.navalEuropeMed, battleService.getLeaderConditions(lion));

        Assert.assertEquals(Leader.navalRotw, battleService.getLeaderConditions("tempetes"));
        Assert.assertEquals(Leader.navalRotw, battleService.getLeaderConditions(tempetes));

        Assert.assertEquals(Leader.landEurope, battleService.getLeaderConditions("idf"));
        Assert.assertEquals(Leader.landEurope, battleService.getLeaderConditions(idf));

        Assert.assertEquals(Leader.landRotwAsia, battleService.getLeaderConditions("aral"));
        Assert.assertEquals(Leader.landRotwAsia, battleService.getLeaderConditions(aral));

        Assert.assertEquals(Leader.landRotw, battleService.getLeaderConditions("siberie"));
        Assert.assertEquals(Leader.landRotw, battleService.getLeaderConditions(siberie));

        Assert.assertEquals(Leader.landRotwAmerica, battleService.getLeaderConditions("oregon"));
        Assert.assertEquals(Leader.landRotwAmerica, battleService.getLeaderConditions(oregon));
    }

    @Test
    public void testCheckLeader() {
        // All replacement leaders -> no diffs.
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NONE).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().replacementLeader())
                .notPhasing(CheckLeaderSideBuilder.create().replacementLeader())
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create());

        // Phasing wounded, not phasing replacement leader
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NONE).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).leaderStats("A 555").checkDie(1).woundDie(8))
                .notPhasing(CheckLeaderSideBuilder.create().size(3).replacementLeader())
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .phasingWound(4).phasingBox("S5"));

        // Phasing replacement leader, not phasing killed
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NONE).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).leaderStats("A 555").replacementLeader())
                .notPhasing(CheckLeaderSideBuilder.create().size(3).leaderStats("B 333").checkDie(1).woundDie(1))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .notPhasingWound(-1).notPhasingLeaderDead());

        // Both leaders wounded
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NONE).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).leaderStats("A 555").checkDie(2).woundDie(2))
                .notPhasing(CheckLeaderSideBuilder.create().size(3).leaderStats("B 333").checkDie(1).woundDie(4))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .phasingWound(1).phasingBox("W3")
                        .notPhasingWound(2).notPhasingBox("S4"));

        // Phasing leader wounded, not phasing no check because won the battle
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NON_PHASING).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).leaderStats("A 555").checkDie(2).woundDie(6))
                .notPhasing(CheckLeaderSideBuilder.create().size(3).leaderStats("B 333").checkDie(2))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .phasingWound(3).phasingBox("W4"));

        // Phasing leader no check because won the battle, not phasing leader wounded
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.PHASING).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).leaderStats("A 555").checkDie(2))
                .notPhasing(CheckLeaderSideBuilder.create().size(3).leaderStats("B 333").checkDie(1).woundDie(10))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .notPhasingWound(5).notPhasingBox("W5"));

        // Phasing leader killed because stack annihilated
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NON_PHASING).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).annihilated().leaderStats("A 555").checkDie(7).woundDie(3))
                .notPhasing(CheckLeaderSideBuilder.create().size(3).leaderStats("B 333").checkDie(2))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .phasingWound(-1).phasingLeaderDead());

        // Phasing leader survived despite total annihilation
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NON_PHASING).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).annihilated().leaderStats("A 555").checkDie(8))
                .notPhasing(CheckLeaderSideBuilder.create().size(3).leaderStats("B 333").checkDie(2))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create());

        // Phasing leader no check because enemy has less than 3LD in Europe
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NON_PHASING).roundBox("S3")
                .phasing(CheckLeaderSideBuilder.create().size(3).annihilated().leaderStats("A 555"))
                .notPhasing(CheckLeaderSideBuilder.create().size(2).leaderStats("B 333").checkDie(2))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create());

        // Phasing leader check despite enemy has less than 3 LD because it is in Rotw
        CheckLeaderBuilder.create().winner(BattleWinnerEnum.NON_PHASING).roundBox("S3").rotw()
                .phasing(CheckLeaderSideBuilder.create().size(3).annihilated().leaderStats("A 555").checkDie(7).woundDie(9))
                .notPhasing(CheckLeaderSideBuilder.create().size(2).leaderStats("B 333").checkDie(2))
                .whenCheckLeader(battleService, this)
                .thenExpect(CheckLeaderResultBuilder.create()
                        .phasingWound(-1).phasingLeaderDead());
    }

    static class CheckLeaderBuilder {
        boolean rotw;
        CheckLeaderSideBuilder phasing;
        CheckLeaderSideBuilder notPhasing;
        BattleWinnerEnum winner;
        String roundBox;
        BattleEntity battle;
        List<DiffEntity> diffs;
        List<DiffAttributesEntity> attributes;

        static CheckLeaderBuilder create() {
            return new CheckLeaderBuilder();
        }

        CheckLeaderBuilder rotw() {
            this.rotw = true;
            return this;
        }

        CheckLeaderBuilder phasing(CheckLeaderSideBuilder phasing) {
            this.phasing = phasing;
            return this;
        }

        CheckLeaderBuilder notPhasing(CheckLeaderSideBuilder notPhasing) {
            this.notPhasing = notPhasing;
            return this;
        }

        CheckLeaderBuilder winner(BattleWinnerEnum winner) {
            this.winner = winner;
            return this;
        }

        CheckLeaderBuilder roundBox(String roundBox) {
            this.roundBox = roundBox;
            return this;
        }

        CheckLeaderBuilder whenCheckLeader(BattleServiceImpl battleService, BattleServiceTest testClass) {
            GameEntity game = new GameEntity();
            AbstractBack.TABLES = new Tables();
            battle = new BattleEntity();
            battle.setId(25L);
            battle.setProvince("pecs");
            battle.setGame(game);
            battle.setWinner(winner);

            battle.getPhasing().setCountry("france");
            battle.getPhasing().setLeader("phasingLeader");
            battle.getPhasing().setSize(phasing.size);
            if (phasing.annihilated) {
                battle.getPhasing().setLosses(new BattleLossesEntity().add(AbstractWithLossEntity.create((int) (3 * phasing.size))));
            }

            battle.getNonPhasing().setCountry("espagne");
            battle.getNonPhasing().setLeader("notPhasingLeader");
            battle.getNonPhasing().setSize(notPhasing.size);
            if (notPhasing.annihilated) {
                battle.getNonPhasing().setLosses(new BattleLossesEntity().add(AbstractWithLossEntity.create((int) (3 * notPhasing.size))));
            }

            StackEntity stack = new StackEntity();
            stack.setGame(game);
            stack.setProvince(battle.getProvince());
            game.getStacks().add(stack);
            if (!phasing.replacementLeader) {
                stack.getCounters().add(createLeader(1L, "france", CounterFaceTypeEnum.LEADER, "phasingLeader", LeaderTypeEnum.GENERAL, phasing.leaderStats, AbstractBack.TABLES, stack));
            }
            if (!notPhasing.replacementLeader) {
                stack.getCounters().add(createLeader(2L, "espagne", CounterFaceTypeEnum.LEADER, "notPhasingLeader", LeaderTypeEnum.GENERAL, notPhasing.leaderStats, AbstractBack.TABLES, stack));
            }

            AbstractProvinceEntity province;
            if (rotw) {
                province = new RotwProvinceEntity();
            } else {
                province = new EuropeanProvinceEntity();
            }
            if (phasing.checkDie != null) {
                OngoingStubbing<Integer> dice = when(testClass.oeUtil.rollDie(game, battle.getPhasing().getCountry()));
                dice = dice.thenReturn(phasing.checkDie);
                if (phasing.woundDie != null) {
                    dice.thenReturn(phasing.woundDie);
                }
            }
            if (notPhasing.checkDie != null) {
                OngoingStubbing<Integer> dice = when(testClass.oeUtil.rollDie(game, battle.getNonPhasing().getCountry()));
                dice = dice.thenReturn(notPhasing.checkDie);
                if (notPhasing.woundDie != null) {
                    dice.thenReturn(notPhasing.woundDie);
                }
            }
            when(testClass.counterDomain.removeCounter(any())).thenAnswer(removeCounterAnswer());
            when(testClass.counterDomain.moveToSpecialBox(any(), any(), any())).thenAnswer(moveToSpecialBoxAnswer());
            when(testClass.oeUtil.getRoundBox(game)).thenReturn("B_MR_" + roundBox);

            diffs = new ArrayList<>();
            attributes = new ArrayList<>();
            diffs.addAll(battleService.checkLeaderDeaths(battle, true, province, attributes));
            diffs.addAll(battleService.checkLeaderDeaths(battle, false, province, attributes));

            return this;
        }

        CheckLeaderBuilder thenExpect(CheckLeaderResultBuilder result) {
            int nbDiffs = 0;
            boolean noChange = phasing.checkDie == null && notPhasing.checkDie == null;
            if (noChange) {
                Assert.assertEquals("No check was needed on leaders, so no modify battle diff is needed.", 0, attributes.size());
            } else {
                if (phasing.checkDie != null) {
                    Assert.assertEquals("The phasing leader check die is incorrect in the modify battle diff.", phasing.checkDie.toString(), getAttribute(attributes, DiffAttributeTypeEnum.PHASING_LEADER_CHECK));
                } else {
                    Assert.assertNull("The phasing leader check die attribute should not be sent.", getAttributeFull(attributes, DiffAttributeTypeEnum.PHASING_LEADER_CHECK));
                }
                if (phasing.woundDie != null) {
                    Assert.assertEquals("The phasing leader wound is incorrect in the modify battle diff.", result.phasingWound.toString(), getAttribute(attributes, DiffAttributeTypeEnum.PHASING_LEADER_WOUNDS));
                } else {
                    Assert.assertNull("The phasing leader wound attribute should not be sent.", getAttributeFull(attributes, DiffAttributeTypeEnum.PHASING_LEADER_WOUNDS));
                }

                if (notPhasing.checkDie != null) {
                    Assert.assertEquals("The not phasing leader check die is incorrect in the modify battle diff.", notPhasing.checkDie.toString(), getAttribute(attributes, DiffAttributeTypeEnum.NON_PHASING_LEADER_CHECK));
                } else {
                    Assert.assertNull("The not phasing leader check die attribute should not be sent.", getAttributeFull(attributes, DiffAttributeTypeEnum.NON_PHASING_LEADER_CHECK));
                }
                if (notPhasing.woundDie != null) {
                    Assert.assertEquals("The not phasing leader wound is incorrect in the modify battle diff.", result.notPhasingWound.toString(), getAttribute(attributes, DiffAttributeTypeEnum.NON_PHASING_LEADER_WOUNDS));
                } else {
                    Assert.assertNull("The not phasing leader wound attribute should not be sent.", getAttributeFull(attributes, DiffAttributeTypeEnum.NON_PHASING_LEADER_WOUNDS));
                }

                Assert.assertEquals("The phasing leader check die is incorrect in the battle.", phasing.checkDie, battle.getPhasing().getLeaderCheck());
                Assert.assertEquals("The phasing leader wound is incorrect in the battle.", result.phasingWound, battle.getPhasing().getLeaderWounds());
                Assert.assertEquals("The not phasing leader check die is incorrect in the battle.", notPhasing.checkDie, battle.getNonPhasing().getLeaderCheck());
                Assert.assertEquals("The not phasing leader wound is incorrect in the battle.", result.notPhasingWound, battle.getNonPhasing().getLeaderWounds());
            }
            DiffEntity diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                            Objects.equals(d.getIdObject(), 1L))
                    .findAny()
                    .orElse(null);
            if (result.phasingLeaderDead) {
                Assert.assertNotNull("The remove counter diff was not sent for phasing leader death.", diff);
                nbDiffs++;
            } else {
                Assert.assertNull("The phasing leader was not dead, no remove counter is needed.", diff);
            }
            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                            Objects.equals(d.getIdObject(), 2L))
                    .findAny()
                    .orElse(null);
            if (result.notPhasingLeaderDead) {
                Assert.assertNotNull("The remove counter diff was not sent for not phasing leader death.", diff);
                nbDiffs++;
            } else {
                Assert.assertNull("The not phasing leader was not dead, no remove counter is needed.", diff);
            }
            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                            Objects.equals(d.getIdObject(), 1L))
                    .findAny()
                    .orElse(null);
            if (result.phasingWound != null && result.phasingWound != -1) {
                Assert.assertNotNull("The move counter diff was not sent for phasing leader wound.", diff);
                Assert.assertEquals("The phasing leader was sent on the wrong round box.", "B_MR_" + result.phasingBox, getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_TO));
                nbDiffs++;
            } else {
                Assert.assertNull("The phasing leader was not wounded, no move counter is needed.", diff);
            }
            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER &&
                            Objects.equals(d.getIdObject(), 2L))
                    .findAny()
                    .orElse(null);
            if (result.notPhasingWound != null && result.notPhasingWound != -1) {
                Assert.assertNotNull("The move counter diff was not sent for not phasing leader wound.", diff);
                Assert.assertEquals("The not phasing leader was sent on the wrong round box.", "B_MR_" + result.notPhasingBox, getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_TO));
                nbDiffs++;
            } else {
                Assert.assertNull("The not phasing leader was not wounded, no move counter is needed.", diff);
            }

            Assert.assertEquals("Number of diffs received is incorrect.", nbDiffs, diffs.size());

            return this;
        }
    }

    static class CheckLeaderResultBuilder {
        boolean phasingLeaderDead;
        boolean notPhasingLeaderDead;
        Integer phasingWound;
        Integer notPhasingWound;
        String phasingBox;
        String notPhasingBox;

        static CheckLeaderResultBuilder create() {
            return new CheckLeaderResultBuilder();
        }

        CheckLeaderResultBuilder phasingLeaderDead() {
            this.phasingLeaderDead = true;
            return this;
        }

        CheckLeaderResultBuilder notPhasingLeaderDead() {
            this.notPhasingLeaderDead = true;
            return this;
        }

        CheckLeaderResultBuilder phasingWound(Integer phasingWound) {
            this.phasingWound = phasingWound;
            return this;
        }

        CheckLeaderResultBuilder notPhasingWound(Integer notPhasingWound) {
            this.notPhasingWound = notPhasingWound;
            return this;
        }

        CheckLeaderResultBuilder phasingBox(String phasingBox) {
            this.phasingBox = phasingBox;
            return this;
        }

        CheckLeaderResultBuilder notPhasingBox(String notPhasingBox) {
            this.notPhasingBox = notPhasingBox;
            return this;
        }
    }

    static class CheckLeaderSideBuilder {
        double size;
        boolean annihilated;
        boolean replacementLeader;
        String leaderStats;
        Integer checkDie;
        Integer woundDie;

        static CheckLeaderSideBuilder create() {
            return new CheckLeaderSideBuilder();
        }

        CheckLeaderSideBuilder size(double size) {
            this.size = size;
            return this;
        }

        CheckLeaderSideBuilder annihilated() {
            this.annihilated = true;
            return this;
        }

        CheckLeaderSideBuilder replacementLeader() {
            this.replacementLeader = true;
            return this;
        }

        CheckLeaderSideBuilder leaderStats(String leaderStats) {
            this.leaderStats = leaderStats;
            return this;
        }

        CheckLeaderSideBuilder checkDie(Integer checkDie) {
            this.checkDie = checkDie;
            return this;
        }

        CheckLeaderSideBuilder woundDie(Integer woundDie) {
            this.woundDie = woundDie;
            return this;
        }
    }
}
