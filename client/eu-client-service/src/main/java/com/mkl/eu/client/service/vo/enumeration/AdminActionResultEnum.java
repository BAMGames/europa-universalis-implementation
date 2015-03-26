package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of the administrative actions.
 *
 * @author MKL
 */
public enum AdminActionResultEnum {
    /** Fumble (big failaure). */
    FUMBLE,
    /** Failed. */
    FAILED,
    /** Half success. */
    AVERAGE,
    /** Half success with honors. */
    AVERAGE_PLUS,
    /** Success. */
    SUCCESS,
    /** Exceptional success. */
    CRITICAL_HIT
}
