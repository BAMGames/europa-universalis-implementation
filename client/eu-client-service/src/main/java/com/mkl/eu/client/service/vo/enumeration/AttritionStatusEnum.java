package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of status for attrition.
 *
 * @author MKL
 */
public enum AttritionStatusEnum {
    /** When the attrition is going on. */
    ON_GOING,
    /** When attrition is done, and losses must be taken. */
    CHOOSE_LOSS,
    /** Attrition computed. */
    DONE
}
