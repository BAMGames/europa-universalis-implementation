package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.board.EndMoveStackRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.service.board.TakeStackControlRequest;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Board Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class BoardServiceImpl extends AbstractService implements IBoardService {
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

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST)
                .setParams(METHOD_MOVE_STACK));
        // TODO authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
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

        int allyForces = game.getStacks().stream()
                .filter(s -> !s.isBesieged() && StringUtils.equals(s.getProvince(), stack.getProvince()) && !idStack.equals(s.getId()))
                .flatMap(x -> x.getCounters().stream())
                .filter(counter -> allies.contains(counter.getCountry()))
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .collect(Collectors.summingInt(value -> value));
        int enemyForces = game.getStacks().stream()
                .filter(s -> !s.isBesieged() && StringUtils.equals(s.getProvince(), stack.getProvince()))
                .flatMap(x -> x.getCounters().stream())
                .filter(counter -> enemies.contains(counter.getCountry()))
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .collect(Collectors.summingInt(value -> value));

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

        checkCanManipulateObject(stack.getCountry(), country, game, METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MOVE);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        diff.setIdObject(idStack);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_FROM);
        diffAttributes.setValue(stack.getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_TO);
        diffAttributes.setValue(provinceTo);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.MOVE_POINTS);
        diffAttributes.setValue(Integer.toString(stack.getMove() + movePoints));
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        if (firstMove) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.MOVE_PHASE);
            diffAttributes.setValue(MovePhaseEnum.IS_MOVING.name());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);

            stack.setMovePhase(MovePhaseEnum.IS_MOVING);
        }

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        stack.setProvince(provinceTo);
        stack.setMove(stack.getMove() + movePoints);
        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
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

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_TAKE_STACK_CONTROL, PARAMETER_TAKE_STACK_CONTROL);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_TAKE_STACK_CONTROL, PARAMETER_TAKE_STACK_CONTROL);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST)
                .setParams(METHOD_TAKE_STACK_CONTROL));
        // TODO authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
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

        String newController = request.getRequest().getCountry();

        failIfNull(new CheckForThrow<>()
                .setTest(newController)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(newController, stack.getCountry()))
                .setCodeError(IConstantsServiceException.STACK_ALREADY_CONTROLLED)
                .setMsgFormat("{1}: {0} Stack {2} is already controlled by {3}.")
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL, idStack, newController));

        checkCanManipulateObject(newController, country, game, METHOD_TAKE_STACK_CONTROL, PARAMETER_TAKE_STACK_CONTROL);

        Map<String, Integer> countersByCountry = stack.getCounters().stream()
                .collect(Collectors.groupingBy(CounterEntity::getCountry, Collectors.summingInt(value -> CounterUtil.getSizeFromType(value.getType()))));
        int maxInStack = countersByCountry.values().stream()
                .max(Comparator.<Integer>naturalOrder())
                .orElse(1);

        int newControllerPresence = countersByCountry.get(newController) == null ? 0 : countersByCountry.get(newController);

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(newControllerPresence >= maxInStack)
                .setCodeError(IConstantsServiceException.STACK_CANT_CONTROL)
                .setMsgFormat("{1}: {0} {3} has not enough presence to control the stack {2} ({4}/{5}).")
                .setName(PARAMETER_TAKE_STACK_CONTROL, PARAMETER_REQUEST, PARAMETER_COUNTRY)
                .setParams(METHOD_TAKE_STACK_CONTROL, idStack, newController, newControllerPresence, maxInStack));

        stack.setCountry(newController);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        diff.setIdObject(idStack);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.COUNTRY);
        diffAttributes.setValue(newController);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
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

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST)
                .setParams(METHOD_END_MOVE_STACK));
        // TODO authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
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

        checkCanManipulateObject(stack.getCountry(), country, game, METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);

        MovePhaseEnum movePhase;
        List<String> enemies = oeUtil.getEnemies(country, game);
        AbstractProvinceEntity province = provinceDao.getProvinceByName(stack.getProvince());
        String controller = oeUtil.getController(province, game);
        int enemyForces = game.getStacks().stream()
                .filter(s -> !s.isBesieged() && StringUtils.equals(s.getProvince(), stack.getProvince()))
                .flatMap(x -> x.getCounters().stream())
                .filter(counter -> enemies.contains(counter.getCountry()))
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .collect(Collectors.summingInt(value -> value));
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

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        diff.setIdObject(idStack);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.MOVE_PHASE);
        diffAttributes.setValue(movePhase.name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(Request<MoveCounterRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_COUNTER)
                .setParams(METHOD_MOVE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);

        // TODO Authorization

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST)
                .setParams(METHOD_MOVE_COUNTER));

        Long idCounter = request.getRequest().getIdCounter();
        Long idStack = request.getRequest().getIdStack();
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
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
                .setParams(METHOD_MOVE_COUNTER, game.getId()));

        checkCanManipulateObject(counter.getCountry(), country, game, METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);

        Optional<StackEntity> stackOpt = null;
        if (idStack != null) {
            stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();
        }


        StackEntity stack;
        if (stackOpt != null && stackOpt.isPresent()) {
            stack = stackOpt.get();
        } else {
            stack = new StackEntity();
            stack.setProvince(counter.getOwner().getProvince());
            stack.setGame(game);
            stack.setCountry(counter.getCountry());

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

            stackDao.create(stack);

            game.getStacks().add(stack);
        }

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(counter.getOwner().getProvince(), stack.getProvince()))
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_PROVINCE)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_COUNTER, counter.getOwner().getProvince(), stack.getProvince()));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(counter.getOwner() != stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_STACK)
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_COUNTER));

        int futureNbCounters = stack.getCounters().size() + 1;
        int futureSize = stack.getCounters().stream()
                .map(c -> CounterUtil.getSizeFromType(c.getType()))
                .collect(Collectors.summingInt(value -> value));
        futureSize += CounterUtil.getSizeFromType(counter.getType());

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(futureNbCounters <= 3 && futureSize <= 8)
                .setCodeError(IConstantsServiceException.STACK_TOO_BIG)
                .setMsgFormat("{1}: {0} The stack {2} is too big to add the counter (size: {3} / 3, force: {4} / 8}.")
                .setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_COUNTER, stack.getId(), futureNbCounters, futureSize));

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MOVE);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(idCounter);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STACK_FROM);
        diffAttributes.setValue(counter.getOwner().getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STACK_TO);
        diffAttributes.setValue(stack.getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_FROM);
        diffAttributes.setValue(counter.getOwner().getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_TO);
        diffAttributes.setValue(stack.getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        if (counter.getOwner().getCounters().size() == 1) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.STACK_DEL);
            diffAttributes.setValue(counter.getOwner().getId().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
        }

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        StackEntity oldStack = counter.getOwner();
        counter.setOwner(stack);
        oldStack.getCounters().remove(counter);
        stack.getCounters().add(counter);
        if (oldStack.getCounters().isEmpty()) {
            oldStack.setGame(null);
            game.getStacks().remove(oldStack);
        }

        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /**
     * Throws an exception if the country has no right to manipulate an object owned by countryName.
     *
     * @param countryName real owner of the object. Can be a minor country in which case country must be a patron of countryName.
     * @param country     country asking to manipulate the object.
     * @param game        the game.
     * @param method      calling this. For logging purpose.
     * @param param       name of the param holding the gameInfo. For logging purpose.
     * @throws FunctionalException
     */
    private void checkCanManipulateObject(String countryName, PlayableCountryEntity country, GameEntity game, String method, String param) throws FunctionalException {
        PlayableCountryEntity owner = game.getCountries().stream()
                .filter(x -> StringUtils.equals(countryName, x.getName()))
                .findFirst()
                .orElse(null);
        if (owner != null) {
            failIfFalse(new CheckForThrow<Boolean>()
                    .setTest(country.getId().equals(owner.getId()))
                    .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                    .setMsgFormat(MSG_ACCESS_RIGHT)
                    .setName(param, PARAMETER_ID_COUNTRY)
                    .setParams(method, country.getName(), countryName));

        } else {
            List<String> patrons = counterDao.getPatrons(countryName, game.getId());
            if (patrons.size() == 1) {
                owner = game.getCountries().stream()
                        .filter(x -> StringUtils.equals(patrons.get(0), x.getName()))
                        .findFirst()
                        .orElse(null);
                boolean ok = owner != null && country.getId().equals(owner.getId());
                failIfFalse(new CheckForThrow<Boolean>()
                        .setTest(ok)
                        .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                        .setMsgFormat(MSG_ACCESS_RIGHT)
                        .setName(param, PARAMETER_ID_COUNTRY)
                        .setParams(method, country.getName(), patrons.get(0)));

            } else {
                // TODO manage minor countries in war with no or multiple patrons
                // If minor at war with no patron, creation of a fake playable country
                // so only multiple patrons use case remains
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateMilitaryRound(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_MIL_ROUND)
                .setParams(METHOD_VALIDATE_MIL_ROUND));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_VALIDATE_MIL_ROUND, PARAMETER_VALIDATE_MIL_ROUND);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_VALIDATE_MIL_ROUND, PARAMETER_VALIDATE_MIL_ROUND);

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
                .setTest(request.getIdCountry())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_MIL_ROUND));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_MIL_ROUND, request.getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_AUTHENT, PARAMETER_USERNAME)
                .setParams(METHOD_VALIDATE_MIL_ROUND, request.getAuthent().getUsername(), country.getUsername()));

        CountryOrderEntity order = game.getOrders().stream()
                .filter(o -> o.isActive() && o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.getCountry().getId().equals(country.getId()))
                .findFirst()
                .orElse(null);


        List<DiffEntity> newDiffs = new ArrayList<>();

        if (order != null && order.isReady() != request.getRequest().isValidate()) {
            order.setReady(request.getRequest().isValidate());

            long countriesNotReady = game.getOrders().stream()
                    .filter(o -> o.isActive() &&
                            o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                            !o.isReady())
                    .count();

            if (countriesNotReady == 0) {
                List<String> provincesAtWar = game.getStacks().stream()
                        .filter(s -> s.getMovePhase() == MovePhaseEnum.FIGHTING)
                        .map(StackEntity::getProvince)
                        .collect(Collectors.toList());

                // Are there somme battles ?
                if (!provincesAtWar.isEmpty()) {
                    // Yes -> battle phase !
                    game.setStatus(GameStatusEnum.MILITARY_BATTLES);

                    DiffEntity diff = new DiffEntity();
                    diff.setIdGame(game.getId());
                    diff.setVersionGame(game.getVersion());
                    diff.setType(DiffTypeEnum.MODIFY);
                    diff.setTypeObject(DiffTypeObjectEnum.STATUS);
                    DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
                    diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
                    diffAttributes.setValue(GameStatusEnum.MILITARY_BATTLES.name());
                    diffAttributes.setDiff(diff);
                    diff.getAttributes().add(diffAttributes);
                    newDiffs.add(diff);

                    for (String province : provincesAtWar) {
                        BattleEntity battle = new BattleEntity();
                        battle.setProvince(province);
                        battle.setTurn(game.getTurn());
                        battle.setStatus(BattleStatusEnum.NEW);
                        battle.setGame(game);

                        game.getBattles().add(battle);
                    }

                    diff = new DiffEntity();
                    diff.setIdGame(game.getId());
                    diff.setVersionGame(game.getVersion());
                    diff.setType(DiffTypeEnum.INVALIDATE);
                    diff.setTypeObject(DiffTypeObjectEnum.BATTLE);
                    diffAttributes = new DiffAttributesEntity();
                    diffAttributes.setType(DiffAttributeTypeEnum.TURN);
                    diffAttributes.setValue(game.getTurn().toString());
                    diffAttributes.setDiff(diff);
                    diff.getAttributes().add(diffAttributes);
                    newDiffs.add(diff);
                } else {
                    // No -> next round
                    newDiffs.addAll(endMilitaryPhase(game, order.getPosition()));
                }
            } else {
                DiffEntity diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                if (request.getRequest().isValidate()) {
                    diff.setType(DiffTypeEnum.VALIDATE);
                } else {
                    diff.setType(DiffTypeEnum.INVALIDATE);
                }
                diff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
                diff.setIdObject(null);
                DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
                diffAttributes.setValue(GameStatusEnum.MILITARY_MOVE.name());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.ID_COUNTRY);
                diffAttributes.setValue(country.getId().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);

                newDiffs.add(diff);
            }
        }

        createDiffs(newDiffs);
        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.addAll(newDiffs);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /**
     * Method to call at the end of the military phase of a group of playing countries.
     *
     * @param game              the game.
     * @param currentOrderIndex the index in the order of the active countries.
     * @return the diff corresponding to the switch of the next round (or phase).
     */
    private List<DiffEntity> endMilitaryPhase(GameEntity game, int currentOrderIndex) {
        List<DiffEntity> diffs = new ArrayList<>();
        // Is it the last country of the round ?
        Integer next = game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.getPosition() > currentOrderIndex)
                .map(CountryOrderEntity::getPosition)
                .min(Comparator.<Integer>naturalOrder())
                .orElse(null);

        if (next != null) {
            // No it isn't, proceed to next countries.
            game.getOrders().stream()
                    .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                    .forEach(o -> o.setReady(false));

            DiffEntity diff = new DiffEntity();
            diff.setIdGame(game.getId());
            diff.setVersionGame(game.getVersion());
            diff.setType(DiffTypeEnum.INVALIDATE);
            diff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
            diff.setIdObject(null);
            DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
            diffAttributes.setValue(GameStatusEnum.MILITARY_MOVE.name());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);

            diffs.add(diff);

            game.getOrders().stream()
                    .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE)
                    .forEach(o -> o.setActive(false));
            game.getOrders().stream()
                    .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                            o.getPosition() == next)
                    .forEach(o -> o.setActive(true));

            diff = new DiffEntity();
            diff.setIdGame(game.getId());
            diff.setVersionGame(game.getVersion());
            diff.setType(DiffTypeEnum.MODIFY);
            diff.setTypeObject(DiffTypeObjectEnum.TURN_ORDER);
            diff.setIdObject(null);
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.ACTIVE);
            diffAttributes.setValue(next.toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);

            diffs.add(diff);
        } else {
            // Yes it is, are there some sieges ?
            List<String> provincesAtSiege = game.getStacks().stream()
                    .filter(s -> s.getMovePhase() == MovePhaseEnum.BESIEGING || s.getMovePhase() == MovePhaseEnum.STILL_BESIEGING)
                    .map(StackEntity::getProvince)
                    .collect(Collectors.toList());

            if (!provincesAtSiege.isEmpty()) {
                // Yes -> siege phase !
                game.setStatus(GameStatusEnum.MILITARY_SIEGES);

                DiffEntity diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                diff.setType(DiffTypeEnum.MODIFY);
                diff.setTypeObject(DiffTypeObjectEnum.STATUS);
                DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.STATUS);
                diffAttributes.setValue(GameStatusEnum.MILITARY_SIEGES.name());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                diffs.add(diff);


                for (String province : provincesAtSiege) {
                    SiegeEntity siege = new SiegeEntity();
                    siege.setProvince(province);
                    siege.setTurn(game.getTurn());
                    siege.setGame(game);

                    game.getSieges().add(siege);
                }

                diff = new DiffEntity();
                diff.setIdGame(game.getId());
                diff.setVersionGame(game.getVersion());
                diff.setType(DiffTypeEnum.INVALIDATE);
                diff.setTypeObject(DiffTypeObjectEnum.SIEGE);
                diffAttributes = new DiffAttributesEntity();
                diffAttributes.setType(DiffAttributeTypeEnum.TURN);
                diffAttributes.setValue(game.getTurn().toString());
                diffAttributes.setDiff(diff);
                diff.getAttributes().add(diffAttributes);
                diffs.add(diff);
            } else {
                // No -> next round !

                diffs.addAll(statusWorkflowDomain.nextRound(game));
            }
        }

        return diffs;
    }
}
