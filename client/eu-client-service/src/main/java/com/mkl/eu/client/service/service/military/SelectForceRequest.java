package com.mkl.eu.client.service.service.military;

/**
 * Request for selectForce service.
 *
 * @author MKL.
 */
public class SelectForceRequest {
    /** Id of the counter to select/deselect for the battle. */
    private Long idCounter;
    /** Flag saying if the counter is to select or deselect. */
    private boolean add;

    /**
     * Constructor for jaxb.
     */
    public SelectForceRequest() {
    }

    /**
     * Constructor.
     *
     * @param idCounter the idCounter to set.
     * @param add       the add to set.
     */
    public SelectForceRequest(Long idCounter, boolean add) {
        this.idCounter = idCounter;
        this.add = add;
    }

    /** @return the idCounter. */
    public Long getIdCounter() {
        return idCounter;
    }

    /** @param idCounter the idCounter to set. */
    public void setIdCounter(Long idCounter) {
        this.idCounter = idCounter;
    }

    /** @return the add. */
    public boolean isAdd() {
        return add;
    }

    /** @param add the add to set. */
    public void setAdd(boolean add) {
        this.add = add;
    }
}
