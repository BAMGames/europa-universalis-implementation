package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

/**
 * Counter (A+, MNU, fortress,...) used for the service createCounter.
 * Does not extends EuOBject on purpose.
 *
 * @author MKL
 */
public class CounterForCreation {
    /** Type of the counter. */
    private CounterFaceTypeEnum type;
    /** Name of the country. */
    private String country;

    /** @return the type. */
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }
}
