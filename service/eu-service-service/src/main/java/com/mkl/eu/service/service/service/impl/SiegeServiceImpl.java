package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.military.ChooseProvinceRequest;
import com.mkl.eu.client.service.service.military.SelectForcesRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeCounterEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
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
import java.util.stream.Collectors;

/**
 * Service for siege purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class SiegeServiceImpl extends AbstractService implements ISiegeService {
    @Autowired
    private IOEUtil oeUtil;

    /** {@inheritDoc} */
    @Override
    public DiffResponse chooseSiege(Request<ChooseProvinceRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new CheckForThrow<>()
                .setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER)
                .setName(PARAMETER_CHOOSE_SIEGE)
                .setParams(METHOD_CHOOSE_SIEGE));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_CHOOSE_SIEGE, PARAMETER_CHOOSE_SIEGE);
        GameEntity game = gameDiffs.getGame();

        checkGameStatus(game, GameStatusEnum.MILITARY_SIEGES, request.getIdCountry(), METHOD_CHOOSE_SIEGE, PARAMETER_CHOOSE_SIEGE);

        // TODO Authorization
        PlayableCountryEntity country = game.getCountries().stream()
                .filter(x -> x.getId().equals(request.getIdCountry()))
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

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, (String) null));

        List<String> allies = oeUtil.getAllies(country, game);

        List<CounterEntity> attackerCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                        allies.contains(stack.getCountry()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()))
                .collect(Collectors.toList());

        Double attackerSize = attackerCounters.stream()
                .map(counter -> CounterUtil.getSizeFromType(counter.getType()))
                .reduce(Double::sum)
                .orElse(0d);
        if (attackerCounters.size() <= 3 && attackerSize <= 8) {
            attackerCounters.forEach(counter -> {
                SiegeCounterEntity comp = new SiegeCounterEntity();
                comp.setSiege(siege);
                comp.setCounter(counter);
                comp.setPhasing(true);
                siege.getCounters().add(comp);

                DiffAttributesEntity attribute = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTER_ADD, counter.getId());
                attribute.setDiff(diff);
                diff.getAttributes().add(attribute);
            });
            siege.setStatus(SiegeStatusEnum.CHOOSE_MODE);
        } else {
            siege.setStatus(SiegeStatusEnum.SELECT_FORCES);
        }

        List<CounterEntity> defenderCounters = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                        stack.isBesieged())
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> CounterUtil.isArmy(counter.getType()))
                .collect(Collectors.toList());
        defenderCounters.forEach(counter -> {
            SiegeCounterEntity comp = new SiegeCounterEntity();
            comp.setSiege(siege);
            comp.setCounter(counter);
            comp.setPhasing(false);
            siege.getCounters().add(comp);

            DiffAttributesEntity attribute = DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD, counter.getId());
            attribute.setDiff(diff);
            diff.getAttributes().add(attribute);
        });

        diff.getAttributes().get(0).setValue(siege.getStatus().name());

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

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_SELECT_FORCES, PARAMETER_SELECT_FORCES);
        GameEntity game = gameDiffs.getGame();

        checkSimpleStatus(game, GameStatusEnum.MILITARY_SIEGES, METHOD_SELECT_FORCES, PARAMETER_SELECT_FORCES);

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

        boolean phasing = isCountryActive(game, request.getIdCountry());

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(phasing)
                .setCodeError(IConstantsServiceException.SIEGE_SELECT_VALIDATED)
                .setMsgFormat("{1}: {0} The non phasing forces are always automatically added in a siege.")
                .setName(PARAMETER_SELECT_FORCES)
                .setParams(METHOD_SELECT_FORCES));

        List<DiffAttributesEntity> attributes = new ArrayList<>();
        for (Long idCounter : request.getRequest().getForces()) {
            List<String> allies = oeUtil.getAllies(country, game);

            CounterEntity counter = game.getStacks().stream()
                    .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()) &&
                            allies.contains(stack.getCountry()))
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(c -> CounterUtil.isArmy(c.getType()) &&
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
            comp.setCounter(counter);
            siege.getCounters().add(comp);

            attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PHASING_COUNTER_ADD, counter.getId()));
        }

        List<Long> alliedCounters = siege.getCounters().stream()
                .filter(bc -> bc.isPhasing() == phasing)
                .map(bc -> bc.getCounter().getId())
                .collect(Collectors.toList());
        Double armySize = siege.getCounters().stream()
                .map(bc -> CounterUtil.getSizeFromType(bc.getCounter().getType()))
                .reduce(Double::sum)
                .orElse(0d);

        if (alliedCounters.size() < 3 && armySize < 8) {
            List<String> allies = oeUtil.getAllies(country, game);
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

        siege.setStatus(SiegeStatusEnum.CHOOSE_MODE);
        attributes.add(DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.STATUS, SiegeStatusEnum.CHOOSE_MODE));

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.SIEGE, siege.getId(),
                attributes.toArray(new DiffAttributesEntity[attributes.size()]));

        return createDiff(diff, gameDiffs, request);
    }
}
