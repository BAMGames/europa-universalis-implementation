package com.mkl.eu.service.service.persistence.country;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

import java.util.List;

/**
 * Interface of the PlayableCountry DAO.
 *
 * @author MKL.
 */
public interface IPlayableCountryDao extends IGenericDao<PlayableCountryEntity, Long> {
    /**
     * Returns the country given its name.
     *
     * @param name   name of the country.
     * @param idGame id of the game.
     * @return the country given its name.
     */
    PlayableCountryEntity getCountryByName(String name, Long idGame);

    /**
     * Returns the owned provinces of the country.
     *
     * @param name   name of the country.
     * @param idGame if of the game.
     * @return the owned provinces of the country.
     */
    List<String> getOwnedProvinces(String name, Long idGame);
}
