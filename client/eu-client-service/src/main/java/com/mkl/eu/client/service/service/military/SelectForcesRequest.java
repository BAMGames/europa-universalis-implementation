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

    /**
     * Constructor for jaxb.
     */
    public SelectForcesRequest() {
    }

    /** @return the forces. */
    public List<Long> getForces() {
        return forces;
    }

    /** @param forces the forces to set. */
    public void setForces(List<Long> forces) {
        this.forces = forces;
    }
}
