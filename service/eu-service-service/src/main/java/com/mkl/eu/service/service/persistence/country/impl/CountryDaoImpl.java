package com.mkl.eu.service.service.persistence.country.impl;

import com.mkl.eu.service.service.persistence.country.ICountryDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
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
    public CountryEntity getCountryByName(String name, Long idGame) {
        Criteria criteria = getSession().createCriteria(CountryEntity.class);

        criteria.add(Restrictions.eq("name", name));
        criteria.add(Restrictions.eq("idGame", idGame));

        return (CountryEntity) criteria.uniqueResult();
    }
}
