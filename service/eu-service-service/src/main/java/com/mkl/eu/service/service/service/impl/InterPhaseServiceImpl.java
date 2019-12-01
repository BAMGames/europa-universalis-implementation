package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.eco.ExchequerRepartitionRequest;
import com.mkl.eu.client.service.service.eco.ImproveStabilityRequest;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.service.military.LandRedeployRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.EconomicUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RegionEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of IInterPhaseService.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class InterPhaseServiceImpl extends AbstractService implements IInterPhaseService {
    /** Counter Domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** Status workflow domain. */
    @Autowired
    private IStatusWorkflowDomain statusWorkflowDomain;
    /** OeUtil. */
    @Autowired
    private IOEUtil oeUtil;
    /** Province Dao. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Counter Dao. */
    @Autowired
    private ICounterDao counterDao;

    /** {@inheritDoc} */
    @Override
    public DiffResponse landLooting(Request<LandLootingRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_LAND_LOOTING)
                .setParams(METHOD_LAND_LOOTING));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_LAND_LOOTING, PARAMETER_LAND_LOOTING);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.REDEPLOYMENT, request.getGame().getIdCountry(), METHOD_LAND_LOOTING, PARAMETER_LAND_LOOTING);

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
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST)
                .setParams(METHOD_LAND_LOOTING));
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest().getType())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_TYPE)
                .setParams(METHOD_LAND_LOOTING));
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest().getIdStack())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING));
        StackEntity stack = game.getStacks().stream()
                .filter(s -> Objects.equals(request.getRequest().getIdStack(), s.getId()))
                .findAny()
                .orElse(null);
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, request.getRequest().getIdStack()));
        boolean canLootWithStack = oeUtil.isMobile(stack);
        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(canLootWithStack)
                .setCodeError(IConstantsServiceException.LAND_LOOTING_INVALID_STACK)
                .setMsgFormat("{1}: {0} The stack of id {2} cannot loot.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, request.getRequest().getIdStack()));
        AbstractProvinceEntity province = provinceDao.getProvinceByName(stack.getProvince());

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(stack.getMovePhase() == MovePhaseEnum.LOOTING)
                .setCodeError(IConstantsServiceException.LAND_LOOTING_TWICE)
                .setMsgFormat("{1}: {0} The stack of id {2} has already looted.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, stack.getId()));

        List<String> patrons = counterDao.getPatrons(stack.getCountry(), game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, country.getName(), patrons));

        List<String> enemies = oeUtil.getEnemies(country, game);
        String owner = oeUtil.getOwner(province, game);

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(enemies.contains(owner))
                .setCodeError(IConstantsServiceException.LAND_LOOTING_NOT_ENEMY)
                .setMsgFormat("{1}: {0} You must loot a province that is owned by one of your enemy. Current owner of {2} is {3}.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, province.getName(), owner));

        List<DiffEntity> diffs;
        switch (request.getRequest().getType()) {
            case PILLAGE:
                diffs = pillageLand(province, country, stack, game);
                break;
            case BURN_TP:
                diffs = burnTradingPost(province, country, game);
                break;
            default:
                diffs = new ArrayList<>();
        }
        if (stack.getMovePhase() != MovePhaseEnum.LOOTING_BESIEGING) {
            stack.setMovePhase(MovePhaseEnum.LOOTING);
        }
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, stack.getMovePhase())));

        return createDiffs(diffs, gameDiffs, request);
    }

    /**
     * Pillage a land province.
     *
     * @param province     the province to be pillaged.
     * @param country      the country pillaging the province.
     * @param pillageStack the stack pillaging.
     * @param game         the game.
     * @return the diffs involved.
     * @throws FunctionalException functional exception.
     */
    private List<DiffEntity> pillageLand(AbstractProvinceEntity province, PlayableCountryEntity country, StackEntity pillageStack, GameEntity game) throws FunctionalException {
        List<String> allies = oeUtil.getAllies(country, game);
        String controller = oeUtil.getController(province, game);

        boolean canLoot = allies.contains(controller);
        if (!canLoot) {
            Double size = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(province.getName(), stack.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(counter -> allies.contains(counter.getCountry()))
                    .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType())));
            canLoot = size >= oeUtil.getFortressLevel(province, game);
            pillageStack.setMovePhase(MovePhaseEnum.LOOTING_BESIEGING);
        }

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(canLoot)
                .setCodeError(IConstantsServiceException.LAND_LOOTING_INSUFFICIENT_FORCES)
                .setMsgFormat("{1}: {0} You do not has enough forces to loot province {2}.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, province.getName()));

        List<CounterEntity> existingPillages = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(province.getName(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.PILLAGE_MINUS || counter.getType() == CounterFaceTypeEnum.PILLAGE_PLUS)
                .collect(Collectors.toList());
        Long idPillageStack = existingPillages.stream()
                .map(counter -> counter.getOwner().getId())
                .findAny()
                .orElse(null);

        List<DiffEntity> diffs = new ArrayList<>();
        if (StringUtils.equals(pillageStack.getCountry(), country.getName())) {
            int landIncome = 0;
            if (province instanceof EuropeanProvinceEntity) {
                landIncome = ((EuropeanProvinceEntity) province).getIncome();
            } else if (province instanceof RotwProvinceEntity) {
                RegionEntity region = provinceDao.getRegionByName(((RotwProvinceEntity) province).getRegion());
                boolean hasArmy = game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(country.getName(), stack.getCountry()) && StringUtils.equals(province.getName(), stack.getProvince()))
                        .flatMap(stack -> stack.getCounters().stream())
                        .anyMatch(counter -> CounterUtil.isArmyCounter(counter.getType()));
                if (hasArmy) {
                    landIncome = region.getIncome();
                } else {
                    landIncome = region.getIncome() / 2;
                }
            }
            if (existingPillages.isEmpty() && landIncome > 0) {
                EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                        .filter(ecoSheet -> Objects.equals(ecoSheet.getTurn(), game.getTurn()))
                        .findAny()
                        .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_ENTITY, MSG_MISSING_ENTITY,
                                "EconomicalSheetEntity", "country : " + country.getId() + " - turn : " + game.getTurn()));
                sheet.setPillages(sheet.getPillages() + landIncome);
                diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PILLAGE, sheet.getPillages())));
            }
        }

        if (existingPillages.size() <= 1) {
            if (idPillageStack == null) {
                idPillageStack = counterDomain.createStack(province.getName(), null, game).getId();
            }
            diffs.add(counterDomain.createCounter(CounterFaceTypeEnum.PILLAGE_PLUS, null, idPillageStack, game));
        } else {
            CounterEntity pillageMinus = existingPillages.stream()
                    .filter(counter -> counter.getType() == CounterFaceTypeEnum.PILLAGE_MINUS)
                    .findAny()
                    .orElse(null);
            if (pillageMinus != null) {
                diffs.add(counterDomain.switchCounter(pillageMinus.getId(), CounterFaceTypeEnum.PILLAGE_PLUS, null, game));
            }
        }

        return diffs;
    }

    /**
     * Burn a trading post.
     *
     * @param province the province where the trading post will be burnt.
     * @param country  the country burning the trading post.
     * @param game     the game.
     * @return the diffs involved.
     * @throws FunctionalException functional exception.
     */
    private List<DiffEntity> burnTradingPost(AbstractProvinceEntity province, PlayableCountryEntity country, GameEntity game) throws FunctionalException {
        List<String> enemies = oeUtil.getEnemies(country, game);

        CounterEntity tradingPost = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(province.getName(), stack.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> (counter.getType() == CounterFaceTypeEnum.TRADING_POST_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_POST_PLUS)
                        && enemies.contains(counter.getCountry()))
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(tradingPost)
                .setCodeError(IConstantsServiceException.LAND_LOOTING_BURN_TP_NO_TP)
                .setMsgFormat("{1}: {0} There must exist an enemy trading post in order to burn it in {2}.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, province.getName()));

        List<String> allies = oeUtil.getAllies(country, game);
        String controller = oeUtil.getController(province, game);

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(allies.contains(controller))
                .setCodeError(IConstantsServiceException.LAND_LOOTING_BURN_TP_NO_CONTROL)
                .setMsgFormat("{1}: {0} You have to control the trading post in {2} to burn it.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_LOOTING, province.getName()));

        List<DiffEntity> diffs = new ArrayList<>();

        diffs.add(counterDomain.removeCounter(tradingPost.getId(), game));

        return diffs;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse landRedeploy(Request<LandRedeployRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_LAND_REDEPLOY)
                .setParams(METHOD_LAND_REDEPLOY));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_LAND_REDEPLOY, PARAMETER_LAND_REDEPLOY);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.REDEPLOYMENT, request.getGame().getIdCountry(), METHOD_LAND_REDEPLOY, PARAMETER_LAND_REDEPLOY);

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
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST)
                .setParams(METHOD_LAND_REDEPLOY));
        failIfEmpty(new AbstractService.CheckForThrow<String>()
                .setTest(request.getRequest().getProvince())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_LAND_REDEPLOY));
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest().getIdStack())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_REDEPLOY));
        StackEntity stack = game.getStacks().stream()
                .filter(s -> Objects.equals(request.getRequest().getIdStack(), s.getId()))
                .findAny()
                .orElse(null);
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(stack)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_REDEPLOY, request.getRequest().getIdStack()));
        boolean isMobile = oeUtil.isMobile(stack);
        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(isMobile)
                .setCodeError(IConstantsServiceException.STACK_NOT_MOBILE)
                .setMsgFormat("{1}: {0} {2} Stack is not mobile.")
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_REDEPLOY, request.getRequest().getIdStack()));
        AbstractProvinceEntity provinceFrom = provinceDao.getProvinceByName(stack.getProvince());

        List<String> patrons = counterDao.getPatrons(stack.getCountry(), game.getId());
        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(patrons.contains(country.getName()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_REDEPLOY, country.getName(), patrons));

        List<String> enemies = oeUtil.getEnemies(country, game);
        String controller = oeUtil.getController(provinceFrom, game);

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(enemies.contains(controller))
                .setCodeError(IConstantsServiceException.LAND_REDEPLOY_NOT_ENEMY)
                .setMsgFormat("{1}: {0} You must loot redeploy from a province that is controlled by one of your enemy. Current controller of {2} is {3}.")
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_ID_STACK)
                .setParams(METHOD_LAND_REDEPLOY, provinceFrom.getName(), controller));

        AbstractProvinceEntity provinceTo = provinceDao.getProvinceByName(request.getRequest().getProvince());
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(provinceTo)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_LAND_REDEPLOY, request.getRequest().getProvince()));

        boolean canRedeploy = oeUtil.canRetreat(provinceTo, false, 0d, country, game);

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(canRedeploy)
                .setCodeError(IConstantsServiceException.UNIT_CANT_REDEPLOY_PROVINCE)
                .setMsgFormat("{1}: {0} Impossible to redeploy units in the province {2}.")
                .setName(PARAMETER_LAND_REDEPLOY, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_CHOOSE_MODE, provinceTo.getName()));

        // TODO TG-135 provinceTo should be closest friendly province in MP

        List<DiffEntity> diffs = new ArrayList<>();
        stack.setProvince(provinceTo.getName());
        stack.setMovePhase(MovePhaseEnum.MOVED);
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.STACK, stack.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_FROM, provinceFrom.getName()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, provinceTo.getName()),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.MOVED)));
        game.getStacks().stream()
                .filter(s -> StringUtils.equals(provinceFrom.getName(), s.getProvince()))
                .flatMap(s -> s.getCounters().stream())
                .filter(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_MINUS || counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS)
                .forEach(counter -> diffs.add(counterDomain.removeCounter(counter.getId(), game)));

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateRedeploy(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_REDEPLOY)
                .setParams(METHOD_VALIDATE_REDEPLOY));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_VALIDATE_REDEPLOY, PARAMETER_VALIDATE_REDEPLOY);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.REDEPLOYMENT, request.getGame().getIdCountry(), METHOD_VALIDATE_REDEPLOY, PARAMETER_VALIDATE_REDEPLOY);

        // TODO TG-2 Authorization

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_REDEPLOY, PARAMETER_REQUEST)
                .setParams(METHOD_VALIDATE_REDEPLOY));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getGame().getIdCountry())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_REDEPLOY, PARAMETER_GAME, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_REDEPLOY));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getGame().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_VALIDATE_REDEPLOY, PARAMETER_GAME, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_REDEPLOY, request.getGame().getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_VALIDATE_REDEPLOY, PARAMETER_AUTHENT, PARAMETER_USERNAME)
                .setParams(METHOD_VALIDATE_REDEPLOY, request.getAuthent().getUsername(), country.getUsername()));

        List<String> allies = oeUtil.getAllies(country, game);
        List<String> minors = counterDao.getMinors(country.getName(), game.getId());

        List<String> minorsAndSelf = minors.stream()
                .filter(allies::contains)
                .collect(Collectors.toList());
        minorsAndSelf.add(country.getName());

        List<String> besiegingProvinces = game.getStacks().stream()
                .filter(stack -> stack.getMovePhase() != null && stack.getMovePhase().isBesieging()
                        && minorsAndSelf.contains(stack.getCountry()))
                .map(StackEntity::getProvince)
                .distinct()
                .collect(Collectors.toList());

        List<String> mustRedeployProvinces = new ArrayList<>();
        for (String provinceName : besiegingProvinces) {
            AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceName);
            String controller = oeUtil.getController(province, game);
            int fortress = oeUtil.getFortressLevel(province, game);
            double size = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(provinceName, stack.getProvince()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(counter -> allies.contains(counter.getCountry()))
                    .collect(Collectors.summingDouble(counter -> CounterUtil.getSizeFromType(counter.getType())));
            boolean canStay = allies.contains(controller);
            if (!canStay && size >= fortress) {
                canStay = game.getStacks().stream()
                        .filter(stack -> StringUtils.equals(provinceName, stack.getProvince()))
                        .flatMap(stack -> stack.getCounters().stream())
                        .anyMatch(counter -> counter.getType() == CounterFaceTypeEnum.SIEGEWORK_PLUS);
                if (!canStay) {
                    canStay = game.getSieges().stream()
                            .anyMatch(siege -> Objects.equals(game.getTurn(), siege.getTurn()) && StringUtils.equals(provinceName, siege.getProvince())
                                    && siege.isBreach());
                }
            }
            if (!canStay) {
                mustRedeployProvinces.add(provinceName);
            }
        }

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(mustRedeployProvinces.isEmpty())
                .setCodeError(IConstantsServiceException.STACK_MUST_REDEPLOY)
                .setMsgFormat("{1}: {0} Some stacks in {2} must redeploy.")
                .setName(PARAMETER_VALIDATE_REDEPLOY, PARAMETER_REQUEST, PARAMETER_VALIDATE)
                .setParams(METHOD_VALIDATE_REDEPLOY, mustRedeployProvinces.stream().collect(Collectors.joining(","))));

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
                newDiffs.addAll(statusWorkflowDomain.endRedeploymentPhase(game));
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
    public DiffResponse exchequerRepartition(Request<ExchequerRepartitionRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_EXCHEQUER_REPARTITION)
                .setParams(METHOD_EXCHEQUER_REPARTITION));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_EXCHEQUER_REPARTITION, PARAMETER_EXCHEQUER_REPARTITION);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.EXCHEQUER, null, METHOD_EXCHEQUER_REPARTITION, PARAMETER_EXCHEQUER_REPARTITION);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_EXCHEQUER_REPARTITION, PARAMETER_REQUEST)
                .setParams(METHOD_EXCHEQUER_REPARTITION));

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                .filter(es -> Objects.equals(game.getTurn(), es.getTurn()))
                .findAny()
                .orElse(null);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(sheet)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_EXCHEQUER_REPARTITION, PARAMETER_REQUEST, PARAMETER_GAME, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_EXCHEQUER_REPARTITION));

        int maxValue = Math.min(CommonUtil.toInt(sheet.getPrestigeIncome()), CommonUtil.toInt(sheet.getRemainingExpenses()));

        failIfTrue(new CheckForThrow<Boolean>()
                .setTest(request.getRequest().getPrestige() < 0 ||
                        CommonUtil.subtract(request.getRequest().getPrestige(), maxValue) > 0)
                .setCodeError(IConstantsServiceException.PRESTIGE_TOO_HIGH)
                .setMsgFormat("{1}: {0} Prestige turned into income ({2}) is invalid, it should be positive and lesser than {3}.")
                .setName(PARAMETER_EXCHEQUER_REPARTITION, PARAMETER_REQUEST, PARAMETER_REQUEST, PARAMETER_PRESTIGE)
                .setParams(METHOD_EXCHEQUER_REPARTITION, request.getRequest().getPrestige(), maxValue));

        List<DiffEntity> diffs = new ArrayList<>();
        if (sheet.getPrestigeSpent() == null || sheet.getPrestigeSpent() != request.getRequest().getPrestige()) {
            sheet.setPrestigeSpent(request.getRequest().getPrestige());
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.EXCHEQUER_PRESTIGE_SPENT, sheet.getPrestigeSpent())));
        }

        return createDiffs(diffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse validateExchequer(Request<ValidateRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_EXCHEQUER)
                .setParams(METHOD_VALIDATE_EXCHEQUER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_VALIDATE_EXCHEQUER, PARAMETER_VALIDATE_EXCHEQUER);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.EXCHEQUER, null, METHOD_VALIDATE_EXCHEQUER, PARAMETER_VALIDATE_EXCHEQUER);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getAuthent())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_EXCHEQUER, PARAMETER_AUTHENT)
                .setParams(METHOD_VALIDATE_EXCHEQUER));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_EXCHEQUER, PARAMETER_REQUEST)
                .setParams(METHOD_VALIDATE_EXCHEQUER));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getGame().getIdCountry())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_VALIDATE_EXCHEQUER, PARAMETER_GAME, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_EXCHEQUER));

        PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), c -> c.getId().equals(request.getGame().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(country)
                .setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND)
                .setName(PARAMETER_VALIDATE_EXCHEQUER, PARAMETER_ID_COUNTRY)
                .setParams(METHOD_VALIDATE_EXCHEQUER, request.getGame().getIdCountry()));

        failIfFalse(new CheckForThrow<Boolean>()
                .setTest(StringUtils.equals(request.getAuthent().getUsername(), country.getUsername()))
                .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                .setMsgFormat(MSG_ACCESS_RIGHT)
                .setName(PARAMETER_VALIDATE_EXCHEQUER, PARAMETER_AUTHENT, PARAMETER_USERNAME)
                .setParams(METHOD_VALIDATE_EXCHEQUER, request.getAuthent().getUsername(), country.getUsername()));

        List<DiffEntity> newDiffs = new ArrayList<>();

        if (country.isReady() != request.getRequest().isValidate()) {
            country.setReady(request.getRequest().isValidate());

            long countriesNotReady = game.getCountries().stream()
                    .filter(c -> StringUtils.isNotEmpty(c.getUsername()) && !c.isReady())
                    .count();

            if (countriesNotReady == 0) {
                newDiffs.addAll(statusWorkflowDomain.endExchequerPhase(game));
            } else {
                DiffEntity diff = DiffUtil.createDiff(game, request.getRequest().isValidate() ? DiffTypeEnum.VALIDATE : DiffTypeEnum.INVALIDATE, DiffTypeObjectEnum.STATUS, country.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()));
                newDiffs.add(diff);
            }
        }

        return createDiffs(newDiffs, gameDiffs, request);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse improveStability(Request<ImproveStabilityRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request)
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_IMPROVE_STABILITY)
                .setParams(METHOD_IMPROVE_STABILITY));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffsAsWriter(request.getGame(), METHOD_IMPROVE_STABILITY, PARAMETER_IMPROVE_STABILITY);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.STABILITY, null, METHOD_IMPROVE_STABILITY, PARAMETER_IMPROVE_STABILITY);

        failIfNull(new AbstractService.CheckForThrow<>()
                .setTest(request.getRequest())
                .setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_IMPROVE_STABILITY, PARAMETER_REQUEST)
                .setParams(METHOD_IMPROVE_STABILITY));

        // TODO TG-2 Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getGame().getIdCountry()))
                .findFirst()
                .orElse(null);
        // No check on null of country because it will be done in Authorization before

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(country.isReady())
                .setCodeError(IConstantsServiceException.ACTION_ALREADY_DONE)
                .setMsgFormat("{1}: {0} The action {2} has already been done by the country {3}.")
                .setName(PARAMETER_IMPROVE_STABILITY, PARAMETER_REQUEST)
                .setParams(METHOD_IMPROVE_STABILITY, METHOD_IMPROVE_STABILITY, country.getName()));

        int stability = oeUtil.getStability(game, country.getName());

        failIfTrue(new AbstractService.CheckForThrow<Boolean>()
                .setTest(request.getRequest().getInvestment() != null && stability == 3)
                .setCodeError(IConstantsServiceException.STABILITY_MAX)
                .setMsgFormat("{1}: {0} The stability is already at max.")
                .setName(PARAMETER_IMPROVE_STABILITY, PARAMETER_REQUEST, PARAMETER_INVESTMENT)
                .setParams(METHOD_IMPROVE_STABILITY));

        List<DiffEntity> diffs = new ArrayList<>();

        EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                .filter(es -> Objects.equals(game.getTurn(), es.getTurn()))
                .findAny()
                .orElse(null);
        if (sheet != null && request.getRequest().getInvestment() != null) {
            sheet.setStab(EconomicUtil.getAdminActionCost(AdminActionTypeEnum.MNU, request.getRequest().getInvestment()));
            int modifier = CommonUtil.toInt(sheet.getStabModifier());
            if (request.getRequest().getInvestment() == InvestmentEnum.M) {
                modifier += 2;
            } else if (request.getRequest().getInvestment() == InvestmentEnum.L) {
                modifier += 5;
            }
            int die = oeUtil.rollDie(game, country);
            sheet.setStabDie(die);
            int stabDiff = 0;
            if (die + modifier <= 5) {
                stabDiff = -1;
            } else if (die + modifier >= 18) {
                stabDiff = 3;
            } else if (die + modifier >= 15) {
                stabDiff = 2;
            } else if (die + modifier >= 11) {
                stabDiff = 1;
            }

            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STAB, sheet.getStab()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STAB_DIE, sheet.getStabDie())));

            int newStab = Math.min(Math.max(stability + stabDiff, -3), 3);
            if (newStab != stability) {
                diffs.add(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.STABILITY, country.getName(), GameUtil.getStabilityBox(newStab), game));
            }
        }

        country.setReady(true);

        long countriesNotReady = game.getCountries().stream()
                .filter(c -> StringUtils.isNotEmpty(c.getUsername()) && !c.isReady())
                .count();

        if (countriesNotReady == 0) {
            diffs.addAll(statusWorkflowDomain.endStabilityPhase(game));
        } else {
            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.VALIDATE, DiffTypeObjectEnum.STATUS, country.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, country.getId()));
            diffs.add(diff);
        }

        return createDiffs(diffs, gameDiffs, request);
    }
}
