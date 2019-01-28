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
    /** If the stack is besieging or lifting the siege. */
    BESIEGED,
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
    MOVE_POINTS,
    /** To say if the attacker is ready. */
    PHASING_READY,
    /** To say if the defender is ready. */
    NON_PHASING_READY,
    /** Counter to add as attacker. */
    PHASING_COUNTER_ADD,
    /** Counter to add as defender. */
    NON_PHASING_COUNTER_ADD,
    /** Counter to remove as attacker. */
    PHASING_COUNTER_REMOVE,
    /** Counter to remove as defender. */
    NON_PHASING_COUNTER_REMOVE,
    /** Cause of end. */
    END,
    /** Who is the winner. */
    WINNER,
    /** Size of phasing forces in a battle. */
    BATTLE_PHASING_SIZE,
    /** Tech of phasing forces in a battle. */
    BATTLE_PHASING_TECH,
    /** Fire column of phasing forces in a battle. */
    BATTLE_PHASING_FIRE_COL,
    /** Shock column of phasing forces in a battle. */
    BATTLE_PHASING_SHOCK_COL,
    /** Moral of phasing forces in a battle. */
    BATTLE_PHASING_MORAL,
    /** Pursuit modifier of phasing forces in a battle. */
    BATTLE_PHASING_PURSUIT_MOD,
    /** Pursuit unmodified die roll of phasing forces in a battle. */
    BATTLE_PHASING_PURSUIT,
    /** Size modifier of phasing forces in a battle. */
    BATTLE_PHASING_SIZE_DIFF,
    /** Retreat unmodified die roll of phasing forces in a battle. */
    BATTLE_PHASING_RETREAT,
    /** Round losses taken by phasing forces in a battle. */
    BATTLE_PHASING_ROUND_LOSS,
    /** Third losses taken by phasing forces in a battle. */
    BATTLE_PHASING_THIRD_LOSS,
    /** Morale losses taken by phasing forces in a battle. */
    BATTLE_PHASING_MORALE_LOSS,
    /** Fire modifier of phasing forces in the first day of a battle. */
    BATTLE_PHASING_FIRST_DAY_FIRE_MOD,
    /** Fire unmodified die roll of phasing forces in the first day of a battle. */
    BATTLE_PHASING_FIRST_DAY_FIRE,
    /** Shock modifier of phasing forces in the first day of a battle. */
    BATTLE_PHASING_FIRST_DAY_SHOCK_MOD,
    /** Shock unmodified die roll of phasing forces in the first day of a battle. */
    BATTLE_PHASING_FIRST_DAY_SHOCK,
    /** Fire modifier of phasing forces in the second day of a battle. */
    BATTLE_PHASING_SECOND_DAY_FIRE_MOD,
    /** Fire unmodified die roll of phasing forces in the second day of a battle. */
    BATTLE_PHASING_SECOND_DAY_FIRE,
    /** Shock modifier of phasing forces in the second day of a battle. */
    BATTLE_PHASING_SECOND_DAY_SHOCK_MOD,
    /** Shock unmodified die roll of phasing forces in the second day of a battle. */
    BATTLE_PHASING_SECOND_DAY_SHOCK,
    /** Size of non phasing forces in a battle. */
    BATTLE_NON_PHASING_SIZE,
    /** Tech of non phasing forces in a battle. */
    BATTLE_NON_PHASING_TECH,
    /** Fire column of non phasing forces in a battle. */
    BATTLE_NON_PHASING_FIRE_COL,
    /** Shock column of non phasing forces in a battle. */
    BATTLE_NON_PHASING_SHOCK_COL,
    /** Moral of non phasing forces in a battle. */
    BATTLE_NON_PHASING_MORAL,
    /** Pursuit modifier of non phasing forces in a battle. */
    BATTLE_NON_PHASING_PURSUIT_MOD,
    /** Pursuit unmodified die roll of non phasing forces in a battle. */
    BATTLE_NON_PHASING_PURSUIT,
    /** Size modifier of non phasing forces in a battle. */
    BATTLE_NON_PHASING_SIZE_DIFF,
    /** Retreat unmodified die roll of non phasing forces in a battle. */
    BATTLE_NON_PHASING_RETREAT,
    /** Round losses taken by non phasing forces in a battle. */
    BATTLE_NON_PHASING_ROUND_LOSS,
    /** Third losses taken by non phasing forces in a battle. */
    BATTLE_NON_PHASING_THIRD_LOSS,
    /** Morale losses taken by non phasing forces in a battle. */
    BATTLE_NON_PHASING_MORALE_LOSS,
    /** Fire modifier of non phasing forces in the first day of a battle. */
    BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD,
    /** Fire unmodified die roll of non phasing forces in the first day of a battle. */
    BATTLE_NON_PHASING_FIRST_DAY_FIRE,
    /** Shock modifier of non phasing forces in the first day of a battle. */
    BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD,
    /** Shock unmodified die roll of non phasing forces in the first day of a battle. */
    BATTLE_NON_PHASING_FIRST_DAY_SHOCK,
    /** Fire modifier of non phasing forces in the second day of a battle. */
    BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD,
    /** Fire unmodified die roll of non phasing forces in the second day of a battle. */
    BATTLE_NON_PHASING_SECOND_DAY_FIRE,
    /** Shock modifier of non phasing forces in the second day of a battle. */
    BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD,
    /** Shock unmodified die roll of non phasing forces in the second day of a battle. */
    BATTLE_NON_PHASING_SECOND_DAY_SHOCK


}
