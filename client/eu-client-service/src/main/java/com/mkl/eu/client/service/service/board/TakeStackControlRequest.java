package com.mkl.eu.client.service.service.board;

/**
 * Request for takeStackControl service.
 *
 * @author MKL.
 */
public class TakeStackControlRequest {
    /** Id of the stack to take control. */
    private Long idStack;
    /** Country as to take control (can be a minor under influence). */
    private String country;

    /**
     * Constructor for jaxb.
     */
    public TakeStackControlRequest() {
    }

    /**
     * Constructor.
     *
     * @param idStack the idStack to set.
     */
    public TakeStackControlRequest(Long idStack, String country) {
        this.idStack = idStack;
        this.country = country;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
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
