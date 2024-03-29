package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for the type of object concerned by a diff.
 *
 * @author MKL.
 */
public enum DiffTypeObjectEnum {
    /** Game. */
    GAME,
    /** Status. */
    STATUS,
    /** Counter. */
    COUNTER,
    /** Stack. */
    STACK,
    /** Room. */
    ROOM,
    /** Economical sheet. */
    ECO_SHEET,
    /** Administrative action. */
    ADM_ACT,
    /** Country. */
    COUNTRY,
    /** Turn order. */
    TURN_ORDER,
    /** Battle. */
    BATTLE,
    /** Siege. */
    SIEGE,
    /** Attrition. */
    ATTRITION,


    /********************************************/
    /**         Types used by NOTIFY            */
    /********************************************/
    /** Redeploy. */
    REDEPLOY
}
