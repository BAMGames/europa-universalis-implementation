package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for cause of end battle.
 *
 * @author MKL.
 */
public enum BattleEndEnum {
    /** If the non phasing stack withdraw before battle. */
    WITHDRAW_BEFORE_BATTLE,
    /** If the stack with wind advantage retreat after first fire (sea only). */
    RETREAT_AT_FIRST_FRE,
    /** If any stack is routed after first fire. */
    ROUTED_AT_FIRST_FIRE,
    /** If any stack is routed after first shock. */
    ROUTED_AT_FIRST_SHOCK,
    /** If any stack is annihilated after first day (land only). */
    ANNIHILATED_AT_FIRST_DAY,
    /** If any stack retreats after first day. */
    RETREAT_AT_FIRST_DAY,
    /** If the stack with wind advantage retreat after second fire (sea only). */
    RETREAT_AT_SECOND_FIRE,
    /** If any stack is routed after second fire. */
    ROUTED_AT_SECOND_FIRE,
    /** If any stack is routed after second shock. */
    ROUTED_AT_SECOND_SHOCK,
    /** If there was two days of battle. */
    END_OF_SECOND_DAY
}
