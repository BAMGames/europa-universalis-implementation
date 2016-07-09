package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.IGameAdminService;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
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
    /** Counter Domain. */
    @Autowired
    private ICounterDomain counterDomain;
    /** Game DAO. */
    @Autowired
    private IGameDao gameDao;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Country DAO. */
    @Autowired
    private ICountryDao countryDao;
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
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ID_GAME).setParams(METHOD_CREATE_COUNTER, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(PARAMETER_VERSION_GAME).setParams(METHOD_CREATE_COUNTER, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        CountryEntity country = countryDao.getCountryByName(counter.getCountry());

        failIfNull(new CheckForThrow<>().setTest(country).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_COUNTER + ".country")
                .setParams(METHOD_CREATE_COUNTER, counter.getCountry()));

        AbstractProvinceEntity prov = provinceDao.getProvinceByName(province);

        failIfNull(new CheckForThrow<>().setTest(prov).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_PROVINCE).setParams(METHOD_CREATE_COUNTER, province));

        DiffEntity diff = counterDomain.createCounter(counter.getType(), counter.getCountry(), province, game);

        gameDao.update(game, true);

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
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ID_GAME).setParams(METHOD_REMOVE_COUNTER, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(PARAMETER_VERSION_GAME).setParams(METHOD_REMOVE_COUNTER, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        DiffEntity diff = counterDomain.removeCounter(idCounter, game);

        failIfNull(new CheckForThrow<>().setTest(diff).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_ID_COUNTER).setParams(METHOD_REMOVE_COUNTER, idGame));

        gameDao.update(game, true);

        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        return response;
    }
}
