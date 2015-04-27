package com.mkl.eu.service.service.persistence.diff.board;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.board.AbstractProvinceEntity;

/**
 * Interface of the Game DAO.
 *
 * @author MKL.
 */
public interface IProvinceDao extends IGenericDao<AbstractProvinceEntity, Long> {
    /**
     * Returns the province given its name.
     *
     * @param name name of the province.
     * @return the province given its name.
     */
    AbstractProvinceEntity getProvinceByName(String name);
}
