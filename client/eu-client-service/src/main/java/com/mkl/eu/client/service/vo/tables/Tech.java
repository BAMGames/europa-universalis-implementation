package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * VO for the technology.
 *
 * @author MKL.
 */
public class Tech extends EuObject implements Comparable<Tech> {
    /** Name of the medieval technology. */
    public static final String MEDIEVAL = "MEDIEVAL";
    /** Name of the renaissance technology. */
    public static final String RENAISSANCE = "RENAISSANCE";
    /** Name of the arquebus technology. */
    public static final String ARQUEBUS = "ARQUEBUS";
    /** Name of the musket technology. */
    public static final String MUSKET = "MUSKET";
    /** Name of the baroque technology. */
    public static final String BAROQUE = "BAROQUE";
    /** Name of the manoeuvre technology. */
    public static final String MANOEUVRE = "MANOEUVRE";
    /** Name of the lace war technology. */
    public static final String LACE_WAR = "LACE_WAR";
    /** Name of the galley technology. */
    public static final String GALLEY = "GALLEY";
    /** Name of the carrack technology. */
    public static final String CARRACK = "CARRACK";
    /** Name of the nae galeon technology. */
    public static final String NAE_GALEON = "NAE_GALEON";
    /** Name of the galleon fluyt technology. */
    public static final String GALLEON_FLUYT = "GALLEON_FLUYT";
    /** Name of the battery technology. */
    public static final String BATTERY = "BATTERY";
    /** Name of the vessel technology. */
    public static final String VESSEL = "VESSEL";
    /** Name of the three decker technology. */
    public static final String THREE_DECKER = "THREE_DECKER";
    /** Name of the seventy four technology. */
    public static final String SEVENTY_FOUR = "SEVENTY_FOUR";
    /** Name of the tercio technology. */
    public static final String TERCIO = "TERCIO";
    /** Name of the galleass technology. */
    public static final String GALLEASS = "GALLEASS";
    /** Name of the technology. */
    private String name;
    /** Flag to know if the technology is land or naval. */
    private boolean land;
    /** Country that can have this technology, <code>null</code> for all. */
    private String country;
    /** Box where the technology begins. */
    private Integer beginBox;
    /** Turn when the technology can be reached. */
    private Integer beginTurn;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the land. */
    public boolean isLand() {
        return land;
    }

    /** @param land the land to set. */
    public void setLand(boolean land) {
        this.land = land;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the beginBox. */
    public Integer getBeginBox() {
        return beginBox;
    }

    /** @param beginBox the beginBox to set. */
    public void setBeginBox(Integer beginBox) {
        this.beginBox = beginBox;
    }

    /** @return the beginTurn. */
    public Integer getBeginTurn() {
        return beginTurn;
    }

    /** @param beginTurn the beginTurn to set. */
    public void setBeginTurn(Integer beginTurn) {
        this.beginTurn = beginTurn;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Tech o) {
        return getBeginTurn().compareTo(o.getBeginTurn());
    }
}
