package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.common.RedeployRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
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
import com.mkl.eu.service.service.persistence.oe.military.SiegeCounterEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeSideEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.SeaProvinceEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
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
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;
import static com.mkl.eu.client.common.util.CommonUtil.THIRD;

/**
 * Service for siege purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class SiegeServiceImpl extends AbstractMilitaryService implements ISiegeService {
    public final static Predicate<CounterEntity> HAS_SIEGEWORK = counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS || counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS;
    /** Counter domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** Status workflow domain. */
    @Autowired
    private IStatusWorkflowDomain statusWorkflowDomain;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseSiege(Request<ChooseProvinceRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_SIEGE)
                .setParams(METHOD_CHOOSE_SIEGE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_SIEGE, PARAMETER_CHOOSE_SIEGE);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_CHOOSE_SIEGE, PARAMETER_CHOOSE_SIEGE, GameStatusEnum.MILITARY_SIEGES);

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_SIEGE, PARAMETER_REQUEST)
                .setParams(METHOD_CHOOSE_SIEGE));

        failIfEmpty(new CheckForThrow<String>()
                .setTest(request.getRequest().getProvince())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_SIEGE, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_CHOOSE_SIEGE));
        String province = request.getRequest().getProvince();

        String siegeInProcess = game.getSieges().stream()
                .filter(siege -> siege.getStatus().isActive())
                .map(SiegeEntity::getProvince)
                .findAny()
                .orElse(null);

        failIfNotNull(new CheckForThrow<>()
                .setTest(siegeInProcess)
                .setCodeError(IConstantsServiceException.SIEGE_IN_PROCESS)
                .setMsgFormat("{1}: {0} No siege can be initiated while the siege in {2} is not finished.")
                .setName(PARAMETER_CHOOSE_SIEGE)
                .setParams(METHOD_CHOOSE_SIEGE, siegeInProcess));

        List<String> provincesInSiege = game.getSieges().stream()
                .filter(siege -> siege.getStatus() == SiegeStatusEnum.NEW)
                .map(SiegeEntity::getProvince)
                .collect(Collectors.toList());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(provincesInSiege.contains(province))
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} is not a province where a siege can be done.")
                .setName(PARAMETER_CHOOSE_SIEGE, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_CHOOSE_SIEGE));

        SiegeEntity siege = game.getSieges().stream()
                .filter(bat -> bat.getStatus() == SiegeStatusEnum.NEW &&
                        StringUtils.equals(bat.getProvince(), province))
                .findAny()
                .orElse(null);

        boolean accessRight = oeUtil.isWarAlly(country, siege.getWar(),
                siege.isBesiegingOffensive());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(accessRight)
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_CHOOSE_SIEGE, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_CHOOSE_SIEGE, country.getName(), "complex"));

        List<DiffAttributesEntity> attributes = new ArrayList<>();

        List<String> allies = oeUtil.getWarFaction(siege.getWar(), siege.isBesiegingOffensive());

        List<CounterEntity> attackerCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                        allies.contains(stack.getCountry()))
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
        List<Leader> leaders = oeUtil.getLeaders(attackerCounters, getTables(), Leader.landEurope);
        boolean leaderOk = leaders.size() <= 1;

        if (sizeOk && StringUtils.isNotEmpty(leadingCountry) && leaderOk) {
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTRY, leadingCountry));
            siege.getPhasing().setCountry(leadingCountry);

            if (leaders.size() == 1) {
                String leader = leaders.get(0).getCode();
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_LEADER, leader));
                siege.getPhasing().setLeader(leader);
            }
            attackerCounters.removeIf(counter -> CounterUtil.isLeader(counter.getType()) &&
                    !StringUtils.equals(counter.getCode(), siege.getPhasing().getLeader()));

            attackerCounters.forEach(counter -> {
                SiegeCounterEntity comp = new SiegeCounterEntity();
                comp.setSiege(siege);
                comp.setCounter(counter.getId());
                comp.setCountry(counter.getCountry());
                comp.setType(counter.getType());
                comp.setCode(counter.getCode());
                comp.setPhasing(true);
                siege.getCounters().add(comp);

                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTER_ADD, counter.getId()));
            });
            siege.setStatus(SiegeStatusEnum.CHOOSE_MODE);
            computeSiegeBonus(siege, attributes);
        } else {
            siege.setStatus(SiegeStatusEnum.SELECT_FORCES);
        }

        List<CounterEntity> defenderCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                        stack.isBesieged())
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()) || CounterUtil.isLeader(counter.getType()))
                .collect(Collectors.toList());
        AbstractProvinceEntity fullProvince = provinceDao.getProvinceByName(province);
        leadingCountry = oeUtil.getController(fullProvince, game);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_COUNTRY, leadingCountry));
        siege.getNonPhasing().setCountry(leadingCountry);
        if (CollectionUtils.isNotEmpty(defenderCounters)) {
            leaders = oeUtil.getLeaders(defenderCounters, getTables(), Leader.landEurope);
            if (leaders.size() >= 1) {
                String leader = leaders.get(0).getCode();
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_LEADER, leader));
                siege.getNonPhasing().setLeader(leader);
            }
            defenderCounters.removeIf(counter -> CounterUtil.isLeader(counter.getType()) &&
                    !StringUtils.equals(counter.getCode(), siege.getNonPhasing().getLeader()));

            defenderCounters.forEach(counter -> {
                SiegeCounterEntity comp = new SiegeCounterEntity();
                comp.setSiege(siege);
                comp.setCounter(counter.getId());
                comp.setCountry(counter.getCountry());
                comp.setType(counter.getType());
                comp.setCode(counter.getCode());
                comp.setPhasing(false);
                siege.getCounters().add(comp);

                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD, counter.getId()));
            });
        }

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, siege.getStatus()));
        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));

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

        checkSimpleStatus(game, GameStatusEnum.MILITARY_SIEGES, METHOD_SELECT_FORCES, PARAMETER_SELECT_FORCES);

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

        SiegeEntity siege = game.getSieges().stream()
                .filter(bat -> bat.getStatus() == SiegeStatusEnum.SELECT_FORCES)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(siege)
                .setCodeError(IConstantsServiceException.SIEGE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No siege of status {2} can be found.")
                .setName(PARAMETER_SELECT_FORCES)
                .setParams(METHOD_SELECT_FORCES, SiegeStatusEnum.SELECT_FORCES.name()));

        boolean phasing = isCountryActive(game, request.getGame().getIdCountry());

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(phasing)
                .setCodeError(IConstantsServiceException.SIEGE_SELECT_VALIDATED)
                .setMsgFormat("{1}: {0} The non phasing forces are always automatically added in a siege.")
                .setName(PARAMETER_SELECT_FORCES)
                .setParams(METHOD_SELECT_FORCES));

        List<DiffAttributesEntity> attributes = new ArrayList<>();
        List<CounterEntity> counters = new ArrayList<>();
        List<String> allies = oeUtil.getWarFaction(siege.getWar(), siege.isBesiegingOffensive());
        for (Long idCounter : request.getRequest().getForces()) {

            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
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

            SiegeCounterEntity comp = new SiegeCounterEntity();
            comp.setPhasing(phasing);
            comp.setSiege(siege);
            comp.setCounter(counter.getId());
            comp.setCountry(counter.getCountry());
            comp.setType(counter.getType());
            comp.setCode(counter.getCode());
            siege.getCounters().add(comp);
            counters.add(counter);

            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTER_ADD, counter.getId()));
        }

        List<Long> alliedCounters = siege.getCounters().stream()
                .filter(bc -> bc.isPhasing() == phasing && CounterUtil.isArmy(bc.getType()))
                .map(SiegeCounterEntity::getCounter)
                .collect(Collectors.toList());
        Double armySize = siege.getCounters().stream()
                .map(bc -> CounterUtil.getSizeFromType(bc.getType()))
                .reduce(Double::sum)
                .orElse(0d);

        if (alliedCounters.size() < 3 && armySize < 8) {
            Double remainingMinSize = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                            allies.contains(stack.getCountry()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(counter -> CounterUtil.isArmy(counter.getType()) &&
                            !alliedCounters.contains(counter.getId()))
                    .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                    .min(Double::compare)
                    .orElse(0d);

            failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                    .setTest(remainingMinSize > 0 && remainingMinSize <= 8 - armySize)
                    .setCodeError(IConstantsServiceException.SIEGE_VALIDATE_OTHER_FORCE)
                    .setMsgFormat("{1}: {0} Impossible to select forces in this siege because there are other forces to select.")
                    .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_VALIDATE)
                    .setParams(METHOD_SELECT_FORCES));
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
                .setMsgFormat("{1}: {0} Impossible to select forces in this siege because there are too many leaders selected : {2}.")
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                .setParams(METHOD_SELECT_FORCES, leaders.stream().map(CounterEntity::getCode).collect(Collectors.joining(","))));

        List<String> countries = oeUtil.getLeadingCountries(counters);
        String selectedCountry = StringUtils.isEmpty(request.getRequest().getCountry()) && countries.size() == 1
                ? countries.get(0) : request.getRequest().getCountry();

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(!countries.contains(selectedCountry))
                .setCodeError(IConstantsServiceException.BATTLE_FORCES_LEADING_COUNTRY_AMBIGUOUS)
                .setMsgFormat("{1}: {0} Impossible to select forces in this siege because the selected country cannot lead this battle or you must select a country (selected country: {2}, eligible countries: {3}).")
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_SELECT_FORCES, selectedCountry, countries));

        Predicate<Leader> conditions = getLeaderConditions(siege.getProvince());
        List<Leader> availableLeaders = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                        allies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.LEADER &&
                        StringUtils.equals(counter.getCountry(), selectedCountry))
                .map(counter -> getTables().getLeader(counter.getCode(), counter.getCountry()))
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
                    .setMsgFormat("{1}: {0} Impossible to select forces in this siege because the selected leader {2} cannot lead this battle.")
                    .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                    .setParams(METHOD_SELECT_FORCES, selectedLeader));
        }

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(availableLeaders.size() > 0)
                .setCodeError(IConstantsServiceException.BATTLE_FORCES_INVALID_LEADER)
                .setMsgFormat("{1}: {0} Impossible to select forces in this siege because the selected leader {2} is not optimal (better leaders : {3}).")
                .setName(PARAMETER_SELECT_FORCES, PARAMETER_REQUEST, PARAMETER_FORCES)
                .setParams(METHOD_SELECT_FORCES, selectedLeader, availableLeaders.stream().map(Leader::getCode).collect(Collectors.joining(","))));

        siege.setStatus(SiegeStatusEnum.CHOOSE_MODE);
        siege.getPhasing().setCountry(selectedCountry);
        siege.getPhasing().setLeader(selectedLeader);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.CHOOSE_MODE));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTRY, siege.getPhasing().getCountry()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_LEADER, siege.getPhasing().getLeader()));
        computeSiegeBonus(siege, attributes);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));

        return createDiff(diff, gameDiffs, request);
    }

    /**
     * Compute the undermining bonus of a siege.
     *
     * @param siege      the siege.
     * @param attributes the attributes of the MODIFY SIEGE diff.
     */
    protected void computeSiegeBonus(SiegeEntity siege, List<DiffAttributesEntity> attributes) {
        GameEntity game = siege.getGame();
        AbstractProvinceEntity province = provinceDao.getProvinceByName(siege.getProvince());
        int fortress = oeUtil.getFortressLevel(province, game);
        siege.setFortressLevel(fortress);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEVEL, fortress));

        List<Long> countersPhasingId = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isPhasing)
                .map(SiegeCounterEntity::getCounter)
                .collect(Collectors.toList());

        List<CounterEntity> countersPhasing = siege.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(siege.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersPhasingId.contains(counter.getId()))
                .collect(Collectors.toList());

        int artilleries = oeUtil.getArtilleryBonus(countersPhasing, getReferential(), getTables(), game);
        int artilleryBonus = getTables().getArtillerySieges().stream()
                .filter(as -> as.getFortress() == fortress && as.getArtillery() <= artilleries)
                .map(ArtillerySiege::getBonus)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(0);
        boolean plain = province.getTerrain() == TerrainEnum.PLAIN;
        boolean port = province.getBorders().stream()
                .anyMatch(border -> border.getProvinceTo() instanceof SeaProvinceEntity);
        // TODO TG-11 cancel port if it is blockaded
        boolean rotw = province instanceof RotwProvinceEntity;
        int terrainMalus = 0;
        if (!rotw) {
            if (!plain && port) {
                terrainMalus = 3;
            } else if (!plain || port) {
                terrainMalus = 2;
            }
        } else {
            if (!plain || port) {
                terrainMalus = 2;
            }
            if (fortress == 0) {
                terrainMalus /= 2;
            }
        }

        int breachBonus = siege.isBreach() ? 2 : 0;

        ToIntFunction<CounterEntity> siegeworkValue = counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS ? 3 : counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS ? 1 : 0;
        int siegeworkBonus = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .collect(Collectors.summingInt(siegeworkValue));
        List<String> allies = oeUtil.getWarFaction(siege.getWar(), siege.isBesiegingOffensive());
        int leaderSiege = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isLeader(counter.getType()) && allies.contains(counter.getCountry()))
                .map(counter -> getTables().getLeader(counter.getCode(), counter.getCountry()))
                .map(Leader::getSiege)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(0);
        int besiegingBonus = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isNotPhasing)
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()) >= 2 ? 3 : 1)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(0);

        int bonus = -fortress + artilleryBonus - terrainMalus + breachBonus + siegeworkBonus + leaderSiege + besiegingBonus;
        if (bonus != siege.getBonus()) {
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BONUS, bonus));
        }
        siege.setBonus(bonus);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseMode(Request<ChooseModeForSiegeRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_MODE)
                .setParams(METHOD_CHOOSE_MODE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_MODE, PARAMETER_CHOOSE_MODE);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_CHOOSE_MODE, PARAMETER_CHOOSE_MODE, GameStatusEnum.MILITARY_SIEGES);

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
                .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST)
                .setParams(METHOD_CHOOSE_MODE));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest().getMode())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST, PARAMETER_MODE)
                .setParams(METHOD_CHOOSE_MODE));

        SiegeEntity siege = game.getSieges().stream()
                .filter(bat -> bat.getStatus() == SiegeStatusEnum.CHOOSE_MODE)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(siege)
                .setCodeError(IConstantsServiceException.SIEGE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No siege of status {2} can be found.")
                .setName(PARAMETER_CHOOSE_MODE)
                .setParams(METHOD_CHOOSE_MODE, SiegeStatusEnum.CHOOSE_MODE.name()));

        double size = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isPhasing)
                .collect(Collectors.summingDouble(c -> CounterUtil.getSizeFromType(c.getType())));
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        List<DiffEntity> diffs = new ArrayList<>();
        switch (request.getRequest().getMode()) {
            case UNDERMINE:
                failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                        .setTest(size >= siege.getFortressLevel())
                        .setCodeError(IConstantsServiceException.SIEGE_UNDERMINE_TOO_FEW)
                        .setMsgFormat("{1}: {0} Impossible to undermine the fortress because of insufficient besieging forces.")
                        .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST, PARAMETER_MODE)
                        .setParams(METHOD_CHOOSE_MODE));

                diffs.addAll(computeUndermine(siege, country, attributes));
                break;
            case REDEPLOY:
                String provinceTo = request.getRequest().getProvinceTo();
                failIfEmpty(new AbstractService.CheckForThrow<String>()
                        .setTest(provinceTo)
                        .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                        .setMsgFormat(MSG_MISSING_PARAMETER)
                        .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                        .setParams(METHOD_CHOOSE_MODE));

                AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);

                failIfNull(new CheckForThrow<>()
                        .setTest(province)
                        .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                        .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                        .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                        .setParams(METHOD_CHOOSE_MODE, province));

                AbstractProvinceEntity provinceFrom = provinceDao.getProvinceByName(siege.getProvince());
                boolean isNear = provinceFrom.getBorders().stream()
                        .anyMatch(x -> Objects.equals(province.getId(), x.getProvinceTo().getId()));

                failIfFalse(new CheckForThrow<Boolean>()
                        .setTest(isNear)
                        .setCodeError(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR)
                        .setMsgFormat(MSG_NOT_NEIGHBOR)
                        .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                        .setParams(METHOD_CHOOSE_MODE, siege.getProvince(), provinceTo));

                boolean canRetreat = oeUtil.canRetreat(province, false, 0, country, game);

                failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                        .setTest(canRetreat)
                        .setCodeError(IConstantsServiceException.SIEGE_CANT_REDEPLOY)
                        .setMsgFormat("{1}: {0} Impossible to redeploy besieging forces in the province {2}.")
                        .setName(PARAMETER_CHOOSE_MODE, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                        .setParams(METHOD_CHOOSE_MODE, provinceTo));

                Consumer<StackEntity> retreatStack = stack -> {
                    diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.STACK, stack.getId(),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, siege.getProvince()),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, provinceTo),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED)));
                    stack.setMovePhase(MovePhaseEnum.MOVED);
                    stack.setProvince(provinceTo);
                };
                List<String> allies = oeUtil.getWarFaction(siege.getWar(), siege.isBesiegingOffensive());
                game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(siege.getProvince(), stack.getProvince()) && oeUtil.isMobile(stack) && allies.contains(stack.getCountry()))
                        .forEach(retreatStack);
                diffs.addAll(cleanUpSiege(siege, attributes));
                break;
            case ASSAULT:
                diffs.addAll(computeAssault(siege, country, false, attributes));
                break;
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        diffs.add(diff);

        return createDiffs(diffs, gameDiffs, request);
    }

    /**
     * Compute an undermine in the siege.
     *
     * @param siege      the siege.
     * @param country    the country doing the undermine.
     * @param attributes the attributes about the MODIFY SIEGE diff.
     * @return the diffs involved.
     */
    private List<DiffEntity> computeUndermine(SiegeEntity siege, PlayableCountryEntity country, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();

        int die = oeUtil.rollDie(siege.getGame(), country);
        siege.setUndermineDie(die);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.SIEGE_UNDERMINE_DIE, die));

        int modifiedDie = die + siege.getBonus();
        if (modifiedDie < 3) {
            modifiedDie = 3;
        }
        if (modifiedDie > 13) {
            modifiedDie = 13;
        }
        switch (modifiedDie) {
            case 3:
                diffs.addAll(cleanUpSiege(siege, attributes));
                break;
            case 4:
            case 5:
            case 6:
                siege.setUndermineResult(SiegeUndermineResultEnum.SIEGE_WORK_MINUS);
                diffs.addAll(addSiegework(CounterFaceTypeEnum.SIEGEWORK_MINUS, siege));
                diffs.addAll(cleanUpSiege(siege, attributes));
                break;
            case 7:
            case 8:
            case 9:
                siege.setUndermineResult(SiegeUndermineResultEnum.SIEGE_WORK_PLUS);
                diffs.addAll(addSiegework(CounterFaceTypeEnum.SIEGEWORK_PLUS, siege));
                diffs.addAll(cleanUpSiege(siege, attributes));
                break;
            case 10:
            case 11:
            case 12:
                siege.setStatus(SiegeStatusEnum.CHOOSE_BREACH);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.CHOOSE_BREACH));
                siege.setBreach(true);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.SIEGE_BREACH, true));
                break;
            case 13:
                siege.setUndermineResult(SiegeUndermineResultEnum.SURRENDER);
                diffs.addAll(fortressFalls(siege, country, attributes));
                break;
        }
        if (siege.getUndermineResult() != null) {
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.SIEGE_UNDERMINE_RESULT, siege.getUndermineResult()));
        }

        return diffs;
    }

    /**
     * Adds a siegework in the province where the siege is.
     *
     * @param siegework the type of siegework to add.
     * @param siege     the siege.
     * @return the diffs involved.
     */
    private List<DiffEntity> addSiegework(CounterFaceTypeEnum siegework, SiegeEntity siege) {
        List<DiffEntity> diffs = new ArrayList<>();

        StackEntity stack = siege.getGame().getStacks().stream()
                .filter(s -> StringUtils.equals(s.getProvince(), siege.getProvince()) && s.getCounters().stream().anyMatch(HAS_SIEGEWORK))
                .findAny()
                .orElseGet(() -> counterDomain.createStack(siege.getProvince(), null, siege.getGame()));

        long siegeworkPlus = stack.getCounters().stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS)
                .collect(Collectors.counting());
        long siegeworkMinus = stack.getCounters().stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS)
                .collect(Collectors.counting());

        if (siegeworkPlus < 2 && siegeworkMinus == 1 && (siegework == CounterFaceTypeEnum.SIEGEWORK_MINUS || siegeworkPlus == 1)) {
            // Transforms a SIEGEWORK_MINUS into a SIEGEWORK_PLUS
            CounterEntity counterSiegework = stack.getCounters().stream()
                    .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS)
                    .findAny()
                    .orElse(null);

            diffs.add(counterDomain.switchCounter(counterSiegework, CounterFaceTypeEnum.SIEGEWORK_PLUS, null, siege.getGame()));
        } else if (siegeworkPlus < 2) {
            // Other cases : add a siegework
            diffs.add(counterDomain.createCounter(siegework, null, stack.getId(), siege.getGame()));
        }

        return diffs;
    }

    /**
     * When the fortress fails, we will check if the fortress can be manned or not, and go to the according status.
     *
     * @param siege      the siege.
     * @param country    the besieger.
     * @param attributes the attributes about the MODIFY SIEGE diff.
     * @return the diffs involved.
     */
    private List<DiffEntity> fortressFalls(SiegeEntity siege, PlayableCountryEntity country, List<DiffAttributesEntity> attributes) {
        siege.setFortressFalls(true);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.SIEGE_FORTRESS_FALLS, true));
        return areLossesAuto(siege, country, attributes);
    }

    /**
     * At the end of a siege, check if some losses are waiting for an input or not.
     *
     * @param siege      the siege.
     * @param country    the besieger.
     * @param attributes the attributes about the MODIFY SIEGE diff.
     * @return the diffs involved.
     */
    private List<DiffEntity> areLossesAuto(SiegeEntity siege, PlayableCountryEntity country, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        boolean phasingLossesAuto = siege.getPhasing().getLosses().getTotalThird() == 0 || siege.getPhasing().getLosses().isGreaterThanSize(siege.getPhasing().getSize());
        boolean nonPhasingLossesAuto = siege.getNonPhasing().getLosses().getTotalThird() == 0 || siege.getNonPhasing().getLosses().isGreaterThanSize(siege.getNonPhasing().getSize());

        siege.getPhasing().setLossesSelected(phasingLossesAuto);
        siege.getNonPhasing().setLossesSelected(nonPhasingLossesAuto);

        // if annihilated, remove all counters
        boolean testLosses = siege.getUndermineDie() == 0 || siege.getUndermineResult() == SiegeUndermineResultEnum.BREACH_TAKEN;
        Function<SiegeCounterEntity, CounterEntity> toCounter = source -> siege.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> Objects.equals(counter.getId(), source.getCounter()))
                .findAny()
                .orElse(null);
        if (testLosses && siege.getPhasing().getLosses().isGreaterThanSize(siege.getPhasing().getSize())) {
            siege.getCounters().stream()
                    .filter(SiegeCounterEntity::isPhasing)
                    .map(toCounter)
                    .forEach(counter -> diffs.add(counterDomain.removeCounter(counter)));
        }
        if ((testLosses && siege.getNonPhasing().getLosses().isGreaterThanSize(siege.getNonPhasing().getSize())) || siege.getUndermineResult() == SiegeUndermineResultEnum.SURRENDER) {
            siege.getCounters().stream()
                    .filter(SiegeCounterEntity::isNotPhasing)
                    .map(toCounter)
                    .forEach(counter -> diffs.add(counterDomain.removeCounter(counter)));
        }

        if (!phasingLossesAuto || !nonPhasingLossesAuto) {
            siege.setStatus(SiegeStatusEnum.CHOOSE_LOSS);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.CHOOSE_LOSS));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, phasingLossesAuto, phasingLossesAuto));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, nonPhasingLossesAuto, nonPhasingLossesAuto));
        } else if (siege.isFortressFalls()) {
            diffs.addAll(isFortressMan(siege, country, attributes));
        } else {
            diffs.addAll(cleanUpSiege(siege, attributes));
        }

        return diffs;
    }

    /**
     * At the end of a successfull siege, check if the fortress can be manned.
     *
     * @param siege      the siege.
     * @param country    the besieger.
     * @param attributes the attributes about the MODIFY SIEGE diff.
     * @return the diffs involved.
     */
    private List<DiffEntity> isFortressMan(SiegeEntity siege, PlayableCountryEntity country, List<DiffAttributesEntity> attributes) {
        boolean manPossible = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isPhasing)
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType()))) >= 1 && siege.getFortressLevel() > 1;
        if (manPossible && siege.getFortressLevel() == 2) {
            AbstractProvinceEntity province = provinceDao.getProvinceByName(siege.getProvince());
            int naturalFortress = oeUtil.getNaturalFortressLevel(province, siege.getGame());
            manPossible = naturalFortress == 0;
        }
        if (manPossible) {
            siege.setStatus(SiegeStatusEnum.CHOOSE_MAN);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.CHOOSE_MAN));
            return Collections.emptyList();
        } else {
            return endSiege(siege, attributes, false);
        }
    }

    /**
     * Called the the siege ends. Can be when the fortress falls, or when the besieger redeploy.
     *
     * @param siege      the siege.
     * @param attributes the attributes about the MODIFY SIEGE diff.
     * @param man        if the fortress is manned by the besieger.
     * @return the diffs involved.
     */
    private List<DiffEntity> endSiege(SiegeEntity siege, List<DiffAttributesEntity> attributes, boolean man) {
        GameEntity game = siege.getGame();
        String country = siege.getPhasing().getCountry();
        List<DiffEntity> diffs = new ArrayList<>();
        List<CounterEntity> presentCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(siege.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .collect(Collectors.toList());

        Consumer<CounterEntity> deleteCounter = counter -> diffs.add(counterDomain.removeCounter(counter));
        presentCounters.stream()
                .filter(HAS_SIEGEWORK)
                .forEach(deleteCounter);

        AbstractProvinceEntity province = provinceDao.getProvinceByName(siege.getProvince());
        List<String> enemies = oeUtil.getWarFaction(siege.getWar(), !siege.isBesiegingOffensive());
        String owner = oeUtil.getOwner(province, game);
        CounterEntity control = presentCounters.stream()
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.CONTROL)
                .findAny()
                .orElse(null);
        if (enemies.contains(owner)) {
            if (control != null) {
                diffs.add(counterDomain.changeCounterCountry(control, country, game));
            } else {
                diffs.add(counterDomain.createCounter(CounterFaceTypeEnum.CONTROL, country, siege.getProvince(), null, game));
            }
        } else {
            diffs.add(counterDomain.removeCounter(control));
        }
        // TODO TG-130 use case of praesidios
        CounterEntity fortress = presentCounters.stream()
                .filter(counter -> CounterUtil.isFortress(counter.getType()))
                .findAny()
                .orElse(null);
        int naturalFortressLevel = oeUtil.getNaturalFortressLevel(province, game);
        int levelLost = man ? 1 : 2;
        int level = siege.getFortressLevel() - levelLost;
        if (fortress != null) {
            // TODO TG-130 special fortresses
            CounterFaceTypeEnum newFortress = CounterUtil.getFortressesFromLevel(level, CounterUtil.isArsenal(fortress.getType()));
            diffs.add(counterDomain.removeCounter(fortress));
            if (level > naturalFortressLevel) {
                String newController;
                if (enemies.contains(owner)) {
                    newController = country;
                } else {
                    newController = owner;
                }
                diffs.add(counterDomain.createCounter(newFortress, newController, siege.getProvince(), null, game));
            } else if (level < naturalFortressLevel && level > 0) {
                diffs.add(counterDomain.createCounter(newFortress, null, siege.getProvince(), null, game));
            }
        } else if (naturalFortressLevel > 1) {
            // TODO TG-113 add a rule that remove these neutral fortress 1 counters at end of turn
            diffs.add(counterDomain.createCounter(CounterFaceTypeEnum.FORTRESS_1, null, siege.getProvince(), null, game));
        }
        List<String> allies = oeUtil.getWarFaction(siege.getWar(), siege.isBesiegingOffensive());
        List<StackEntity> phasingStacks = presentCounters.stream()
                .filter(counter -> allies.contains(counter.getCountry()) && CounterUtil.isArmy(counter.getType()))
                .map(CounterEntity::getOwner)
                .distinct()
                .collect(Collectors.toList());
        for (StackEntity phasingStack : phasingStacks) {
            phasingStack.setMovePhase(MovePhaseEnum.MOVED);
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, phasingStack.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED)));
        }

        diffs.addAll(cleanUpSiege(siege, attributes));

        return diffs;
    }

    /**
     * Clean up the siege when it is done.
     *
     * @param siege the siege to clean up.
     * @return the diffs.
     */
    private List<DiffEntity> cleanUpSiege(SiegeEntity siege, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        siege.setStatus(SiegeStatusEnum.DONE);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.DONE));

        List<Long> countersId = siege.getCounters().stream()
                .map(SiegeCounterEntity::getCounter)
                .collect(Collectors.toList());

        siege.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(siege.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersId.contains(counter.getId()))
                .map(CounterEntity::getOwner)
                .distinct()
                .forEach(stack -> {
                    String newStackController = oeUtil.getController(stack);
                    if (!StringUtils.equals(newStackController, stack.getCountry())) {
                        String newLeader = oeUtil.getLeader(stack, getTables(), getLeaderConditions(stack.getProvince()));
                        diffs.add(DiffUtil.createDiff(siege.getGame(), DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newStackController),
                                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, !StringUtils.equals(newLeader, stack.getLeader()))));
                        stack.setCountry(newStackController);
                        stack.setLeader(newLeader);
                    }
                });

        diffs.addAll(statusWorkflowDomain.endMilitaryPhase(siege.getGame()));
        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseMan(Request<ChooseManForSiegeRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_MAN)
                .setParams(METHOD_CHOOSE_MAN));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_MAN, PARAMETER_CHOOSE_MAN);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_CHOOSE_MAN, PARAMETER_CHOOSE_MAN, GameStatusEnum.MILITARY_SIEGES);

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
                .setName(PARAMETER_CHOOSE_MAN, PARAMETER_REQUEST)
                .setParams(METHOD_CHOOSE_MAN));

        SiegeEntity siege = game.getSieges().stream()
                .filter(bat -> bat.getStatus() == SiegeStatusEnum.CHOOSE_MAN)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(siege)
                .setCodeError(IConstantsServiceException.SIEGE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No siege of status {2} can be found.")
                .setName(PARAMETER_CHOOSE_MAN)
                .setParams(METHOD_CHOOSE_MAN, SiegeStatusEnum.CHOOSE_MAN.name()));

        List<DiffAttributesEntity> attributes = new ArrayList<>();
        List<DiffEntity> diffs = new ArrayList<>();

        if (request.getRequest().isMan()) {
            failIfNull(new AbstractService.CheckForThrow<>()
                    .setTest(request.getRequest().getIdCounter())
                    .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                    .setMsgFormat(MSG_MISSING_PARAMETER)
                    .setName(PARAMETER_CHOOSE_MAN, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                    .setParams(METHOD_CHOOSE_MAN));

            Long counterId = siege.getCounters().stream()
                    .filter(bc -> bc.isPhasing() && Objects.equals(request.getRequest().getIdCounter(), bc.getCounter()))
                    .map(SiegeCounterEntity::getCounter)
                    .findAny()
                    .orElse(null);
            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(count -> Objects.equals(counterId, count.getId()))
                    .findAny()
                    .orElse(null);

            failIfNull(new AbstractService.CheckForThrow<>()
                    .setTest(counter)
                    .setCodeError(IConstantsServiceException.BATTLE_LOSSES_INVALID_COUNTER)
                    .setMsgFormat("{1}: {0} The losses cannot involve the counter {2}.")
                    .setName(PARAMETER_CHOOSE_MAN, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                    .setParams(METHOD_CHOOSE_MAN, request.getRequest().getIdCounter()));

            double size = CounterUtil.getSizeFromType(counter.getType());
            failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                    .setTest(size < 1)
                    .setCodeError(IConstantsServiceException.BATTLE_LOSSES_TOO_BIG)
                    .setMsgFormat("{1}: {0} The counter {2} cannot take {3} losses because it cannot take more than {4}.")
                    .setName(PARAMETER_CHOOSE_MAN, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                    .setParams(METHOD_CHOOSE_MAN, request.getRequest().getIdCounter(), 1, size));

            if (size > 1) {
                List<CounterFaceTypeEnum> faces = new ArrayList<>();
                if (size == 2) {
                    faces.add(CounterUtil.getSize2FromType(counter.getType()));
                } else if (size == 4) {
                    faces.add(CounterUtil.getSize2FromType(counter.getType()));
                    faces.add(CounterUtil.getSize1FromType(counter.getType()));
                }
                faces.removeIf(o -> o == null);

                long newStackSize = siege.getCounters().stream()
                        .filter(SiegeCounterEntity::isPhasing)
                        .map(SiegeCounterEntity::getCounter)
                        .collect(Collectors.counting()) + faces.size() - 1;

                failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                        .setTest(newStackSize > 3)
                        .setCodeError(IConstantsServiceException.STACK_TOO_BIG)
                        .setMsgFormat("{1}: {0} The stack {2} is too big to add the counter (size: {3} / 3, force: {4} / 8}.")
                        .setName(PARAMETER_CHOOSE_MAN, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                        .setParams(METHOD_CHOOSE_MAN, "besieger", newStackSize, "N/A"));

                diffs.addAll(faces.stream()
                        .map(face -> counterDomain.createCounter(face, counter.getCountry(), counter.getOwner().getId(), game))
                        .collect(Collectors.toList()));
            }
            diffs.add(counterDomain.removeCounter(counter));
        }

        diffs.addAll(endSiege(siege, attributes, request.getRequest().isMan()));

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        diffs.add(diff);

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseBreach(Request<ChooseBreachForSiegeRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_BREACH)
                .setParams(METHOD_CHOOSE_BREACH));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_BREACH, PARAMETER_CHOOSE_BREACH);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_CHOOSE_BREACH, PARAMETER_CHOOSE_BREACH, GameStatusEnum.MILITARY_SIEGES);

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
                .setName(PARAMETER_CHOOSE_BREACH, PARAMETER_REQUEST)
                .setParams(METHOD_CHOOSE_BREACH));

        SiegeEntity siege = game.getSieges().stream()
                .filter(bat -> bat.getStatus() == SiegeStatusEnum.CHOOSE_BREACH)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(siege)
                .setCodeError(IConstantsServiceException.SIEGE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No siege of status {2} can be found.")
                .setName(PARAMETER_CHOOSE_BREACH)
                .setParams(METHOD_CHOOSE_BREACH, SiegeStatusEnum.CHOOSE_BREACH.name()));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest().getChoice())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_BREACH, PARAMETER_REQUEST, PARAMETER_CHOICE)
                .setParams(METHOD_CHOOSE_BREACH));

        boolean warHonorsImpossible = request.getRequest().getChoice() == ChooseBreachForSiegeRequest.ChoiceBreachEnum.WAR_HONORS && siege.getBonus() + siege.getUndermineDie() != 12;

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(warHonorsImpossible)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} War honors is not a possibility.")
                .setName(PARAMETER_CHOOSE_BREACH, PARAMETER_REQUEST, PARAMETER_CHOICE)
                .setParams(METHOD_CHOOSE_BREACH));

        List<DiffAttributesEntity> attributes = new ArrayList<>();
        List<DiffEntity> diffs = new ArrayList<>();

        switch (request.getRequest().getChoice()) {
            case BREACH:
                siege.setUndermineResult(SiegeUndermineResultEnum.BREACH_TAKEN);
                diffs.addAll(computeAssault(siege, country, true, attributes));
                break;
            case WAR_HONORS:
                siege.setUndermineResult(SiegeUndermineResultEnum.WAR_HONOUR);
                siege.setStatus(SiegeStatusEnum.REDEPLOY);
                attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.REDEPLOY));
                break;
            case NOTHING:
                siege.setUndermineResult(SiegeUndermineResultEnum.BREACH_NOT_TAKEN);
                diffs.addAll(cleanUpSiege(siege, attributes));
                break;
        }

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.SIEGE_UNDERMINE_RESULT, siege.getUndermineResult()));
        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        diffs.add(diff);

        return createDiffs(diffs, gameDiffs, request);
    }

    /**
     * Compute an assault, be it after a breach or not.
     *
     * @param siege      the siege.
     * @param country    the country doing the assault.
     * @param breach     if the fortress was just breached.
     * @param attributes the attributes about the MODIFY SIEGE diff.
     * @return the diffs involved.
     */
    private List<DiffEntity> computeAssault(SiegeEntity siege, PlayableCountryEntity country, boolean breach, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        attributes.addAll(fillSiegeModifiers(siege, breach));
        attributes.add(computeSequence(true, siege.getPhasing(), siege.getNonPhasing(), true, breach, siege.getGame()));
        attributes.add(computeSequence(true, siege.getNonPhasing(), siege.getPhasing(), false, breach, siege.getGame()));
        boolean phasingRouted = CommonUtil.subtract(siege.getPhasing().getMoral(), siege.getPhasing().getLosses().getMoraleLoss()) <= 0;
        boolean nonPhasingRouted = CommonUtil.subtract(siege.getNonPhasing().getMoral(), siege.getNonPhasing().getLosses().getMoraleLoss()) <= 0;
        if (!phasingRouted) {
            attributes.add(computeSequence(false, siege.getPhasing(), siege.getNonPhasing(), true, breach, siege.getGame()));
        }
        if (!nonPhasingRouted) {
            attributes.add(computeSequence(false, siege.getNonPhasing(), siege.getPhasing(), false, breach, siege.getGame()));
        }
        reduceBesiegerLosses(siege, breach);
        reduceBesiegingLosses(siege);


        boolean fortressRouted = CommonUtil.subtract(siege.getNonPhasing().getMoral(), siege.getNonPhasing().getLosses().getMoraleLoss()) <= 0;
        FortressResistance fortressResistance = getTables().getFortressResistances().stream()
                .filter(resistance -> resistance.isBreach() == breach && resistance.getFortress() == siege.getFortressLevel())
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "fortresssResistances", siege.getFortressLevel() + " - " + breach));
        boolean fortressDestroyed = siege.getNonPhasing().getLosses().isGreaterThanSize(siege.getNonPhasing().getSize() + fortressResistance.getSize());

        AbstractProvinceEntity province = provinceDao.getProvinceByName(siege.getProvince());
        if (province instanceof EuropeanProvinceEntity) {
            siege.getPhasing().getLosses().roundToClosestInteger();
            siege.getNonPhasing().getLosses().roundToClosestInteger();
        }
        diffs.addAll(checkLeaderDeaths(siege, true, province, attributes));
        diffs.addAll(checkLeaderDeaths(siege, false, province, attributes));
        boolean besiegerDestroyed = siege.getPhasing().getLosses().isGreaterThanSize(siege.getPhasing().getSize());

        if (!besiegerDestroyed && (fortressRouted || fortressDestroyed)) {
            diffs.addAll(fortressFalls(siege, country, attributes));
        } else {
            diffs.addAll(areLossesAuto(siege, country, attributes));
        }
        attributes.addAll(getLossesAttributes(siege));

        return diffs;
    }

    /**
     * Check if a specific side of a siege needs to check for leader death and then do it.
     *
     * @param siege     the siege.
     * @param phasing    the side we want to check.
     * @param province   the province where the battle is.
     * @param attributes the diff attributes of the battle modify diff event.
     * @return the diffs involved.
     */
    protected List<DiffEntity> checkLeaderDeaths(SiegeEntity siege, boolean phasing, AbstractProvinceEntity province, List<DiffAttributesEntity> attributes) {
        List<DiffEntity> diffs = new ArrayList<>();
        GameEntity game = siege.getGame();
        SiegeSideEntity side = phasing ? siege.getPhasing() : siege.getNonPhasing();
        CounterEntity counterLeader = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> StringUtils.equals(counter.getCode(), side.getLeader()))
                .findAny()
                .orElse(null);
        boolean needCheck = counterLeader != null;

        if (needCheck) {
            int die = oeUtil.rollDie(game, side.getCountry());
            side.setLeaderCheck(die);
            attributes.add(DiffUtil.createDiffAttributes(phasing ? DiffAttributeTypeEnum.PHASING_LEADER_CHECK : DiffAttributeTypeEnum.NON_PHASING_LEADER_CHECK, side.getLeaderCheck()));
            int modifier = 0;
            if (phasing && !siege.isFortressFalls() || !phasing && siege.isFortressFalls()) {
                modifier -= 1;
            }
            // only besieger can have the stack annihilated modifier
            if (phasing && side.getLosses().isGreaterThanSize(side.getSize())) {
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
     * @param siege the siege.
     * @return the diff attributes of the losses of both side of the siege.
     */
    private List<DiffAttributesEntity> getLossesAttributes(SiegeEntity siege) {
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS, siege.getNonPhasing().getLosses().getRoundLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS, siege.getNonPhasing().getLosses().getThirdLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS, siege.getNonPhasing().getLosses().getMoraleLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS, siege.getPhasing().getLosses().getRoundLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS, siege.getPhasing().getLosses().getThirdLoss()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS, siege.getPhasing().getLosses().getMoraleLoss()));
        return attributes;
    }

    /**
     * Fill the assault modifiers of a siege:
     * - fire bonus
     * - shock bonus
     * - size
     * - tech
     * - moral
     *
     * @param siege  the siege.
     * @param breach if the fortress was just breached.
     * @return the eventual attributes, if any.
     */
    protected List<DiffAttributesEntity> fillSiegeModifiers(SiegeEntity siege, boolean breach) {
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        siege.getPhasing().getModifiers().clear();
        siege.getNonPhasing().getModifiers().clear();

        List<Long> countersPhasingId = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isPhasing)
                .map(SiegeCounterEntity::getCounter)
                .collect(Collectors.toList());
        List<Long> countersNotPhasingId = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isNotPhasing)
                .map(SiegeCounterEntity::getCounter)
                .collect(Collectors.toList());

        List<CounterEntity> countersPhasing = siege.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(siege.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersPhasingId.contains(counter.getId()))
                .collect(Collectors.toList());
        List<CounterEntity> countersNotPhasing = siege.getGame().getStacks().stream()
                .filter(stack -> StringUtils.equals(siege.getProvince(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> countersNotPhasingId.contains(counter.getId()))
                .collect(Collectors.toList());

        siege.getPhasing().setSize(countersPhasing.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType()))));
        siege.getNonPhasing().setSize(countersNotPhasing.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType()))));

        String techPhasing = oeUtil.getTechnology(countersPhasing,
                true, getReferential(), getTables(), siege.getGame());
        siege.getPhasing().setTech(techPhasing);

        String techNotPhasing = oeUtil.getTechnology(Collections.singletonList(createFakeFortress(siege.getProvince(), siege.getFortressLevel(), siege.getGame())),
                true, getReferential(), getTables(), siege.getGame());
        siege.getNonPhasing().setTech(techNotPhasing);

        // TODO TG-131 tercios moral boost
        BattleTech battleTechPhasing = getTables().getBattleTechs().stream()
                .filter(bt -> StringUtils.equals(bt.getTechnologyFor(), techPhasing) && StringUtils.equals(bt.getTechnologyAgainst(), techNotPhasing))
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "battleTechs", techPhasing + " - " + techNotPhasing));
        if (battleTechPhasing.isMoralBonusVeteran() && oeUtil.isStackVeteran(countersPhasing)) {
            siege.getPhasing().setMoral(battleTechPhasing.getMoral() + 1);
        } else {
            siege.getPhasing().setMoral(battleTechPhasing.getMoral());
        }

        BattleTech battleTechNonPhasing = getTables().getBattleTechs().stream()
                .filter(bt -> StringUtils.equals(bt.getTechnologyFor(), techNotPhasing) && StringUtils.equals(bt.getTechnologyAgainst(), techPhasing))
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "battleTechs", techNotPhasing + " - " + techPhasing));
        if (battleTechNonPhasing.isMoralBonusVeteran()) {
            siege.getNonPhasing().setMoral(battleTechNonPhasing.getMoral() + 1);
        } else {
            siege.getNonPhasing().setMoral(battleTechNonPhasing.getMoral());
        }

        if (StringUtils.isEmpty(siege.getPhasing().getLeader())) {
            siege.getPhasing().setLeader(getReplacementLeader(siege.getPhasing().getCountry(), siege.getGame()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_LEADER, siege.getPhasing().getLeader()));
        }
        if (StringUtils.isEmpty(siege.getNonPhasing().getLeader())) {
            siege.getNonPhasing().setLeader(getReplacementLeader(siege.getNonPhasing().getCountry(), siege.getGame()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_LEADER, siege.getNonPhasing().getLeader()));
        }
        Leader phasingLeader = getTables().getLeader(siege.getPhasing().getLeader(), siege.getPhasing().getCountry());
        Leader notPhasingLeader = getTables().getLeader(siege.getNonPhasing().getLeader(), siege.getNonPhasing().getCountry());
        int fireMod = phasingLeader.getFire() - notPhasingLeader.getFire();
        int shockMod = phasingLeader.getShock() - notPhasingLeader.getShock();
        siege.getPhasing().getModifiers().addFire(fireMod);
        siege.getPhasing().getModifiers().addShock(shockMod);
        siege.getNonPhasing().getModifiers().addFire(-fireMod);
        siege.getNonPhasing().getModifiers().addShock(-shockMod);


        if (StringUtils.equals(techNotPhasing, Tech.MEDIEVAL)) {
            siege.getPhasing().getModifiers().addFireAndShock(1);
        } else if (!StringUtils.equals(techNotPhasing, Tech.RENAISSANCE)) {
            siege.getPhasing().getModifiers().addFireAndShock(-1);
        }

        if (!breach) {
            siege.getPhasing().getModifiers().addFireAndShock(-siege.getFortressLevel());
        }

        int artilleries = oeUtil.getArtilleryBonus(countersPhasing, getReferential(), getTables(), siege.getGame());
        int artilleryBonus = getTables().getArtillerySieges().stream()
                .filter(as -> as.getFortress() == siege.getFortressLevel() && as.getArtillery() <= artilleries)
                .map(ArtillerySiege::getBonus)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(0);
        siege.getPhasing().getModifiers().addFireAndShock(artilleryBonus);

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_SIZE, siege.getPhasing().getSize()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_TECH, siege.getPhasing().getTech()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_MORAL, siege.getPhasing().getMoral()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE_MOD, siege.getPhasing().getModifiers().getFireMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK_MOD, siege.getPhasing().getModifiers().getShockMod()));

        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_SIZE, siege.getNonPhasing().getSize()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_TECH, siege.getNonPhasing().getTech()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORAL, siege.getNonPhasing().getMoral()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD, siege.getNonPhasing().getModifiers().getFireMod()));
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD, siege.getNonPhasing().getModifiers().getShockMod()));

        return attributes;
    }

    /**
     * @param province the province.
     * @param game     the game.
     * @return a fake fortress counter owned by the controller of the province.
     */
    private CounterEntity createFakeFortress(String province, int level, GameEntity game) {
        AbstractProvinceEntity fullProvince = provinceDao.getProvinceByName(province);
        String controller = oeUtil.getController(fullProvince, game);
        CounterEntity fakeControlCounter = new CounterEntity();
        fakeControlCounter.setCountry(controller);
        fakeControlCounter.setType(CounterUtil.getFortressesFromLevel(level, false));
        return fakeControlCounter;
    }

    /**
     * Compute a sequence of assault for one side.
     *
     * @param fire     if it is the fire sequence of the assault.
     * @param active   the side doing the damage.
     * @param passive  the side receiving the damage.
     * @param besieger if the side doing the damage is the besieger.
     * @param breach   if the fortress was just breached.
     * @param game     the game.
     * @return the eventual attributes, if any.
     */
    private DiffAttributesEntity computeSequence(boolean fire, SiegeSideEntity active, SiegeSideEntity passive, boolean besieger, boolean breach, GameEntity game) {
        int modifier;
        Consumer<Integer> setDice;
        DiffAttributeTypeEnum type;
        if (fire) {
            modifier = active.getModifiers().getFireMod();
            setDice = die -> active.getModifiers().setFire(die);
            if (besieger) {
                type = DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE;
            } else {
                type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE;
            }
        } else {
            modifier = active.getModifiers().getShockMod();
            setDice = die -> active.getModifiers().setShock(die);
            if (besieger) {
                type = DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK;
            } else {
                type = DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK;
            }
        }

        // Only a besieging with medieval tech will not roll for fire
        if (!fire || !besieger || !StringUtils.equals(Tech.MEDIEVAL, active.getTech())) {
            Integer die = oeUtil.rollDie(game);
            setDice.accept(die);


            AbstractWithLoss phasingResult = getResult(die, modifier, fire, besieger, breach && !besieger);
            if (fire && besieger) {
                phasingResult = phasingResult.adjustToTech(active.getTech());
            }
            passive.getLosses().add(phasingResult);
            // losses attributes will be sent at the end of the main method

            return DiffUtil.createDiffAttributes(type, die);
        }
        return null;
    }

    /**
     * @param die      rolled.
     * @param modifier to the die.
     * @param fire     if it is the fire sequence.
     * @param besieger if it is the besieger side.
     * @param breach   if the fortress was just breached.
     * @return the result of a combat round for the given die, modifier and column.
     */
    private AssaultResult getResult(Integer die, Integer modifier, boolean fire, boolean besieger, boolean breach) {
        int min = getTables().getAssaultResults().stream()
                .filter(result -> result.isFire() == fire && result.isBesieger() == besieger && result.isBreach() == breach)
                .map(AssaultResult::getDice)
                .min(Comparator.naturalOrder())
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "assaultResults", "N/A"));
        int max = getTables().getAssaultResults().stream()
                .filter(result -> result.isFire() == fire && result.isBesieger() == besieger && result.isBreach() == breach)
                .map(AssaultResult::getDice)
                .max(Comparator.naturalOrder())
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "assaultResults", "N/A"));
        int modifiedDie = die + modifier < min ? min : die + modifier > max ? max : die + modifier;

        return getTables().getAssaultResults().stream()
                .filter(result -> result.isFire() == fire && result.isBesieger() == besieger && result.isBreach() == breach && modifiedDie == result.getDice())
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "assaultResults", "N/A" + " - " + modifiedDie));
    }

    /**
     * Reduce the losses because of the stack sizes.
     *
     * @param siege the siege.
     */
    private void reduceBesiegingLosses(SiegeEntity siege) {
        int thirds = siege.getNonPhasing().getLosses().getTotalThird();
        if (siege.getPhasing().getSize() <= 6) {
            thirds--;
        }
        if (siege.getPhasing().getSize() <= 4) {
            thirds--;
        }
        List<CounterEntity> besiegerCounters = siege.getCounters().stream()
                .filter(SiegeCounterEntity::isPhasing)
                .map(counter -> {
                    CounterEntity fakeCounter = new CounterEntity();
                    fakeCounter.setType(counter.getType());
                    fakeCounter.setCountry(counter.getCountry());
                    return fakeCounter;
                })
                .collect(Collectors.toList());
        if (!besiegerCounters.stream()
                .anyMatch(counter -> CounterUtil.isArmyCounter(counter.getType()))) {
            thirds -= 2;
        }
        if (oeUtil.getAssaultBonus(besiegerCounters, getTables(), siege.getGame())) {
            thirds += 2;
        }
        if (thirds < 0) {
            thirds = 0;
        }
        AbstractWithLossEntity finalLosses = AbstractWithLossEntity.create(thirds);
        siege.getNonPhasing().getLosses().setRoundLoss(finalLosses.getRoundLoss());
        siege.getNonPhasing().getLosses().setThirdLoss(finalLosses.getThirdLoss());
    }

    /**
     * Reduce the losses because of the stack sizes.
     *
     * @param siege  the siege.
     * @param breach if the fortress was just breached.
     */
    private void reduceBesiegerLosses(SiegeEntity siege, boolean breach) {
        FortressResistance fortressResistance = getTables().getFortressResistances().stream()
                .filter(resistance -> resistance.isBreach() == breach && resistance.getFortress() == siege.getFortressLevel())
                .findAny()
                .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_TABLE, MSG_MISSING_TABLE, "fortresssResistances", siege.getFortressLevel() + " - " + breach));
        double counterSize = siege.getNonPhasing().getSize();

        double size = 2 * fortressResistance.getSize() + counterSize;

        siege.getPhasing().getLosses().maxToSize(size);

        boolean routed = CommonUtil.subtract(siege.getPhasing().getMoral(), siege.getPhasing().getLosses().getMoraleLoss()) <= 0;
        if (routed) {
            siege.getPhasing().getLosses().add(AbstractWithLossEntity.create(2));
        }
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse redeploy(Request<RedeployRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_REDEPLOY)
                .setParams(METHOD_REDEPLOY));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_REDEPLOY, PARAMETER_REDEPLOY);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_SIEGES, METHOD_REDEPLOY, PARAMETER_REDEPLOY);

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
                .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST)
                .setParams(METHOD_REDEPLOY));

        SiegeEntity siege = game.getSieges().stream()
                .filter(bat -> bat.getStatus() == SiegeStatusEnum.REDEPLOY)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(siege)
                .setCodeError(IConstantsServiceException.SIEGE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No siege of status {2} can be found.")
                .setName(PARAMETER_REDEPLOY)
                .setParams(METHOD_REDEPLOY, SiegeStatusEnum.REDEPLOY.name()));

        boolean accessRight = oeUtil.isWarAlly(country, siege.getWar(), !siege.isBesiegingOffensive());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(accessRight)
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_REDEPLOY, country.getName(), siege.getNonPhasing().getCountry()));

        List<RedeployRequest.ProvinceRedeploy> redeploys = request.getRequest().getRedeploys();
        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(CollectionUtils.isEmpty(redeploys))
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY)
                .setParams(METHOD_REDEPLOY));

        List<DiffAttributesEntity> attributes = new ArrayList<>();
        List<DiffEntity> diffs = new ArrayList<>();
        boolean missingProvince = redeploys.stream()
                .anyMatch(redeploy -> StringUtils.isEmpty(redeploy.getProvince()));
        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(missingProvince)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat("{1} : {0} The province is mandatory.")
                .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_PROVINCE)
                .setParams(METHOD_REDEPLOY));
        Set<String> provinces = new HashSet<>();
        List<String> duplicateProvinces = redeploys.stream()
                .map(RedeployRequest.ProvinceRedeploy::getProvince)
                .filter(province -> !provinces.add(province))
                .collect(Collectors.toList());
        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(duplicateProvinces.isEmpty())
                .setCodeError(IConstantsServiceException.PROVINCE_REDEPLOY_TWICE)
                .setMsgFormat("{1} : {0} The province {2} has already been redeployed.")
                .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_PROVINCE)
                .setParams(METHOD_REDEPLOY, duplicateProvinces));

        Set<Long> idCounters = new HashSet<>();
        List<Long> duplicateCounters = redeploys.stream()
                .flatMap(redeploy -> redeploy.getUnits().stream())
                .map(RedeployRequest.Unit::getIdCounter)
                .filter(id -> !idCounters.add(id))
                .collect(Collectors.toList());
        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(duplicateCounters.isEmpty())
                .setCodeError(IConstantsServiceException.UNIT_CANT_REDEPLOY_TWICE)
                .setMsgFormat("{1} : {0} The counter {2} has already been redeployed.")
                .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_UNIT)
                .setParams(METHOD_REDEPLOY, duplicateCounters));

        for (RedeployRequest.ProvinceRedeploy redeploy : redeploys) {
            StackEntity stack = counterDomain.createStack(redeploy.getProvince(), country.getName(), game);
            AbstractProvinceEntity province = provinceDao.getProvinceByName(redeploy.getProvince());

            failIfNull(new AbstractService.CheckForThrow<>()
                    .setTest(province)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_PROVINCE)
                    .setParams(METHOD_REDEPLOY, redeploy.getProvince()));

            boolean canRedeploy = oeUtil.canRetreat(province, false, 0d, country, game);

            failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                    .setTest(canRedeploy)
                    .setCodeError(IConstantsServiceException.UNIT_CANT_REDEPLOY_PROVINCE)
                    .setMsgFormat("{1}: {0} Impossible to redeploy units in the province {2}.")
                    .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_PROVINCE)
                    .setParams(METHOD_CHOOSE_MODE, province.getName()));

            for (RedeployRequest.Unit unit : redeploy.getUnits()) {
                failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                        .setTest(unit.getIdCounter() == null && unit.getFace() != CounterFaceTypeEnum.LAND_DETACHMENT)
                        .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                        .setMsgFormat("{1} : {0} The id or a land detachment is mandatory.")
                        .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_UNIT)
                        .setParams(METHOD_REDEPLOY));

                if (unit.getIdCounter() == null) {
                    failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                            .setTest(siege.getUndermineResult() == SiegeUndermineResultEnum.WAR_HONOUR)
                            .setCodeError(IConstantsServiceException.GARRISON_CANT_REDEPLOY)
                            .setMsgFormat("{1} : {0} The garrison can only be redeployed if War Honor were given.")
                            .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_UNIT)
                            .setParams(METHOD_REDEPLOY));
                    AbstractProvinceEntity siegeProvince = provinceDao.getProvinceByName(siege.getProvince());
                    String controller = oeUtil.getController(siegeProvince, game);
                    failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                            .setTest(StringUtils.equals(controller, unit.getCountry()))
                            .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                            .setMsgFormat("{1} : {0} The country {2} is wrong.")
                            .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_UNIT, PARAMETER_COUNTRY)
                            .setParams(METHOD_REDEPLOY, unit.getCountry()));
                    diffs.add(counterDomain.createCounter(CounterFaceTypeEnum.LAND_DETACHMENT, unit.getCountry(), province.getName(), null, game));
                } else {
                    CounterEntity counter = game.getStacks().stream()
                            .filter(stac -> stac.isBesieged() && StringUtils.equals(stac.getProvince(), siege.getProvince()))
                            .flatMap(stac -> stac.getCounters().stream())
                            .filter(count -> Objects.equals(unit.getIdCounter(), count.getId()) && CounterUtil.isMobile(count.getType()))
                            .findAny()
                            .orElse(null);

                    failIfNull(new AbstractService.CheckForThrow<>()
                            .setTest(counter)
                            .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                            .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                            .setName(PARAMETER_REDEPLOY, PARAMETER_REQUEST, PARAMETER_REDEPLOY, PARAMETER_UNIT, PARAMETER_ID_COUNTER)
                            .setParams(METHOD_REDEPLOY, unit.getIdCounter()));

                    diffs.add(counterDomain.changeCounterOwner(counter, stack, game));
                }
            }
        }

        diffs.addAll(fortressFalls(siege, country, attributes));

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        diffs.add(diff);

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseLossesAfterAssault(Request<ChooseLossesRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CHOOSE_LOSSES, PARAMETER_CHOOSE_LOSSES);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_SIEGES, METHOD_CHOOSE_LOSSES, PARAMETER_CHOOSE_LOSSES);

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

        SiegeEntity siege = game.getSieges().stream()
                .filter(sieg -> sieg.getStatus() == SiegeStatusEnum.CHOOSE_LOSS)
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(siege)
                .setCodeError(IConstantsServiceException.SIEGE_STATUS_NONE)
                .setMsgFormat("{1}: {0} No siege of status {2} can be found.")
                .setName(PARAMETER_CHOOSE_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES, SiegeStatusEnum.CHOOSE_LOSS.name()));

        boolean playerPhasing = isPhasingPlayer(game, request.getGame().getIdCountry());
        boolean accessRight = oeUtil.isWarAlly(country, siege.getWar(),
                playerPhasing && siege.isBesiegingOffensive() || !playerPhasing && !siege.isBesiegingOffensive());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(accessRight)
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_CHOOSE_LOSSES, country.getName(),
                        playerPhasing ? siege.getPhasing().getCountry() : siege.getNonPhasing().getCountry()));

        boolean lossesAlreadyChosen = playerPhasing && BooleanUtils.isTrue(siege.getPhasing().isLossesSelected()) ||
                !playerPhasing && BooleanUtils.isTrue(siege.getNonPhasing().isLossesSelected());

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(lossesAlreadyChosen)
                .setCodeError(IConstantsServiceException.ACTION_ALREADY_DONE)
                .setMsgFormat("{1}: {0} The action {1} has already been done by the country or the side {2}.")
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_CHOOSE_LOSSES, METHOD_CHOOSE_LOSSES, playerPhasing ? "phasing" : "non phasing"));

        SiegeSideEntity side;
        if (playerPhasing) {
            side = siege.getPhasing();
        } else {
            side = siege.getNonPhasing();
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
                .setMsgFormat("{1}: {0} The losses taken {1} does not match the losses that should be taken {2}.")
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES, AbstractWithLossEntity.create(3 * roundLosses + thirdLosses).toString(), side.getLosses().toString()));

        AbstractProvinceEntity province = provinceDao.getProvinceByName(siege.getProvince());
        if (province instanceof EuropeanProvinceEntity) {
            boolean hasThird = request.getRequest().getLosses().stream().anyMatch(ul -> ul.getThirdLosses() > 0);

            failIfTrue(new CheckForThrow<Boolean>()
                    .setTest(hasThird)
                    .setCodeError(IConstantsServiceException.BATTLE_LOSSES_NO_THIRD)
                    .setMsgFormat("{1}: {0} The losses cannot involve third in an european province.")
                    .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                    .setParams(METHOD_CHOOSE_LOSSES));
        }

        List<DiffEntity> diffs = new ArrayList<>();
        List<DiffAttributesEntity> attributes = new ArrayList<>();
        long thirdBefore = siege.getCounters().stream()
                .filter(sc -> sc.isPhasing() == playerPhasing && CounterUtil.isExploration(sc.getType()))
                .count();
        int thirdDiff = 0;

        for (ChooseLossesRequest.UnitLoss loss : request.getRequest().getLosses()) {
            Long counterId = siege.getCounters().stream()
                    .filter(bc -> bc.isPhasing() == playerPhasing && Objects.equals(loss.getIdCounter(), bc.getCounter()))
                    .map(SiegeCounterEntity::getCounter)
                    .findAny()
                    .orElse(null);
            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
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
                diffs.add(counterDomain.removeCounter(counter));
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
                    diffs.add(counterDomain.removeCounter(counter));
                } else {
                    diffs.addAll(faces.stream()
                            .map(face -> counterDomain.createCounter(face, counter.getCountry(), counter.getOwner().getId(), game))
                            .collect(Collectors.toList()));
                    diffs.add(counterDomain.removeCounter(counter));
                }
            }
        }

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(thirdBefore + thirdDiff >= 3)
                .setCodeError(IConstantsServiceException.BATTLE_LOSSES_TOO_MANY_THIRD)
                .setMsgFormat("{1}: {0} The losses are invalid because it will result with too many thirds.")
                .setName(PARAMETER_CHOOSE_LOSSES, PARAMETER_REQUEST, PARAMETER_LOSSES)
                .setParams(METHOD_CHOOSE_LOSSES));

        if (playerPhasing) {
            siege.getPhasing().setLossesSelected(true);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_READY, true));
        } else {
            siege.getNonPhasing().setLossesSelected(true);
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_READY, true));
        }

        if (BooleanUtils.isTrue(siege.getPhasing().isLossesSelected()) && BooleanUtils.isTrue(siege.getNonPhasing().isLossesSelected())) {
            if (siege.isFortressFalls()) {
                diffs.addAll(isFortressMan(siege, country, attributes));
            } else {
                diffs.addAll(cleanUpSiege(siege, attributes));
            }
        }

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));
        diffs.add(diff);

        return createDiffs(diffs, gameDiffs, request);
    }
}
