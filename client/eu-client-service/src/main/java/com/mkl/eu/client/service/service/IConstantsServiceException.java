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
    /**           ECONOMIC SERVICE EXCEPTION                              */
    /**********************************************************************/

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
}
