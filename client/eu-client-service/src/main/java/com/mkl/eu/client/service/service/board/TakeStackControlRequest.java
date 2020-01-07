package com.mkl.eu.client.service.service.board;

/**
 * Request for takeStackControl service.
 *
 * @author MKL.
 */
public class TakeStackControlRequest {
    /** Id of the stack to take control. */
    private Long idStack;
    /** Id of the leader that will take control. Can be <code>null</code>, then country is used. */
    private Long idLeader;
    /** Country as to take control (can be a minor under influence). Only used if no leader is specified. */
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
     * @param country the country that will lead the stack.
     */
    public TakeStackControlRequest(Long idStack, String country) {
        this.idStack = idStack;
        this.country = country;
    }

    /**
     * Constructor.
     *
     * @param idStack  the idStack to set.
     * @param idLeader id of the leader that will lead the stack.
     */
    public TakeStackControlRequest(Long idStack, Long idLeader) {
        this.idStack = idStack;
        this.idLeader = idLeader;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
    }

    /** @return the idLeader. */
    public Long getIdLeader() {
        return idLeader;
    }

    /** @param idLeader the idLeader to set. */
    public void setIdLeader(Long idLeader) {
        this.idLeader = idLeader;
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
