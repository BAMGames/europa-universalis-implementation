package com.mkl.eu.service.service.persistence.board.impl;

import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the Stack DAO.
 *
 * @author MKL.
 */
@Repository
public class StackDaoImpl extends GenericDaoImpl<StackEntity, Long> implements IStackDao {
    /**
     * Constructor.
     */
    public StackDaoImpl() {
        super(StackEntity.class);
    }

    /** {@inheritDoc] */
    @Override
    public List<StackEntity> getStacksOnProvince(String province, Long idGame) {
        Criteria criteria = getSession().createCriteria(StackEntity.class);

        criteria.add(Restrictions.eq("province", province));
        criteria.add(Restrictions.eq("game.id", idGame));

        //noinspection unchecked
        return criteria.list();
    }
}
