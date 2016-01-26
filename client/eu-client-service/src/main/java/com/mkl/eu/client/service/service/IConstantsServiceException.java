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
     * <li>1: province target of the action</li>
     * </ul>
     * </p>
     */
    String COUNTER_CANT_PURCHASE = "exception.eu.admin_action.counter_cant_purchase";

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
}
