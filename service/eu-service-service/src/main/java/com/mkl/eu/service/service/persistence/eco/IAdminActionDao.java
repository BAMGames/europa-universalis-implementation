package com.mkl.eu.service.service.persistence.eco;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;

import java.util.List;

/**
 * Interface of the AdministrativeAction DAO.
 *
 * @author MKL.
 */
public interface IAdminActionDao extends IGenericDao<AdministrativeActionEntity, Long> {
    /**
     * Find the administrative actions that matches.
     *
     * @param idCountry id of the country owning the action.
     * @param turn      turn of the action.
     * @param idObject  external id of the action.
     * @param types     the action must be one of the types.
     * @return the administrative actions that matches.
     */
    List<AdministrativeActionEntity> findAdminActions(Long idCountry, Integer turn, Long idObject, AdminActionTypeEnum... types);
}
