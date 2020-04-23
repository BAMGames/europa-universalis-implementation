package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of move phase.
 *
 * @author MKL
 */
public enum MovePhaseEnum {
    /** Has not moved in the round yet. */
    NOT_MOVED(false, false),
    /** Is currently moving in the round. */
    IS_MOVING(false, false),
    /** Is currently moving in the round and has entered at least once enemy territory. */
    IS_MOVING_AGGRESSIVE(false, false),
    /** Has moved in the round yet. */
    MOVED(true, false),
    /** Has moved and will fight at the end of the round. */
    FIGHTING(true, false),
    /** Has moved and will siege at the end of the round. */
    BESIEGING(true, true),
    /** Has not moved and will siege at the end of the round. */
    STILL_BESIEGING(false, true),
    /** Is looting during the interphase. */
    LOOTING(false, false),
    /** Is looting and still besieging during the interphase. */
    LOOTING_BESIEGING(false, true);

    /** Flag to say if it has finished moving. */
    private boolean moved;
    /** Flag to say if it is besieging. */
    private boolean besieging;

    /**
     * Constructor.
     *
     * @param moved the moved to set.
     */
    MovePhaseEnum(boolean moved, boolean besieging) {
        this.moved = moved;
        this.besieging = besieging;
    }

    /** @return the moved. */
    public boolean isMoved() {
        return moved;
    }

    /** @return the besieging. */
    public boolean isBesieging() {
        return besieging;
    }
}
