package com.mkl.eu.client.service.vo;

import org.apache.commons.lang3.StringUtils;

/**
 * Class description.
 *
 * @author MKL
 */
public class Country {
    /** Constant for Neutral country. */
    private static final String NEUTRAL_COUNTRY_NAME = "TECH_COUNTRY_NEUTRAL";
    /** Neutral country. */
    public static final Country NEUTRAL_COUNTRY;
    /** Name of the country. */
    private String name;

    static {
        NEUTRAL_COUNTRY = new Country();
        NEUTRAL_COUNTRY.setName(NEUTRAL_COUNTRY_NAME);
    }


    /** Constructor. */
    public Country() {

    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the name. */
    public String getName() {
        return name;
    }

    /**
     * @return <code>true</code> if the country is the neutral one.
     */
    public boolean isNeutral() {
        return StringUtils.equals(NEUTRAL_COUNTRY_NAME, name);
    }
}
