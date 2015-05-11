package com.mkl.eu.service.service.persistence.board.impl;

import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Counter DAO.
 *
 * @author MKL.
 */
@Repository
public class CounterDaoImpl extends GenericDaoImpl<CounterEntity, Long> implements ICounterDao {
    /**
     * Constructor.
     */
    public CounterDaoImpl() {
        super(CounterEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public CounterEntity getCounter(Long idCounter, Long idGame) {
        CounterEntity counter = load(idCounter);

        if (idGame == null || counter == null || counter.getOwner() == null
                || counter.getOwner().getGame() == null
                || counter.getOwner().getGame().getId().longValue() != idGame.longValue()) {
            counter = null;
        }

        return counter;
    }
}
