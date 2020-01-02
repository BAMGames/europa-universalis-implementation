package com.mkl.eu.client.service.service;

/**
 * Constants for common code exceptions. with their description and their parameters.
 *
 * @author MKL
 */
public interface IConstantsServiceException {
    /**********************************************************************/
    /**           RELATIVE COMMON EXCEPTION                               */
    /**********************************************************************/

    /**
     * <p>
     * Exception thrown when an action is performed on a counter not owned by the player performing the action.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the counter</li>
     * <li>2: id of the country</li>
     * </ul>
     * </p>
     */
    String COUNTER_NOT_OWNED = "exception.eu.service.counter_not_owned";

    /**
     * <p>
     * Exception thrown when an action is performed on a province not owned and controlled by the country performing the action.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province target of the action</li>
     * <li>2: name of the country performing the action</li>
     * </ul>
     * </p>
     */
    String PROVINCE_NOT_OWN_CONTROL = "exception.eu.service.province_not_own_control";

    /**
     * <p>
     * Exception thrown when an action is performed and the game status is invalid given the action.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: actual status of the game</li>
     * <li>2: expected status of the game</li>
     * </ul>
     * </p>
     */
    String INVALID_STATUS = "exception.eu.service.invalid_status";

    /**********************************************************************/
    /**           ECONOMIC SERVICE EXCEPTION                              */
    /**********************************************************************/

    /**
     * <p>
     * Exception thrown when a counter can't be maintain low.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the counter</li>
     * <li>2: type of the counter</li>
     * </ul>
     * </p>
     */
    String COUNTER_CANT_MAINTAIN_LOW = "exception.eu.admin_action.counter_cant_maintain_low";

    /**
     * <p>
     * Exception thrown when an army counter can't be lowered.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the country</li>
     * </ul>
     * </p>
     */
    String COUNTER_MAINTAIN_LOW_FORBIDDEN = "exception.eu.admin_action.counter_lower_unit_forbidden";

    /**
     * <p>
     * Exception thrown when a counter can't be disbanded.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the counter</li>
     * <li>2: type of the counter</li>
     * </ul>
     * </p>
     */
    String COUNTER_CANT_DISBAND = "exception.eu.admin_action.counter_cant_disband";

    /**
     * <p>
     * Exception thrown when a fortress counter can't be lowered.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the counter</li>
     * <li>2: type of the counter</li>
     * </ul>
     * </p>
     */
    String COUNTER_CANT_LOWER_FORTRESS = "exception.eu.admin_action.counter_cant_lower_fortress";

    /**
     * <p>
     * Exception thrown when a fortress is asked a wrong level to be lowered.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the counter</li>
     * <li>2: desired level of the fortress</li>
     * <li>3: natural level of the fortress</li>
     * <li>4: actual level of the fortress</li>
     * </ul>
     * </p>
     */
    String COUNTER_WRONG_LOWER_FORTRESS = "exception.eu.admin_action.counter_wrong_lower_fortress";

    /**
     * <p>
     * Exception thrown when a counter is asked to do an action but is already planned to do one.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the counter</li>
     * </ul>
     * </p>
     */
    String COUNTER_ALREADY_PLANNED = "exception.eu.admin_action.counter_already_planned";

    /**
     * <p>
     * Exception thrown when a counter can't be purchased on the selected province.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the counter</li>
     * <li>2: province target of the action</li>
     * </ul>
     * </p>
     */
    String COUNTER_CANT_PURCHASE = "exception.eu.admin_action.counter_cant_purchase";

    /**
     * <p>
     * Exception thrown when a fortress can't be purchased (lack of technology).
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the counter</li>
     * <li>1: actual technology of the country</li>
     * </ul>
     * </p>
     */
    String FORTRESS_CANT_PURCHASE = "exception.eu.admin_action.fortress_cant_purchase";

    /**
     * <p>
     * Exception thrown when a fortress can't be purchased because it is already planned.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the counter</li>
     * <li>2: province target of the action</li>
     * </ul>
     * </p>
     */
    String FORTRESS_ALREADY_PLANNED = "exception.eu.admin_action.fortress_already_planned";

