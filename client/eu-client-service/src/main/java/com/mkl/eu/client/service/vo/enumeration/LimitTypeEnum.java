package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of type of limits (Max DTI or FTI, number of MNU/COL/TP, number of diplomatic/TFI/COL/TP/Concurrency actions,,..).
 *
 * @author MKL
 */
public enum LimitTypeEnum {
    /** Number of diplomatic action. */
    ACTION_DIPLO,
    /** Number of trade fleet implantation action. */
    ACTION_TFI,
    /** Number of colony action. */
    ACTION_COL,
    /** Number of trading post action. */
    ACTION_TP,
    /** Number of colony/trading post action. */
    ACTION_COL_TP,
    /** Number of concurrency action. */
    ACTION_CONCURRENCY,
    /** Number of LD at normal cost to buy. */
    PURCHASE_LAND_TROOPS,
    /** Number of ND possible to buy. */
    PURCHASE_NAVAL_TROOPS,
    /** Minimum number of general. */
    LEADER_GENERAL,
    /** Minimum number of general america. */
    LEADER_GENERAL_AMERICA,
    /** Minimum number of admiral. */
    LEADER_ADMIRAL,
    /** Minimum number of conquistador. */
    LEADER_CONQUISTADOR,
    /** Minimum number of conquistador india. */
    LEADER_CONQUISTADOR_INDIA,
    /** Minimum number of explorer. */
    LEADER_EXPLORER,
    /** Minimum number of governor. */
    LEADER_GOVERNOR
}
