package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of status for battles.
 *
 * @author MKL
 */
public enum BattleStatusEnum {
    /** When the battle is waiting for process. */
    NEW(false),
    /** When a side has too many forces on the battlefield and must select the forces in battle. */
    SELECT_FORCES(true),
    /** Defender can withdraw before battle. */
    WITHDRAW_BEFORE_BATTLE(true),
    /** The attacker can retreat after first day of battle. */
    RETREAT_AFTER_FIRST_DAY_ATT(true),
    /** The defender can retreat after first day of battle. */
    RETREAT_AFTER_FIRST_DAY_DEF(true),
    /** If needed, each sides must select the forces lost in battle. */
    CHOOSE_LOSS(true),
    /** Retreat of the looser of the battle. */
    RETREAT(true),
    /** Battle computed. */
    DONE(false);

    /** Flag saying that the status represents an active battle. */
    private boolean active;

    /**
     * Constructor.
     *
     * @param active the active to set.
     */
    BattleStatusEnum(boolean active) {
        this.active = active;
    }

    /** @return the active. */
    public boolean isActive() {
        return active;
    }
}
