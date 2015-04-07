package com.mkl.eu.client.service.vo.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration of the relation types.
 *
 * @author MKL
 */
public enum RelationTypeEnum {
    /** Alliance. */
    ALLIANCE("ALLIANCE"),
    /** War. */
    WAR("WAR");

    /** Code of the enum. */
    private String code;

    /**
     * Constructor.
     *
     * @param code the code to set.
     */
    private RelationTypeEnum(String code) {
        this.code = code;
    }

    /** @return the code. */
    public String getCode() {
        return code;
    }

    /**
     * Retrieve a border by its code.
     *
     * @param code of the adminActionResult.
     * @return the adminActionResult.
     */
    public static RelationTypeEnum getByCode(String code) {
        RelationTypeEnum returnValue = null;
        for (RelationTypeEnum e : values()) {
            if (StringUtils.equals(code, e.getCode())) {
                returnValue = e;
                break;
            }
        }

        return returnValue;
    }
}