    /**
     * <p>
     * Exception thrown when a counter can't be purchased because it would exceed the purchase limit.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the counter</li>
     * <li>2: number of LD already planned to purchase</li>
     * <li>3: purchase limit of the country</li>
     * </ul>
     * </p>
     */
    String PURCHASE_LIMIT_EXCEED = "exception.eu.admin_action.purchase_limit_exceed";

    /**
     * <p>
     * Exception thrown when an administrative action can't be planned because it would exceed the country limit.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the administrative action</li>
     * <li>2: country performing the administrative action</li>
     * <li>3: number of already planned action of same type</li>
     * <li>4: limit of this type for this country</li>
     * </ul>
     * </p>
     */
    String ADMIN_ACTION_LIMIT_EXCEED = "exception.eu.admin_action.admin_action_limit_exceed";

    /**
     * <p>
     * Exception thrown when the action would create a counter that is already at his limit.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the counter</li>
     * <li>2: country performing the administrative action</li>
     * <li>3: number of counter of same type already created</li>
     * <li>4: limit of counter of this type for this country</li>
     * </ul>
     * </p>
     */
    String COUNTER_LIMIT_EXCEED = "exception.eu.admin_action.counter_limit_exceed";

    /**
     * <p>
     * Exception thrown when a province type is unexpected.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the province</li>
     * <li>2: type of the province</li>
     * <li>3: expected type of the province</li>
     * </ul>
     * </p>
     */
    String PROVINCE_WRONG_TYPE = "exception.eu.admin_action.province_wrong_type";

    /**
     * <p>
     * Exception thrown when a trade fleet is already full.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province of the trade fleet</li>
     * <li>2: country of the trade fleet</li>
     * </ul>
     * </p>
     */
    String TRADE_FLEET_FULL = "exception.eu.admin_action.trade_fleet_full";

    /**
     * <p>
     * Exception thrown when a trade fleet can't be implemented in the caspian trade zone.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province of the trade fleet (should be caspian sea)</li>
     * <li>2: country of the trade fleet</li>
     * </ul>
     * </p>
     */
    String TRADE_FLEET_ACCESS_CASPIAN = "exception.eu.admin_action.trade_fleet_access_caspian";

    /**
     * <p>
     * Exception thrown when a trade fleet can't be implemented in a rotw trade zone.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province of the trade fleet</li>
     * <li>2: country of the trade fleet</li>
     * </ul>
     * </p>
     */
    String TRADE_FLEET_ACCESS_ROTW = "exception.eu.admin_action.trade_fleet_access_rotw";

    /**
     * <p>
     * Exception thrown when an action is not planned (when trying to remove it for example).
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the action</li>
     * </ul>
     * </p>
     */
    String ACTION_NOT_PLANNED = "exception.eu.admin_action.action_not_planned";

    /**
     * <p>
     * Exception thrown when an action is already planned.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the action</li>
     * <li>2: country of the action</li>
     * </ul>
     * </p>
     */
    String ACTION_ALREADY_PLANNED = "exception.eu.admin_action.action_already_planned";

    /**
     * <p>
     * Exception thrown when planning a technology enhancement with high investment
     * when the other technology has already been planned with a high investment.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the action</li>
     * <li>2: country of the action</li>
     * </ul>
     * </p>
     */
    String TECH_ALREADY_HIGH_INVESTMENT = "exception.eu.admin_action.tech_already_high_investment";

    /**
     * <p>
     * Exception thrown when planning a technology enhancement while its counter is already at maximum level.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the action</li>
     * <li>2: country of the action</li>
     * </ul>
     * </p>
     */
    String TECH_ALREADY_MAX = "exception.eu.admin_action.tech_already_max";

    /**
     * <p>
     * Exception thrown when a counter that should exist does not.
     * May require admin manipulation.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the counter</li>
     * <li>2: country of the counter</li>
     * </ul>
     * </p>
     */
    String MISSING_COUNTER = "exception.eu.missing_counter";

