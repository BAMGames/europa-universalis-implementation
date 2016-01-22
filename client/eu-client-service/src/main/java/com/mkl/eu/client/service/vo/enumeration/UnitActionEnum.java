package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for the action of a unit (purchase, maintenance).
 *
 * @author MKL.
 */
public enum UnitActionEnum {
    /** Purchase. */
    PURCHASE,
    /** Maintenance (naval). */
    MAINT,
    /** Maintenance in war (land). */
    MAINT_WAR,
    /** Maintenance in peace (land). */
    MAINT_PEACE;
}
