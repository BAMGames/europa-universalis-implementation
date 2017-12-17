package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IMilitaryService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
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
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.BooleanUtils;
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
            battle.getPhasing().setForces(true);
            attackerCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setPhasing(true);
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
            battle.getNonPhasing().setForces(true);
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
        if (battle.getPhasing().isForces() != null && battle.getPhasing().isForces() &&
                battle.getNonPhasing().isForces() != null && battle.getNonPhasing().isForces()) {
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

        boolean phasing = isCountryActive(game, request.getIdCountry());

        Boolean validated = phasing ? battle.getPhasing().isForces() : battle.getNonPhasing().isForces();

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(validated)
                .setCodeError(IConstantsServiceException.BATTLE_SELECT_VALIDATED)
                .setMsgFormat("{1}: {0} Forces cannot be added or removed to the battle because it has already been validated.")
                .setName(PARAMETER_SELECT_FORCE)
                .setParams(METHOD_SELECT_FORCE, phasing));

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
                    .setParams(METHOD_SELECT_FORCE, request.getRequest().getIdCounter()));

            BattleCounterEntity comp = new BattleCounterEntity();
            comp.setPhasing(phasing);
            comp.setBattle(battle);
            comp.setCounter(counter);
            battle.getCounters().add(comp);

            DiffAttributesEntity attribute = new DiffAttributesEntity();
            if (phasing) {
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
            if (phasing) {
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

        Boolean oldValidation = phasing ? battle.getPhasing().isForces() : battle.getNonPhasing().isForces();

        List<DiffEntity> diffs = gameDiffs.getDiffs();

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
                Integer armySize = battle.getCounters().stream()
                        .map(bc -> CounterUtil.getSizeFromType(bc.getCounter().getType()))
                        .reduce(Integer::sum)
                        .orElse(0);

                if (alliedCounters.size() < 3 && armySize < 8) {
                    List<String> allies = oeUtil.getAllies(country, game);
                    Integer remainingMinSize = game.getStacks().stream()
                            .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                                    allies.contains(stack.getCountry()))
                            .flatMap(stack -> stack.getCounters().stream())
                            .filter(counter -> CounterUtil.isArmy(counter.getType()) &&
                                    !alliedCounters.contains(counter.getId()))
                            .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                            .min(Integer::compare)
                            .orElse(0);

                    failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                            .setTest(remainingMinSize > 0 && remainingMinSize <= 8 - armySize)
                            .setCodeError(IConstantsServiceException.BATTLE_VALIDATE_OTHER_FORCE)
                            .setMsgFormat("{1}: {0} Impossible to validate forces in this battle because there are other forces to select (phasing player: {2}).")
                            .setName(PARAMETER_VALIDATE_FORCES, PARAMETER_REQUEST, PARAMETER_VALIDATE)
                            .setParams(METHOD_VALIDATE_FORCES, phasing));
                }
            }

            DiffEntity diff = new DiffEntity();
            diff.setIdGame(game.getId());
            diff.setVersionGame(game.getVersion());
            diff.setType(DiffTypeEnum.MODIFY);
            diff.setTypeObject(DiffTypeObjectEnum.BATTLE);
            diff.setIdObject(battle.getId());
            DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
            diffAttributes.setValue(Boolean.toString(request.getRequest().isValidate()));
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);

            if (phasing) {
                diffAttributes.setType(DiffAttributeTypeEnum.ATTACKER_READY);
                battle.getPhasing().setForces(request.getRequest().isValidate());
            } else {
                diffAttributes.setType(DiffAttributeTypeEnum.DEFENDER_READY);
                battle.getNonPhasing().setForces(request.getRequest().isValidate());
            }

            diffs.add(diff);
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    private void fillBattleModifiers(BattleEntity battle, boolean phasing) {
        List<CounterEntity> countersPhasing = battle.getCounters().stream()
                .filter(BattleCounterEntity::isPhasing)
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());
        List<CounterEntity> countersNotPhasing = battle.getCounters().stream()
                .filter(bc -> !bc.isPhasing())
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());

        battle.getPhasing().setSize(countersPhasing.stream()
                .collect(Collectors.summingInt(counter -> CounterUtil.getSizeFromType(counter.getType()))));
        battle.getNonPhasing().setSize(countersNotPhasing.stream()
                .collect(Collectors.summingInt(counter -> CounterUtil.getSizeFromType(counter.getType()))));

        String techPhasing = oeUtil.getTechnology(countersPhasing,
                true, getReferential(), getTables(), battle.getGame());
        battle.getPhasing().setTech(techPhasing);

        String techNotPhasing = oeUtil.getTechnology(countersNotPhasing,
                true, getReferential(), getTables(), battle.getGame());
        battle.getNonPhasing().setTech(techNotPhasing);

        // Second day, everyone -1
        battle.getPhasing().getSecondDay().add(-1, -1, 0);
        battle.getNonPhasing().getSecondDay().add(-1, -1, 0);

        // TODO tercios

        // TODO foraging

        // Terrain
        AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
        switch (province.getTerrain()) {
            case DENSE_FOREST:
            case SPARSE_FOREST:
            case DESERT:
            case SWAMP:
                battle.getPhasing().getFirstDay().add(-1, -1, -1);
                battle.getNonPhasing().getFirstDay().add(-1, -1, -1);
                battle.getPhasing().getSecondDay().add(-1, -1, -1);
                battle.getNonPhasing().getSecondDay().add(-1, -1, -1);
                break;
            case MOUNTAIN:
                battle.getPhasing().getFirstDay().add(-1, -1, -1);
                battle.getNonPhasing().getFirstDay().add(0, 0, -1);
                battle.getPhasing().getSecondDay().add(-1, -1, -1);
                battle.getNonPhasing().getSecondDay().add(0, 0, -1);
                break;
            case PLAIN:
            default:
                break;
        }

        // TODO river/straits

        List<ArmyInfo> armyPhasing = oeUtil.getArmyInfo(countersPhasing, getReferential());
        List<ArmyInfo> armyNotPhasing = oeUtil.getArmyInfo(countersNotPhasing, getReferential());
        int bonusArtilleryPhasing = oeUtil.getArtilleryBonus(armyPhasing, getTables(), battle.getGame());
        int bonusArtilleryNotPhasing = oeUtil.getArtilleryBonus(armyNotPhasing, getTables(), battle.getGame());
        if (bonusArtilleryPhasing >= 6) {
            battle.getPhasing().getFirstDay().add(1, 0, 0);
            battle.getPhasing().getSecondDay().add(1, 0, 0);
        }
        if (bonusArtilleryNotPhasing >= 6) {
            battle.getNonPhasing().getFirstDay().add(1, 0, 0);
            battle.getNonPhasing().getSecondDay().add(1, 0, 0);
        }

        if (battle.getPhasing().getSize() >= battle.getNonPhasing().getSize() + 3 ||
                oeUtil.getCavalryBonus(armyPhasing, province.getTerrain(), getTables(), battle.getGame())) {
            battle.getPhasing().getFirstDay().add(0, 1, 0);
            battle.getPhasing().getSecondDay().add(0, 1, 0);
        }
        if (battle.getNonPhasing().getSize() >= battle.getPhasing().getSize() + 3 ||
                oeUtil.getCavalryBonus(armyNotPhasing, province.getTerrain(), getTables(), battle.getGame())) {
            battle.getNonPhasing().getFirstDay().add(0, 1, 0);
            battle.getNonPhasing().getSecondDay().add(0, 1, 0);
        }
    }
}
