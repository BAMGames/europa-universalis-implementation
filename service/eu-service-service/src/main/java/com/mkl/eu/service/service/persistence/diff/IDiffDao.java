package com.mkl.eu.service.service.persistence.diff;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;

import java.util.List;

/**
 * Interface of the Diff DAO.
 *
 * @author MKL.
 */
public interface IDiffDao extends IGenericDao<DiffEntity, Long> {
    /**
     * Returns the diffs of a game since the given version.
     *
     * @param idGame    id of the game.
     * @param idCountry id of the country.
     * @param version   version since we want the diffs.
     * @return the diffs of a game since the given version.
     */
    List<DiffEntity> getDiffsSince(Long idGame, Long idCountry, Long version);
}
