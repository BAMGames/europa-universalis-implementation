package com.mkl.eu.client.service.vo.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.eco.AdministrativeAction;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.event.EconomicalEvent;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Country (can be minor or major).
 *
 * @author MKL
 */
public class Country extends EuObject {
    /**
     * Constant for Neutral country.
     */
    private static final String NEUTRAL_COUNTRY_NAME = "TECH_COUNTRY_NEUTRAL";
    /**
     * Neutral country.
     */
    public static final Country NEUTRAL_COUNTRY;
    /**
     * Name of the country.
     */
    private String name;
    /**
     * Counters of the country.
     */
    private List<Counter> counters = new ArrayList<>();
    /**
     * Monarchs (past and present) of the country.
     */
    private List<Monarch> monarchs = new ArrayList<>();
    /**
     * Discoveries of the country.
     */
    private List<Discovery> discoveries = new ArrayList<>();
    /**
     * Economical sheet by turn of the country.
     */
    private List<EconomicalSheet> economicalSheets;
    /**
     * Administrative actions by turn of the country.
     */
    private List<AdministrativeAction> administrativeActions;
    /**
     * Economical events by turn of the country.
     */
    private List<EconomicalEvent> economicalEvents;

    static {
        NEUTRAL_COUNTRY = new Country();
        NEUTRAL_COUNTRY.setName(NEUTRAL_COUNTRY_NAME);
    }

    /**
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return <code>true</code> if the country is the neutral one.
     */
    public boolean isNeutral() {
        return StringUtils.equals(NEUTRAL_COUNTRY_NAME, name);
    }

    /**
     * @return the counters.
     */
    @XmlTransient
    public List<Counter> getCounters() {
        return counters;
    }

    /**
     * @param counters the counters to set.
     */
    public void setCounters(List<Counter> counters) {
        this.counters = counters;
    }

    /**
     * @return the monarchs.
     */
    public List<Monarch> getMonarchs() {
        return monarchs;
    }

    /**
     * @param monarchs the monarchs to set.
     */
    public void setMonarchs(List<Monarch> monarchs) {
        this.monarchs = monarchs;
    }

    /**
     * @return the discoveries.
     */
    public List<Discovery> getDiscoveries() {
        return discoveries;
    }

    /**
     * @param discoveries the discoveries to set.
     */
    public void setDiscoveries(List<Discovery> discoveries) {
        this.discoveries = discoveries;
    }

    /**
     * @return the economicalSheets.
     */
    public List<EconomicalSheet> getEconomicalSheets() {
        return economicalSheets;
    }

    /**
     * @param economicalSheets the economicalSheets to set.
     */
    public void setEconomicalSheets(List<EconomicalSheet> economicalSheets) {
        this.economicalSheets = economicalSheets;
    }

    /**
     * @return the administrativeActions.
     */
    public List<AdministrativeAction> getAdministrativeActions() {
        return administrativeActions;
    }

    /**
     * @param administrativeActions the administrativeActions to set.
     */
    public void setAdministrativeActions(List<AdministrativeAction> administrativeActions) {
        this.administrativeActions = administrativeActions;
    }

    /**
     * @return the economicalEvents.
     */
    public List<EconomicalEvent> getEconomicalEvents() {
        return economicalEvents;
    }

    /**
     * @param economicalEvents the economicalEvents to set.
     */
    public void setEconomicalEvents(List<EconomicalEvent> economicalEvents) {
        this.economicalEvents = economicalEvents;
    }
}
