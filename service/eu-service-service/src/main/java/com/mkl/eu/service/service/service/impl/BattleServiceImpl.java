package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.util.StackUtil;
import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.BattleTech;
import com.mkl.eu.client.service.vo.tables.CombatResult;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
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
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.ArmyInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;
import static com.mkl.eu.client.common.util.CommonUtil.THIRD;

/**
 * Service for military purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class BattleServiceImpl extends AbstractMilitaryService implements IBattleService {
    /** Counter Domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** Status Workflow Domain. */
    @Autowired
    private IStatusWorkflowDomain statusWorkflowDomain;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseBattle(Request<ChooseProvinceRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_BATTLE)
                .setParams(METHOD_CHOOSE_BATTLE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_BATTLE, PARAMETER_CHOOSE_BATTLE);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_CHOOSE_BATTLE, PARAMETER_CHOOSE_BATTLE, GameStatusEnum.MILITARY_BATTLES);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
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

        List<String> allies = oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive());
        List<String> enemies = oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive());

        List<CounterEntity> attackerCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                        allies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()) || CounterUtil.isLeader(counter.getType()))
                .collect(Collectors.toList());

        List<CounterEntity> defenderCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                        enemies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()) || CounterUtil.isLeader(counter.getType()))
                .collect(Collectors.toList());

        Double attackerSize = attackerCounters.stream()
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .reduce(Double::sum)
                .orElse(0d);
        boolean sizeOk = attackerCounters.stream()
                .filter(counter -> CounterUtil.isArmy(counter.getType()))
                .count() <= 3 && attackerSize <= 8;
        List<String> leadingCountries = oeUtil.getLeadingCountries(attackerCounters);
        String leadingCountry = leadingCountries.size() == 1 ? leadingCountries.get(0) : null;
        List<Leader> leaders = oeUtil.getLeaders(attackerCounters, getTables(), getLeaderConditions(province));
        boolean leaderOk = leaders.size() <= 1;

        if (sizeOk && StringUtils.isNotEmpty(leadingCountry) && leaderOk) {
            DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, true);
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.getPhasing().setForces(true);

            diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTRY, leadingCountry);
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.getPhasing().setCountry(leadingCountry);

            if (leaders.size() == 1) {
                String leader = leaders.get(0).getCode();
                diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_LEADER, leader);
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                battle.getPhasing().setLeader(leader);
            }
            attackerCounters.removeIf(counter -> CounterUtil.isLeader(counter.getType()) &&
                    !StringUtils.equals(counter.getCode(), battle.getPhasing().getLeader()));

            attackerCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setPhasing(true);
                comp.setBattle(battle);
                comp.setCounter(counter.getId());
                comp.setCountry(counter.getCountry());
                comp.setType(counter.getType());
                comp.setCode(counter.getCode());
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
        sizeOk = defenderCounters.stream()
                .filter(counter -> CounterUtil.isArmy(counter.getType()))
                .count() <= 3 && defenderSize <= 8;
        leadingCountries = oeUtil.getLeadingCountries(defenderCounters);
        leadingCountry = leadingCountries.size() == 1 ? leadingCountries.get(0) : null;
        leaders = oeUtil.getLeaders(defenderCounters, getTables(), getLeaderConditions(province));
        leaderOk = leaders.size() <= 1;

        if (sizeOk && StringUtils.isNotEmpty(leadingCountry) && leaderOk) {
            DiffAttributesEntity diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, true);
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.getNonPhasing().setForces(true);

            diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_COUNTRY, leadingCountry);
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
            battle.getNonPhasing().setCountry(leadingCountry);

            if (leaders.size() == 1) {
                String leader = leaders.get(0).getCode();
                diffAttributes = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_LEADER, leader);
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                battle.getNonPhasing().setLeader(leader);
            }
            defenderCounters.removeIf(counter -> CounterUtil.isLeader(counter.getType()) &&
                    !StringUtils.equals(counter.getCode(), battle.getNonPhasing().getLeader()));

            defenderCounters.forEach(counter -> {
                BattleCounterEntity comp = new BattleCounterEntity();
                comp.setBattle(battle);
                comp.setCounter(counter.getId());
                comp.setCountry(counter.getCountry());
                comp.setType(counter.getType());
                comp.setCode(counter.getCode());
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
    public DiffResponse selectForces(Request<SelectForcesRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCES)
                .setParams(METHOD_SELECT_FORCES));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_SELECT_FORCES, PARAMETER_SELECT_FORCES);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_SELECT_FORCES, PARAMETER_SELECT_FORCES);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST)
                .setParams(METHOD_SELECT_FORCES));

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(CollectionUtils.isEmpty(request.getRequest().getForces()))
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                .setParams(METHOD_SELECT_FORCES));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.SELECT_FORCES)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_SELECT_FORCES)
                .setParams(METHOD_SELECT_FORCES, BattleStatusEnum.SELECT_FORCES.name()));

        boolean phasing = isCountryActive(game, request.getGame().getIdCountry());

        Boolean validated = phasing ? battle.getPhasing().isForces() : battle.getNonPhasing().isForces();

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(validated)
                .setCodeError(IConstantsServiceException.BATTLE_SELECT_VALIDATED)
                .setMsgFormat("{1}: {0} Forces cannot be added or removed to the battle because it has already been validated.")
                .setName(PARAMETER_SELECT_FORCES)
                .setParams(METHOD_SELECT_FORCES, phasing));

        List<DiffAttributesEntity> attributes = new ArrayList<>();
        List<CounterEntity> counters = new ArrayList<>();
        List<String> allies = oeUtil.getWarFaction(battle.getWar(),
                phasing ? battle.isPhasingOffensive() : !battle.isPhasingOffensive());
        for (Long idCounter : request.getRequest().getForces()) {

            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                            allies.contains(stack.getCountry()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(c -> (CounterUtil.isArmy(c.getType()) || CounterUtil.isLeader(c.getType())) &&
                            c.getId().equals(idCounter))
                    .findAny()
                    .orElse(null);

            failIfNull(new AbstractService.CheckForThrow<>()
                    .setTest(counter)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                    .setParams(METHOD_SELECT_FORCES, idCounter));

            BattleCounterEntity comp = new BattleCounterEntity();
            comp.setPhasing(phasing);
            comp.setBattle(battle);
            comp.setCounter(counter.getId());
            comp.setCountry(counter.getCountry());
            comp.setType(counter.getType());
            comp.setCode(counter.getCode());
            battle.getCounters().add(comp);
            counters.add(counter);

            attributes.add(DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_COUNTER_ADD : DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD,
                    counter.getId()));
        }

        List<Long> alliedCounters = battle.getCounters().stream()
                .filter(bc -> bc.isPhasing() == phasing && CounterUtil.isArmy(bc.getType()))
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());
        Double armySize = battle.getCounters().stream()
                .map(bc -> CounterUtil.getSizeFromType(bc.getType()))
                .reduce(Double::sum)
                .orElse(0d);

        if (alliedCounters.size() < 3 && armySize < 8) {
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
                    .setMsgFormat("{1}: {0} Impossible to select forces in this battle because there are other forces to select (phasing player: {2}).")
                    .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_VALIDATE)
                    .setParams(METHOD_SELECT_FORCES, phasing));
        }

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(alliedCounters.size() > 3 || armySize > 8)
                .setCodeError(IConstantsServiceException.BATTLE_FORCES_TOO_BIG)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                .setParams(METHOD_SELECT_FORCES, alliedCounters.size(), armySize));

        List<CounterEntity> leaders = counters.stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.LEADER)
                .collect(Collectors.toList());

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(leaders.size() > 1)
                .setCodeError(IConstantsServiceException.BATTLE_FORCES_TOO_MANY_LEADERS)
                .setMsgFormat("{1}: {0} Impossible to select forces in this battle because there are too many leaders selected : {2}.")
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                .setParams(METHOD_SELECT_FORCES, leaders.stream().map(CounterEntity::getCode).collect(Collectors.joining(","))));

        List<String> countries = oeUtil.getLeadingCountries(counters);
        String selectedCountry = StringUtils.isEmpty(request.getRequest().getCountry()) && countries.size() == 1
                ? countries.get(0) : request.getRequest().getCountry();

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(!countries.contains(selectedCountry))
                .setCodeError(IConstantsServiceException.BATTLE_FORCES_LEADING_COUNTRY_AMBIGUOUS)
                .setMsgFormat("{1}: {0} Impossible to select forces in this battle because the selected country cannot lead this battle or you must select a country (selected country: {2}, eligible countries: {3}).")
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_SELECT_FORCES, selectedCountry, countries));

        Predicate<Leader> conditions = getLeaderConditions(battle.getProvince());
        List<Leader> availableLeaders = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                        allies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.LEADER &&
                        StringUtils.equals(counter.getCountry(), selectedCountry))
                .map(counter -> getTables().getLeader(counter.getCode(), selectedCountry))
                .filter(conditions)
                .collect(Collectors.toList());
        String selectedLeader = null;
        if (leaders.size() == 1) {
            Leader leader = getTables().getLeader(leaders.get(0).getCode(), leaders.get(0).getCountry());
            selectedLeader = leader.getCode();
            availableLeaders.removeIf(lead -> leader.getRank().compareTo(lead.getRank()) <= 0);

            failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                    .setTest(conditions.test(leader) && StringUtils.equals(leader.getCountry(), selectedCountry))
                    .setCodeError(IConstantsServiceException.BATTLE_FORCES_NOT_SUITABLE_LEADER)
                    .setMsgFormat("{1}: {0} Impossible to select forces in this battle because the selected leader {2} cannot lead this battle.")
                    .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                    .setParams(METHOD_SELECT_FORCES, selectedLeader));
        }

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(availableLeaders.size() > 0)
                .setCodeError(IConstantsServiceException.BATTLE_FORCES_INVALID_LEADER)
                .setMsgFormat("{1}: {0} Impossible to select forces in this battle because the selected leader {2} is not optimal (better leaders : {3}).")
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                .setParams(METHOD_SELECT_FORCES, selectedLeader, availableLeaders.stream().map(Leader::getCode).collect(Collectors.joining(","))));

        if (phasing) {
            battle.getPhasing().setCountry(selectedCountry);
            battle.getPhasing().setLeader(selectedLeader);
            battle.getPhasing().setForces(true);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTRY, battle.getPhasing().getCountry()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_LEADER, battle.getPhasing().getLeader()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, true));
        } else {
            battle.getNonPhasing().setCountry(selectedCountry);
            battle.getNonPhasing().setLeader(selectedLeader);
            battle.getNonPhasing().setForces(true);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_COUNTRY, battle.getNonPhasing().getCountry()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_LEADER, battle.getNonPhasing().getLeader()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, true));
        }

        if (battle.getPhasing().isForces() && battle.getNonPhasing().isForces()) {
            battle.setStatus(BattleStatusEnum.WITHDRAW_BEFORE_BATTLE);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.WITHDRAW_BEFORE_BATTLE));
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse withdrawBeforeBattle(Request<WithdrawBeforeBattleRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_WITHDRAW_BEFORE_BATTLE)
                .setParams(METHOD_WITHDRAW_BEFORE_BATTLE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_WITHDRAW_BEFORE_BATTLE, PARAMETER_WITHDRAW_BEFORE_BATTLE);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_WITHDRAW_BEFORE_BATTLE, PARAMETER_WITHDRAW_BEFORE_BATTLE);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(isPhasingPlayer(game, request.getGame().getIdCountry()))
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
                    .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType())));
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

                Leader phasingLeader = getTables().getLeader(battle.getPhasing().getLeader(), battle.getPhasing().getCountry());
                Leader nonPhasingLeader = getTables().getLeader(battle.getNonPhasing().getLeader(), battle.getNonPhasing().getCountry());
                int mod = Math.max(0, Optional.ofNullable(nonPhasingLeader).map(Leader::getManoeuvre).orElse(0)
                        - Optional.ofNullable(phasingLeader).map(Leader::getManoeuvre).orElse(0));
                if (die + mod >= 8) {
                    success = true;
                }
            }

            if (success) {
                battle.setStatus(BattleStatusEnum.DONE);
                battle.setEnd(BattleEndEnum.WITHDRAW_BEFORE_BATTLE);
                battle.setWinner(BattleWinnerEnum.NONE);

                List<DiffEntity> newDiffs = new ArrayList<>();
                List<DiffAttributesEntity> attributes = Arrays.asList(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.DONE),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.END, BattleEndEnum.WITHDRAW_BEFORE_BATTLE),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.WINNER, BattleWinnerEnum.NONE));
                Consumer<StackEntity> retreatStack = stack -> {
                    boolean besieged = StringUtils.equals(battle.getProvince(), provinceTo);
                    newDiffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.STACK, stack.getId(),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, battle.getProvince()),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, provinceTo),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BESIEGED, besieged, besieged)));
                    stack.setMovePhase(MovePhaseEnum.MOVED);
                    stack.setProvince(provinceTo);
                    stack.setBesieged(besieged);
                };
                List<String> allies = oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive());
                game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()) && oeUtil.isMobile(stack) && allies.contains(stack.getCountry()))
                        .forEach(retreatStack);
                newDiffs.addAll(cleanUpBattle(battle));
                DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                        attributes.toArray(new DiffAttributesEntity[attributes.size()]));
                newDiffs.add(diff);

                return createDiffs(newDiffs, gameDiffs, request);
            }
        }

        List<DiffEntity> newDiffs = new ArrayList<>();
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        attributes.addAll(fillBattleModifiers(battle));
        attributes.addAll(computeBothSequence(battle, BattleSequenceEnum.FIRST_FIRE));
        newDiffs.addAll(checkRouted(battle, BattleEndEnum.ROUTED_AT_FIRST_FIRE, 3, attributes));
        // TODO TG-10 at sea, WIND advantage forces can retreat
        if (battle.getEnd() == null) {
            attributes.addAll(computeBothSequence(battle, BattleSequenceEnum.FIRST_SHOCK));
            newDiffs.addAll(checkRouted(battle, BattleEndEnum.ROUTED_AT_FIRST_SHOCK, 2, attributes));
            if (battle.getEnd() == null) {
                newDiffs.addAll(checkAnnihilated(battle, BattleEndEnum.ANNIHILATED_AT_FIRST_DAY, 2, attributes));
            }
        }
        if (battle.getEnd() == null) {
            battle.setStatus(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF));
        }

        attributes.addAll(getLossesAttributes(battle));

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
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        battle.getPhasing().getFirstDay().clear();
        battle.getPhasing().getSecondDay().clear();
        battle.getNonPhasing().getFirstDay().clear();
        battle.getNonPhasing().getSecondDay().clear();
        battle.getPhasing().setPursuitMod(0);
        battle.getNonPhasing().setPursuitMod(0);

        List<Long> countersPhasingId = battle.getCounters().stream()
                .filter(BattleCounterEntity::isPhasing)
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());
        List<Long> countersNotPhasingId = battle.getCounters().stream()
                .filter(BattleCounterEntity::isNotPhasing)
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());

        List<CounterEntity> countersPhasing = battle.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersPhasingId.contains(counter.getId()))
                .collect(Collectors.toList());
        List<CounterEntity> countersNotPhasing = battle.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersNotPhasingId.contains(counter.getId()))
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

        // TODO TG-131 tercios moral boost
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

        if (StringUtils.isEmpty(battle.getPhasing().getLeader())) {
            battle.getPhasing().setLeader(getReplacementLeader(battle.getPhasing().getCountry(), battle.getGame()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_LEADER, battle.getPhasing().getLeader()));
        }
        if (StringUtils.isEmpty(battle.getNonPhasing().getLeader())) {
            battle.getNonPhasing().setLeader(getReplacementLeader(battle.getNonPhasing().getCountry(), battle.getGame()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_LEADER, battle.getNonPhasing().getLeader()));
        }
        Leader phasingLeader = getTables().getLeader(battle.getPhasing().getLeader(), battle.getPhasing().getCountry());
        Leader notPhasingLeader = getTables().getLeader(battle.getNonPhasing().getLeader(), battle.getNonPhasing().getCountry());
        int fireMod = phasingLeader.getFire() - notPhasingLeader.getFire();
        int shockMod = phasingLeader.getShock() - notPhasingLeader.getShock();
        battle.getPhasing().getFirstDay().addFire(fireMod);
        battle.getPhasing().getFirstDay().addShock(shockMod);
        battle.getNonPhasing().getFirstDay().addFire(-fireMod);
        battle.getNonPhasing().getFirstDay().addShock(-shockMod);
        battle.getPhasing().getSecondDay().addFire(fireMod);
        battle.getPhasing().getSecondDay().addShock(shockMod);
        battle.getNonPhasing().getSecondDay().addFire(-fireMod);
        battle.getNonPhasing().getSecondDay().addShock(-shockMod);

        // TODO TG-131 tercios

        // TODO TG-6 foraging

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
                .anyMatch(counter -> CounterUtil.isArmyCounter(counter.getType()));
        boolean nonPhasingNoArmy = !countersNotPhasing.stream()
                .anyMatch(counter -> CounterUtil.isArmyCounter(counter.getType()));
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

        // TODO TG-131 TUR Sipahi for pursuit

        // Size diff
        double phasingSize = oeUtil.getArmySize(armyPhasing, getTables(), battle.getGame());
        double nonPhasingSize = oeUtil.getArmySize(armyNotPhasing, getTables(), battle.getGame());
        battle.getPhasing().setSizeDiff(oeUtil.getSizeDiff(phasingSize, nonPhasingSize));
        battle.getNonPhasing().setSizeDiff(oeUtil.getSizeDiff(nonPhasingSize, phasingSize));

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

    /** {@inheritDoc} */
    @Override
    public DiffResponse retreatFirstDay(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_RETREAT_FIRST_DAY)
                .setParams(METHOD_RETREAT_FIRST_DAY));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_RETREAT_FIRST_DAY, PARAMETER_RETREAT_FIRST_DAY);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_RETREAT_FIRST_DAY, PARAMETER_RETREAT_FIRST_DAY);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_RETREAT_FIRST_DAY, PARAMETER_REQUEST)
                .setParams(METHOD_RETREAT_FIRST_DAY));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF || bat.getStatus() == BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_RETREAT_FIRST_DAY)
                .setParams(METHOD_RETREAT_FIRST_DAY, BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF.name()));

        // TODO TG-7 if intercepting battle, it is the opposite
        boolean phasing;
        int remainingMoral;
        if (battle.getStatus() == BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF) {
            phasing = false;
            remainingMoral = CommonUtil.subtract(battle.getNonPhasing().getMoral(), battle.getNonPhasing().getLosses().getMoraleLoss());
        } else {
            phasing = true;
            remainingMoral = CommonUtil.subtract(battle.getPhasing().getMoral(), battle.getPhasing().getLosses().getMoraleLoss());
        }
        boolean playerPhasing = isPhasingPlayer(game, request.getGame().getIdCountry());
        boolean ok = phasing == playerPhasing;
        if (ok) {
            List<String> allies = oeUtil.getWarFaction(battle.getWar(),
                    playerPhasing ? battle.isPhasingOffensive() : !battle.isPhasingOffensive());
            List<String> enemies = oeUtil.getWarFaction(battle.getWar(),
                    playerPhasing ? !battle.isPhasingOffensive() : battle.isPhasingOffensive());
            ok = !battle.getCounters().stream()
                    .anyMatch(bc -> bc.isPhasing() == playerPhasing && !allies.contains(bc.getCountry()) ||
                            bc.isPhasing() != playerPhasing && !enemies.contains(bc.getCountry()));
        }

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(ok)
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_RETREAT_FIRST_DAY, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_RETREAT_FIRST_DAY, country.getName(),
                        phasing ? battle.getPhasing().getCountry() : battle.getNonPhasing().getCountry()));

        List<DiffEntity> newDiffs = new ArrayList<>();
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        if (request.getRequest().isValidate()) {
            int die = oeUtil.rollDie(game);

            Leader leader;
            if (phasing) {
                leader = getTables().getLeader(battle.getPhasing().getLeader(), battle.getPhasing().getCountry());
            } else {
                leader = getTables().getLeader(battle.getNonPhasing().getLeader(), battle.getNonPhasing().getCountry());
            }
            boolean success = die <= leader.getManoeuvre() + remainingMoral;

            if (success) {
                battle.setEnd(BattleEndEnum.RETREAT_AT_FIRST_DAY);
                battle.setWinner(phasing ? BattleWinnerEnum.NON_PHASING : BattleWinnerEnum.PHASING);
                battle.getPhasing().addPursuit(2);
                battle.getNonPhasing().addPursuit(2);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.END, BattleEndEnum.RETREAT_AT_FIRST_DAY));
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, battle.getPhasing().getPursuitMod()));
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, battle.getNonPhasing().getPursuitMod()));

                newDiffs.addAll(computePursuit(battle, attributes));

                attributes.addAll(getLossesAttributes(battle));

                DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                        attributes.toArray(new DiffAttributesEntity[attributes.size()]));
                newDiffs.add(diff);

                return createDiffs(newDiffs, gameDiffs, request);
            } else {
                if (phasing) {
                    battle.getPhasing().getSecondDay().addFireAndShock(1);
                    attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE_MOD, battle.getPhasing().getSecondDay().getFireMod()));
                    attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK_MOD, battle.getPhasing().getSecondDay().getShockMod()));
                } else {
                    battle.getNonPhasing().getSecondDay().addFireAndShock(1);
                    attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD, battle.getNonPhasing().getSecondDay().getFireMod()));
                    attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD, battle.getNonPhasing().getSecondDay().getShockMod()));
                }
            }
        }
        if (battle.getStatus() == BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF) {
            battle.setStatus(BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT));

            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                    attributes.toArray(new DiffAttributesEntity[attributes.size()]));
            return createDiff(diff, gameDiffs, request);
        } else {
            attributes.addAll(computeBothSequence(battle, BattleSequenceEnum.SECOND_FIRE));
            newDiffs.addAll(checkRouted(battle, BattleEndEnum.ROUTED_AT_SECOND_FIRE, 1, attributes));
            // TODO TG-10 at sea, WIND advantage forces can retreat
            if (battle.getEnd() == null) {
                attributes.addAll(computeBothSequence(battle, BattleSequenceEnum.SECOND_SHOCK));
                newDiffs.addAll(checkRouted(battle, BattleEndEnum.ROUTED_AT_SECOND_SHOCK, 0, attributes));
            }
            if (battle.getEnd() == null) {
                battle.setEnd(BattleEndEnum.END_OF_SECOND_DAY);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.END, BattleEndEnum.END_OF_SECOND_DAY));
                newDiffs.addAll(computePursuit(battle, attributes));
            }

            attributes.addAll(getLossesAttributes(battle));

            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                    attributes.toArray(new DiffAttributesEntity[attributes.size()]));
            newDiffs.add(diff);

            return createDiffs(newDiffs, gameDiffs, request);
        }
    }

    /**
     * @param battle the battle.
     * @return the diff attributes of the losses of both side of the battle.
     */
    private List<DiffAttributesEntity> getLossesAttributes(BattleEntity battle) {
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS, battle.getNonPhasing().getLosses().getRoundLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS, battle.getNonPhasing().getLosses().getThirdLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS, battle.getNonPhasing().getLosses().getMoraleLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS, battle.getPhasing().getLosses().getRoundLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS, battle.getPhasing().getLosses().getThirdLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS, battle.getPhasing().getLosses().getMoraleLoss()));
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
    private List<DiffAttributesEntity> computeBothSequence(BattleEntity battle, BattleSequenceEnum sequence) {
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

            // If both side routed, then no winner, it is defeat for both side
            if (phasingRouted && nonPhasingRouted) {
                battle.setWinner(BattleWinnerEnum.NONE);
            } else if (phasingRouted) {
                battle.setWinner(BattleWinnerEnum.NON_PHASING);
            } else {
                battle.setWinner(BattleWinnerEnum.PHASING);
            }

            if (pursuitBonus != 0) {
                battle.getPhasing().addPursuit(pursuitBonus);
                battle.getNonPhasing().addPursuit(pursuitBonus);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, battle.getPhasing().getPursuitMod()));
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, battle.getNonPhasing().getPursuitMod()));
            }
            return computePursuit(battle, attributes);
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

            if (pursuitBonus != 0) {
                battle.getPhasing().addPursuit(pursuitBonus);
                battle.getNonPhasing().addPursuit(pursuitBonus);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, battle.getPhasing().getPursuitMod()));
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, battle.getNonPhasing().getPursuitMod()));
            }
            return computePursuit(battle, attributes);
        }

        return Collections.emptyList();
    }

    /**
     * Determine the real winner of the battle.
     * Then compute the pursuit if necessary.
     * Finally, ends the battle.
     *
     * @param battle the battle.
     * @return the eventual attributes, if any.
     */
    private List<DiffEntity> computePursuit(BattleEntity battle, List<DiffAttributesEntity> attributes) {
        // First determine winner
        // If one side is annihilated, other side is winner, even if there was already a winner in this battle
        boolean phasingAnnihilated = isAnnihilated(battle.getPhasing(), battle.getNonPhasing());
        boolean nonPhasingAnnihilated = isAnnihilated(battle.getNonPhasing(), battle.getPhasing());
        if (phasingAnnihilated && nonPhasingAnnihilated) {
            battle.setWinner(BattleWinnerEnum.NONE);
        } else if (phasingAnnihilated) {
            battle.setWinner(BattleWinnerEnum.NON_PHASING);
        } else if (nonPhasingAnnihilated) {
            battle.setWinner(BattleWinnerEnum.PHASING);
        }
        // If not, then the already winning side is the winner.
        // But if no side was winning, then the one with most remaining morale is the winner
        if (battle.getWinner() == null) {
            int phasingRemainingMoral = CommonUtil.subtract(battle.getPhasing().getMoral(), battle.getPhasing().getLosses().getMoraleLoss());
            int nonPhasingRemainingMoral = CommonUtil.subtract(battle.getNonPhasing().getMoral(), battle.getNonPhasing().getLosses().getMoraleLoss());
            if (phasingRemainingMoral > nonPhasingRemainingMoral) {
                battle.setWinner(BattleWinnerEnum.PHASING);
            } else if (nonPhasingRemainingMoral > phasingRemainingMoral) {
                battle.setWinner(BattleWinnerEnum.NON_PHASING);
            } else {
                battle.setWinner(BattleWinnerEnum.NONE);
            }
        }
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.WINNER, battle.getWinner()));

        BattleSideEntity winner = null;
        BattleSideEntity loser = null;
        // only non routed side can pursuit
        switch (battle.getWinner()) {
            case PHASING:
                if (CommonUtil.subtract(battle.getPhasing().getMoral(), battle.getPhasing().getLosses().getMoraleLoss()) > 0) {
                    winner = battle.getPhasing();
                    loser = battle.getNonPhasing();
                }
                break;
            case NON_PHASING:
                if (CommonUtil.subtract(battle.getNonPhasing().getMoral(), battle.getNonPhasing().getLosses().getMoraleLoss()) > 0) {
                    winner = battle.getNonPhasing();
                    loser = battle.getPhasing();
                }
                break;
            case NONE:
            default:
                break;
        }

        if (winner != null && loser != null) {
            attributes.add(computeSequence(BattleSequenceEnum.PURSUIT, winner, loser, battle.getWinner() == BattleWinnerEnum.PHASING, battle.getGame()));
        }

        return endBattle(battle, attributes);
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
        Integer modifier = 0;
        String column = null;
        Consumer<Integer> setDice = null;
        DiffAttributeTypeEnum type = null;
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
        Function<BattleCounterEntity, CounterEntity> toCounter = source -> battle.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> Objects.equals(counter.getId(), source.getCounter()))
                .findAny()
                .orElse(null);
        if (battle.getPhasing().getLosses().isGreaterThanSize(battle.getPhasing().getSize())) {
            battle.getCounters().stream()
                    .filter(counter -> counter.isPhasing() && CounterUtil.isArmy(counter.getType()))
                    .map(toCounter)
                    .forEach(counter -> diffs.add(counterDomain.removeCounter(counter)));
        }
        if (battle.getNonPhasing().getLosses().isGreaterThanSize(battle.getNonPhasing().getSize())) {
            battle.getCounters().stream()
                    .filter(counter -> counter.isNotPhasing() && CounterUtil.isArmy(counter.getType()))
                    .map(toCounter)
                    .forEach(counter -> diffs.add(counterDomain.removeCounter(counter)));
        }
        AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
        diffs.addAll(checkLeaderDeaths(battle, true, province, attributes));
        diffs.addAll(checkLeaderDeaths(battle, false, province, attributes));

        if (!phasingLossesAuto || !nonPhasingLossesAuto) {
            battle.setStatus(BattleStatusEnum.CHOOSE_LOSS);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.CHOOSE_LOSS));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, phasingLossesAuto, phasingLossesAuto));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, nonPhasingLossesAuto, nonPhasingLossesAuto));
        } else {
            diffs.addAll(prepareRetreat(battle, attributes));
        }

        return diffs;
    }

    /**
     * Prepare the retreat phase of a battle.
     * If no retreat is necessary, then ends the battle.
     *
     * @param battle     the battle.
     * @param attributes the diff attributes of the battle modify diff event.
     * @return eventual other diffs.
     */
    private List<DiffEntity> prepareRetreat(BattleEntity battle, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        GameEntity game = battle.getGame();
        List<String> phasingFaction = oeUtil.getWarFaction(battle.getWar(), battle.isPhasingOffensive());
        boolean phasingRemaining = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .anyMatch(counter -> CounterUtil.isMobile(counter.getType()) && phasingFaction.contains(counter.getCountry()));
        if (battle.getWinner() == BattleWinnerEnum.PHASING || !phasingRemaining) {
            battle.getPhasing().setRetreatSelected(true);
        } else {
            battle.getPhasing().setRetreatSelected(false);
        }
        List<String> notPhasingFaction = oeUtil.getWarFaction(battle.getWar(), !battle.isPhasingOffensive());
        boolean notPhasingRemaining = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .anyMatch(counter -> CounterUtil.isMobile(counter.getType()) && notPhasingFaction.contains(counter.getCountry()));
        if (battle.getWinner() == BattleWinnerEnum.NON_PHASING || !notPhasingRemaining) {
            battle.getNonPhasing().setRetreatSelected(true);
        } else {
            battle.getNonPhasing().setRetreatSelected(false);
        }
        // Maybe later, if only one province possible to retreat, then force the retreat there.

        if (BooleanUtils.isTrue(battle.getPhasing().isRetreatSelected()) && BooleanUtils.isTrue(battle.getNonPhasing().isRetreatSelected())) {
            battle.setStatus(BattleStatusEnum.DONE);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.DONE));
            diffs.addAll(cleanUpBattle(battle));
        } else {
            battle.setStatus(BattleStatusEnum.RETREAT);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.RETREAT));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, battle.getPhasing().isRetreatSelected(), BooleanUtils.toBoolean(battle.getPhasing().isRetreatSelected())));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, battle.getNonPhasing().isRetreatSelected(), BooleanUtils.toBoolean(battle.getNonPhasing().isRetreatSelected())));
        }
        return diffs;
    }

    /**
     * Clean up a battle when it is finished.
     *
     * @param battle to clean up.
     * @return eventual other diffs.
     */
    private List<DiffEntity> cleanUpBattle(BattleEntity battle) {
        List<DiffEntity> diffs = new ArrayList<>();
        GameEntity game = battle.getGame();
        AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
        String controller = oeUtil.getController(province, game);

        List<Long> countersId = battle.getCounters().stream()
                .map(BattleCounterEntity::getCounter)
                .collect(Collectors.toList());

        game.getStacks().stream()
                .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersId.contains(counter.getId()))
                .map(CounterEntity::getOwner)
                .distinct()
                .forEach(stack -> {
                    String newStackController = oeUtil.getController(stack);
                    boolean changeController = !StringUtils.equals(newStackController, stack.getCountry());
                    String newLeader = oeUtil.getLeader(stack, getTables(), getLeaderConditions(province));
                    MovePhaseEnum movePhase = stack.getMovePhase();
                    if (StackUtil.isFighting(stack)) {
                        List<String> enemies = oeUtil.getEnemies(stack.getCountry(), game);
                        if (enemies.contains(controller)) {
                            movePhase = MovePhaseEnum.BESIEGING;
                        } else {
                            movePhase = MovePhaseEnum.MOVED;
                        }
                    }
                    boolean changeMovePhase = movePhase != stack.getMovePhase();
                    if (changeController || changeMovePhase) {
                        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, movePhase, changeMovePhase),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newStackController, changeController),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, !StringUtils.equals(newLeader, stack.getLeader()))));
                    }
                    stack.setMovePhase(movePhase);
                    stack.setCountry(newStackController);
                    stack.setLeader(newLeader);
                });

        if (!game.getStacks().stream()
                .anyMatch(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()) &&
                        StackUtil.isBesieging(stack))) {
            List<CounterEntity> siegeworks = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(SiegeServiceImpl.HAS_SIEGEWORK)
                    .collect(Collectors.toList());
            diffs.addAll(siegeworks.stream()
                    .map(counterDomain::removeCounter)
                    .collect(Collectors.toList()));
        }

        diffs.addAll(statusWorkflowDomain.endMilitaryPhase(game));
        return diffs;
    }

    /**
     * Check if a specific side of a battle needs to check for leader death and then do it.
     *
     * @param battle     the battle.
     * @param phasing    the side we want to check.
     * @param province   the province where the battle is.
     * @param attributes the diff attributes of the battle modify diff event.
     * @return the diffs involved.
     */
    protected List<DiffEntity> checkLeaderDeaths(BattleEntity battle, boolean phasing, AbstractProvinceEntity province, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        GameEntity game = battle.getGame();
        BattleSideEntity side = phasing ? battle.getPhasing() : battle.getNonPhasing();
        CounterEntity counterLeader = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> StringUtils.equals(counter.getCode(), side.getLeader()))
                .findAny()
                .orElse(null);
        boolean needCheck = counterLeader != null;
        if (province instanceof EuropeanProvinceEntity) {
            needCheck &= (phasing ? battle.getNonPhasing().getSize() : battle.getPhasing().getSize()) >= 3;
        }

        if (needCheck) {
            int die = oeUtil.rollDie(game, side.getCountry());
            side.setLeaderCheck(die);
            attributes.add(DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_LEADER_CHECK : DiffAttributeTypeEnum.NON_PHASING_LEADER_CHECK, side.getLeaderCheck()));
            int modifier = 0;
            if (phasing && battle.getWinner() != BattleWinnerEnum.PHASING || !phasing && battle.getWinner() != BattleWinnerEnum.NON_PHASING) {
                modifier -= 1;
            }
            if (side.getLosses().isGreaterThanSize(side.getSize())) {
                modifier -= 5;
            }
            Leader leader = getTables().getLeader(side.getLeader(), side.getCountry());
            if (Leader.leaderFragility.test(leader)) {
                modifier -= 1;
            }

            int result = die + modifier;
            if (result <= 1) {
                int dieWound = 1;
                if (!leader.isAnonymous()) {
                    dieWound = oeUtil.rollDie(game, side.getCountry());
                }
                if (dieWound % 2 == 1) {
                    side.setLeaderWounds(-1);
                    diffs.add(counterDomain.removeCounter(counterLeader));
                } else {
                    int nbWounds = dieWound / 2;
                    side.setLeaderWounds(nbWounds);
                    String roundBox = GameUtil.getRoundBoxAdd(oeUtil.getRoundBox(game), nbWounds);
                    diffs.add(counterDomain.moveToSpecialBox(counterLeader, roundBox, game));
                }
                attributes.add(DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_LEADER_WOUNDS : DiffAttributeTypeEnum.NON_PHASING_LEADER_WOUNDS, side.getLeaderWounds()));

                // The stack that was led by this leader will change leader in the cleanUp phase.
            }
        }

        return diffs;
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
            if (CommonUtil.subtract(side.getMoral(), side.getLosses().getMoraleLoss()) > 0) {
                Leader leader = getTables().getLeader(side.getLeader(), side.getCountry());
                retreat -= leader.getManoeuvre();
            }
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

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseLossesFromBattle(Request<ChooseLossesRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_LOSSES, PARAMETER_CHOOSE_LOSSES);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_CHOOSE_LOSSES, PARAMETER_CHOOSE_LOSSES);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST)
                .setParams(METHOD_CHOOSE_LOSSES));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.CHOOSE_LOSS)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_CHOOSE_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES, BattleStatusEnum.CHOOSE_LOSS.name()));

        boolean playerPhasing = isPhasingPlayer(game, request.getGame().getIdCountry());
        boolean accessRight = oeUtil.isWarAlly(country, battle.getWar(),
                playerPhasing && battle.isPhasingOffensive() || !playerPhasing && !battle.isPhasingOffensive());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(accessRight)
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_CHOOSE_LOSSES, country.getName(),
                        playerPhasing ? battle.getPhasing().getCountry() : battle.getNonPhasing().getCountry()));

        boolean lossesAlreadyChosen = playerPhasing && BooleanUtils.isTrue(battle.getPhasing().isLossesSelected()) ||
                !playerPhasing && BooleanUtils.isTrue(battle.getNonPhasing().isLossesSelected());

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(lossesAlreadyChosen)
                .setCodeError(IConstantsServiceException.ACTION_ALREADY_DONE)
                .setMsgFormat("{1}: {0} The action {1} has already been done by the country or the side {2}.")
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_CHOOSE_LOSSES, METHOD_CHOOSE_LOSSES, playerPhasing ? "phasing" : "non phasing"));

        BattleSideEntity side;
        if (playerPhasing) {
            side = battle.getPhasing();
        } else {
            side = battle.getNonPhasing();
        }
        // Remove useless entries from request
        request.getRequest().getLosses().removeIf(ul -> ul.getRoundLosses() <= 0 && ul.getThirdLosses() <= 0);
        int roundLosses = request.getRequest().getLosses().stream().collect(Collectors.summingInt(ChooseLossesRequest.UnitLoss::getRoundLosses));
        int thirdLosses = request.getRequest().getLosses().stream().collect(Collectors.summingInt(ChooseLossesRequest.UnitLoss::getThirdLosses));

        if (thirdLosses >= 3) {
            roundLosses += thirdLosses / 3;
            thirdLosses = thirdLosses % 3;
        }

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(!CommonUtil.equals(roundLosses, side.getLosses().getRoundLoss()) || !CommonUtil.equals(thirdLosses, side.getLosses().getThirdLoss()))
                .setCodeError(IConstantsServiceException.BATTLE_LOSSES_MISMATCH)
                .setMsgFormat("{1}: {0} The losses taken {2} does not match the losses that should be taken {3}.")
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES, AbstractWithLossEntity.create(3 * roundLosses + thirdLosses).toString(), side.getLosses().toString()));

        AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
        if (province instanceof EuropeanProvinceEntity) {
            boolean hasThird = request.getRequest().getLosses().stream().anyMatch(ul -> ul.getThirdLosses() > 0);

            failIfTrue(new CheckForThrow<Boolean>()
                    .setTest(hasThird)
                    .setCodeError(IConstantsServiceException.BATTLE_LOSSES_NO_THIRD)
                    .setMsgFormat("{1}: {0} The losses cannot involve third in an european province.")
                    .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                    .setParams(METHOD_CHOOSE_LOSSES));
        }

        List<DiffEntity> newDiffs = new ArrayList<>();
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        long thirdBefore = battle.getCounters().stream()
                .filter(bc -> bc.isPhasing() == playerPhasing && CounterUtil.isExploration(bc.getType()))
                .count();
        int thirdDiff = 0;

        for (ChooseLossesRequest.UnitLoss loss : request.getRequest().getLosses()) {
            Long counterId = battle.getCounters().stream()
                    .filter(bc -> bc.isPhasing() == playerPhasing && Objects.equals(loss.getIdCounter(), bc.getCounter()))
                    .map(BattleCounterEntity::getCounter)
                    .findAny()
                    .orElse(null);
            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(count -> Objects.equals(counterId, count.getId()))
                    .findAny()
                    .orElse(null);

            failIfNull(new CheckForThrow<>()
                    .setTest(counter)
                    .setCodeError(IConstantsServiceException.BATTLE_LOSSES_INVALID_COUNTER)
                    .setMsgFormat("{1}: {0} The losses cannot involve the counter {2}.")
                    .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                    .setParams(METHOD_CHOOSE_LOSSES, loss.getIdCounter()));

            double lossSize = loss.getRoundLosses() + THIRD * loss.getThirdLosses();
            double lossMax = CounterUtil.getSizeFromType(counter.getType());
            failIfTrue(new CheckForThrow<Boolean>()
                    .setTest(lossSize > lossMax + EPSILON)
                    .setCodeError(IConstantsServiceException.BATTLE_LOSSES_TOO_BIG)
                    .setMsgFormat("{1}: {0} The counter {2} cannot take {3} losses because it cannot take more than {4}.")
                    .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                    .setParams(METHOD_CHOOSE_LOSSES, loss.getIdCounter(), lossSize, lossMax));

            if (lossMax - lossSize <= EPSILON) {
                newDiffs.add(counterDomain.removeCounter(counter));
                thirdDiff -= loss.getThirdLosses();
            } else {
                List<CounterFaceTypeEnum> faces = new ArrayList<>();
                double remain = lossMax - lossSize;
                int round = (int) remain;
                int third = (int) ((remain - round) / THIRD);
                if (round >= 2) {
                    faces.add(CounterUtil.getSize2FromType(counter.getType()));
                    round -= 2;
                }
                if (round >= 1) {
                    faces.add(CounterUtil.getSize1FromType(counter.getType()));
                    round -= 1;
                }
                while (third > 0) {
                    CounterFaceTypeEnum face = CounterUtil.getSizeThirdFromType(counter.getType());
                    if (face != null) {
                        faces.add(face);
                        thirdDiff++;
                    }
                    third--;
                }
                // TODO check if round and third are 0 ?
                faces.removeIf(o -> o == null);
                if (faces.isEmpty()) {
                    newDiffs.add(counterDomain.removeCounter(counter));
                } else {
                    // FIXME veterans
                    newDiffs.addAll(faces.stream()
                            .map(face -> counterDomain.createCounter(face, counter.getCountry(), counter.getOwner().getId(), game))
                            .collect(Collectors.toList()));
                    newDiffs.add(counterDomain.removeCounter(counter));
                }
            }
        }

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(thirdBefore + thirdDiff >= 3)
                .setCodeError(IConstantsServiceException.BATTLE_LOSSES_TOO_MANY_THIRD)
                .setMsgFormat("{1}: {0} The losses are invalid because it will result with too many thirds.")
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES));

        DiffAttributeTypeEnum type;
        if (playerPhasing) {
            battle.getPhasing().setLossesSelected(true);
            type = DiffAttributeTypeEnum.PHASING_READY;
        } else {
            battle.getNonPhasing().setLossesSelected(true);
            type = DiffAttributeTypeEnum.NON_PHASING_READY;
        }

        if (BooleanUtils.isTrue(battle.getPhasing().isLossesSelected()) && BooleanUtils.isTrue(battle.getNonPhasing().isLossesSelected())) {
            newDiffs.addAll(prepareRetreat(battle, attributes));
        } else {
            attributes.add(DiffUtil.createDiffAttributes(type, true));
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        newDiffs.add(diff);

        return createDiffs(newDiffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse retreatAfterBattle(Request<RetreatAfterBattleRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_RETREAT_AFTER_BATTLE)
                .setParams(METHOD_RETREAT_AFTER_BATTLE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_RETREAT_AFTER_BATTLE, PARAMETER_RETREAT_AFTER_BATTLE);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_BATTLES, METHOD_RETREAT_AFTER_BATTLE, PARAMETER_RETREAT_AFTER_BATTLE);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST)
                .setParams(METHOD_RETREAT_AFTER_BATTLE));

        BattleEntity battle = game.getBattles().stream()
                .filter(bat -> bat.getStatus() == BattleStatusEnum.RETREAT)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(battle)
                .setCodeError(IConstantsServiceException.BATTLE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No battle of status {2} can be found.")
                .setName(PARAMETER_RETREAT_AFTER_BATTLE)
                .setParams(METHOD_RETREAT_AFTER_BATTLE, BattleStatusEnum.RETREAT.name()));

        boolean playerPhasing = isPhasingPlayer(game, request.getGame().getIdCountry());
        boolean accessRight = oeUtil.isWarAlly(country, battle.getWar(),
                playerPhasing && battle.isPhasingOffensive() || !playerPhasing && !battle.isPhasingOffensive());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(accessRight)
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_RETREAT_AFTER_BATTLE, country.getName(),
                        playerPhasing ? battle.getPhasing().getCountry() : battle.getNonPhasing().getCountry()));

        boolean retreatAlreadyChosen = playerPhasing && BooleanUtils.isTrue(battle.getPhasing().isRetreatSelected()) ||
                !playerPhasing && BooleanUtils.isTrue(battle.getNonPhasing().isRetreatSelected());

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(retreatAlreadyChosen)
                .setCodeError(IConstantsServiceException.ACTION_ALREADY_DONE)
                .setMsgFormat("{1}: {0} The action {1} has already been done by the country or the side {2}.")
                .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_RETREAT_AFTER_BATTLE, METHOD_RETREAT_AFTER_BATTLE, playerPhasing ? "phasing" : "non phasing"));

        List<String> allies = oeUtil.getWarFaction(battle.getWar(),
                playerPhasing ? battle.isPhasingOffensive() : !battle.isPhasingOffensive());
        List<DiffEntity> newDiffs = new ArrayList<>();
        List<DiffAttributesEntity> attributes = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(request.getRequest().getRetreatInFortress())) {
            AbstractProvinceEntity province = provinceDao.getProvinceByName(battle.getProvince());
            StackEntity besiegedStack = counterDomain.createStack(battle.getProvince(), country.getName(), game);
            besiegedStack.setBesieged(true);
            besiegedStack.setMovePhase(MovePhaseEnum.MOVED);
            newDiffs.add(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.STACK, besiegedStack.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, battle.getProvince()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, country.getName()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BESIEGED, true),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED)));
            double size = 0;
            for (Long idCounter : request.getRequest().getRetreatInFortress()) {
                CounterEntity counter = game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                        .flatMap(stack -> stack.getCounters().stream())
                        .filter(unit -> Objects.equals(unit.getId(), idCounter) && allies.contains(unit.getCountry()) &&
                                CounterUtil.isMobile(unit.getType()))
                        .findAny()
                        .orElse(null);

                failIfNull(new CheckForThrow<>()
                        .setTest(counter)
                        .setCodeError(IConstantsServiceException.BATTLE_RETREAT_INVALID_COUNTER)
                        .setMsgFormat("{1}: {0} The retreat in fortress cannot involve the counter {2}.")
                        .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_RETREAT_IN_FORTRESS)
                        .setParams(METHOD_RETREAT_AFTER_BATTLE, idCounter));

                size += CounterUtil.getSizeFromType(counter.getType());
                newDiffs.add(counterDomain.changeCounterOwner(counter, besiegedStack, game));
            }

            boolean canRetreatInFortress = oeUtil.canRetreat(province, true, size, country, game);

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(canRetreatInFortress)
                    .setCodeError(IConstantsServiceException.BATTLE_CANT_RETREAT)
                    .setMsgFormat("{1}: {0} {2} is not a valid province to retreat.")
                    .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_RETREAT_IN_FORTRESS)
                    .setParams(METHOD_RETREAT_AFTER_BATTLE, battle.getProvince()));
        }

        if (StringUtils.isEmpty(request.getRequest().getProvinceTo())) {
            if (request.getRequest().isDisbandRemaining()) {
                List<CounterEntity> deleteCounters = game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()) && oeUtil.isMobile(stack) && allies.contains(stack.getCountry()))
                        .flatMap(stack -> stack.getCounters().stream())
                        .collect(Collectors.toList());
                List<DiffEntity> deleteDiffs = deleteCounters.stream()
                        .map(counterDomain::removeCounter)
                        .collect(Collectors.toList());
                newDiffs.addAll(deleteDiffs);
            } else {
                boolean remainingCounters = game.getStacks().stream()
                        .anyMatch(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()) && oeUtil.isMobile(stack) && allies.contains(stack.getCountry()));
                failIfTrue(new CheckForThrow<Boolean>()
                        .setTest(remainingCounters)
                        .setCodeError(IConstantsServiceException.BATTLE_RETREAT_NEEDED)
                        .setMsgFormat("{1}: {0} There are still some units that need to be retreated..")
                        .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                        .setParams(METHOD_RETREAT_AFTER_BATTLE));
            }
        } else {
            String provinceTo = request.getRequest().getProvinceTo();
            AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);
            failIfNull(new CheckForThrow<>()
                    .setTest(province)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                    .setParams(METHOD_RETREAT_AFTER_BATTLE, provinceTo));

            // no check on size because retreat in multiple provinces is nightmare. Player will have to destroy the surplus at the end of the round.
            boolean canRetreat = oeUtil.canRetreat(province, false, 0, country, game);

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(canRetreat)
                    .setCodeError(IConstantsServiceException.BATTLE_CANT_RETREAT)
                    .setMsgFormat("{1}: {0} {2} is not a valid province to retreat.")
                    .setName(PARAMETER_RETREAT_AFTER_BATTLE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                    .setParams(METHOD_RETREAT_AFTER_BATTLE, province.getName()));

            Consumer<StackEntity> retreatStack = stack -> {
                String newStackController = oeUtil.getController(stack);
                String newLeader = oeUtil.getLeader(stack, getTables(), getLeaderConditions(province));
                newDiffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.STACK, stack.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, battle.getProvince()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, provinceTo),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newStackController, !StringUtils.equals(newStackController, stack.getCountry())),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, !StringUtils.equals(newLeader, stack.getLeader()))));
                stack.setMovePhase(MovePhaseEnum.MOVED);
                stack.setProvince(provinceTo);
                stack.setCountry(newStackController);
                stack.setLeader(newLeader);
            };
            game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(battle.getProvince(), stack.getProvince()) && oeUtil.isMobile(stack) && allies.contains(stack.getCountry()))
                    .forEach(retreatStack);
        }

        DiffAttributeTypeEnum type;
        if (playerPhasing) {
            battle.getPhasing().setRetreatSelected(true);
            type = DiffAttributeTypeEnum.PHASING_READY;
        } else {
            battle.getNonPhasing().setRetreatSelected(true);
            type = DiffAttributeTypeEnum.NON_PHASING_READY;
        }

        if (BooleanUtils.isTrue(battle.getPhasing().isRetreatSelected()) && BooleanUtils.isTrue(battle.getNonPhasing().isRetreatSelected())) {
            battle.setStatus(BattleStatusEnum.DONE);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, BattleStatusEnum.DONE));
            newDiffs.addAll(cleanUpBattle(battle));
        } else {
            attributes.add(DiffUtil.createDiffAttributes(type, true));
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.BATTLE, battle.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        newDiffs.add(diff);

        return createDiffs(newDiffs, gameDiffs, request);
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
