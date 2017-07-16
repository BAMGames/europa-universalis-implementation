package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IMilitaryService;
import com.mkl.eu.client.service.service.military.ChooseBattleRequest;
import com.mkl.eu.client.service.service.military.SelectForceRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleCounterEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for military purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class MilitaryServiceImpl extends AbstractService implements IMilitaryService {
    @Autowired
    private IOEUtil oeUtil;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseBattle(Request<ChooseBattleRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_BATTLE)
                .setParams(METHOD_CHOOSE_BATTLE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_CHOOSE_BATTLE, PARAMETER_CHOOSE_BATTLE);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.MILITARY_BATTLES, request.getIdCountry(), METHOD_CHOOSE_BATTLE, PARAMETER_CHOOSE_BATTLE);

        // TODO Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_BATTLE, PARAMETER_REQUEST)
                .setParams(METHOD_CHOOSE_BATTLE));

        failIfEmpty(new AbstractService.CheckForThrow<String>()
                .setTest(request.getRequest().getProvince())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_CHOOSE_BATTLE));
        String province = request.getRequest().getProvince();

        String battleInProcess = game.getBattles().stream()
                .filter(battle -> battle.getStatus().isActive())
                .map(BattleEntity::getProvince)
                .findAny()
                .orElse(null);

        failIfNotNull(new AbstractService.CheckForThrow<>()
                .setTest(battleInProcess)
                .setCodeError(IConstantsServiceException.BATTLE_IN_PROCESS)
                .setMsgFormat("{1}: {0} No battle can be initiated while the battle in {2} is not finished.")
                .setName(PARAMETER_CHOOSE_BATTLE)
                .setParams(METHOD_CHOOSE_BATTLE, battleInProcess));

        List<String> provincesInBattle = game.getBattles().stream()
                .filter(battle -> battle.getStatus() == BattleStatusEnum.NEW)
                .map(BattleEntity::getProvince)
                .collect(Collectors.toList());

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(provincesInBattle.contains(province))
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} is not a province where a battle can be fought.")
                .setName(PARAMETER_CHOOSE_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_CHOOSE_BATTLE));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.NEW &&
                        StringUtils.equals(bat.getProvince(), province))
                .findAny()
                .orElse(null);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.BATTLE);
        diff.setIdObject(battle.getId());
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        List<String> allies = oeUtil.getAllies(country, game);
        List<String> enemies = oeUtil.getEnemies(country, game);

        List<CounterEntity> attackerCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                        allies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()))
                .collect(Collectors.toList());

        List<CounterEntity> defenderCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                        enemies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()))
                .collect(Collectors.toList());

        Integer attackerSize = attackerCounters.stream()
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .reduce(Integer::sum)
                .orElse(0);
        if (attackerCounters.size() <= 3 && attackerSize <= 8) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.ATTACKER_READY);
            diffAttributes.setValue(Boolean.TRUE.toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.setAttackerForces(true);
            attackerCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setAttacker(true);
                comp.setBattle(battle);
                comp.setCounter(counter);
                battle.getCounters().add(comp);

                DiffAttributesEntity attribute = new DiffAttributesEntity();
                attribute.setType(DiffAttributeTypeEnum.ATTACKER_COUNTER_ADD);
                attribute.setValue(counter.getId().toString());
                attribute.setDiff(diff);
                diff.getAttributes().add(attribute);
            });
        }

        Integer defenderSize = defenderCounters.stream()
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .reduce(Integer::sum)
                .orElse(0);
        if (defenderCounters.size() <= 3 && defenderSize <= 8) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.DEFENDER_READY);
            diffAttributes.setValue(Boolean.TRUE.toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.setDefenderForces(true);
            defenderCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setBattle(battle);
                comp.setCounter(counter);
                battle.getCounters().add(comp);

                DiffAttributesEntity attribute = new DiffAttributesEntity();
                attribute.setType(DiffAttributeTypeEnum.DEFENDER_COUNTER_ADD);
                attribute.setValue(counter.getId().toString());
                attribute.setDiff(diff);
                diff.getAttributes().add(attribute);
            });
        }

        BattleStatusEnum battleStatus = BattleStatusEnum.SELECT_FORCES;
        if (battle.isAttackerForces() != null && battle.isAttackerForces() &&
                battle.isDefenderForces() != null && battle.isDefenderForces()) {
            battleStatus = BattleStatusEnum.WITHDRAW_BEFORE_BATTLE;
        }
        battle.setStatus(battleStatus);
        diff.getAttributes().get(0).setValue(battleStatus.name());

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse selectForce(Request<SelectForceRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCE)
                .setParams(METHOD_SELECT_FORCE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_SELECT_FORCE, PARAMETER_SELECT_FORCE);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_SELECT_FORCE, PARAMETER_SELECT_FORCE);

        // TODO Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCE, PARAMETER_REQUEST)
                .setParams(METHOD_SELECT_FORCE));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest().getIdCounter())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCE, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_SELECT_FORCE));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.SELECT_FORCES)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_SELECT_FORCE)
                .setParams(METHOD_SELECT_FORCE, BattleStatusEnum.SELECT_FORCES.name()));

        boolean offensive = isCountryActive(game, request.getIdCountry());

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.BATTLE);
        diff.setIdObject(battle.getId());
        if (request.getRequest().isAdd()) {
            List<String> allies = oeUtil.getAllies(country, game);

            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                            allies.contains(stack.getCountry()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(c -> CounterUtil.isArmy(c.getType()) &&
                            c.getId().equals(request.getRequest().getIdCounter()))
                    .findAny()
                    .orElse(null);

            failIfNull(new AbstractService.CheckForThrow<>()
                    .setTest(counter)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_SELECT_FORCE, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                    .setParams(METHOD_SELECT_FORCE));

            BattleCounterEntity comp = new BattleCounterEntity();
            comp.setAttacker(offensive);
            comp.setBattle(battle);
            comp.setCounter(counter);
            battle.getCounters().add(comp);

            DiffAttributesEntity attribute = new DiffAttributesEntity();
            if (offensive) {
                attribute.setType(DiffAttributeTypeEnum.ATTACKER_COUNTER_ADD);
            } else {
                attribute.setType(DiffAttributeTypeEnum.DEFENDER_COUNTER_ADD);
            }
            attribute.setValue(counter.getId().toString());
            attribute.setDiff(diff);
            diff.getAttributes().add(attribute);
        } else {
            BattleCounterEntity battleCounter = battle.getCounters().stream()
                    .filter(c -> c.getCounter().getId().equals(request.getRequest().getIdCounter()))
                    .findAny()
                    .orElse(null);

            failIfNull(new AbstractService.CheckForThrow<>()
                    .setTest(battleCounter)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_SELECT_FORCE, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                    .setParams(METHOD_SELECT_FORCE));

            battle.getCounters().remove(battleCounter);

            DiffAttributesEntity attribute = new DiffAttributesEntity();
            if (offensive) {
                attribute.setType(DiffAttributeTypeEnum.ATTACKER_COUNTER_REMOVE);
            } else {
                attribute.setType(DiffAttributeTypeEnum.DEFENDER_COUNTER_REMOVE);
            }
            attribute.setValue(battleCounter.getCounter().getId().toString());
            attribute.setDiff(diff);
            diff.getAttributes().add(attribute);
        }

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }
}
