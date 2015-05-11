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
     * <li>DiffAttributeTypeEnum#STACK_TO (can be a new one)</li>
     * <li>DiffAttributeTypeEnum#PROVINCE where the move is done</li>
     * <li>DiffAttributeTypeEnum#STACK_DEL (if STACK_FROM has no counter anymore) - optional attribute</li>
     * </ul>
     * </li>
     * </ul>
     */
    MOVE
}
