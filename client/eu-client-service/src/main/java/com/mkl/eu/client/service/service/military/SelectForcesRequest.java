package com.mkl.eu.client.service.service.military;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for selectForces service.
 *
 * @author MKL.
 */
public class SelectForcesRequest {
    /** List of Ids of the counters to select for the battle. */
    private List<Long> forces = new ArrayList<>();
    /** If there is an ambiguity on the leading country, select it here. */
    private String country;

    /**
     * Constructor for jaxb.
     */
    public SelectForcesRequest() {
    }

    /**
     * Constructor.
     *
     * @param forces the forces.
     */
    public SelectForcesRequest(List<Long> forces) {
        this.forces = forces;
    }

    /** @return the forces. */
    public List<Long> getForces() {
        return forces;
    }

    /** @param forces the forces to set. */
    public void setForces(List<Long> forces) {
        this.forces = forces;
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
