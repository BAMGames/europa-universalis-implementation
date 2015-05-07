package com.mkl.eu.service.service.persistence.board.impl;

import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
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
        Criteria criteria = getSession().createCriteria(CounterEntity.class);

        criteria.add(Restrictions.eq("id", idCounter));
        criteria.add(Restrictions.eq("owner.game.id", idGame));

        return (CounterEntity) criteria.uniqueResult();
    }
}
