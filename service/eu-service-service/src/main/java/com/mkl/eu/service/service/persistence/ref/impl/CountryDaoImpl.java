package com.mkl.eu.service.service.persistence.ref.impl;

import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Country DAO.
 *
 * @author MKL.
 */
@Repository
public class CountryDaoImpl extends GenericDaoImpl<CountryEntity, Long> implements ICountryDao {
    /**
     * Constructor.
     */
    public CountryDaoImpl() {
        super(CountryEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public CountryEntity getCountryByName(String name) {
        Criteria criteria = getSession().createCriteria(CountryEntity.class);

        criteria.add(Restrictions.eq("name", name));

        return (CountryEntity) criteria.uniqueResult();
    }
}
