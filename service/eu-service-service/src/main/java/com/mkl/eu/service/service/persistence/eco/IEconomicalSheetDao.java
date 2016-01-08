package com.mkl.eu.service.service.persistence.eco;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface of the EconomicalSheet DAO.
 *
 * @author MKL.
 */
public interface IEconomicalSheetDao extends IGenericDao<EconomicalSheetEntity, Long> {
    /**
     * Returns the provinces owned and controlled by a country and their income.
     *
     * @param name   name of the country.
     * @param idGame id of the game.
     * @return the provinces owned and controlled by the country.
     */
    Map<String, Integer> getOwnedAndControlledProvinces(String name, Long idGame);

    /**
     * Returns the pillaged provinces from a list of provinces.
     *
     * @param provinces list of provinces to check.
     * @param idGame    id of the game.
     * @return the pillaged provinces.
     */
    List<String> getPillagedProvinces(List<String> provinces, Long idGame);

    /**
     * Load the sheets of the given turn, country and turn.
     *
     * @param idCountry id of the country. Can be <code>null</code>.
     * @param turn      turn of the game.
     * @param idGame    id of the game.
     * @return the sheets.
     */
    List<EconomicalSheetEntity> loadSheets(Long idCountry, Integer turn, Long idGame);

    /**
     * Returns the mnu income of the country.
     *
     * @param name              name of the country.
     * @param pillagedProvinces pillaged provinces of the country.
     * @param idGame            id of the game.
     * @return the mnu income of the country.
     */
    Integer getMnuIncome(String name, List<String> pillagedProvinces, Long idGame);

    /**
     * Returns the european gold income of the country.
     *
     * @param provinces provinces owned and not pillaged by the country.
     * @param idGame    id of the game.
     * @return the european gold income of the country.
     */
    Integer getGoldIncome(List<String> provinces, Long idGame);

    /**
     * Returns the fleet (STZ + CTZ) level income.
     *
     * @param name   of the country.
     * @param idGame id of the game.
     * @return the fleet (STZ + CTZ) level income.
     */
    Integer getFleetLevelIncome(String name, Long idGame);

    /**
     * Returns the fleet (STZ + CTZ) monopoly income.
     *
     * @param name   of the country.
     * @param idGame id of the game.
     * @return the fleet (STZ + CTZ) level income.
     */
    Integer getFleetLevelMonopoly(String name, Long idGame);

    /**
     * Returns the trade centers by country.
     *
     * @param idGame id of the game.
     * @return the trade centers by country.
     */
    Map<String, List<CounterFaceTypeEnum>> getTradeCenters(Long idGame);
}