    /**
     * <p>
     * Exception thrown when an entry in a table that should exist does not.
     * May require admin manipulation.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the table</li>
     * <li>2: entry missing</li>
     * </ul>
     * </p>
     */
    String MISSING_TABLE_ENTRY = "exception.eu.missing_counter";

    /**
     * <p>
     * Exception thrown when a manufacture can't be created in the province.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: type of the manufacture</li>
     * <li>2: province where the manufacture would have benn created</li>
     * <li>3: additional actual info</li>
     * <li>4: additional expected info</li>
     * </ul>
     * </p>
     */
    String MNU_WRONG_PROVINCE = "exception.eu.admin_action.mnu_wrong_province";

    /**
     * <p>
     * Exception thrown when a country wants to levy exceptional taxes while it is not at war.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: country doing the action/li>
     * </ul>
     * </p>
     */
    String EXC_TAXES_NOT_AT_WAR = "exception.eu.exc_taxes_not_at_war";

    /**
     * <p>
     * Exception thrown when the stability of a country is too low.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: country owning the stability/li>
     * <li>2: actual stability</li>
     * <li>3: minimum stability required</li>
     * </ul>
     * </p>
     */
    String INSUFFICIENT_STABILITY = "exception.eu.insufficient_stability";

    /**
     * <p>
     * Exception thrown when trying to improve a colony contradicting pioneering rules.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province where the colony would have improved.</li>
     * <li>2: actual level of the colony</li>
     * </ul>
     * </p>
     */
    String PIONEERING = "exception.eu.pioneering";

    /**
     * <p>
     * Exception thrown when trying to improve or settle a colony or a trading post contradicting settlements rules.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province where the establishment would have settled.</li>
     * </ul>
     * </p>
     */
    String SETTLEMENTS = "exception.eu.settlements";

    /**
     * <p>
     * Exception thrown when trying to improve a colony contradicting inland advance rules.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province where the establishment would have settled.</li>
     * </ul>
     * </p>
     */
    String INLAND_ADVANCE = "exception.eu.inland_advance";

    /**********************************************************************/
    /**              BOARD SERVICE EXCEPTION                              */
    /**********************************************************************/

    /**
     * <p>
     * Exception thrown when trying to move a counter to a too big stack.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * <li>1: future number of counters of the stack.</li>
     * <li>1: future size of the stack.</li>
     * </ul>
     * </p>
     */
    String STACK_TOO_BIG = "exception.eu.board.stack_too_big";

    /**
     * <p>
     * Exception thrown when trying to move a non mobile stack.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * </ul>
     * </p>
     */
    String STACK_NOT_MOBILE = "exception.eu.board.stack_not_mobile";

    /**
     * <p>
     * Exception thrown when trying to move a stack that already moved.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * </ul>
     * </p>
     */
    String STACK_ALREADY_MOVED = "exception.eu.board.stack_already_moved";

    /**
     * <p>
     * Exception thrown when trying to move a stack while another one is moving.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack trying to move.</li>
     * <li>2: ids of the stack currently moving.</li>
     * </ul>
     * </p>
     */
    String OTHER_STACK_MOVING = "exception.eu.board.other_stack_moving";

    /**
     * <p>
     * Exception thrown when trying to move a stack in a neutral province.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the province.</li>
     * <li>2: name of the controller of the province.</li>
     * </ul>
     * </p>
     */
    String CANT_MOVE_PROVINCE = "exception.eu.board.cant_move_province";

    /**
     * <p>
     * Exception thrown when trying to move out from enemy forces while they are not pinned.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province.</li>
     * <li>2: ally forces.</li>
     * <li>3: enemy forces.</li>
     * </ul>
     * </p>
     */
    String ENEMY_FORCES_NOT_PINNED = "exception.eu.board.enemy_forces_not_pinned";

    /**
     * <p>
     * Exception thrown when trying to move out from a siege and not going home.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province.</li>
     * <li>2: ally forces.</li>
     * <li>3: fortress level.</li>
     * </ul>
     * </p>
     */
    String CANT_BREAK_SIEGE = "exception.eu.board.cant_break_siege";

