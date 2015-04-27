package com.mkl.eu.client.service.vo.diff;

import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;

/**
 * Attribute of a diff.
 *
 * @author MKL.
 */
public class DiffAttributes {
    /** Type of the diff attribute. */
    private DiffAttributeTypeEnum type;
    /** Value of the diff attribute. */
    private String value;

    /** @return the type. */
    public DiffAttributeTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(DiffAttributeTypeEnum type) {
        this.type = type;
    }

    /** @return the value. */
    public String getValue() {
        return value;
    }

    /** @param value the value to set. */
    public void setValue(String value) {
        this.value = value;
    }
}
