package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for the types of diff attributes.
 *
 * @author MKL.
 */
public enum DiffAttributeTypeEnum {
    /** Where the diff occurred (creation/removal of a counter/stack): name of the province. */
    PROVINCE,
    /** Where the diff came from (move of a stack): name of the province. */
    PROVINCE_FROM,
    /** Where the diff went to (move of a stack): name of the province. */
    PROVINCE_TO,
    /** Phase of the moving stack. */
    MOVE_PHASE,
    /** Where the diff came from (move of a counter): id of the stack. */
    STACK_FROM,
    /** Where the diff went to (move of a counter): id of the stack. */
    STACK_TO,
    /** To which stack the diff was made: id of the stack. */
    STACK,
    /** Which stack was deleted during the diff: id of the stack. */
    STACK_DEL,
    /** To which counter the diff was made: id of the counter. */
    COUNTER,
    /** Type of the  principal object. */
    TYPE,
    /** Country of the principal object. */
    COUNTRY,
    /** Name of the principal object. */
    NAME,
    /** Id of the playable country of the principal object (owner). */
    ID_COUNTRY,
    /** Flag saying that it is an invite (if not, it is a kick). */
    INVITE,
    /** Turn of the game. */
    TURN,
    /** Id of an external object. */
    ID_OBJECT,
    /** Type of the face of a counter. */
    COUNTER_FACE_TYPE,
    /** Cost of the action/object. */
    COST,
    /** Column of the action. */
    COLUMN,
    /** Bonus of the action. */
    BONUS,
    /** Status of the game. */
    STATUS,
    /** Number of veterans of a counter. */
    VETERANS,
    /** DTI of a country. */
    DTI,
    /** FTI of a country. */
    FTI,
    /** FTI for the Rotw of a country. */
    FTI_ROTW,
    /** Level of the item (can be colony, trading post, trade fleet,...). */
    LEVEL,
    /** Name of the land technology of the country. */
    TECH_LAND,
    /** Name of the naval technology of the country. */
    TECH_NAVAL,
    /** To use active field (for turn order, it will be the position of the new active turn orders). */
    ACTIVE,
    /** Number of move points of a stack. */
    MOVE_POINTS
}
