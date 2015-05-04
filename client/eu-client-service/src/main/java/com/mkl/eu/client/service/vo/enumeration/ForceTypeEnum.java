package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of type of forces (A+, A-, LD,..).
 *
 * @author MKL
 */
public enum ForceTypeEnum {
    /** A+. */
    ARMY_PLUS,
    /** A-. */
    ARMY_MINUS,
    /** F+. */
    FLEET_PLUS,
    /** F-. */
    FLEET_MINUS,
    /** LD. */
    LD,
    /** ND. */
    ND,
    /** LD or ND. */
    LDND,
    /** NDe. */
    DE,
    /** P+. */
    P_PLUS,
    /** P-. */
    P_MINUS,
    /** Fortress. */
    F,
    /** Multiple Campaign. */
    MC,
    /** Anonymous General. */
    LG,
    /** Anonymous Admiral. */
    LA,
    /** Anonymous Explorer. */
    LE,
    /** Anonymous Conquistador. */
    LC,
    /** King. */
    LK;
}
