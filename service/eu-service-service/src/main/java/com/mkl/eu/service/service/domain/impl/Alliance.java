package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Alliance of countries that will move at the same segment.
 */
public class Alliance {
    /** Countries part of the alliance. */
    private List<PlayableCountryEntity> countries;
    /** Initiative of the alliance. */
    private int initiative;

    public Alliance(List<PlayableCountryEntity> countries, int initiative) {
        this.countries = countries;
        this.initiative = initiative;
    }

    /** @return the countries. */
    public List<PlayableCountryEntity> getCountries() {
        return countries;
    }

    /** @return the initiative. */
    public int getInitiative() {
        return initiative;
    }

    /**
     * Try to fusion this with alliance.
     * If this and alliance shares a country, then countries are merged into
     * this and minimum initiative of both.
     * Do nothing if no country is shared.
     *
     * @param alliance to fusion with.
     * @return <code>true</code> if fusion succeeded, <code>false</code> otherwise.
     */
    public boolean fusion(Alliance alliance) {
        boolean fusion = false;

        for (PlayableCountryEntity country : alliance.getCountries()) {
            if (countries.contains(country)) {
                fusion = true;
                break;
            }
        }

        if (fusion) {
            for (PlayableCountryEntity country : alliance.getCountries()) {
                if (!countries.contains(country)) {
                    countries.add(country);
                }
            }

            initiative = Math.min(initiative, alliance.getInitiative());
        }

        return fusion;
    }

    /**
     * Fusion alliances base on countries.
     * Fusion is transitive.
     *
     * @param alliances to fusion.
     */
    public static void fusion(List<Alliance> alliances) {
        List<Alliance> allianceToDelete = new ArrayList<>();

        for (Alliance alliance : alliances) {
            if (allianceToDelete.contains(alliance)) {
                continue;
            }
            for (Alliance allianceNext : alliances) {
                if (alliance == allianceNext) {
                    continue;
                }

                if (alliance.fusion(allianceNext)) {
                    allianceToDelete.add(allianceNext);
                }
            }
        }

        if (!allianceToDelete.isEmpty()) {
            alliances.removeAll(allianceToDelete);
            fusion(alliances);
        }
    }
}
