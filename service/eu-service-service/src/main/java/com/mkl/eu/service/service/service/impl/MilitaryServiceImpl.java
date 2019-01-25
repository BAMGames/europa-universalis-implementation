package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IMilitaryService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.ChooseBattleRequest;
import com.mkl.eu.client.service.service.military.SelectForceRequest;
import com.mkl.eu.client.service.service.military.WithdrawBeforeBattleRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.BattleTech;
import com.mkl.eu.client.service.vo.tables.CombatResult;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.persistence.oe.AbstractWithLossEntity;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleCounterEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleSideEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service for military purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class MilitaryServiceImpl extends AbstractService implements IMilitaryService {
    /** Counter Domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
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

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, (String) null));

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

        Double attackerSize = attackerCounters.stream()
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .reduce(Double::sum)
                .orElse(0d);
        if (attackerCounters.size() <= 3 && attackerSize <= 8) {
            DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, true);
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.getPhasing().setForces(true);
            attackerCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setPhasing(true);
                comp.setBattle(battle);
                comp.setCounter(counter);
                battle.getCounters().add(comp);

                DiffAttributesEntity attribute = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTER_ADD, counter.getId());
                attribute.setDiff(diff);
                diff.getAttributes().add(attribute);
            });
        }

        Double defenderSize = defenderCounters.stream()
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .reduce(Double::sum)
                .orElse(0d);
        if (defenderCounters.size() <= 3 && defenderSize <= 8) {
            DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, true);
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.getNonPhasing().setForces(true);
            defenderCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setBattle(battle);
                comp.setCounter(counter);
                battle.getCounters().add(comp);

                DiffAttributesEntity attribute = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD, counter.getId());
                attribute.setDiff(diff);
                diff.getAttributes().add(attribute);
            });
        }

        BattleStatusEnum battleStatus = BattleStatusEnum.SELECT_FORCES;
        if (battle.getPhasing().isForces() != null && battle.getPhasing().isForces() &&
                battle.getNonPhasing().isForces() != null && battle.getNonPhasing().isForces()) {
            battleStatus = BattleStatusEnum.WITHDRAW_BEFORE_BATTLE;
        }
        battle.setStatus(battleStatus);
        diff.getAttributes().get(0).setValue(battleStatus.name());

        return createDiff(diff, gameDiffs, request);
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

        boolean phasing = isCountryActive(game, request.getIdCountry());

        Boolean validated = phasing ? battle.getPhasing().isForces() : battle.getNonPhasing().isForces();

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(validated)
                .setCodeError(IConstantsServiceException.BATTLE_SELECT_VALIDATED)
                .setMsgFormat("{1}: {0} Forces cannot be added or removed to the battle because it has already been validated.")
                .setName(PARAMETER_SELECT_FORCE)
                .setParams(METHOD_SELECT_FORCE, phasing));

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId());
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
                    .setParams(METHOD_SELECT_FORCE, request.getRequest().getIdCounter()));

            BattleCounterEntity comp = new BattleCounterEntity();
            comp.setPhasing(phasing);
            comp.setBattle(battle);
            comp.setCounter(counter);
            battle.getCounters().add(comp);

            DiffAttributesEntity attribute = DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_COUNTER_ADD : DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD,
                    counter.getId());
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

            DiffAttributesEntity attribute = DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_COUNTER_REMOVE : DiffAttributeTypeEnum.NON_PHASING_COUNTER_REMOVE,
                    battleCounter.getCounter().getId());
            attribute.setDiff(diff);
            diff.getAttributes().add(attribute);
        }

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateForces(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_FORCES)
                .setParams(METHOD_VALIDATE_FORCES));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_VALIDATE_FORCES, PARAMETER_VALIDATE_FORCES);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_VALIDATE_FORCES, PARAMETER_VALIDATE_FORCES);

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
                .setName(PARAMETER_VALIDATE_FORCES, PARAMETER_REQUEST)
                .setParams(METHOD_VALIDATE_FORCES));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.SELECT_FORCES)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_VALIDATE_FORCES)
                .setParams(METHOD_VALIDATE_FORCES, BattleStatusEnum.SELECT_FORCES.name()));

        boolean phasing = isCountryActive(game, request.getIdCountry());
        // TODO check for non phasing country that he is part of the stack

        Boolean oldValidation = phasing ? battle.getPhasing().isForces() : battle.getNonPhasing().isForces();

        List<DiffEntity> diffs = new ArrayList<>();

        if (request.getRequest().isValidate() != BooleanUtils.toBoolean(oldValidation)) {
            if (!request.getRequest().isValidate()) {
                List<String> allies = oeUtil.getAllies(country, game);

                List<Long> alliedCounters = battle.getCounters().stream()
                        .filter(bc -> bc.isPhasing() == phasing)
                        .map(bc -> bc.getCounter().getId())
                        .collect(Collectors.toList());
                List<CounterEntity> remainingCounters = game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                                allies.contains(stack.getCountry()))
                        .flatMap(stack -> stack.getCounters().stream())
                        .filter(counter -> CounterUtil.isArmy(counter.getType()) &&
                                !alliedCounters.contains(counter.getId()))
                        .collect(Collectors.toList());

                failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                        .setTest(remainingCounters.size() == 0)
                        .setCodeError(IConstantsServiceException.BATTLE_INVALIDATE_NO_FORCE)
                        .setMsgFormat("{1}: {0} Impossible to invalidate forces in this battle because there is no other forces to select (phasing player: {2}).")
                        .setName(PARAMETER_VALIDATE_FORCES, PARAMETER_REQUEST, PARAMETER_VALIDATE)
                        .setParams(METHOD_VALIDATE_FORCES, phasing));
            } else {
                List<Long> alliedCounters = battle.getCounters().stream()
                        .filter(bc -> bc.isPhasing() == phasing)
                        .map(bc -> bc.getCounter().getId())
                        .collect(Collectors.toList());
                Double armySize = battle.getCounters().stream()
                        .map(bc -> CounterUtil.getSizeFromType(bc.getCounter().getType()))
                        .reduce(Double::sum)
                        .orElse(0d);

                if (alliedCounters.size() < 3 && armySize < 8) {
                    List<String> allies = oeUtil.getAllies(country, game);
                    Double remainingMinSize = game.getStacks().stream()
                            .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                                    allies.contains(stack.getCountry()))
                            .flatMap(stack -> stack.getCounters().stream())
                            .filter(counter -> CounterUtil.isArmy(counter.getType()) &&
                                    !alliedCounters.contains(counter.getId()))
                            .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                            .min(Double::compare)
                            .orElse(0d);

                    failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                            .setTest(remainingMinSize > 0 && remainingMinSize <= 8 - armySize)
                            .setCodeError(IConstantsServiceException.BATTLE_VALIDATE_OTHER_FORCE)
                            .setMsgFormat("{1}: {0} Impossible to validate forces in this battle because there are other forces to select (phasing player: {2}).")
                            .setName(PARAMETER_VALIDATE_FORCES, PARAMETER_REQUEST, PARAMETER_VALIDATE)
                            .setParams(METHOD_VALIDATE_FORCES, phasing));
                }
            }

            if (phasing) {
                battle.getPhasing().setForces(request.getRequest().isValidate());
            } else {
                battle.getNonPhasing().setForces(request.getRequest().isValidate());
            }

            if (battle.getPhasing().isForces() && battle.getNonPhasing().isForces()) {
                battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
            }

            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                    DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_READY : DiffAttributeTypeEnum.NON_PHASING_READY, request.getRequest().isValidate()),
                    // If both side are ready, then go to next phase
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.WITHDRAW_BEFORE_BATTLE, battle.getPhasing().isForces() && battle.getNonPhasing().isForces()));

            diffs.add(diff);
        }

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse withdrawBeforeBattle(Request<WithdrawBeforeBattleRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE)
                .setParams(METHOD_WITHDRAW_BEFORE_BATTLE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_WITHDRAW_BEFORE_BATTLE, PARAMETER_WITHDRAW_BEFORE_BATTLE);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_WITHDRAW_BEFORE_BATTLE, PARAMETER_WITHDRAW_BEFORE_BATTLE);

        // TODO Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
                .findFirst()
                .orElse(null);

        // TODO check that the player doing the request is leader of the stack
        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(isPhasingPlayer(game, request.getIdCountry()))
                .setCodeError(IConstantsServiceException.BATTLE_ONLY_NON_PHASING_CAN_WITHDRAW)
                .setMsgFormat("{1}: {0} only non phasing player can withdraw before battle.")
                .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_WITHDRAW_BEFORE_BATTLE));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE, PARAMETER_REQUEST)
                .setParams(METHOD_WITHDRAW_BEFORE_BATTLE));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.WITHDRAW_BEFORE_BATTLE)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE)
                .setParams(METHOD_WITHDRAW_BEFORE_BATTLE, BattleStatusEnum.WITHDRAW_BEFORE_BATTLE.name()));

        if (request.getRequest().isWithdraw()) {
            String provinceTo = request.getRequest().getProvinceTo();

            failIfEmpty(new AbstractService.CheckForThrow<String>()
                    .setTest(provinceTo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                    .setMsgFormat(MSG_MISSING_PARAMETER)
                    .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                    .setParams(METHOD_WITHDRAW_BEFORE_BATTLE));

            AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);

            failIfNull(new CheckForThrow<>()
                    .setTest(province)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                    .setParams(METHOD_WITHDRAW_BEFORE_BATTLE, provinceTo));

            AbstractProvinceEntity provinceFrom = provinceDao.getProvinceByName(battle.getProvince());
            boolean isNear = StringUtils.equals(provinceTo, battle.getProvince());
            if (!isNear && provinceFrom != null) {
                isNear = provinceFrom.getBorders().stream()
                        .anyMatch(x -> Objects.equals(province.getId(), x.getProvinceTo().getId()));
            }

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(isNear)
                    .setCodeError(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR)
                    .setMsgFormat(MSG_NOT_NEIGHBOR)
                    .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                    .setParams(METHOD_WITHDRAW_BEFORE_BATTLE, battle.getProvince(), provinceTo));

            double stackSize = battle.getCounters().stream()
                    .filter(BattleCounterEntity::isNotPhasing)
                    .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getCounter().getType())));
            boolean canRetreat = oeUtil.canRetreat(province, province == provinceFrom, stackSize, country, game);

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(canRetreat)
                    .setCodeError(IConstantsServiceException.BATTLE_CANT_WITHDRAW)
                    .setMsgFormat("{1}: {0} {2} is not a valid province to withdraw.")
                    .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                    .setParams(METHOD_WITHDRAW_BEFORE_BATTLE, provinceTo));

            boolean success = StringUtils.equals(battle.getProvince(), provinceTo);
            if (!success) {
                int die = oeUtil.rollDie(game, country);

                // TODO leader diff manoeuvre if positive
                if (die >= 8) {
                    success = true;
                }
            }

            if (success) {
                battle.setStatus(BattleStatusEnum.DONE);
                battle.setEnd(BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
                battle.setWinner(BattleWinnerEnum.NONE);

                List<DiffEntity> newDiffs = new ArrayList<>();
                DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.DONE),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.END, BattleEndEnum.WITHDRAW_BEFORE_BATTLE),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.WINNER, BattleWinnerEnum.NONE));
                newDiffs.add(diff);
                Consumer<StackEntity> retreatStack = stack -> {
                    stack.setMovePhase(MovePhaseEnum.MOVED);
                    stack.setProvince(provinceTo);
                    newDiffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.STACK, stack.getId(),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, stack.getProvince()),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, provinceTo),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED)));
                };
                battle.getCounters().stream()
                        .filter(BattleCounterEntity::isNotPhasing)
                        .map(c -> c.getCounter().getOwner())
                        .distinct()
                        .forEach(retreatStack);
                cleanUpBattle(battle);

                return createDiffs(newDiffs, gameDiffs, request);
            }
        }

        List<DiffEntity> newDiffs = new ArrayList<>();
        // TODO replacement leader if needed
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        attributes.addAll(fillBattleModifiers(battle));
        attributes.addAll(computeBothSequence(battle, BattleSequenceEnum.FIRST_FIRE));
        newDiffs.addAll(checkRouted(battle, BattleEndEnum.ROUTED_AT_FIRST_FIRE, 3, attributes));
        // TODO at sea, WIND advantage forces can retreat
        if (battle.getEnd() == null) {
            attributes.addAll(computeBothSequence(battle, BattleSequenceEnum.FIRST_SHOCK));
            newDiffs.addAll(checkRouted(battle, BattleEndEnum.ROUTED_AT_FIRST_SHOCK, 2, attributes));
            if (battle.getEnd() == null) {
                newDiffs.addAll(checkAnnihilated(battle, BattleEndEnum.ANNIHILATED_AT_FIRST_DAY, 2, attributes));
            }
        }
        if (battle.getEnd() == null) {
            battle.setStatus(BattleStatusEnum.WITHDRAW_AFTER_FIRST_DAY_ATT);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.WITHDRAW_AFTER_FIRST_DAY_ATT));
        }

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS, battle.getNonPhasing().getLosses().getRoundLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS, battle.getNonPhasing().getLosses().getThirdLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS, battle.getNonPhasing().getLosses().getMoraleLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS, battle.getPhasing().getLosses().getRoundLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS, battle.getPhasing().getLosses().getThirdLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS, battle.getPhasing().getLosses().getMoraleLoss()));

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        newDiffs.add(diff);

        return createDiffs(newDiffs, gameDiffs, request);
    }

    /**
     * Fill the battle modifiers of a battle:
     * - fire column and bonus
     * - shock column and bonus
     * - pursuit bonus
     * - size
     * - sizeDiff
     * - tech
     * - moral
     *
     * @param battle the battle.
     * @return the eventual attributes, if any.
     */
    protected List<DiffAttributesEntity> fillBattleModifiers(BattleEntity battle) {
        battle.getPhasing().getFirstDay().clear();
        battle.getPhasing().getSecondDay().clear();
        battle.getNonPhasing().getFirstDay().clear();
        battle.getNonPhasing().getSecondDay().clear();
        battle.getPhasing().setPursuitMod(0);
        battle.getNonPhasing().setPursuitMod(0);

        List<CounterEntity> countersPhasing = battle.getCounters().stream()
                .filter(BattleCounterEntity::isPhasing)
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());
        List<CounterEntity> countersNotPhasing = battle.getCounters().stream()
                .filter(bc -> !bc.isPhasing())
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());

        battle.getPhasing().setSize(countersPhasing.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType()))));
        battle.getNonPhasing().setSize(countersNotPhasing.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType()))));

        String techPhasing = oeUtil.getTechnology(countersPhasing,
                true, getReferential(), getTables(), battle.getGame());
        battle.getPhasing().setTech(techPhasing);

        String techNotPhasing = oeUtil.getTechnology(countersNotPhasing,
                true, getReferential(), getTables(), battle.getGame());
        battle.getNonPhasing().setTech(techNotPhasing);

        // TODO tercios moral boost
        BattleTech battleTechPhasing = getTables().getBattleTechs().stream()
                .filter(bt -> StringUtils.equals(bt.getTechnologyFor(), techPhasing) && StringUtils.equals(bt.getTechnologyAgainst(), techNotPhasing))
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "battleTechs", techPhasing + " - " + techNotPhasing));
        battle.getPhasing().setFireColumn(battleTechPhasing.getColumnFire());
        battle.getPhasing().setShockColumn(battleTechPhasing.getColumnShock());
        if (battleTechPhasing.isMoralBonusVeteran() && oeUtil.isStackVeteran(countersPhasing)) {
            battle.getPhasing().setMoral(battleTechPhasing.getMoral() + 1);
        } else {
            battle.getPhasing().setMoral(battleTechPhasing.getMoral());
        }

        BattleTech battleTechNonPhasing = getTables().getBattleTechs().stream()
                .filter(bt -> StringUtils.equals(bt.getTechnologyFor(), techNotPhasing) && StringUtils.equals(bt.getTechnologyAgainst(), techPhasing))
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "battleTechs", techNotPhasing + " - " + techPhasing));
        battle.getNonPhasing().setFireColumn(battleTechNonPhasing.getColumnFire());
        battle.getNonPhasing().setShockColumn(battleTechNonPhasing.getColumnShock());
        if (battleTechNonPhasing.isMoralBonusVeteran() && oeUtil.isStackVeteran(countersNotPhasing)) {
            battle.getNonPhasing().setMoral(battleTechNonPhasing.getMoral() + 1);
        } else {
            battle.getNonPhasing().setMoral(battleTechNonPhasing.getMoral());
        }

        // Second day, everyone -1
        battle.getPhasing().getSecondDay().addFireAndShock(-1);
        battle.getNonPhasing().getSecondDay().addFireAndShock(-1);

        // TODO leaders

        // TODO tercios

        // TODO foraging

        // Terrain modifiers
        AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
        switch (province.getTerrain()) {
            case DENSE_FOREST:
            case SPARSE_FOREST:
            case DESERT:
            case SWAMP:
                battle.getPhasing().getFirstDay().addFireAndShock(-1);
                battle.getNonPhasing().getFirstDay().addFireAndShock(-1);
                battle.getPhasing().getSecondDay().addFireAndShock(-1);
                battle.getNonPhasing().getSecondDay().addFireAndShock(-1);
                battle.getPhasing().addPursuit(-1);
                battle.getNonPhasing().addPursuit(-1);
                break;
            case MOUNTAIN:
                battle.getPhasing().getFirstDay().addFireAndShock(-1);
                battle.getPhasing().getSecondDay().addFireAndShock(-1);
                battle.getPhasing().addPursuit(-1);
                battle.getNonPhasing().addPursuit(-1);
                break;
            case PLAIN:
            default:
                break;
        }

        // TODO river/straits

        boolean phasingNoArmy = !countersPhasing.stream()
                .anyMatch(counter -> CounterUtil.getSizeFromType(counter.getType()) >= 2);
        boolean nonPhasingNoArmy = !countersNotPhasing.stream()
                .anyMatch(counter -> CounterUtil.getSizeFromType(counter.getType()) >= 2);
        // Renaissance in Europe without army => no fire phase
        if (StringUtils.equals(techPhasing, Tech.RENAISSANCE) && province instanceof EuropeanProvinceEntity && phasingNoArmy) {
            battle.getPhasing().setFireColumn(null);
        }
        if (StringUtils.equals(techNotPhasing, Tech.RENAISSANCE) && province instanceof EuropeanProvinceEntity && nonPhasingNoArmy) {
            battle.getNonPhasing().setFireColumn(null);
        }

        // No army counter -> -1 to fire
        if (phasingNoArmy) {
            battle.getPhasing().getFirstDay().addFire(-1);
            battle.getPhasing().getSecondDay().addFire(-1);
        }
        if (nonPhasingNoArmy) {
            battle.getNonPhasing().getFirstDay().addFire(-1);
            battle.getNonPhasing().getSecondDay().addFire(-1);
        }

        List<ArmyInfo> armyPhasing = oeUtil.getArmyInfo(countersPhasing, getReferential());
        List<ArmyInfo> armyNotPhasing = oeUtil.getArmyInfo(countersNotPhasing, getReferential());

        // 6 artilleries -> +1 to fire
        int bonusArtilleryPhasing = oeUtil.getArtilleryBonus(armyPhasing, getTables(), battle.getGame());
        int bonusArtilleryNotPhasing = oeUtil.getArtilleryBonus(armyNotPhasing, getTables(), battle.getGame());
        if (bonusArtilleryPhasing >= 6) {
            battle.getPhasing().getFirstDay().addFire(1);
            battle.getPhasing().getSecondDay().addFire(1);
        }
        if (bonusArtilleryNotPhasing >= 6) {
            battle.getNonPhasing().getFirstDay().addFire(1);
            battle.getNonPhasing().getSecondDay().addFire(1);
        }

        // army bigger by at least 3 regiments -> +1 to shock
        if (battle.getPhasing().getSize() >= battle.getNonPhasing().getSize() + 3) {
            battle.getPhasing().getFirstDay().addShock(1);
            battle.getPhasing().getSecondDay().addShock(1);
        }
        if (battle.getNonPhasing().getSize() >= battle.getPhasing().getSize() + 3) {
            battle.getNonPhasing().getFirstDay().addShock(1);
            battle.getNonPhasing().getSecondDay().addShock(1);
        }

        // structural cavalry modifier -> +1 to shock
        if (oeUtil.getCavalryBonus(armyPhasing, province.getTerrain(), getTables(), battle.getGame())) {
            battle.getPhasing().getFirstDay().addShock(1);
            battle.getPhasing().getSecondDay().addShock(1);
        }
        if (oeUtil.getCavalryBonus(armyNotPhasing, province.getTerrain(), getTables(), battle.getGame())) {
            battle.getNonPhasing().getFirstDay().addShock(1);
            battle.getNonPhasing().getSecondDay().addShock(1);
        }

        // TODO TUR Sipahi for pursuit

        // Size diff
        double phasingSize = oeUtil.getArmySize(armyPhasing, getTables(), battle.getGame());
        double nonPhasingSize = oeUtil.getArmySize(armyNotPhasing, getTables(), battle.getGame());
        battle.getPhasing().setSizeDiff(oeUtil.getSizeDiff(phasingSize, nonPhasingSize));
        battle.getNonPhasing().setSizeDiff(oeUtil.getSizeDiff(nonPhasingSize, phasingSize));

        List<DiffAttributesEntity> attributes = new ArrayList<>();

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SIZE, battle.getPhasing().getSize()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_TECH, battle.getPhasing().getTech()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_FIRE_COL, battle.getPhasing().getFireColumn()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SHOCK_COL, battle.getPhasing().getShockColumn()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_MORAL, battle.getPhasing().getMoral()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, battle.getPhasing().getPursuitMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SIZE_DIFF, battle.getPhasing().getSizeDiff()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE_MOD, battle.getPhasing().getFirstDay().getFireMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK_MOD, battle.getPhasing().getFirstDay().getShockMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE_MOD, battle.getPhasing().getSecondDay().getFireMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK_MOD, battle.getPhasing().getSecondDay().getShockMod()));

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SIZE, battle.getNonPhasing().getSize()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_TECH, battle.getNonPhasing().getTech()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRE_COL, battle.getNonPhasing().getFireColumn()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SHOCK_COL, battle.getNonPhasing().getShockColumn()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORAL, battle.getNonPhasing().getMoral()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, battle.getNonPhasing().getPursuitMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SIZE_DIFF, battle.getNonPhasing().getSizeDiff()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD, battle.getNonPhasing().getFirstDay().getFireMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD, battle.getNonPhasing().getFirstDay().getShockMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD, battle.getNonPhasing().getSecondDay().getFireMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD, battle.getNonPhasing().getSecondDay().getShockMod()));

        return attributes;
    }

    /**
     * Compute a sequence of a battle.
     * A sequence can be fire or shock of first or second day.
     * Will compute both side damage.
     *
     * @param battle the battle.
     * @return the eventual attributes, if any.
     */
    protected List<DiffAttributesEntity> computeBothSequence(BattleEntity battle, BattleSequenceEnum sequence) {
        List<DiffAttributesEntity> attributes = new ArrayList<>();

        DiffAttributesEntity attribute = computeSequence(sequence, battle.getPhasing(), battle.getNonPhasing(), true, battle.getGame());
        if (attribute != null) {
            attributes.add(attribute);
        }
        attribute = computeSequence(sequence, battle.getNonPhasing(), battle.getPhasing(), false, battle.getGame());
        if (attribute != null) {
            attributes.add(attribute);
        }

        return attributes;
    }

    /**
     * @param die      rolled.
     * @param modifier to the die.
     * @param column   of the die.
     * @return the result of a combat round for the given die, modifier and column.
     */
    private CombatResult getResult(Integer die, Integer modifier, String column) {
        int min = getTables().getCombatResults().stream()
                .filter(result -> StringUtils.equals(column, result.getColumn()))
                .map(CombatResult::getDice)
                .min(Comparator.naturalOrder())
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "combatResults", column));
        int max = getTables().getCombatResults().stream()
                .filter(result -> StringUtils.equals(column, result.getColumn()))
                .map(CombatResult::getDice)
                .max(Comparator.naturalOrder())
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "combatResults", column));
        int modifiedDie = die + modifier < min ? min : die + modifier > max ? max : die + modifier;

        return getTables().getCombatResults().stream()
                .filter(result -> StringUtils.equals(column, result.getColumn()) && modifiedDie == result.getDice())
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "combatResults", column + " - " + modifiedDie));
    }

    /**
     * Check if any side has been routed.
     * If so, check if any side has been annihilated.
     * Compute a pursuit if necessary.
     *
     * @param battle       the battle.
     * @param potentialEnd if any side has been routed, will be the end value of the battle.
     * @param pursuitBonus if any side has been routed, will be the pursuit modifier.
     * @param attributes   the diff attributes of the battle modify diff event.
     * @return eventual other diffs.
     */
    private List<DiffEntity> checkRouted(BattleEntity battle, BattleEndEnum potentialEnd, int pursuitBonus, List<DiffAttributesEntity> attributes) {
        boolean phasingRouted = CommonUtil.subtract(battle.getPhasing().getMoral(), battle.getPhasing().getLosses().getMoraleLoss()) <= 0;
        boolean nonPhasingRouted = CommonUtil.subtract(battle.getNonPhasing().getMoral(), battle.getNonPhasing().getLosses().getMoraleLoss()) <= 0;
        if (phasingRouted || nonPhasingRouted) {
            battle.setEnd(potentialEnd);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.END, potentialEnd));

            boolean phasingAnnihilated = isAnnihilated(battle.getPhasing(), battle.getNonPhasing());
            boolean nonPhasingAnnihilated = isAnnihilated(battle.getNonPhasing(), battle.getPhasing());

            // If both side annihilated or no side annihilated and both side routed, then no winner, it is defeat for both side
            boolean pursuit = false;
            if (phasingAnnihilated && nonPhasingAnnihilated || !phasingAnnihilated && !nonPhasingAnnihilated && phasingRouted && nonPhasingRouted) {
                battle.setWinner(BattleWinnerEnum.NONE);
            } else if (phasingAnnihilated || !nonPhasingAnnihilated && phasingRouted) {
                battle.setWinner(BattleWinnerEnum.NON_PHASING);
                pursuit = !nonPhasingRouted;
            } else {
                battle.setWinner(BattleWinnerEnum.PHASING);
                pursuit = !phasingRouted;
            }
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.WINNER, battle.getWinner()));

            if (pursuitBonus != 0) {
                battle.getPhasing().addPursuit(pursuitBonus);
                battle.getNonPhasing().addPursuit(pursuitBonus);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, battle.getPhasing().getPursuitMod()));
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, battle.getNonPhasing().getPursuitMod()));
            }
            if (pursuit) {
                attributes.addAll(computePursuit(battle));
            }
            return endBattle(battle, attributes);
        }

        return Collections.emptyList();
    }

    /**
     * Check if any side has been annihilated.
     * Compute a pursuit if necessary.
     *
     * @param battle       the battle.
     * @param potentialEnd if any side has been annihilated, will be the end value of the battle.
     * @param pursuitBonus if any side has been annihilated, will be the pursuit modifier.
     * @param attributes   the diff attributes of the battle modify diff event.
     * @return eventual other diffs.
     */
    private List<DiffEntity> checkAnnihilated(BattleEntity battle, BattleEndEnum potentialEnd, int pursuitBonus, List<DiffAttributesEntity> attributes) {
        boolean phasingAnnihilated = isAnnihilated(battle.getPhasing(), battle.getNonPhasing());
        boolean nonPhasingAnnihilated = isAnnihilated(battle.getNonPhasing(), battle.getPhasing());
        if (phasingAnnihilated || nonPhasingAnnihilated) {
            battle.setEnd(potentialEnd);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.END, potentialEnd));

            // If both side annihilated, then no winner, it is defeat for both side
            if (phasingAnnihilated && nonPhasingAnnihilated) {
                battle.setWinner(BattleWinnerEnum.NONE);
            } else if (phasingAnnihilated) {
                battle.setWinner(BattleWinnerEnum.NON_PHASING);
            } else {
                battle.setWinner(BattleWinnerEnum.PHASING);
            }
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.WINNER, battle.getWinner()));

            if (pursuitBonus != 0) {
                battle.getPhasing().addPursuit(pursuitBonus);
                battle.getNonPhasing().addPursuit(pursuitBonus);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, battle.getPhasing().getPursuitMod()));
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, battle.getNonPhasing().getPursuitMod()));
            }
            attributes.addAll(computePursuit(battle));
            return endBattle(battle, attributes);
        }

        return Collections.emptyList();
    }

    /**
     * Compute the pursuit of a battle.
     *
     * @param battle the battle.
     * @return the eventual attributes, if any.
     */
    private List<DiffAttributesEntity> computePursuit(BattleEntity battle) {
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        BattleSideEntity winner;
        BattleSideEntity loser;
        switch (battle.getWinner()) {
            case PHASING:
                winner = battle.getPhasing();
                loser = battle.getNonPhasing();
                break;
            case NON_PHASING:
                winner = battle.getNonPhasing();
                loser = battle.getPhasing();
                break;
            case NONE:
            default:
                winner = null;
                loser = null;
                break;
        }

        if (winner != null && loser != null) {
            attributes.add(computeSequence(BattleSequenceEnum.PURSUIT, winner, loser, battle.getWinner() == BattleWinnerEnum.PHASING, battle.getGame()));
        }

        return attributes;
    }

    /**
     * Compute a sequence of battle for one side.
     *
     * @param sequence      of the battle.
     * @param active        the side doing the damage.
     * @param passive       the side receiving the damage.
     * @param activePhasing if the side doing the damage is the phasing side.
     * @param game          the game.
     * @return the eventual attributes, if any.
     */
    private DiffAttributesEntity computeSequence(BattleSequenceEnum sequence, BattleSideEntity active, BattleSideEntity passive, boolean activePhasing, GameEntity game) {
        Integer modifier;
        String column;
        Consumer<Integer> setDice;
        DiffAttributeTypeEnum type;
        switch (sequence) {
            case FIRST_FIRE:
                modifier = active.getFirstDay().getFireMod();
                column = active.getFireColumn();
                setDice = die -> active.getFirstDay().setFire(die);
                if (activePhasing) {
                    type = DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE;
                } else {
                    type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE;
                }
                break;
            case FIRST_SHOCK:
                modifier = active.getFirstDay().getShockMod();
                column = active.getShockColumn();
                setDice = die -> active.getFirstDay().setShock(die);
                if (activePhasing) {
                    type = DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK;
                } else {
                    type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK;
                }
                break;
            case SECOND_FIRE:
                modifier = active.getSecondDay().getFireMod();
                column = active.getFireColumn();
                setDice = die -> active.getSecondDay().setFire(die);
                if (activePhasing) {
                    type = DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE;
                } else {
                    type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE;
                }
                break;
            case SECOND_SHOCK:
                modifier = active.getSecondDay().getShockMod();
                column = active.getShockColumn();
                setDice = die -> active.getSecondDay().setShock(die);
                if (activePhasing) {
                    type = DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK;
                } else {
                    type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK;
                }
                break;
            case PURSUIT:
                modifier = active.getPursuitMod();
                column = CombatResult.COLUMN_E;
                setDice = active::setPursuit;
                if (activePhasing) {
                    type = DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT;
                } else {
                    type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT;
                }
                break;
            default:
                modifier = 0;
                column = null;
                setDice = null;
                type = null;
        }

        if (StringUtils.isNotEmpty(column)) {
            Integer die = oeUtil.rollDie(game);
            setDice.accept(die);


            AbstractWithLoss phasingResult = getResult(die, modifier, column);
            if (sequence == BattleSequenceEnum.FIRST_FIRE || sequence == BattleSequenceEnum.SECOND_FIRE) {
                phasingResult = phasingResult.adjustToTech(active.getTech());
            }
            passive.getLosses().add(phasingResult);
            // losses attributes will be sent at the end of the main method

            return DiffUtil.createDiffAttributes(type, die);
        }
        return null;
    }

    /**
     * @param active  the side checking annihilation.
     * @param passive the opposing side.
     * @return if the stack has been annihilated by its losses.
     */
    private boolean isAnnihilated(BattleSideEntity active, BattleSideEntity passive) {
        // losses can happen after first fire againt medieval
        int thirdLosses = active.getLosses() != null ? active.getLosses().getTotalThird() : 0;
        // Is opposing size too small ?
        AbstractWithLossEntity mitigatedLosses = oeUtil.lossesMitigation(passive.getSize(), true, null);
        // Losses cant be negative
        AbstractWithLossEntity virtualLosses = AbstractWithLossEntity.create(Math.max(0, thirdLosses - mitigatedLosses.getTotalThird()));
        // Losses cant be more than opposing forces
        virtualLosses = AbstractWithLossEntity.create(Math.min((int) (3 * passive.getSize()), virtualLosses.getTotalThird()));
        // Is there a size diff between the stack ?
        AbstractWithLossEntity finalLosses = oeUtil.lossModificationSize(virtualLosses, passive.getSizeDiff());
        return finalLosses.isGreaterThanSize(active.getSize());
    }

    /**
     * Method called after all phases (fire, shock, pursuit) of a battle have been done.
     * It will reduce the losses and then go to the next phase.
     *
     * @param battle     the battle.
     * @param attributes the diff attributes of the battle modify diff event.
     * @return eventual other diffs.
     */
    private List<DiffEntity> endBattle(BattleEntity battle, List<DiffAttributesEntity> attributes) {
        // First, reduce losses
        reduceLosses(battle.getPhasing(), battle.getNonPhasing(), battle.getGame());
        reduceLosses(battle.getNonPhasing(), battle.getPhasing(), battle.getGame());
        // Then do retreat for non winning stacks
        computeRetreat(battle.getPhasing(), battle.getWinner() == BattleWinnerEnum.PHASING, () -> {
            int retreat = oeUtil.rollDie(battle.getGame());
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_RETREAT, retreat));
            return retreat;
        });
        computeRetreat(battle.getNonPhasing(), battle.getWinner() == BattleWinnerEnum.NON_PHASING, () -> {
            int retreat = oeUtil.rollDie(battle.getGame());
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_RETREAT, retreat));
            return retreat;
        });
        AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
        if (province instanceof EuropeanProvinceEntity) {
            battle.getPhasing().getLosses().roundToClosestInteger();
            battle.getNonPhasing().getLosses().roundToClosestInteger();
        }
        // Impossible to take more losses than troops
        battle.getPhasing().getLosses().maxToSize(battle.getPhasing().getSize());
        battle.getNonPhasing().getLosses().maxToSize(battle.getNonPhasing().getSize());

        return prepareChooseLosses(battle, attributes);
    }

    /**
     * Prepare the choose losses phase of a battle.
     * If no choose is necessary, then go the retreat phase.
     *
     * @param battle     the battle.
     * @param attributes the diff attributes of the battle modify diff event.
     * @return eventual other diffs.
     */
    private List<DiffEntity> prepareChooseLosses(BattleEntity battle, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        // if needed CHOOSE_LOSSES if not and losing stacks not annihilated RETREAT if not DONE
        boolean phasingLossesAuto = battle.getPhasing().getLosses().getTotalThird() == 0 || battle.getPhasing().getLosses().isGreaterThanSize(battle.getPhasing().getSize());
        boolean nonPhasingLossesAuto = battle.getNonPhasing().getLosses().getTotalThird() == 0 || battle.getNonPhasing().getLosses().isGreaterThanSize(battle.getNonPhasing().getSize());

        battle.getPhasing().setLossesSelected(phasingLossesAuto);
        battle.getNonPhasing().setLossesSelected(nonPhasingLossesAuto);

        // if annihilated, remove all counters
        if (battle.getPhasing().getLosses().isGreaterThanSize(battle.getPhasing().getSize())) {
            battle.getCounters().stream()
                    .filter(BattleCounterEntity::isPhasing)
                    .forEach(counter -> diffs.add(counterDomain.removeCounter(counter.getCounter().getId(), battle.getGame())));
            battle.getCounters().removeIf(BattleCounterEntity::isPhasing);
        }
        if (battle.getNonPhasing().getLosses().isGreaterThanSize(battle.getNonPhasing().getSize())) {
            battle.getCounters().stream()
                    .filter(BattleCounterEntity::isNotPhasing)
                    .forEach(counter -> diffs.add(counterDomain.removeCounter(counter.getCounter().getId(), battle.getGame())));
            battle.getCounters().removeIf(BattleCounterEntity::isNotPhasing);
        }

        if (!phasingLossesAuto || !nonPhasingLossesAuto) {
            battle.setStatus(BattleStatusEnum.CHOOSE_LOSS);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.CHOOSE_LOSS));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, phasingLossesAuto, phasingLossesAuto));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, nonPhasingLossesAuto, nonPhasingLossesAuto));
        } else {
            prepareRetreat(battle, attributes);
        }

        return diffs;
    }

    /**
     * Prepare the retreat phase of a battle.
     * If no retreat is necessary, then ends the battle.
     *
     * @param battle     the battle.
     * @param attributes the diff attributes of the battle modify diff event.
     */
    private void prepareRetreat(BattleEntity battle, List<DiffAttributesEntity> attributes) {
        if (battle.getWinner() == BattleWinnerEnum.PHASING || battle.getPhasing().getLosses().isGreaterThanSize(battle.getPhasing().getSize())) {
            battle.getPhasing().setRetreatSelected(true);
        }
        if (battle.getWinner() == BattleWinnerEnum.NON_PHASING || battle.getNonPhasing().getLosses().isGreaterThanSize(battle.getNonPhasing().getSize())) {
            battle.getNonPhasing().setRetreatSelected(true);
        }
        // Maybe later, if only one province possible to retreat, then force the retreat there.

        if (BooleanUtils.isTrue(battle.getPhasing().isRetreatSelected()) && BooleanUtils.isTrue(battle.getNonPhasing().isRetreatSelected())) {
            battle.setStatus(BattleStatusEnum.DONE);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.DONE));
            cleanUpBattle(battle);
        } else {
            battle.setStatus(BattleStatusEnum.RETREAT);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.RETREAT));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, battle.getPhasing().isRetreatSelected(), BooleanUtils.toBoolean(battle.getPhasing().isRetreatSelected())));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, battle.getNonPhasing().isRetreatSelected(), BooleanUtils.toBoolean(battle.getNonPhasing().isRetreatSelected())));
        }
    }

    /**
     * Clean up a battle when it is finished.
     *
     * @param battle to clean up.
     */
    private void cleanUpBattle(BattleEntity battle) {
        battle.getCounters().clear();
    }

    /**
     * Compute a retreat if necessary.
     *
     * @param side        the side doing an eventual retreat.
     * @param winner      if the side is the winner of the battle. If so, no retreat will be done.
     * @param dieSupplier the die retreat if needed.
     */
    private void computeRetreat(BattleSideEntity side, boolean winner, Supplier<Integer> dieSupplier) {
        if (!winner) {
            int retreat = dieSupplier.get();
            // TODO subtract leader manoeuvre if not routed
            side.setRetreat(retreat);
            side.getLosses().add(oeUtil.retreat(retreat));
        }
    }

    /**
     * Reduce the losses because of the stack sizes.
     *
     * @param active  the side taking the loss.
     * @param passive the side doing the damage.
     * @param game    the game.
     */
    private void reduceLosses(BattleSideEntity active, BattleSideEntity passive, GameEntity game) {
        // Is opposing size too small ?
        AbstractWithLossEntity mitigatedLosses = oeUtil.lossesMitigation(passive.getSize(), true, () -> oeUtil.rollDie(game));
        // Losses cant be negative
        AbstractWithLossEntity virtualLosses = AbstractWithLossEntity.create(Math.max(0, active.getLosses().getTotalThird() - mitigatedLosses.getTotalThird()));
        // Losses cant be more than opposing forces
        virtualLosses = AbstractWithLossEntity.create(Math.min((int) (3 * passive.getSize()), virtualLosses.getTotalThird()));
        // Is there a size diff between the stack ?
        AbstractWithLossEntity finalLosses = oeUtil.lossModificationSize(virtualLosses, passive.getSizeDiff());

        // Set the modified losses
        active.getLosses().setRoundLoss(finalLosses.getRoundLoss());
        active.getLosses().setThirdLoss(finalLosses.getThirdLoss());
    }

    /**
     * Internal enum for battle sequence.
     */
    private enum BattleSequenceEnum {
        FIRST_FIRE,
        FIRST_SHOCK,
        SECOND_FIRE,
        SECOND_SHOCK,
        PURSUIT
    }
}
