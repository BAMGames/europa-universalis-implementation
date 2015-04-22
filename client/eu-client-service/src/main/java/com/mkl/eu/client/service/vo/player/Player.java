package com.mkl.eu.client.service.vo.player;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.country.Country;

/**
 * Player on a game. A player manages a country.
 *
 * @author MKL.
 */
public class Player extends EuObject {
    /** The country being managed by the player. */
    private Country country;

    /** @return the country. */
    public Country getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(Country country) {
        this.country = country;
    }
}
