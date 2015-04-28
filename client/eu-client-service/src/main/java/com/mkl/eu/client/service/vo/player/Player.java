package com.mkl.eu.client.service.vo.player;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.country.PlayableCountry;

/**
 * Player on a game. A player manages a country.
 *
 * @author MKL.
 */
public class Player extends EuObject {
    /** The country being managed by the player. */
    private PlayableCountry country;

    /** @return the country. */
    public PlayableCountry getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(PlayableCountry country) {
        this.country = country;
    }
}
