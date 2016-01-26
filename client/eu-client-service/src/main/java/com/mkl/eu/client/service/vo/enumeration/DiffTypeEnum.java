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
     * </ul>
     */
    INVALIDATE
}
