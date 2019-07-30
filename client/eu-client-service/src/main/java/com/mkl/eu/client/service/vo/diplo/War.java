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
    /** Name of the war. */
    private String name;
    /** List of countries in war (either side). */
    private List<CountryInWar> countries = new ArrayList<>();
    /** Type of war. */
    private WarTypeEnum type;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

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

    /**
     * @return the light object of this instance.
     */
    public WarLight toLight() {
        WarLight light = new WarLight();

        light.setId(getId());
        light.setType(getType());
        light.setName(getName());

        return light;
    }
}
