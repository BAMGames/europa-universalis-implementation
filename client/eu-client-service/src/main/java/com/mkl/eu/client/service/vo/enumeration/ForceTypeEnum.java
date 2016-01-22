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
    /** A+ Timar. */
    ARMY_TIMAR_PLUS,
    /** A- Timar. */
    ARMY_TIMAR_MINUS,
    /** F+. */
    FLEET_PLUS,
    /** F-. */
    FLEET_MINUS,
    /** F+ Transport. */
    FLEET_TRANSPORT_PLUS,
    /** F- Galley. */
    FLEET_GALLEY_MINUS,
    /** LD. */
    LD,
    /** ND. */
    ND,
    /** LD TIMAR. */
    LDT,
    /** NWD. */
    NWD,
    /** NGD. */
    NGD,
    /** NTD. */
    NTD,
    /** LD or ND. */
    LDND,
    /** LDe. */
    LDE,
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
