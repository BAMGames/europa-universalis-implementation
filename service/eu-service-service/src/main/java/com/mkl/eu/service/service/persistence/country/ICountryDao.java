package com.mkl.eu.service.service.persistence.country;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;

/**
 * Interface of the Country DAO.
 *
 * @author MKL.
 */
public interface ICountryDao extends IGenericDao<CountryEntity, Long> {
    /**
     * Returns the country given its name.
     *
     * @param name   name of the country.
     * @param idGame id of the game.
     * @return the country given its name.
     */
    CountryEntity getCountryByName(String name, Long idGame);
}
