package com.mkl.eu.service.service.persistence.ref;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.GoldEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RegionEntity;

/**
 * Interface of the Province DAO.
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

    /**
     * Returns the region given its name.
     *
     * @param name name of the region.
     * @return the region given its name.
     */
    RegionEntity getRegionByName(String name);

    /**
     * Return the gold in a province.
     *
     * @param name of the province.
     * @return the gold.
     */
    GoldEntity getGoldInProvince(String name);
}
