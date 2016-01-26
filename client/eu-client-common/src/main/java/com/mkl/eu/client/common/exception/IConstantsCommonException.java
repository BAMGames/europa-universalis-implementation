package com.mkl.eu.client.common.exception;

/**
 * Constants for common code exceptions. with their description and their parameters.
 *
 * @author MKL
 */
public interface IConstantsCommonException {
    /**
     * <p>
     * Technical exception thrown when an hibernateException is caught during an update.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: updated entity.</li>
     * </ul>
     * </p>
     */
    String ERROR_UPDATE = "exception.eu.common.error_update";

    /**
     * <p>
     * Technical exception thrown when an hibernateException is caught during a delete.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: deleted entity.</li>
     * </ul>
     * </p>
     */
    String ERROR_DELETE = "exception.eu.common.error_delete";

    /**
     * <p>
     * Technical exception thrown when an hibernateException is caught during a creation.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: created entity.</li>
     * </ul>
     * </p>
     */
    String ERROR_CREATION = "exception.eu.common.error_creation";

    /**
     * <p>
     * Technical exception thrown when an hibernateException is caught during a creation.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Pk id of object in error.</li>
     * </ul>
     * </p>
     */
    String ERROR_READ = "exception.eu.common.error_read";

    /**
     * <p>
     * Functional exception thrown when a search returns too many results.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: search invoked (class/method).</li>
     * <li>1: count of effective results.</li>
     * <li>2: number of maximum result intended.</li>
     * </ul>
     * </p>
     */
    String SEARCH_TOO_LARGE = "exception.eu.common.search_too_large";

    /**
     * <p>
     * Functional exception thrown when a mandatory input parameter is <code>null</code> or empty.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>1: name of <code>null</code> parameter.</li>
     * </ul>
     * </p>
     */
    String NULL_PARAMETER = "exception.eu.common.null_parameter";

    /**
     * <p>
     * Functional exception thrown when an input parameter is invalid.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>1: name of invalid parameter.</li>
     * </ul>
     * </p>
     */
    String INVALID_PARAMETER = "exception.eu.common.invalid_parameter";

    /**
     * <p>
     * Functional exception thrown when a user tries to perform a non authorized action.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Action being performed (or object being updated).</li>
     * <li>1: username of the user.</li>
     * <li>2: username of the user who could perform this action.</li>
     * </ul>
     * </p>
     */
    String ACCESS_RIGHT = "exception.eu.common.access_right";

    /**
     * <p>
     * Technical exception thrown when a technical error occured. Often it is a bad designed code.
     * </p>
     * <p>
     * No parameter.
     * </p>
     */
    String TECHNICAL_ERROR = "exception.eu.common.technical_error";

    /**
     * <p>
     * Technical/Functional exception thrown when a concurrent modification occured.
     * </p>
     * <p>
     * Parameters:
     * <ul>
     * <li>0: Object that was previously updated by another process.</li>
     * </ul>
     * </p>
     */
    String CONCURRENT_MODIFICATION = "exception.eu.common.concurrent_modification";
}
