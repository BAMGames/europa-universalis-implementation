package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diplo.CountryInWar;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility around the war.
 *
 * @author MKL.
 */
public final class WarUtil {
    /**
     * Constructor.
     */
    private WarUtil() {

    }

    /**
     * @param name of the country.
     * @param game the game.
     * @return the allies of the country in the game.
     */
    public static List<String> getAllies(String name, Game game) {
        List<String> allies = game.getWars().stream()
                .flatMap(war -> getCountriesInWar(war, name, true).stream())
                .distinct()
                .collect(Collectors.toList());

        if (allies.isEmpty()) {
            allies.add(name);
        }
        return allies;
    }

    /**
     * @param name of the country.
     * @param game the game.
     * @return the enemies of the country in the game.
     */
    public static List<String> getEnemies(String name, Game game) {
        return game.getWars().stream()
                .flatMap(war -> getCountriesInWar(war, name, false).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * @param war    the war.
     * @param name   of the country.
     * @param allies to retrieve the allies or the enemies.
     * @return the countries allies or enemies with the country in a war.
     */
    public static List<String> getCountriesInWar(War war, String name, boolean allies) {
        CountryInWar self = war.getCountries().stream()
                .filter(warCountry -> warCountry.getImplication() == WarImplicationEnum.FULL &&
                        StringUtils.equals(warCountry.getCountry().getName(), name))
                .findAny()
                .orElse(null);
        return war.getCountries().stream()
                .filter(otherCountry -> self != null && otherCountry.getImplication() == WarImplicationEnum.FULL &&
                        (otherCountry.isOffensive() == self.isOffensive() && allies ||
                                otherCountry.isOffensive() != self.isOffensive() && !allies))
                .map(country -> country.getCountry().getName())
                .collect(Collectors.toList());
    }
}
