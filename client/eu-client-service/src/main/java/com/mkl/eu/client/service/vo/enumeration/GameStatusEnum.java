package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of the status of a game.
 *
 * @author MKL
 */
public enum GameStatusEnum {
    /** Economical event segment. */
    ECONOMICAL_EVENT,
    /** Political event segment. */
    POLITICAL_EVENT,
    /** Diplomacy segment. */
    DIPLOMACY,
    /** Choice of administrative actions segment. */
    ADMINISTRATIVE_ACTIONS_CHOICE,


    /*********************************************************************************************************
     *                                          Military phases                                              *
     *********************************************************************************************************/
    /** Hierarchy adjustment segment. */
    MILITARY_HIERARCHY,
    /** Choice of campaign. */
    MILITARY_CAMPAIGN,
    /** Attrition caused by supply. */
    MILITARY_SUPPLY,
    /** Movement and discovery. */
    MILITARY_MOVE,
    /** Battles. */
    MILITARY_BATTLES,
    /** Sieges. */
    MILITARY_SIEGES,
    /** Fights against revolts, pirates and natives. */
    MILITARY_NEUTRALS

}
