package com.mkl.eu.service.service.persistence.ref;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;

/**
 * Interface of the Country DAO.
 *
 * @author MKL.
 */
public interface ICountryDao extends IGenericDao<CountryEntity, Long> {
    /**
     * Returns the country given its name.
     *
     * @param name name of the country.
     * @return the country given its name.
     */
    CountryEntity getCountryByName(String name);
}
