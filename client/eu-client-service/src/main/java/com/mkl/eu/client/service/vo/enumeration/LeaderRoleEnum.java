package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for the roles of leaders.
 *
 * @author MKL
 */
public enum LeaderRoleEnum {
    /** General (except kings). Land leader in Europe. */
    GENERAL,
    /** Admiral (except doges). Naval leader in Europe. */
    ADMIRAL,
    /** Conquistador. Land leader in ROTW. */
    CONQUISTADOR,
    /** Explorator. Naval leader in ROTW. */
    EXPLORATOR,
    /** Gouvernor. Lander leader in a single region of the ROTW. */
    GOUVERNOR,
    /** Pirate/Corsair. Naval leader in Europe and/or ROTW. */
    PIRATE,
    /** King general. Land leader in Europe, always rank A (AAA even). */
    KING_GENERAL,
    /** King admiral. Naval leader in Europe, always rank A(AAA even). Venise only. */
    KING_ADMIRAL,
    /** Engineer. On land. Can go with another leader. Only gives siege bonus. */
    ENGINEER,
    /** Pasha. On land. Special turkish leader. Can be corrupted in which case it is not a leader anymore. */
    PASHA;
}
