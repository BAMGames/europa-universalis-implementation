package com.mkl.eu.client.service.vo.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.eco.AdministrativeAction;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.event.EconomicalEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Country (can be minor or major).
 *
 * @author MKL
 */
public class Country extends EuObject {
    /** Constant for Neutral country. */
    private static final String NEUTRAL_COUNTRY_NAME = "TECH_COUNTRY_NEUTRAL";
    /** Neutral country. */
    public static final Country NEUTRAL_COUNTRY;
    /** Name of the country. */
    private String name;
    /** Counters of the country. */
    private List<Counter> counters;
    /** Monarchs (past and present) of the country. */
    private List<Monarch> monarchs;
    /** Discoveries of the country. */
    private List<Discovery> discoveries;
    /** Economical sheet by turn of the country. */
    private Map<Integer, EconomicalSheet> economicalSheets;
    /** Administrative actions by turn of the country. */
    private Map<Integer, AdministrativeAction> administrativeActions;
    /** Economical events by turn of the country. */
    private Map<Integer, EconomicalEvent> economicalEvents;

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
