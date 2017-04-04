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
import com.mkl.eu.client.service.service.common.ValidateRequest;
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
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
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
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK).setParams(METHOD_MOVE_STACK));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST).setParams(METHOD_MOVE_STACK));
        // TODO authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        Long idStack = request.getRequest().getIdStack();
        String provinceTo = request.getRequest().getProvinceTo();

        failIfNull(new CheckForThrow<>().setTest(idStack).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_STACK));
        failIfEmpty(new CheckForThrow<String>().setTest(provinceTo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK));

        Optional<StackEntity> stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();

        failIfFalse(new CheckForThrow<Boolean>().setTest(stackOpt.isPresent()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_STACK, idStack));

        StackEntity stack = stackOpt.get();

        boolean isMobile = oeUtil.isMobile(stack);

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(isMobile)
                .setCodeError(IConstantsServiceException.STACK_NOT_MOBILE)
                .setMsgFormat("{1}: {0} {2} Stack is not mobile.")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK, idStack));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(stack.getMovePhase() == MovePhaseEnum.MOVED)
                .setCodeError(IConstantsServiceException.STACK_ALREADY_MOVED)
                .setMsgFormat("{1}: {0} {2} Stack has already moved.")
                .setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_MOVE_STACK, idStack));

        boolean firstMove = false;
        if (stack.getMovePhase() == MovePhaseEnum.NOT_MOVED || stack.getMovePhase() == null) {
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

        failIfNull(new CheckForThrow<>().setTest(province).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK, provinceTo));

        AbstractProvinceEntity provinceStack = provinceDao.getProvinceByName(stack.getProvince());
        boolean isNear = false;
        if (provinceStack != null) {
            isNear = provinceStack.getBorders().stream().filter(x -> province.getId().equals(x.getProvinceTo().getId())).findFirst().isPresent();
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(isNear).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_NEIGHBOR).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK, provinceTo, stack.getProvince()));

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
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_END_MOVE_STACK).setParams(METHOD_END_MOVE_STACK));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST).setParams(METHOD_END_MOVE_STACK));
        // TODO authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        Long idStack = request.getRequest().getIdStack();

        failIfNull(new CheckForThrow<>().setTest(idStack).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_END_MOVE_STACK));

        StackEntity stack = game.getStacks().stream()
                .filter(x -> idStack.equals(x.getId()))
                .findFirst()
                .orElse(null);

        failIfTrue(new CheckForThrow<Boolean>().setTest(stack == null).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_END_MOVE_STACK, idStack));

        failIfFalse(new CheckForThrow<Boolean>().setTest(stack.getMovePhase() == MovePhaseEnum.IS_MOVING)
                .setCodeError(IConstantsServiceException.STACK_NOT_MOVING)
                .setMsgFormat("{1}: {0} {2} Stack is not moving.")
                .setName(PARAMETER_END_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_END_MOVE_STACK, idStack));

        checkCanManipulateObject(stack.getCountry(), country, game, METHOD_END_MOVE_STACK, PARAMETER_END_MOVE_STACK);

        stack.setMovePhase(MovePhaseEnum.MOVED);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MODIFY);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        diff.setIdObject(idStack);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.MOVE_PHASE);
        diffAttributes.setValue(MovePhaseEnum.MOVED.name());
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
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER).setParams(METHOD_MOVE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);
        GameEntity game = gameDiffs.getGame();
        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);

        // TODO Authorization

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST).setParams(METHOD_MOVE_COUNTER));

        Long idCounter = request.getRequest().getIdCounter();
        Long idStack = request.getRequest().getIdStack();
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfNull(new CheckForThrow<>().setTest(idCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER));

        CounterEntity counter = counterDao.getCounter(idCounter, game.getId());

        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER, game.getId()));

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

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

            stackDao.create(stack);

            game.getStacks().add(stack);
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(counter.getOwner().getProvince(), stack.getProvince())).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_PROVINCE).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_COUNTER, counter.getOwner().getProvince(), stack.getProvince()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(counter.getOwner() != stack).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_STACK).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_COUNTER));

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
            failIfFalse(new CheckForThrow<Boolean>().setTest(country.getId().equals(owner.getId()))
                    .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                    .setMsgFormat(MSG_ACCESS_RIGHT).setName(param, PARAMETER_ID_COUNTRY).setParams(method, country.getName(), countryName));

        } else {
            List<String> patrons = counterDao.getPatrons(countryName, game.getId());
            if (patrons.size() == 1) {
                owner = game.getCountries().stream()
                        .filter(x -> StringUtils.equals(patrons.get(0), x.getName()))
                        .findFirst()
                        .orElse(null);
                boolean ok = owner != null && country.getId().equals(owner.getId());
                failIfFalse(new CheckForThrow<Boolean>().setTest(ok)
                        .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                        .setMsgFormat(MSG_ACCESS_RIGHT).setName(param, PARAMETER_ID_COUNTRY).setParams(method, country.getName(), countryName));

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
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_MIL_ROUND).setParams(METHOD_VALIDATE_MIL_ROUND));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_VALIDATE_MIL_ROUND, PARAMETER_VALIDATE_MIL_ROUND);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.MILITARY_MOVE, request.getIdCountry(), METHOD_VALIDATE_MIL_ROUND, PARAMETER_VALIDATE_MIL_ROUND);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_AUTHENT).setParams(METHOD_VALIDATE_MIL_ROUND));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_REQUEST).setParams(METHOD_VALIDATE_MIL_ROUND));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_ID_COUNTRY).setParams(METHOD_VALIDATE_MIL_ROUND));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_ID_COUNTRY).setParams(METHOD_VALIDATE_MIL_ROUND, request.getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_VALIDATE_MIL_ROUND, PARAMETER_AUTHENT, PARAMETER_USERNAME).setParams(METHOD_VALIDATE_MIL_ROUND, request.getAuthent().getUsername(), country.getUsername()));

        CountryOrderEntity order = game.getOrders().stream()
                .filter(o -> o.isActive() && o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.getCountry().getId().equals(country.getId()))
                .findFirst()
                .orElse(null);


        List<DiffEntity> diffs = gameDiffs.getDiffs();

        if (order != null && order.isReady() != request.getRequest().isValidate()) {
            order.setReady(request.getRequest().isValidate());

            long countriesNotReady = game.getOrders().stream()
                    .filter(o -> o.isActive() &&
                            o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                            !o.isReady())
                    .count();

            if (countriesNotReady == 0) {
                // Is it the last country of the round ?
                Integer next = game.getOrders().stream()
                        .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                                o.getPosition() > order.getPosition())
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

                    diffDao.create(diff);
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

                    diffDao.create(diff);
                    diffs.add(diff);
                } else {
                    // Yes it is, proceed to next round.
                    diffs.addAll(statusWorkflowDomain.nextRound(game));
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

                diffDao.create(diff);
                diffs.add(diff);
            }
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }
}
