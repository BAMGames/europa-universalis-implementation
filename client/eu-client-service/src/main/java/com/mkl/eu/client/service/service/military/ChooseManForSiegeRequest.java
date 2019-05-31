package com.mkl.eu.client.service.service.military;

/**
 * Request for chooseMan service.
 *
 * @author MKL.
 */
public class ChooseManForSiegeRequest {
    /** If the fortress will be manned or not. */
    private boolean man;
    /** If the fortress is manned, the counter that will take a loss. */
    private Long idCounter;

    /**
     * Constructor for jaxb.
     */
    public ChooseManForSiegeRequest() {
    }

    /**
     * Constructor.
     *
     * @param man       the man to set.
     * @param idCounter the idCounter to set.
     */
    public ChooseManForSiegeRequest(boolean man, Long idCounter) {
        this.man = man;
        this.idCounter = idCounter;
    }

    /** @return the man. */
    public boolean isMan() {
        return man;
    }

    /** @param man the man to set. */
    public void setMan(boolean man) {
        this.man = man;
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
