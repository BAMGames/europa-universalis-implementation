package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
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
                diffs = pillageLand(province, country, game);
                break;
            case BURN_TP:
                diffs = burnTradingPost(province, country, game);
                break;
            default:
                diffs = new ArrayList<>();
        }
        stack.setMovePhase(MovePhaseEnum.LOOTING);
        diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.STACK, stack.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.MOVE_PHASE, MovePhaseEnum.LOOTING)));

        return createDiffs(diffs, gameDiffs, request);
    }

    /**
     * Pillage a land province.
     *
     * @param province the province to be pillaged.
     * @param country  the country pillaging the province.
     * @param game     the game.
     * @return the diffs involved.
     * @throws FunctionalException functional exception.
     */
    private List<DiffEntity> pillageLand(AbstractProvinceEntity province, PlayableCountryEntity country, GameEntity game) throws FunctionalException {
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
        List<DiffEntity> diffs = new ArrayList<>();
        if (existingPillages.isEmpty() && landIncome > 0) {
            EconomicalSheetEntity sheet = country.getEconomicalSheets().stream()
                    .filter(ecoSheet -> Objects.equals(ecoSheet.getTurn(), game.getTurn()))
                    .findAny()
                    .orElseThrow(createTechnicalExceptionSupplier(IConstantsCommonException.MISSING_ENTITY, MSG_MISSING_ENTITY,
                            "EconomicalSheetEntity", "country : " + country.getId() + " - turn : " + game.getTurn()));
            sheet.setPillages(sheet.getPillages() + landIncome);
            diffs.add(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.ECO_SHEET, sheet.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PILLAGE, sheet.getPillages())));
        }

        if (existingPillages.size() <= 1) {
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
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_LAND_LOOTING, province.getName()));

        List<String> allies = oeUtil.getAllies(country, game);
        String controller = oeUtil.getController(province, game);

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(allies.contains(controller))
                .setCodeError(IConstantsServiceException.LAND_LOOTING_BURN_TP_NO_CONTROL)
                .setMsgFormat("{1}: {0} You have to control the trading post in {2} to burn it.")
                .setName(PARAMETER_LAND_LOOTING, PARAMETER_REQUEST, PARAMETER_PROVINCE)
                .setParams(METHOD_LAND_LOOTING, province.getName()));

        List<DiffEntity> diffs = new ArrayList<>();

        diffs.add(counterDomain.removeCounter(tradingPost.getId(), game));

        return diffs;
    }
}
