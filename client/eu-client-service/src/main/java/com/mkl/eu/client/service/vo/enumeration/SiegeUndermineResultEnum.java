package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of status for sieges.
 *
 * @author MKL
 */
public enum SiegeUndermineResultEnum {
    /** Siegework minus. Siege will go on. */
    SIEGE_WORK_MINUS,
    /** Siegework plus. Siege will go on. */
    SIEGE_WORK_PLUS,
    /** Breach. Besieger had a breach and will immediately perform an assault and maybe end the siege. */
    BREACH_TAKEN,
    /** Breach. Besieger had a breach but choose not to use it immediately. */
    BREACH_NOT_TAKEN,
    /** The fortress falls but any troop inside are given back. The siege ends. */
    WAR_HONOUR,
    /** The fortress surrenders. The siege ends. */
    SURRENDER
}
