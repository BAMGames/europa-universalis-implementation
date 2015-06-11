package com.mkl.eu.client.service.service.board;

/**
 * Request for moveStack service.
 *
 * @author MKL.
 */
public class MoveStackRequest {
    /** Id of the stack to move. */
    private Long idStack;
    /** Province where the stack should move. */
    private String provinceTo;

    /**
     * Constructor for jaxb.
     */
    public MoveStackRequest() {
    }

    /**
     * Constructor.
     *
     * @param idStack     the idStack to set.
     * @param provinceTo  the provinceTo to set.
     */
    public MoveStackRequest(Long idStack, String provinceTo) {
        this.idStack = idStack;
        this.provinceTo = provinceTo;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
    }

    /** @return the provinceTo. */
    public String getProvinceTo() {
        return provinceTo;
    }

    /** @param provinceTo the provinceTo to set. */
    public void setProvinceTo(String provinceTo) {
        this.provinceTo = provinceTo;
    }
}
