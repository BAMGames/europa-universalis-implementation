package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of move phase.
 *
 * @author MKL
 */
public enum MovePhaseEnum {
    /** Has not moved in the round yet. */
    NOT_MOVED,
    /** Is currently moving in the round. */
    IS_MOVING,
    /** Is currently moving in the round and has entered at least once enemy territory. */
    IS_MOVING_AGGRESSIVE,
    /** Has moved in the round yet. */
    MOVED,
    /** Has moved and will fight at the end of the round. */
    FIGHTING,
    /** Has moved and will siege at the end of the round. */
    BESIEGING,
    /** Has not moved and will siege at the end of the round. */
    STILL_BESIEGING,
    /** Is looting during the interphase. */
    LOOTING,
    /** Is looting and still besieging during the interphase. */
    LOOTING_BESIEGING
}
