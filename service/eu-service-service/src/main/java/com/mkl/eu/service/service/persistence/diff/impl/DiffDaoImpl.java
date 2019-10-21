package com.mkl.eu.service.service.persistence.diff.impl;

import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the Game DAO.
 *
 * @author MKL.
 */
@Repository
public class DiffDaoImpl extends GenericDaoImpl<DiffEntity, Long> implements IDiffDao {
    /**
     * Constructor.
     */
    public DiffDaoImpl() {
        super(DiffEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public List<DiffEntity> getDiffsSince(Long idGame, Long idCountry, Long version) {
        Criteria criteria = getSession().createCriteria(DiffEntity.class);

        criteria.add(Restrictions.eq("idGame", idGame));
        criteria.add(Restrictions.gt("versionGame", version));
        criteria.add(Restrictions.or(Restrictions.isNull("idCountry"), Restrictions.eq("idCountry", idCountry)));
        criteria.setFetchMode("attributes", FetchMode.SELECT);

        return listAndCast(criteria);
    }
}
