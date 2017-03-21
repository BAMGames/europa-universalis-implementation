package com.mkl.eu.service.service.persistence.eco;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
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
     * Find the planned administrative actions that matches.
     *
     * @param idCountry id of the country owning the action.
     * @param turn      turn of the action.
     * @param idObject  external id of the action.
     * @param types     the action must be one of the types.
     * @return the administrative actions that matches.
     */
    List<AdministrativeActionEntity> findPlannedAdminActions(Long idCountry, Integer turn, Long idObject, AdminActionTypeEnum... types);

    /**
     * Find the done administrative actions that matches.
     *
     * @param turn   turn of the action.
     * @param idGame the id of the game.
     * @return the administrative actions that matches.
     */
    List<AdministrativeActionEntity> findDoneAdminActions(Integer turn, Long idGame);

    /**
     * @param province the name of the ROTW province.
     * @param idGame   the id of the game.
     * @return the countries that qualify for the inland advance rule for the given ROTW province.
     */
    List<String> getCountriesInlandAdvance(String province, Long idGame);

    /**
     * @param country whose trading post are not count.
     * @param region  where we want the number of trading posts.
     * @param idGame  the id of the game.
     * @return the number of trading posts of others countries in the region.
     */
    int countOtherTpsInRegion(String country, String region, Long idGame);

    /**
     * @param province the name of the ROTW trade zone.
     * @param idGame   the id of the game.
     * @return the countries that qualify for the trade fleet access in the ROTW rule for the given trade zone.
     */
    List<String> getCountriesTradeFleetAccessRotw(String province, Long idGame);

    /**
     * @param land     flag saying if we seek for land or naval tech.
     * @param cultures List of cultures.
     * @param idGame   the id of the game.
     * @return the greater tech box of all countries of the specified cultures for the given tech.
     */
    Integer getMaxTechBox(boolean land, List<CultureEnum> cultures, Long idGame);
}
