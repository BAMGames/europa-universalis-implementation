package com.mkl.eu.client.service.service.board;

/**
 * Request for removeCounter service.
 *
 * @author MKL.
 */
public class RemoveCounterRequest {
    /** Id of the counter to remove. */
    private Long idCounter;

    /**
     * Constructor for jaxb.
     */
    public RemoveCounterRequest() {
    }

    /**
     * Constructor.
     *
     * @param idCounter the idCounter to set.
     */
    public RemoveCounterRequest(Long idCounter) {
        this.idCounter = idCounter;
    }

    /** @return the idCounter. */
    public Long getIdCounter() {
        return idCounter;
    }

    /** @param idCounter the idCounter to set. */
    public void setIdCounter(Long idCounter) {
        this.idCounter = idCounter;
    }
}
