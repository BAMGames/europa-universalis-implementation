package com.mkl.eu.service.service.persistence.eco;

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
     * @param idGame    id of the game.
     * @param idCountry id of the country. Can be <code>null</code>.
     * @param turn      turn of the game.
     * @return the sheets.
     */
    List<EconomicalSheetEntity> loadSheets(Long idGame, Long idCountry, Integer turn);
}
