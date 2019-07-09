package com.mkl.eu.client.service.vo.diplo;

import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import com.mkl.eu.client.service.vo.ref.country.CountryLight;

/**
 * VO of the association table between War and Country.
 *
 * @author MKL.
 */
public class CountryInWar {
    /** Country involved. */
    private CountryLight country;
    /** Flag to know in which side of the war is the country. */
    private boolean offensive;
    /** Implication of the country in the war. */
    private WarImplicationEnum implication;

    /** @return the country. */
    public CountryLight getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(CountryLight country) {
        this.country = country;
    }

    /** @return the offensive. */
    public boolean isOffensive() {
        return offensive;
    }

    /** @param offensive the offensive to set. */
    public void setOffensive(boolean offensive) {
        this.offensive = offensive;
    }

    /** @return the implication. */
    public WarImplicationEnum getImplication() {
        return implication;
    }

    /** @param implication the implication to set. */
    public void setImplication(WarImplicationEnum implication) {
        this.implication = implication;
    }
}
