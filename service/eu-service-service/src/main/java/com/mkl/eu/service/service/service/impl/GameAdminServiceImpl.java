package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.IGameAdminService;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the Game Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class GameAdminServiceImpl extends AbstractService implements IGameAdminService {
    /** Game DAO. */
    @Autowired
    private IGameDao gameDao;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Country DAO. */
    @Autowired
    private ICountryDao countryDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;
    /** Diff mapping. */
    @Autowired
    private DiffMapping diffMapping;

    @Override
    public DiffResponse createCounter(Long idGame, Long versionGame, CounterForCreation counter, String province) throws FunctionalException {
        // TODO authorization
        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ID_GAME).setParams(METHOD_CREATE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VERSION_GAME).setParams(METHOD_CREATE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_COUNTER).setParams(METHOD_CREATE_COUNTER));
        failIfEmpty(new CheckForThrow<String>().setTest(province).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_PROVINCE).setParams(METHOD_CREATE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(counter.getType()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_COUNTER + ".type").setParams(METHOD_CREATE_COUNTER));
        failIfEmpty(new CheckForThrow<String>().setTest(counter.getCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_COUNTER + ".country").setParams(METHOD_CREATE_COUNTER));

        GameEntity game = gameDao.lock(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_ID_GAME).setParams(METHOD_CREATE_COUNTER, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(PARAMETER_VERSION_GAME).setParams(METHOD_CREATE_COUNTER, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        CountryEntity country = countryDao.getCountryByName(counter.getCountry());

        failIfNull(new CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_COUNTER + ".country")
                .setParams(METHOD_CREATE_COUNTER, counter.getCountry()));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_PROVINCE).setParams(METHOD_CREATE_COUNTER, province));

        StackEntity stack = new StackEntity();
        stack.setProvince(province);
        stack.setGame(game);

        CounterEntity counterEntity = new CounterEntity();
        counterEntity.setCountry(counter.getCountry());
        counterEntity.setType(counter.getType());
        counterEntity.setOwner(stack);

        stack.getCounters().add(counterEntity);

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

        stackDao.create(stack);

        game.getStacks().add(stack);

        gameDao.update(game, true);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.ADD);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(counterEntity.getId());
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
        diffAttributes.setValue(province);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.TYPE);
        diffAttributes.setValue(counter.getType().name());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.COUNTRY);
        diffAttributes.setValue(counter.getCountry());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STACK);
        diffAttributes.setValue(stack.getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);

        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse removeCounter(Long idGame, Long versionGame, Long idCounter) throws FunctionalException, TechnicalException {
        // TODO authorization
        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ID_GAME).setParams(METHOD_REMOVE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VERSION_GAME).setParams(METHOD_REMOVE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(idCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ID_COUNTER).setParams(METHOD_REMOVE_COUNTER));

        GameEntity game = gameDao.lock(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_ID_GAME).setParams(METHOD_REMOVE_COUNTER, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(PARAMETER_VERSION_GAME).setParams(METHOD_REMOVE_COUNTER, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        CounterEntity counter = null;
        for (StackEntity stackEntity : game.getStacks()) {
            for (CounterEntity counterEntity : stackEntity.getCounters()) {
                if (counterEntity.getId().equals(idCounter)) {
                    counter = counterEntity;
                    break;
                }
            }
            if (counter != null) {
                break;
            }
        }

        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_ID_COUNTER).setParams(METHOD_REMOVE_COUNTER, idGame));

        StackEntity stack = counter.getOwner();
        stack.getCounters().remove(counter);
        counter.setOwner(null);
        counterDao.delete(counter);

        if (stack.getCounters().isEmpty()) {
            stack.setGame(null);
            game.getStacks().remove(stack);
        }

        gameDao.update(game, true);

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.REMOVE);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(idCounter);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
        diffAttributes.setValue(stack.getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        if (stack.getCounters().isEmpty()) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.STACK_DEL);
            diffAttributes.setValue(stack.getId().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
        }

        diffDao.create(diff);

        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        return response;
    }
}
