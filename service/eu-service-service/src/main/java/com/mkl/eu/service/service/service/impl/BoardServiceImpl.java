package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.attrition.IAttritionDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionCounterEntity;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the Board Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class BoardServiceImpl extends AbstractService implements IBoardService {
    /** Counter Domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** Status workflow domain. */
    @Autowired
    private IStatusWorkflowDomain statusWorkflowDomain;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Country DAO. */
    @Autowired
    private ICountryDao countryDao;
    /** Attrition DAO. */
    @Autowired
    private IAttritionDao attritionDao;
    /** OeUtil. */
    @Autowired
    private IOEUtil oeUtil;

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(Request<MoveStackRequest> request) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_STACK)
                .setParams(METHOD_MOVE_STACK));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK, GameStatusEnum.MILITARY_MOVE);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST)
                .setParams(METHOD_MOVE_STACK));
        // TODO TG-2 authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        Long idStack = request.getRequest().getIdStack();
        String provinceTo = request.getRequest().getProvinceTo();

        failIfNull(new CheckForThrow<>()
                .setTest(idStack)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK));
        failIfEmpty(new CheckForThrow<String>()
                .setTest(provinceTo)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                .setParams(METHOD_MOVE_STACK));

        StackEntity stack = game.getStacks()
                .stream()
                .filter(x -> idStack.equals(x.getId()))
                .findFirst()
                .orElse(null);

        failIfNull(new CheckForThrow<>()
                .setTest(stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK, idStack));

        boolean isMobile = oeUtil.isMobile(stack);

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(isMobile)
                .setCodeError(IConstantsServiceException.STACK_NOT_MOBILE)
                .setMsgFormat("{1}: {0} {2} Stack is not mobile.")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK, idStack));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(stack.getMovePhase() != null && stack.getMovePhase().isMoved())
                .setCodeError(IConstantsServiceException.STACK_ALREADY_MOVED)
                .setMsgFormat("{1}: {0} {2} Stack has already moved.")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK, idStack));

        boolean firstMove = false;
        if (stack.getMovePhase() != MovePhaseEnum.IS_MOVING) {
            List<StackEntity> stacks = stackDao.getMovingStacks(game.getId());
            List<Long> idsStacks = stacks.stream().map(StackEntity::getId).collect(Collectors.toList());

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(idsStacks.isEmpty())
                    .setCodeError(IConstantsServiceException.OTHER_STACK_MOVING)
                    .setMsgFormat("{1}: {0} {2} can''t move because stacks {3} are currently moving.")
                    .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                    .setParams(METHOD_MOVE_STACK, idStack, idsStacks));

            firstMove = true;
        }

        AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);

        failIfNull(new CheckForThrow<>()
                .setTest(province)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                .setParams(METHOD_MOVE_STACK, provinceTo));

        AbstractProvinceEntity provinceFrom = provinceDao.getProvinceByName(stack.getProvince());
        boolean isNear = false;
        if (provinceFrom != null) {
            isNear = provinceFrom.getBorders().stream().filter(x -> province.getId().equals(x.getProvinceTo().getId())).findFirst().isPresent();
        }

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(isNear)
                .setCodeError(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR)
                .setMsgFormat(MSG_NOT_NEIGHBOR)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                .setParams(METHOD_MOVE_STACK, stack.getProvince(), provinceTo));

        List<String> allies = oeUtil.getAllies(country, game);
        List<String> enemies = oeUtil.getEnemies(country, game);
        String controller = oeUtil.getController(province, game);

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(allies.contains(controller) || enemies.contains(controller))
                .setCodeError(IConstantsServiceException.CANT_MOVE_PROVINCE)
                .setMsgFormat("{1}: {0} This stack can''t move in province {2} because it is controlled by {3}.")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                .setParams(METHOD_MOVE_STACK, provinceTo, controller));

        int movePoints = oeUtil.getMovePoints(provinceFrom, province, allies.contains(controller));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(movePoints > -1)
                .setCodeError(IConstantsServiceException.PROVINCES_NOT_NEIGHBOR)
                .setMsgFormat(MSG_NOT_NEIGHBOR)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                .setParams(METHOD_MOVE_STACK, stack.getProvince(), provinceTo));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(stack.getMove() + movePoints <= 12)
                .setCodeError(IConstantsServiceException.PROVINCE_TOO_FAR)
                .setMsgFormat("{1}: {0} This action needs too much move points {2} ({3}/{4}).")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO)
                .setParams(METHOD_MOVE_STACK, movePoints, stack.getMove(), 12));

        double allyForces = game.getStacks().stream()
                .filter(s -> !s.isBesieged() && StringUtils.equals(s.getProvince(), stack.getProvince()) && !idStack.equals(s.getId()))
                .flatMap(x -> x.getCounters().stream())
                .filter(counter -> allies.contains(counter.getCountry()))
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .collect(Collectors.summingDouble(value -> value));
        double enemyForces = game.getStacks().stream()
                .filter(s -> !s.isBesieged() && StringUtils.equals(s.getProvince(), stack.getProvince()))
                .flatMap(x -> x.getCounters().stream())
                .filter(counter -> enemies.contains(counter.getCountry()))
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .collect(Collectors.summingDouble(value -> value));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(allyForces >= enemyForces)
                .setCodeError(IConstantsServiceException.ENEMY_FORCES_NOT_PINNED)
                .setMsgFormat("{1}: {0} The province {2} has too much enemy forces ({3}/{4}).")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK, stack.getProvince(), allyForces, enemyForces));

        String controllerFrom = oeUtil.getController(provinceFrom, game);
        if (enemies.contains(controllerFrom) && !allies.contains(controller)) {
            int fortress = oeUtil.getFortressLevel(provinceFrom, game);

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(allyForces >= fortress)
                    .setCodeError(IConstantsServiceException.CANT_BREAK_SIEGE)
                    .setMsgFormat("{1}: {0} The province {2} siege can''t be abandoned ({3}/{4}).")
                    .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                    .setParams(METHOD_MOVE_STACK, stack.getProvince(), allyForces, fortress));
        }

        List<String> patrons = counterDao.getPatrons(stack.getCountry(), game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_MOVE_STACK, country.getName(), patrons));

        List<DiffEntity> diffs = new ArrayList<>();
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.STACK, idStack,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, stack.getProvince()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, provinceTo),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_POINTS, stack.getMove() + movePoints),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.IS_MOVING, firstMove)));

        AttritionEntity attrition = game.getAttritions().stream()
                .filter(attrit -> attrit.getStatus() == AttritionStatusEnum.ON_GOING)
                .findAny()
                .orElse(createAttrition(stack.getProvince(), stack.getCounters(), AttritionTypeEnum.MOVEMENT, game));
        if (!attrition.getProvinces().contains(provinceTo)) {
            attrition.getProvinces().add(provinceTo);
            if (!firstMove) {
                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ATTRITION, attrition.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, provinceTo)));
            }
        }
        if (firstMove) {
            List<DiffAttributesEntity> attributes = new ArrayList<>();
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TURN, attrition.getTurn()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, attrition.getType()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, attrition.getStatus()));
            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.SIZE, attrition.getSize()));

            attributes.addAll(attrition.getProvinces().stream()
                    .map(prov -> DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, prov))
                    .collect(Collectors.toList()));
            attributes.addAll(attrition.getCounters().stream()
                    .map(counter -> DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTER, counter.getCounter()))
                    .collect(Collectors.toList()));
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.ATTRITION, attrition.getId(),
                    attributes.toArray(new DiffAttributesEntity[attributes.size()])));
        }

        stack.setProvince(provinceTo);
        stack.setMove(stack.getMove() + movePoints);
        stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        gameDao.update(game, false);

        return createDiffs(diffs, gameDiffs, request);
    }

    /**
     * Creates an attrition in the given province with the given counters.
     *
     * @param province where the attrition occurs (or start for movement attrition).
     * @param counters list of counters involved in the attrition.
     * @param type     of the attrition (movement, supply, siege,...)
     * @param game     the game.
     * @return the persisted attrition.
     */
    private AttritionEntity createAttrition(String province, List<CounterEntity> counters, AttritionTypeEnum type, GameEntity game) {
        AttritionEntity attrition = new AttritionEntity();
        attrition.getProvinces().add(province);
        attrition.setTurn(game.getTurn());
        attrition.setStatus(AttritionStatusEnum.ON_GOING);
        attrition.setType(type);
        attrition.setSize(counters.stream()
                .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType()))));
        attrition.setGame(game);
        counters.forEach(counter -> {
            AttritionCounterEntity attritionCounter = new AttritionCounterEntity();
            attritionCounter.setCounter(counter.getId());
            attritionCounter.setAttrition(attrition);
            attritionCounter.setCountry(counter.getCountry());
            attritionCounter.setType(counter.getType());
            attritionCounter.setCode(counter.getCode());
            attrition.getCounters().add(attritionCounter);
        });

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */
        attritionDao.create(attrition);

        game.getAttritions().add(attrition);

        return attrition;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse takeStackControl(Request<TakeStackControlRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_TAKE_STACK_CONTROL)
                .setParams(METHOD_TAKE_STACK_CONTROL));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_TAKE_STACK_CONTROL, PARAMETER_TAKE_STACK_CONTROL);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_TAKE_STACK_CONTROL, PARAMETER_TAKE_STACK_CONTROL, GameStatusEnum.MILITARY_MOVE);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST)
                .setParams(METHOD_TAKE_STACK_CONTROL));
        // TODO TG-2 authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        Long idStack = request.getRequest().getIdStack();

        failIfNull(new CheckForThrow<>()
                .setTest(idStack)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_TAKE_STACK_CONTROL));

        StackEntity stack = game.getStacks().stream()
                .filter(x -> idStack.equals(x.getId()))
                .findFirst()
                .orElse(null);

        failIfNull(new CheckForThrow<>()
                .setTest(stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_TAKE_STACK_CONTROL, idStack));

        Long idLeader = request.getRequest().getIdLeader();
        Leader leader = stack.getCounters().stream()
                .filter(counter -> Objects.equals(counter.getId(), idLeader))
                .map(counter -> getTables().getLeader(counter.getCode(), counter.getCountry()))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(idLeader != null && leader == null)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_ID_LEADER)
                .setParams(METHOD_TAKE_STACK_CONTROL, idLeader));

        String newController = leader != null ? leader.getCountry() : request.getRequest().getCountry();
        String newLeader = leader != null ? leader.getCode() : null;
        boolean controllerChanged = !StringUtils.equals(newController, stack.getCountry());
        boolean leaderChanged = !StringUtils.equals(newLeader, stack.getLeader());

        failIfNull(new CheckForThrow<>()
                .setTest(newController)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(!controllerChanged && !leaderChanged)
                .setCodeError(IConstantsServiceException.STACK_ALREADY_CONTROLLED)
                .setMsgFormat("{1}: {0} Stack {2} is already controlled by {3} and led by {4}.")
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL, idStack, newController, newLeader));

        List<String> patrons = counterDao.getPatrons(newController, game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL, country.getName(), patrons));

        List<String> countries = oeUtil.getLeadingCountries(stack.getCounters());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(countries.contains(newController))
                .setCodeError(IConstantsServiceException.STACK_CONTROL_INVALID_COUNTRY)
                .setMsgFormat("{1}: {0} {3} has not enough presence to control the stack {2}. List of countries who can : {4}.")
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL, idStack, newController, countries));

        List<Leader> leaders = oeUtil.getLeaders(stack.getCounters(), getTables(), getLeaderConditions(stack.getProvince()));
        leaders.removeIf(l -> !StringUtils.equals(l.getCountry(), newController) || (leader != null && leader.getRank().compareTo(l.getRank()) <= 0));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(CollectionUtils.isEmpty(leaders))
                .setCodeError(IConstantsServiceException.STACK_CONTROL_LEADER_ISSUE)
                .setMsgFormat("{1}: {0} {3} cannot take control of the stack {2} with this leader. List of leaders : {4}.")
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_ID_LEADER)
                .setParams(METHOD_TAKE_STACK_CONTROL, idStack, newController, leaders.stream().map(Leader::getCode).collect(Collectors.toList())));

        stack.setCountry(newController);
        stack.setLeader(newLeader);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, idStack,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newController, controllerChanged),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, leaderChanged));

        gameDao.update(game, false);

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse endMoveStack(Request<EndMoveStackRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_END_MOVE_STACK)
                .setParams(METHOD_END_MOVE_STACK));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK, GameStatusEnum.MILITARY_MOVE);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST)
                .setParams(METHOD_END_MOVE_STACK));
        // TODO TG-2 authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        Long idStack = request.getRequest().getIdStack();

        failIfNull(new CheckForThrow<>()
                .setTest(idStack)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_END_MOVE_STACK));

        StackEntity stack = game.getStacks().stream()
                .filter(x -> idStack.equals(x.getId()))
                .findFirst()
                .orElse(null);

        failIfNull(new CheckForThrow<>()
                .setTest(stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_END_MOVE_STACK, idStack));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(stack.getMovePhase() == MovePhaseEnum.IS_MOVING)
                .setCodeError(IConstantsServiceException.STACK_NOT_MOVING)
                .setMsgFormat("{1}: {0} {2} Stack is not moving.")
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_END_MOVE_STACK, idStack));

        List<String> patrons = counterDao.getPatrons(stack.getCountry(), game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_END_MOVE_STACK, country.getName(), patrons));

        MovePhaseEnum movePhase;
        List<String> enemies = oeUtil.getEnemies(country, game);
        AbstractProvinceEntity province = provinceDao.getProvinceByName(stack.getProvince());
        String controller = oeUtil.getController(province, game);
        double enemyForces = game.getStacks().stream()
                .filter(s -> !s.isBesieged() && StringUtils.equals(s.getProvince(), stack.getProvince()))
                .flatMap(x -> x.getCounters().stream())
                .filter(counter -> enemies.contains(counter.getCountry()))
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .collect(Collectors.summingDouble(value -> value));
        if (enemyForces > 0) {
            // If stack is in the same province as a non besieged enemy stack, then the stack is fighting.
            movePhase = MovePhaseEnum.FIGHTING;
        } else if (enemies.contains(controller)) {
            // else if the stack is in an enemy province, then the stack is besieging.
            movePhase = MovePhaseEnum.BESIEGING;
        } else {
            // otherwise, the stack has just moved.
            movePhase = MovePhaseEnum.MOVED;
        }

        stack.setMovePhase(movePhase);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, idStack,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, movePhase));

        gameDao.update(game, false);

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(Request<MoveCounterRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_COUNTER)
                .setParams(METHOD_MOVE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER, GameStatusEnum.MILITARY_MOVE);

        // TODO TG-2 Authorization

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST)
                .setParams(METHOD_MOVE_COUNTER));

        Long idCounter = request.getRequest().getIdCounter();
        Long idStack = request.getRequest().getIdStack();
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new CheckForThrow<>()
                .setTest(idCounter)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_COUNTER));

        CounterEntity counter = counterDao.getCounter(idCounter, game.getId());

        failIfNull(new CheckForThrow<>()
                .setTest(counter)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_COUNTER, idCounter));

        StackEntity oldStack = counter.getOwner();
        List<String> patrons = counterDao.getPatrons(counter.getCountry(), game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_MOVE_COUNTER, country.getName(), patrons));

        Optional<StackEntity> stackOpt = null;
        if (idStack != null) {
            stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();
        }


        StackEntity stack;
        if (stackOpt != null && stackOpt.isPresent()) {
            stack = stackOpt.get();
        } else {
            stack = counterDomain.createStack(oldStack.getProvince(), counter.getCountry(), game);
        }

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(oldStack.getProvince(), stack.getProvince()))
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_PROVINCE)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_COUNTER, oldStack.getProvince(), stack.getProvince()));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(oldStack != stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_STACK)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_COUNTER));

        long futureNbCounters = stack.getCounters().stream()
                .filter(c -> CounterUtil.isArmy(c.getType()))
                .count() + (CounterUtil.isArmy(counter.getType()) ? 1 : 0);
        double futureSize = stack.getCounters().stream()
                .map(c -> CounterUtil.getSizeFromType(c.getType()))
                .collect(Collectors.summingDouble(value -> value));
        futureSize += CounterUtil.getSizeFromType(counter.getType());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(futureNbCounters <= 3 && futureSize <= 8)
                .setCodeError(IConstantsServiceException.STACK_TOO_BIG)
                .setMsgFormat("{1}: {0} The stack {2} is too big to add the counter (size: {3} / 3, force: {4} / 8}.")
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_COUNTER, stack.getId(), futureNbCounters, futureSize));

        List<DiffEntity> diffs = new ArrayList<>();

        diffs.add(counterDomain.changeCounterOwner(counter, stack, game));

        String newController = oeUtil.getController(oldStack);
        boolean changeController = !StringUtils.equals(newController, oldStack.getCountry());
        String newLeader = oeUtil.getLeader(oldStack, getTables(), getLeaderConditions(oldStack.getProvince()));
        boolean changeLeader = !StringUtils.equals(newLeader, oldStack.getLeader());
        if (changeController || changeLeader) {
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, oldStack.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newController, changeController),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, changeLeader)));
            oldStack.setCountry(newController);
            oldStack.setLeader(newLeader);
        }
        newController = oeUtil.getController(stack);
        changeController = !StringUtils.equals(newController, stack.getCountry());
        newLeader = oeUtil.getLeader(stack, getTables(), getLeaderConditions(stack.getProvince()));
        changeLeader = !StringUtils.equals(newLeader, stack.getLeader());
        if (changeController || changeLeader) {
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newController, changeController),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, changeLeader)));
            stack.setCountry(newController);
            stack.setLeader(newLeader);
        }

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveLeader(Request<MoveLeaderRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_LEADER)
                .setParams(METHOD_MOVE_LEADER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_MOVE_LEADER, PARAMETER_MOVE_LEADER);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, null, METHOD_MOVE_LEADER, PARAMETER_MOVE_LEADER, GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE, GameStatusEnum.MILITARY_HIERARCHY);

        // TODO TG-2 Authorization
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST)
                .setParams(METHOD_MOVE_LEADER));

        Long idCounter = request.getRequest().getIdCounter();
        Long idStack = request.getRequest().getIdStack();
        String province = request.getRequest().getProvince();
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new CheckForThrow<>()
                .setTest(idCounter)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER));

        CounterEntity counter = game.getStacks().stream()
                .flatMap(stack -> stack.getCounters().stream())
                .filter(c -> Objects.equals(c.getId(), idCounter))
                .findAny()
                .orElse(null);

        failIfNull(new CheckForThrow<>()
                .setTest(counter)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER, idCounter));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(CounterUtil.isLeader(counter.getType()))
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER, idCounter));

        StackEntity oldStack = counter.getOwner();

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(oldStack.isBesieged())
                .setCodeError(IConstantsServiceException.STACK_BESIEGED)
                .setMsgFormat("{1}: {0} Action is impossible because stack {2} is besieged.")
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER, oldStack.getId()));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(GameUtil.isRoundBox(oldStack.getProvince()))
                .setCodeError(IConstantsServiceException.LEADER_WOUNDED)
                .setMsgFormat("1}: {0} The leader {2} is wounded and cannot be deployed.")
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER, counter.getCode()));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(game.getStatus() == GameStatusEnum.MILITARY_HIERARCHY && !GameUtil.isTurnBox(oldStack.getProvince()))
                .setCodeError(IConstantsServiceException.LEADER_WAS_NOT_WOUNDED)
                .setMsgFormat("1}: {0} The leader {2} wsa not wounded last round, he is in {3}.")
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER, counter.getCode(), oldStack.getProvince()));

        List<String> patrons = counterDao.getPatrons(counter.getCountry(), game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER)
                .setParams(METHOD_MOVE_LEADER, country.getName(), patrons));

        StackEntity newStack = game.getStacks().stream()
                .filter(stack -> Objects.equals(stack.getId(), idStack))
                .findAny()
                .orElse(null);
        List<String> allies = oeUtil.getAllies(country, game);
        if (newStack != null) {
            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(allies.contains(newStack.getCountry()))
                    .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                    .setMsgFormat(MSG_ACCESS_RIGHT)
                    .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                    .setParams(METHOD_MOVE_LEADER, country.getName(), newStack.getCountry()));

            province = newStack.getProvince();
        }

        failIfEmpty(new CheckForThrow<String>()
                .setTest(province)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_MOVE_LEADER));

        AbstractProvinceEntity realProvince = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>()
                .setTest(realProvince)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_MOVE_LEADER, province));

        String controller = oeUtil.getController(realProvince, game);

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(allies.contains(controller))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_MOVE_LEADER, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_MOVE_LEADER, country.getName(), controller));

        List<DiffEntity> diffs = new ArrayList<>();
        diffs.addAll(counterDomain.moveLeader(counter, newStack, province, game));

        if (game.getStatus() == GameStatusEnum.MILITARY_HIERARCHY) {
            List<String> minors = counterDao.getMinors(country.getName(), game.getId());
            if (!game.getStacks().stream()
                    .filter(stack -> GameUtil.isTurnBox(stack.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .anyMatch(leader -> StringUtils.isNotEmpty(leader.getCode()) &&
                            (StringUtils.equals(leader.getCountry(), country.getName()) || minors.contains(leader.getCountry())))) {
                country.setReady(true);

                long countriesNotReady = game.getCountries().stream()
                        .filter(c -> StringUtils.isNotEmpty(c.getUsername()) && !c.isReady())
                        .count();

                if (countriesNotReady == 0) {
                    diffs.addAll(statusWorkflowDomain.endHierarchyPhase(game));
                } else {
                    DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.VALIDATE, DiffTypeObjectEnum.STATUS, country.getId(),
                            DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()));
                    diffs.add(diff);
                }
            }
        }

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateMilitaryRound(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_MIL_ROUND)
                .setParams(METHOD_VALIDATE_MIL_ROUND));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_VALIDATE_MIL_ROUND, PARAMETER_VALIDATE_MIL_ROUND);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, request.getGame().getIdCountry(), METHOD_VALIDATE_MIL_ROUND, PARAMETER_VALIDATE_MIL_ROUND, GameStatusEnum.MILITARY_MOVE);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getAuthent())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_AUTHENT)
                .setParams(METHOD_VALIDATE_MIL_ROUND));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_REQUEST)
                .setParams(METHOD_VALIDATE_MIL_ROUND));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getGame().getIdCountry())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_MIL_ROUND));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getGame().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_MIL_ROUND, request.getGame().getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_AUTHENT, PARAMETER_USERNAME)
                .setParams(METHOD_VALIDATE_MIL_ROUND, request.getAuthent().getUsername(), country.getUsername()));

        // TODO TG-6 check that no stack led by the country is currently moving

        CountryOrderEntity order = game.getOrders().stream()
                .filter(o -> o.isActive() && o.getCountry().getId().equals(country.getId()))
                .findFirst()
                .orElse(null);


        List<DiffEntity> newDiffs = new ArrayList<>();

        if (order != null && order.isReady() != request.getRequest().isValidate()) {
            order.setReady(request.getRequest().isValidate());

            long countriesNotReady = game.getOrders().stream()
                    .filter(o -> o.isActive() && !o.isReady())
                    .count();

            if (countriesNotReady == 0) {
                newDiffs.addAll(statusWorkflowDomain.endMilitaryPhase(game));
            } else {
                DiffTypeEnum type = DiffTypeEnum.INVALIDATE;
                if (request.getRequest().isValidate()) {
                    type = DiffTypeEnum.VALIDATE;
                }
                DiffEntity diff = DiffUtil.createDiff(game, type, DiffTypeObjectEnum.TURN_ORDER,
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, GameStatusEnum.MILITARY_MOVE),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()));

                newDiffs.add(diff);
            }
        }

        return createDiffs(newDiffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse createCounter(Request<CreateCounterRequest> request) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CREATE_COUNTER)
                .setParams(METHOD_CREATE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_CREATE_COUNTER, PARAMETER_CREATE_COUNTER);
        GameEntity game = gameDiffs.getGame();
        // TODO TG-2 authorization ADMIN

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST)
                .setParams(METHOD_CREATE_COUNTER));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getType()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_TYPE).setParams(METHOD_CREATE_COUNTER));
        failIfEmpty(new CheckForThrow<String>().setTest(request.getRequest().getCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_COUNTRY).setParams(METHOD_CREATE_COUNTER));

        CountryEntity country = countryDao.getCountryByName(request.getRequest().getCountry());

        failIfNull(new CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_CREATE_COUNTER, request.getRequest().getCountry()));

        failIfEmpty(new CheckForThrow<String>().setTest(request.getRequest().getProvince()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_CREATE_COUNTER));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(request.getRequest().getProvince());

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_PROVINCE).setParams(METHOD_CREATE_COUNTER, request.getRequest().getProvince()));


        DiffEntity diff;
        if (CounterUtil.isLeader(request.getRequest().getType())) {
            failIfEmpty(new CheckForThrow<String>()
                    .setTest(request.getRequest().getCode())
                    .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                    .setMsgFormat(MSG_MISSING_PARAMETER)
                    .setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_CODE)
                    .setParams(METHOD_CREATE_COUNTER));

            Leader leader = getTables().getLeader(request.getRequest().getCode(), request.getRequest().getCountry());

            failIfNull(new CheckForThrow<>()
                    .setTest(leader)
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_CODE)
                    .setParams(METHOD_CREATE_COUNTER, request.getRequest().getCode()));

            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(StringUtils.equals(leader.getCountry(), request.getRequest().getCountry()))
                    .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                    .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                    .setName(PARAMETER_CREATE_COUNTER, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                    .setParams(METHOD_CREATE_COUNTER, request.getRequest().getCountry()));

            diff = counterDomain.createLeader(request.getRequest().getType(), request.getRequest().getCode(), request.getRequest().getCountry(), null, request.getRequest().getProvince(), game);
        } else {
            diff = counterDomain.createCounter(request.getRequest().getType(), request.getRequest().getCountry(), request.getRequest().getProvince(), null, game);
        }
        gameDao.update(game, true);

        return createDiff(diff, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse removeCounter(Request<RemoveCounterRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_REMOVE_COUNTER)
                .setParams(METHOD_REMOVE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_REMOVE_COUNTER, PARAMETER_REMOVE_COUNTER);
        GameEntity game = gameDiffs.getGame();
        // TODO TG-2 authorization ADMIN

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_REMOVE_COUNTER, PARAMETER_REQUEST)
                .setParams(METHOD_REMOVE_COUNTER));

        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getIdCounter()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_REMOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_REMOVE_COUNTER));

        CounterEntity counter = game.getStacks().stream()
                .flatMap(s -> s.getCounters().stream())
                .filter(c -> c.getId().equals(request.getRequest().getIdCounter()))
                .findAny()
                .orElse(null);

        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_REMOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_REMOVE_COUNTER, request.getRequest().getIdCounter()));

        StackEntity stack = counter.getOwner();
        List<DiffEntity> diffs = new ArrayList<>();
        diffs.add(counterDomain.removeCounter(counter));
        if (stack.getGame() != null) {
            String newStackController = oeUtil.getController(stack);
            String newLeader = oeUtil.getLeader(stack, getTables(), getLeaderConditions(stack.getProvince()));
            boolean controllerChanged = !StringUtils.equals(newStackController, stack.getCountry());
            boolean leaderChanged = !StringUtils.equals(newLeader, stack.getLeader());
            if (controllerChanged || leaderChanged) {
                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, newStackController, controllerChanged),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.LEADER, newLeader, leaderChanged)));
                stack.setCountry(newStackController);
                stack.setLeader(newLeader);
            }
        }

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse initLeaders(Request<Void> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_INIT_LEADERS)
                .setParams(METHOD_INIT_LEADERS));

        // TODO TG-2 Check admin rights
        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_INIT_LEADERS, PARAMETER_INIT_LEADERS);

        GameEntity game = gameDiffs.getGame();

        List<DiffEntity> diffs = statusWorkflowDomain.deployLeaders(game);

        return createDiffs(diffs, gameDiffs, request);
    }
}
