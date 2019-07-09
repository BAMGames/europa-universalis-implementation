package com.mkl.eu.client.service.vo.diplo;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.WarTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * VO that describes a War.
 *
 * @author MKL.
 */
public class War extends EuObject {
    /** List of countries in war (either side). */
    private List<CountryInWar> countries = new ArrayList<>();
    /** Type of war. */
    private WarTypeEnum type;

    /** @return the countries. */
    public List<CountryInWar> getCountries() {
        return countries;
    }

    /** @param countries the countries to set. */
    public void setCountries(List<CountryInWar> countries) {
        this.countries = countries;
    }

    /** @return the type. */
    public WarTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(WarTypeEnum type) {
        this.type = type;
    }
}