    /**
     * <p>
     * Exception thrown when trying to move more than 12 MP.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: move points of the action.</li>
     * <li>2: move points already used.</li>
     * <li>3: move points limit.</li>
     * </ul>
     * </p>
     */
    String PROVINCE_TOO_FAR = "exception.eu.board.province_too_far";

    /**
     * <p>
     * Exception thrown when trying to do an action on neighbor provinces
     * while they aren't.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the source province.</li>
     * <li>2: name of the target province.</li>
     * </ul>
     * </p>
     */
    String PROVINCES_NOT_NEIGHBOR = "exception.eu.board.provinces_not_neighbor";

    /**
     * <p>
     * Exception thrown when trying to end the move a non moving stack.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * </ul>
     * </p>
     */
    String STACK_NOT_MOVING = "exception.eu.board.stack_not_moving";

    /**
     * <p>
     * Exception thrown when trying to control an already controlled stack.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * <li>2: controller (country).</li>
     * </ul>
     * </p>
     */
    String STACK_ALREADY_CONTROLLED = "exception.eu.board.stack_already_controlled";

    /**
     * <p>
     * Exception thrown when trying to control a stack with not enough presence.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * <li>2: country who wants to control the stack.</li>
     * <li>3: presence of the country.</li>
     * <li>4: presence needed to control the stack.</li>
     * </ul>
     * </p>
     */
    String STACK_CANT_CONTROL = "exception.eu.board.stack_already_controlled";

    /**********************************************************************/
    /**                BATTLE SERVICE EXCEPTION                           */
    /**********************************************************************/

    /**
     * <p>
     * Exception thrown when trying to begin a battle while another is in process.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province of the battle in process.</li>
     * </ul>
     * </p>
     */
    String BATTLE_IN_PROCESS = "exception.eu.military.battle_in_process";

    /**
     * <p>
     * Exception thrown when trying to make an action that requires a battle in a special status.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: status of battle expected.</li>
     * </ul>
     * </p>
     */
    String BATTLE_STATUS_NONE = "exception.eu.military.battle_status_none";

    /**
     * <p>
     * Exception thrown when trying to make select a counter in a battle while forces are validated.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: a flag that is true if it is the phasing player, false if it is not.</li>
     * </ul>
     * </p>
     */
    String BATTLE_SELECT_VALIDATED = "exception.eu.military.battle_select_validated";

    /**
     * <p>
     * Exception thrown when validating forces in a battle that would lead to a stack of size more than 8 or more than 3 counters..
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: Number of counters selected</li>
     * <li>2: Size of counters selected</li>
     * </ul>
     * </p>
     */
    String BATTLE_FORCES_TOO_BIG = "exception.eu.military.battle_forces_too_big";

    /**
     * <p>
     * Exception thrown when validating forces in a battle that would lead to an ambiguity about which country is leading it.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: Country selected</li>
     * <li>2: List of countries eligible</li>
     * </ul>
     * </p>
     */
    String BATTLE_FORCES_LEADING_COUNTRY_AMBIGUOUS = "exception.eu.military.battle_leading_country_ambiguous";

    /**
     * <p>
     * Exception thrown when validating forces in a battle that would lead to an ambiguity about which leader is leading it.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: Leader selected</li>
     * <li>2: List of leaders eligible</li>
     * </ul>
     * </p>
     */
    String BATTLE_FORCES_LEADER_AMBIGUOUS = "exception.eu.military.battle_leader_ambiguous";

    /**
     * <p>
     * Exception thrown when validating forces in a battle that could have other forces to select.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: a flag that is true if it is the phasing player, false if it is not.</li>
     * </ul>
     * </p>
     */
    String BATTLE_VALIDATE_OTHER_FORCE = "exception.eu.military.battle_validate_other_force";

    /**
     * <p>
     * Exception thrown when trying to withdraw into a province that is not eligible.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>2: name of the target province.</li>
     * </ul>
     * </p>
     */
    String BATTLE_CANT_WITHDRAW = "exception.eu.board.battle_cant_withdraw";

