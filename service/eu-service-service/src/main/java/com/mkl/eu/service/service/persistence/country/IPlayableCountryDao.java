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
     * Returns the owned provinces of the country.
     *
     * @param name   name of the country.
     * @param idGame if of the game.
     * @return the owned provinces of the country.
     */
    List<String> getOwnedProvinces(String name, Long idGame);
}
