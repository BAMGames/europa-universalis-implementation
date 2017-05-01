package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of move phase.
 *
 * @author MKL
 */
public enum MovePhaseEnum {
    /** Has not moved in the round yet. */
    NOT_MOVED(false),
    /** Is currently moving in the round. */
    IS_MOVING(false),
    /** Has moved in the round yet. */
    MOVED(true),
    /** Has moved and will fight at the end of the round. */
    FIGHTING(true),
    /** Has moved and will siege at the end of the round. */
    BESIEGING(true),
    /** Has not moved and will siege at the end of the round. */
    STILL_BESIEGING(false);

    /** Flag to say if it has finished moving. */
    private boolean moved;

    /**
     * Constructor.
     *
     * @param moved the moved to set.
     */
    MovePhaseEnum(boolean moved) {
        this.moved = moved;
    }

    /** @return the moved. */
    public boolean isMoved() {
        return moved;
    }
}
