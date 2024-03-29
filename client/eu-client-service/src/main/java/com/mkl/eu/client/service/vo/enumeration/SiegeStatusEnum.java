package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of status for sieges.
 *
 * @author MKL
 */
public enum SiegeStatusEnum {
    /** When the battle is waiting for process. */
    NEW(false),
    /** When a side has too many forces on the battlefield and must select the forces in battle. */
    SELECT_FORCES(true),
    /** The besieger chooses between an undermine, an assault and a redeploy. */
    CHOOSE_MODE(true),
    /** The besieger can choose to take a breach after an undermine. */
    CHOOSE_BREACH(true),
    /** If needed, each sides must select the forces lost in battle. */
    CHOOSE_LOSS(true),
    /** In case of undermining result of war honour, the besieging forces are redeployed elsewhere. Also apply to leaders when fortress falls. */
    REDEPLOY(true),
    /** When the fortress falls, the besieger chooses to man it or not. */
    CHOOSE_MAN(true),
    /** Battle computed. */
    DONE(false);

    /** Flag saying that the status represents an active battle. */
    private boolean active;

    /**
     * Constructor.
     *
     * @param active the active to set.
     */
    SiegeStatusEnum(boolean active) {
        this.active = active;
    }

    /** @return the active. */
    public boolean isActive() {
        return active;
    }
}