    /**
     * <p>
     * Exception thrown when a phasing player is trying to withdraw before battle.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String BATTLE_ONLY_NON_PHASING_CAN_WITHDRAW = "exception.eu.board.battle_only_non_phasing_can_withdraw";

    /**
     * <p>
     * Exception thrown when an action has already been done.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: Name of the action.</li>
     * <li>2: Country or side that has already done this action.</li>
     * </ul>
     * </p>
     */
    String ACTION_ALREADY_DONE = "exception.eu.board.action_already_done";

    /**
     * <p>
     * Exception thrown when trying to take more or less losses than needed.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: Number of losses asked.</li>
     * <li>2: Number of losses needed.</li>
     * </ul>
     * </p>
     */
    String BATTLE_LOSSES_MISMATCH = "exception.eu.board.battle_losses_mismatch";

    /**
     * <p>
     * Exception thrown when trying to take third losses in an european province.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String BATTLE_LOSSES_NO_THIRD = "exception.eu.board.battle_losses_no_third";

    /**
     * <p>
     * Exception thrown when trying to take losses on a counter not in the battle or not in the good side.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the invalid counter</li>
     * </ul>
     * </p>
     */
    String BATTLE_LOSSES_INVALID_COUNTER = "exception.eu.board.battle_losses_invalid_counter";

    /**
     * <p>
     * Exception thrown when trying to take more losses on a counter than it can handle.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the invalid counter</li>
     * <li>2: loss wanted</li>
     * <li>3: maximum loss possible on this counter</li>
     * </ul>
     * </p>
     */
    String BATTLE_LOSSES_TOO_BIG = "exception.eu.board.battle_losses_too_big";

    /**
     * <p>
     * Exception thrown when trying to take losses that will result with more than 3 thirds.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String BATTLE_LOSSES_TOO_MANY_THIRD = "exception.eu.board.battle_losses_too_many_third";

    /**
     * <p>
     * Exception thrown when trying to retreat in fortress a counter not in the battle or not in the good side.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the invalid counter</li>
     * </ul>
     * </p>
     */
    String BATTLE_RETREAT_INVALID_COUNTER = "exception.eu.board.battle_retreat_invalid_counter";

    /**
     * <p>
     * Exception thrown when trying to retreat into a province that is not eligible.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>2: name of the target province.</li>
     * </ul>
     * </p>
     */
    String BATTLE_CANT_RETREAT = "exception.eu.board.battle_cant_retreat";

    /**
     * <p>
     * Exception thrown when trying to retreat without telling which province (can be ok if all units are in fortress).
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String BATTLE_RETREAT_NEEDED = "exception.eu.board.battle_retreat_needed";

    /**********************************************************************/
    /**                SIEGE SERVICE EXCEPTION                            */
    /**********************************************************************/

    /**
     * <p>
     * Exception thrown when trying to begin a siege while another is in process.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: province of the siege in process.</li>
     * </ul>
     * </p>
     */
    String SIEGE_IN_PROCESS = "exception.eu.military.siege_in_process";

    /**
     * <p>
     * Exception thrown when trying to make an action that requires a siege in a special status.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: status of siege expected.</li>
     * </ul>
     * </p>
     */
    String SIEGE_STATUS_NONE = "exception.eu.military.siege_status_none";

    /**
     * <p>
     * Exception thrown when trying to make select a counter in a siege while it has already been done.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: a flag that is true if it is the phasing player, false if it is not.</li>
     * </ul>
     * </p>
     */
    String SIEGE_SELECT_VALIDATED = "exception.eu.military.siege_select_validated";

    /**
     * <p>
     * Exception thrown when validating forces in a siege that could have other forces to select.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String SIEGE_VALIDATE_OTHER_FORCE = "exception.eu.military.siege_validate_other_force";

    /**
     * <p>
     * Exception thrown when trying to undermine a fortress with insufficient forces.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String SIEGE_UNDERMINE_TOO_FEW = "exception.eu.military.siege_undermine_too_few";

    /**
     * <p>
     * Exception thrown when trying to retreat into a province that is not eligible.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>2: name of the target province.</li>
     * </ul>
     * </p>
     */
    String SIEGE_CANT_REDEPLOY = "exception.eu.military.siege_cant_redeploy";

