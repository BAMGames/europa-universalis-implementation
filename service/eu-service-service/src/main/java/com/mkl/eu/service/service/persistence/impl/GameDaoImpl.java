package com.mkl.eu.service.service.persistence.impl;

import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Game DAO.
 *
 * @author MKL.
 */
@Repository
public class GameDaoImpl extends GenericDaoImpl<GameEntity, Long> implements IGameDao {
    /**
     * Constructor.
     */
    public GameDaoImpl() {
        super(GameEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public GameEntity lock(Long idGame) {
        GameEntity game = load(idGame);

        lock(game);

        return game;
    }

    /** {@inheritDoc} */
    @Override
    public void lock(GameEntity game) {
        if (game != null) {
            getSession().buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(game);
        }
    }
}
