package com.mkl.eu.client.service.service.board;

/**
 * Request for moveCounter service.
 *
 * @author MKL.
 */
public class MoveCounterRequest {
    /** Id of the counter to move. */
    private Long idCounter;
    /** Id of the stack where the counter will move to. Can be <code>null</code> for creation of a stack. */
    private Long idStack;

    /**
     * Constructor for jaxb.
     */
    public MoveCounterRequest() {
    }

    /**
     * Constructor.
     *
     * @param idCounter the idCounter to set.
     * @param idStack   the idStack to set.
     */
    public MoveCounterRequest(Long idCounter, Long idStack) {
        this.idCounter = idCounter;
        this.idStack = idStack;
    }

    /** @return the idCounter. */
    public Long getIdCounter() {
        return idCounter;
    }

    /** @param idCounter the idCounter to set. */
    public void setIdCounter(Long idCounter) {
        this.idCounter = idCounter;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
    }
}
