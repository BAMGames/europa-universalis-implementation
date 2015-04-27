package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechniqueException;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
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
@Transactional(rollbackFor = {TechniqueException.class, FunctionalException.class})
public class GameServiceImpl extends AbstractService implements IGameService {
    /** Game DAO. */
    @Autowired
    private IGameDao gameDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;
    /** Game mapping. */
    @Autowired
    private GameMapping gameMapping;
    /** Diff mapping. */
    @Autowired
    private DiffMapping diffMapping;

    /** {@inheritDoc} */
    @Override
    public Game loadGame(Long id) {
        GameEntity game = gameDao.read(id);
        return gameMapping.oeToVo(game);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(Long idGame, Long versionGame) {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_ID_GAME).setParams(METHOD_MOVE_STACK));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_VERSION_GAME).setParams(METHOD_MOVE_STACK));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);
        List<Diff> diffVos = diffMapping.oesToVos(diffs);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffVos);
        response.setVersionGame(diffs.stream().max((o1, o2) -> (int) (o1.getVersionGame() - o2.getVersionGame())).get().getVersionGame());

        return response;
    }
}
