package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechniqueException;
import com.mkl.eu.client.service.service.IGameAdminService;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.diff.board.IProvinceDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the Game Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechniqueException.class, FunctionalException.class})
public class GameAdminServiceImpl implements IGameAdminService {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameAdminServiceImpl.class);
    /** Game DAO. */
    @Autowired
    private IGameDao gameDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Diff mapping. */
    @Autowired
    private DiffMapping diffMapping;

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(Long idGame, Long versionGame, Long idStack, String provinceTo) {
        if (idGame == null) {
            String msg = "moveStack: idGame missing.";
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.NULL_PARAMETER, msg, null, "idGame");
        }
        if (versionGame == null) {
            String msg = "moveStack: versionGame missing.";
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.NULL_PARAMETER, msg, null, "versionGame");
        }
        if (idStack == null) {
            String msg = "moveStack: idStack missing.";
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.NULL_PARAMETER, msg, null, "idStack");
        }
        if (StringUtils.isEmpty(provinceTo)) {
            String msg = "moveStack: provinceTo missing.";
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.NULL_PARAMETER, msg, null, "provinceTo");
        }

        GameEntity game = gameDao.lock(idGame);

        if (game == null) {
            String msg = MessageFormat.format("moveStack: idGame {0} does not exist.", idGame);
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.INVALID_PARAMETER, msg, null, "idGame");
        }

        if (versionGame >= game.getVersion()) {
            String msg = MessageFormat.format("moveStack: versionGame {0} is greater than actual ({1}).", versionGame, game.getVersion());
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.INVALID_PARAMETER, msg, null, "versionGame");
        }

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        Optional<StackEntity> stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();

        if (!stackOpt.isPresent()) {
            String msg = MessageFormat.format("moveStack: idStack {0} does not exist.", idStack);
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.INVALID_PARAMETER, msg, null, "idStack");
        }

        StackEntity stack = stackOpt.get();

        AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);

        if (province == null) {
            String msg = MessageFormat.format("moveStack: provinceTo {0} does not exist.", provinceTo);
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.INVALID_PARAMETER, msg, null, "provinceTo");
        }

        boolean isNear = stack.getProvince().getBorders().stream().filter(x -> province.getId().equals(x.getProvinceTo().getId())).findFirst().isPresent();

        if (!isNear) {
            String msg = MessageFormat.format("moveStack: provinceTo {0} is not a border or former stack location ({1}).", provinceTo, stack.getProvince().getName());
            LOGGER.error(msg);
            throw new TechniqueException(IConstantsCommonException.INVALID_PARAMETER, msg, null, "provinceTo");
        }

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MOVE);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        diff.setIdObject(idStack);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_FROM);
        diffAttributes.setValue(stack.getProvince().getName());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_TO);
        diffAttributes.setValue(provinceTo);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        diffDao.create(diff);

        diffs.add(diff);

        stack.setProvince(province);
        gameDao.update(game);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        return response;
    }
}
