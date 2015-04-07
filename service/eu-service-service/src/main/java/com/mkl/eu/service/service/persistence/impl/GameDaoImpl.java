package com.mkl.eu.service.service.persistence.impl;

import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
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
}
