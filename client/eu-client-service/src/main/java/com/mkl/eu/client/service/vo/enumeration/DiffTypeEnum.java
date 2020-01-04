package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration for the type of diff.
 *
 * @author MKL.
 */
public enum DiffTypeEnum {
    /**
     * <p>
     * Add something.
     * </p>
     * <p>
     * Can be used with:
     * <ul>
     * <li>DiffTypeObjectEnum#COUNTER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#PROVINCE</li>
     * <li>DiffAttributeTypeEnum#TYPE for the type of the counter</li>
     * <li>DiffAttributeTypeEnum#COUNTRY for the name of the country of the counter</li>
     * <li>DiffAttributeTypeEnum#STACK for the id of the stack (can be a new one)</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#ROOM in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#NAME for the name of the room</li>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY for the id of the owner</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#ADM_ACT in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY for the id of the owner</li>
     * <li>DiffAttributeTypeEnum#TURN for the turn of the action</li>
     * <li>DiffAttributeTypeEnum#TYPE for the type of the action</li>
     * <li>DiffAttributeTypeEnum#COST for the an eventual cost of the action</li>
     * <li>DiffAttributeTypeEnum#ID_OBJECT for the id of an eventual object</li>
     * <li>DiffAttributeTypeEnum#PROVINCE for the name of an eventual province</li>
     * <li>DiffAttributeTypeEnum#COUNTER_FACE_TYPE for the type of an eventual counter face</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#STACK in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#PROVINCE</li>
     * <li>DiffAttributeTypeEnum#COUNTRY</li>
     * <li>DiffAttributeTypeEnum#MOVE_PHASE - optional attribute</li>
     * <li>DiffAttributeTypeEnum#BESIEGED - optional attribute</li>
     * </ul>
     * </li>
     * </li>
     * <li>DiffTypeObjectEnum#BATTLE in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#PROVINCE</li>
     * <li>DiffAttributeTypeEnum#TURN</li>
     * <li>DiffAttributeTypeEnum#STATUS</li>
     * <li>DiffAttributeTypeEnum#ID_WAR the war related to this battle</li>
     * <li>DiffAttributeTypeEnum#PHASING_OFFENSIVE if the phasing side is the offensive side of the war</li>
     * </ul>
     * </li>
     * </li>
     * <li>DiffTypeObjectEnum#SIEGE in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#PROVINCE</li>
     * <li>DiffAttributeTypeEnum#TURN</li>
     * <li>DiffAttributeTypeEnum#STATUS</li>
     * <li>DiffAttributeTypeEnum#ID_WAR the war related to this battle</li>
     * <li>DiffAttributeTypeEnum#PHASING_OFFENSIVE if the besieging side is the offensive side of the war</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#ECHO_SHEET in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY for the country owning the sheet (mandatory).</li>
     * <li>DiffAttributeTypeEnum#TURN</li>
     * <li>DiffAttributeTypeEnum#ROYAL_TREASURE_START for the royal treasure at start line.</li>
     * </ul>
     * </li>
     * </ul>
     */
    ADD,
    /**
     * <p>
     * Remove something.
     * </p>
     * <p>
     * Can be used with:
     * <ul>
     * <li>DiffTypeObjectEnum#COUNTER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#PROVINCE where the remove is done</li>
     * <li>DiffAttributeTypeEnum#STACK_DEL (if the stack owning the counter has no counter anymore) - optional attribute</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#ADM_ACT in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY for the id of the owner</li>
     * <li>DiffAttributeTypeEnum#TYPE for the type of the action</li>
     * </ul>
     * </li>
     * </ul>
     */
    REMOVE,
    /**
     * <p>
     * Move something.
     * </p>
     * <p>
     * Can be used with:
     * <ul>
     * <li>DiffTypeObjectEnum#STACK in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#PROVINCE_FROM</li>
     * <li>DiffAttributeTypeEnum#PROVINCE_TO</li>
     * <li>DiffAttributeTypeEnum#MOVE_POINTS - optional attribute</li>
     * <li>DiffAttributeTypeEnum#MOVE_PHASE - optional attribute</li>
     * <li>DiffAttributeTypeEnum#BESIEGED - optional attribute</li>
     * <li>DiffAttributeTypeEnum#COUNTRY to know the new controller of the stack.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#COUNTER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STACK_FROM</li>
     * <li>DiffAttributeTypeEnum#STACK_TO (can be a new one)</li>
     * <li>DiffAttributeTypeEnum#PROVINCE_FROM where the STACK_FROM is</li>
     * <li>DiffAttributeTypeEnum#PROVINCE_TO where the STACK_TO is</li>
     * <li>DiffAttributeTypeEnum#STACK_DEL (if STACK_FROM has no counter anymore) - optional attribute</li>
     * </ul>
     * </li>
     * </ul>
     */
    MOVE,
    /**
     * <p>
     * Link something.
     * </p>
     * <p>
     * Can be used with:
     * <ul>
     * <li>DiffTypeObjectEnum#ROOM in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which playable country is invited/kicked in the room.</li>
     * <li>DiffAttributeTypeEnum#INVITE a flag to know if it is an invite/kick.</li>
     * </ul>
     * </li>
     * </ul>
     */
    LINK,
    /**
     * <p>
     * Invalidate something.
     * </p>
     * <p>
     * Can be used with:
     * <ul>
     * <li>DiffTypeObjectEnum#ECO_SHEET in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which playable country sheet is invalidated.
     * Can be <code>null</code> in which case it means that all countries are invalidated.</li>
     * <li>DiffAttributeTypeEnum#TURN to know the turn of the sheets that are invalidated.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#STATUS in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which playable country status is invalidated.
     * Can be <code>null</code> in which case it means that all countries are invalidated.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#TURN_ORDER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STATUS to know which status the turn order is invalidated.</li>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which country turn order is invalidated.
     * Can be <code>null</code> in which case it means that all countries are invalidated.</li>
     * </ul>
     * </li>
     * </ul>
     */
    INVALIDATE,
    /**
     * <p>
     * Invalidate something.
     * </p>
     * <p>
     * Can be used with:
     * </li>
     * <li>DiffTypeObjectEnum#STATUS in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which playable country status is validated.
     * Can be <code>null</code> in which case it means that all countries are validated.</li>
     * </ul>
     * </li>
     * </li>
     * <li>DiffTypeObjectEnum#ADM_ACT in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which playable country administrative actions are validated.
     * Can be <code>null</code> in which case it means that administrative actions of all countries are validated.</li>
     * <li>DiffAttributeTypeEnum#TURN to know the turn of the administrative actions that are validated.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#TURN_ORDER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STATUS to know which status the turn order is validated.</li>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY to know which country turn order is validated.
     * Can be <code>null</code> in which case it means that all countries are validated.</li>
     * </ul>
     * </li>
     * </ul>
     */
    VALIDATE,
    /**
     * <p>
     * Modifying something.
     * </p>
     * <p>
     * Can be used with:
     * </li>
     * <li>DiffTypeObjectEnum#GAME in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STATUS to know at which new status should be the game.</li>
     * <li>DiffAttributeTypeEnum#ACTIVE at false to un ready every country.</li>
     * <li>DiffAttributeTypeEnum#TURN the new turn of the game.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#STACK in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#MOVE_PHASE to know the new move phase of the stack.</li>
     * <li>DiffAttributeTypeEnum#COUNTRY to know the new controller of the stack.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#COUNTER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#TYPE to know the new type of the counter.</li>
     * <li>DiffAttributeTypeEnum#VETERANS to know the new number of veterans in the counter.</li>
     * <li>DiffAttributeTypeEnum#PROVINCE to know where the counter is.</li>
     * <li>DiffAttributeTypeEnum#LEVEL to know the new level of the counter (for trade fleet or establishment only).</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#COUNTRY in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#DTI to know the new DTI of the country.</li>
     * <li>DiffAttributeTypeEnum#FTI to know the new FTI of the country.</li>
     * <li>DiffAttributeTypeEnum#FTI_ROTW to know the new FTI_ROTW of the country.</li>
     * <li>DiffAttributeTypeEnum#TECH_LAND to know the new land technology of the country.</li>
     * <li>DiffAttributeTypeEnum#NAVAL_LAND to know the new naval technology of the country.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#TURN_ORDER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ACTIVE to know which position are active (all others are then inactive).</li>
     * <li>DiffAttributeTypeEnum#STATUS to know which status turn order should be activated.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#BATTLE in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STATUS to know which is the new status of the battle.</li>
     * <li>DiffAttributeTypeEnum#PHASING_READY to know if the phasing forces are ready for current battle status.</li>
     * <li>DiffAttributeTypeEnum#NON_PHASING_READY to know if the non phasing forces are ready for current battle status.</li>
     * <li>DiffAttributeTypeEnum#PHASING_COUNTER_ADD the attacking counter added to this battle.</li>
     * <li>DiffAttributeTypeEnum#NON_PHASING_COUNTER_ADD the defending counter added to this battle.</li>
     * <li>DiffAttributeTypeEnum#END the cause of ending of this battle.</li>
     * <li>DiffAttributeTypeEnum#WINNER the winner of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_LEADER the leader of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_COUNTRY the country of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SIZE the size of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_TECH the tech of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRE_COL the fire column of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SHOCK_COL the shock column of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_MORAL the moral of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_PURSUIT_MOD the pursuit modifier of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_PURSUIT the pursuit unmodified die roll of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SIZE_DIFF the size diff of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_RETREAT the retreat unmodified die roll of the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_ROUND_LOSS the round losses taken by the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_THIRD_LOSS the third losses taken by the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_MORALE_LOSS the morale losses taken by the phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_FIRE_MOD the fire modifier of the phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_FIRE the fire unmodified die roll of the phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_SHOCK_MOD the shock modifier of the phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_SHOCK the shock unmodified die roll of the phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SECOND_DAY_FIRE_MOD the fire modifier of the phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SECOND_DAY_FIRE the fire unmodified die roll of the phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SECOND_DAY_SHOCK_MOD the shock modifier of the phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SECOND_DAY_SHOCK the shock unmodified die roll of the phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_LEADER the leader of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_COUNTRY the country of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SIZE the size of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_TECH the tech of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRE_COL the fire column of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SHOCK_COL the shock column of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_MORAL the moral of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_PURSUIT_MOD the pursuit modifier of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_PURSUIT the pursuit unmodified die roll of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SIZE_DIFF the size diff of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_RETREAT the retreat unmodified die roll of the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_ROUND_LOSS the round losses taken by the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_THIRD_LOSS the third losses taken by the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_MORALE_LOSS the morale losses taken by the non phasing forces of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD the fire modifier of the non phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_FIRE the fire unmodified die roll of the non phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD the shock modifier of the non phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_SHOCK the shock unmodified die roll of the non phasing forces in the first day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD the fire modifier of the non phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SECOND_DAY_FIRE the fire unmodified die roll of the non phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD the shock modifier of the non phasing forces in the second day of this battle.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SECOND_DAY_SHOCK the shock unmodified die roll of the non phasing forces in the second day of this battle.</li>
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#SIEGE in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STATUS to know which is the new status of the this.</li>
     * <li>DiffAttributeTypeEnum#PHASING_READY to know if the besieger is ready for current siege status.</li>
     * <li>DiffAttributeTypeEnum#NON_PHASING_READY to know if the besieged is ready for current siege status.</li>
     * <li>DiffAttributeTypeEnum#PHASING_COUNTER_ADD the attacking counter added to this this.</li>
     * <li>DiffAttributeTypeEnum#NON_PHASING_COUNTER_ADD the defending counter added to this this.</li>
     * <li>DiffAttributeTypeEnum#LEVEL the level of the besieged fortress.</li>
     * <li>DiffAttributeTypeEnum#BONUS the bonus for undermining to this siege.</li>
     * <li>DiffAttributeTypeEnum#SIEGE_UNDERMINE_DIE the unmodified die roll for undermining to this siege.</li>
     * <li>DiffAttributeTypeEnum#SIEGE_UNDERMINE_RESULT the result of the undermining to this siege.</li>
     * <li>DiffAttributeTypeEnum#SIEGE_FORTRESS_FALLS if the fortress fell due to this siege.</li>
     * <li>DiffAttributeTypeEnum#SIEGE_BREACH if a breach was just done.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_LEADER the leader of the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_COUNTRY the country of the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_SIZE the size of the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_TECH the tech of the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_MORAL the moral of the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_FIRE_MOD the fire modifier of the besieger forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_FIRE the fire unmodified die roll of the besieger forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_SHOCK_MOD the shock modifier of the besieger forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_FIRST_DAY_SHOCK the shock unmodified die roll of the besieger forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_ROUND_LOSS the round losses taken by the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_THIRD_LOSS the third losses taken by the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_PHASING_MORALE_LOSS the morale losses taken by the besieger forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_LEADER the leader of the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_COUNTRY the country of the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_SIZE the size of the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_TECH the tech of the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_MORAL the moral of the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD the fire modifier of the besieged forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_FIRE the fire unmodified die roll of the besieged forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD the shock modifier of the besieged forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_FIRST_DAY_SHOCK the shock unmodified die roll of the besieged forces in the first day of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_ROUND_LOSS the round losses taken by the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_THIRD_LOSS the third losses taken by the besieged forces of this siege.</li>
     * <li>DiffAttributeTypeEnum#BATTLE_NON_PHASING_MORALE_LOSS the morale losses taken by the besieged forces of this siege.</li>
     * </ul>
     * </li>
     * </li>
     * <li>DiffTypeObjectEnum#ECHO_SHEET in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_COUNTRY for the country owning the sheet (mandatory).</li>
     * <li>DiffAttributeTypeEnum#PILLAGE for the pillage line.</li>
     * <li>DiffAttributeTypeEnum#EXC_TAXES for the exceptional taxes line.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_ROYAL_TREASURE for the royal treasure before exchequer line.</li>
     * <li>DiffAttributeTypeEnum#EXPENSES for the expenses line.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_COL for the exchequer column.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_MOD for the exchequer modifier.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_DIE for the unmodified exchequer die roll.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_REGULAR for the regular income line.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_PRESTIGE for the prestige income line.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_MAX_NAT_LOAN for the maximum national loan line.</li>
     * <li>DiffAttributeTypeEnum#EXCHEQUER_PRESTIGE_SPENT for the prestige spent line.</li>
     * <li>DiffAttributeTypeEnum#ROYAL_TREASURE_BALANCE for the royal treasure line.</li>
     * <li>DiffAttributeTypeEnum#ROYAL_TREASURE_AFTER_EXCHEQUER for the royal treasure after exchequer test line.</li>
     * <li>DiffAttributeTypeEnum#PRESTIGE_VPS for the prestige VPs line.</li>
     * <li>DiffAttributeTypeEnum#WEALTH for the wealth line.</li>
     * <li>DiffAttributeTypeEnum#PERIOD_WEALTH for the period wealth line.</li>
     * <li>DiffAttributeTypeEnum#STAB for the stab line.</li>
     * <li>DiffAttributeTypeEnum#STAB_MODIFIER for the modifier on the stab improvement action.</li>
     * <li>DiffAttributeTypeEnum#STAB_DIE for the unmodified die roll on the stab improvement action.</li>
     * <li>DiffAttributeTypeEnum#ROYAL_TREASURE_PEACE for the royal treasure at peace line.</li>
     * <li>DiffAttributeTypeEnum#INFLATION for the inflation line.</li>
     * <li>DiffAttributeTypeEnum#ROYAL_TREASURE_END for the royal treasure at end line.</li>
     * </ul>
     * </li>
     * </ul>
     */
    MODIFY,
    /**
     * <p>
     * Notify the client (never sent by back, not really a diff).
     * </p>
     * <p>
     * Can be used with:
     * </li>
     * <li>DiffTypeObjectEnum#REDEPLOY in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#ID_STACK to know the id of the stack.</li>
     * </ul>
     * </li>
     * </ul>
     */
    NOTIFY

}
