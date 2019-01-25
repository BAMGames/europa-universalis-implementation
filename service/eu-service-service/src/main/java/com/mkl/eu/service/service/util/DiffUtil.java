package com.mkl.eu.service.service.util;

import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.apache.commons.lang.BooleanUtils;

/**
 * Utility class for diff objects.
 *
 * @author MKL.
 */
public final class DiffUtil {
    /**
     * no callable constructor.
     */
    private DiffUtil() {

    }

    /**
     * Creates a diff.
     *
     * @param game       the game.
     * @param type       the type of the diff.
     * @param typeObject the type object on which is performed a diff.
     * @param idObject   the id of the object on which is performed a diff.
     * @param attributes the attributes. Can be <code>null</code>.
     * @return the diff created.
     */
    public static DiffEntity createDiff(GameEntity game, DiffTypeEnum type, DiffTypeObjectEnum typeObject, Long idObject, DiffAttributesEntity... attributes) {
        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(type);
        diff.setTypeObject(typeObject);
        diff.setIdObject(idObject);
        if (attributes != null) {
            for (DiffAttributesEntity attribute : attributes) {
                if (attribute != null) {
                    attribute.setDiff(diff);
                    diff.getAttributes().add(attribute);
                }
            }
        }

        return diff;
    }

    /**
     * Creates a diff.
     *
     * @param game       the game.
     * @param type       the type of the diff.
     * @param typeObject the type object on which is performed a diff.
     * @param attributes the attributes. Can be <code>null</code>.
     * @return the diff created.
     */
    public static DiffEntity createDiff(GameEntity game, DiffTypeEnum type, DiffTypeObjectEnum typeObject, DiffAttributesEntity... attributes) {
        return createDiff(game, type, typeObject, null, attributes);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @param check a test to check that this attribute should be really created.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, String value, boolean check) {
        if (!check) {
            return null;
        }
        DiffAttributesEntity attribute = new DiffAttributesEntity();
        attribute.setType(type);
        attribute.setValue(value);
        return attribute;
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, String value) {
        return createDiffAttributes(type, value, true);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @param check a test to check that this attribute should be really created.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, Enum value, boolean check) {
        return createDiffAttributes(type, value != null ? value.name() : "", check);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, Enum value) {
        return createDiffAttributes(type, value, true);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @param check a test to check that this attribute should be really created.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, Number value, boolean check) {
        return createDiffAttributes(type, value != null ? value.toString() : "", check);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, Number value) {
        return createDiffAttributes(type, value, true);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @param check a test to check that this attribute should be really created.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, Boolean value, boolean check) {
        return createDiffAttributes(type, Boolean.toString(BooleanUtils.toBoolean(value)), check);
    }

    /**
     * Creates a diff attribute.
     *
     * @param type  the type of the diff attribute.
     * @param value the value of the diff attribute.
     * @return the diff attribute created.
     */
    public static DiffAttributesEntity createDiffAttributes(DiffAttributeTypeEnum type, Boolean value) {
        return createDiffAttributes(type, value, true);
    }
}
