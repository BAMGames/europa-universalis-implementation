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
     * <li>nothing</li>
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
     * </ul>
     * </li>
     * <li>DiffTypeObjectEnum#COUNTER in which case it will use:
     * <ul>
     * <li>DiffAttributeTypeEnum#STACK_FROM</li>
     * <li>DiffAttributeTypeEnum#STACK_TO</li>
     * </ul>
     * </li>
     * </ul>
     */
    MOVE;
}
