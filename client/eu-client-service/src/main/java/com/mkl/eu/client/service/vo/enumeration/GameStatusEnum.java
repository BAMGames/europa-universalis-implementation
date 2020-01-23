package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of the status of a game.
 *
 * @author MKL
 */
public enum GameStatusEnum {
    /** Economical event segment. */
    ECONOMICAL_EVENT(true),
    /** Political event segment. */
    POLITICAL_EVENT(true),
    /** Diplomacy segment. */
    DIPLOMACY(true),
    /** Choice of administrative actions segment. */
    ADMINISTRATIVE_ACTIONS_CHOICE(true),


    /*********************************************************************************************************
     *                                          Military phases                                              *
     *********************************************************************************************************/
    /** Hierarchy adjustment segment. */
    MILITARY_HIERARCHY(true),
    /** Choice of campaign. */
    MILITARY_CAMPAIGN(false),
    /** Attrition caused by supply. */
    MILITARY_SUPPLY(false),
    /** Movement and discovery. */
    MILITARY_MOVE(false),
    /** Battles. */
    MILITARY_BATTLES(false),
    /** Sieges. */
    MILITARY_SIEGES(false),
    /** Fights against revolts, pirates and natives. */
    MILITARY_NEUTRALS(false),
    /*********************************************************************************************************
     *                                     End of military phases                                            *
     *********************************************************************************************************/


    /** Redeployment. */
    REDEPLOYMENT(false),
    /** Exchequer repartition. */
    EXCHEQUER(true),
    /** Stability improvement. */
    STABILITY(true);

    /** Flag saying if the phase is played simultaneous by all players or not. */
    private boolean simultaneous;

    private GameStatusEnum(boolean simultaneous) {
        this.simultaneous = simultaneous;
    }

    /** @return the simultaneous. */
    public boolean isSimultaneous() {
        return simultaneous;
    }
}
