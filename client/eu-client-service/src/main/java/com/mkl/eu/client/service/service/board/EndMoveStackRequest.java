package com.mkl.eu.client.service.service.board;

/**
 * Request for endMoveStack service.
 *
 * @author MKL.
 */
public class EndMoveStackRequest {
    /** Id of the stack to move. */
    private Long idStack;

    /**
     * Constructor for jaxb.
     */
    public EndMoveStackRequest() {
    }

    /**
     * Constructor.
     *
     * @param idStack the idStack to set.
     */
    public EndMoveStackRequest(Long idStack) {
        this.idStack = idStack;
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