    /**
     * <p>
     * Exception thrown when trying to retreat multiple times in the same province.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>2: name of the province</li>
     * </ul>
     * </p>
     */
    String PROVINCE_REDEPLOY_TWICE = "exception.eu.military.province_redeploy_twice";

    /**
     * <p>
     * Exception thrown when trying to retreat twice.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>2: id or size of the unit trying to redeploy</li>
     * </ul>
     * </p>
     */
    String UNIT_CANT_REDEPLOY_TWICE = "exception.eu.military.unit_cant_redeploy_twice";

    /**
     * <p>
     * Exception thrown when trying to retreat into a province that is not eligible.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>2: name of the target province.</li>
     * </ul>
     * </p>
     */
    String UNIT_CANT_REDEPLOY_PROVINCE = "exception.eu.military.unit_cant_redeploy_province";

    /**
     * <p>
     * Exception thrown when trying to redeploy a garrison without a War Honors result.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String GARRISON_CANT_REDEPLOY = "exception.eu.military.garrison_cant_redeploy";

    /**********************************************************************/
    /**                INTERPHASE SERVICE EXCEPTION                       */
    /**********************************************************************/

    /**
     * <p>
     * Exception thrown when trying to loot with a stack that cannot loot.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * </ul>
     * </p>
     */
    String LAND_LOOTING_INVALID_STACK = "exception.eu.inter.land_looting_invalid_stack";

    /**
     * <p>
     * Exception thrown when trying to loot a province twice with the same stack.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: id of the stack.</li>
     * </ul>
     * </p>
     */
    String LAND_LOOTING_TWICE = "exception.eu.inter.land_looting_twice";

    /**
     * <p>
     * Exception thrown when trying to loot a province that is not owned by an enemy.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the target province.</li>
     * <li>2: name of the current owner of the province.</li>
     * </ul>
     * </p>
     */
    String LAND_LOOTING_NOT_ENEMY = "exception.eu.inter.land_looting_not_enemy";

    /**
     * <p>
     * Exception thrown when trying to loot a province with insufficient forces.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the target province.</li>
     * </ul>
     * </p>
     */
    String LAND_LOOTING_INSUFFICIENT_FORCES = "exception.eu.inter.land_looting_insufficient_forces";

    /**
     * <p>
     * Exception thrown when trying to burn a trading post while there is no enemy trading post.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the target province.</li>
     * </ul>
     * </p>
     */
    String LAND_LOOTING_BURN_TP_NO_TP = "exception.eu.inter.land_looting_burn_tp_no_tp";

    /**
     * <p>
     * Exception thrown when trying to burn a trading post without controlling it.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the target province.</li>
     * </ul>
     * </p>
     */
    String LAND_LOOTING_BURN_TP_NO_CONTROL = "exception.eu.inter.land_looting_burn_tp_no_control";

    /**
     * <p>
     * Exception thrown when trying to redeploy from a province that is not controlled by an enemy.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: name of the target province.</li>
     * <li>2: name of the current owner of the province.</li>
     * </ul>
     * </p>
     */
    String LAND_REDEPLOY_NOT_ENEMY = "exception.eu.inter.land_redeploy_not_enemy";

    /**
     * <p>
     * Exception thrown when trying to validate redeployment while there are still some forces redeployment pending.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: List of provinces where a redeployment is forced.</li>
     * </ul>
     * </p>
     */
    String STACK_MUST_REDEPLOY = "exception.eu.inter.stack_must_redeploy";

    /**
     * <p>
     * Exception thrown when trying to spend more prestige than possible in the income.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: the wanting prestige into income.</li>
     * <li>2: the maxprestige that can be turn into income.</li>
     * </ul>
     * </p>
     */
    String PRESTIGE_TOO_HIGH = "exception.eu.inter.prestige_too_high";

    /**
     * <p>
     * Exception thrown when trying to improve stability when stability is already at max.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * </ul>
     * </p>
     */
    String STABILITY_MAX = "exception.eu.inter.stability_max";
}
