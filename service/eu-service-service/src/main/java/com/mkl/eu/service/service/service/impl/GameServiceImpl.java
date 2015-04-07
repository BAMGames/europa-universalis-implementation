package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechniqueException;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the Game Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechniqueException.class, FunctionalException.class})
public class GameServiceImpl implements IGameService {
    /** Game DAO. */
    @Autowired
    private IGameDao gameDao;
    /** Game mapping. */
    @Autowired
    private GameMapping gameMapping;

    /** {@inheritDoc} */
    @Override
    public Game loadGame(Long id) {
        GameEntity game = gameDao.read(id);
        return gameMapping.oeToVo(game);
    }
}
