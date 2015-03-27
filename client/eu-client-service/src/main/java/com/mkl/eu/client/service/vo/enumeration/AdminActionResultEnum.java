package com.mkl.eu.client.service.vo.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration of the administrative actions.
 *
 * @author MKL
 */
public enum AdminActionResultEnum {
    /** Fumble (big failaure). */
    FUMBLE("F*"),
    /** Failed. */
    FAILED("F"),
    /** Half success. */
    AVERAGE("1/2"),
    /** Half success with honors. */
    AVERAGE_PLUS("1/2*"),
    /** Success. */
    SUCCESS("S"),
    /** Exceptional success. */
    CRITICAL_HIT("S*");

    /** Code of the enum. */
    private String code;

    /**
     * Constructor.
     * @param code the code to set.
     */
    private AdminActionResultEnum(String code) {
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
    public static AdminActionResultEnum getByCode(String code) {
        AdminActionResultEnum adminActionResult = null;
        for (AdminActionResultEnum e: values()) {
            if (StringUtils.equals(code, e.getCode())) {
                adminActionResult = e;
                break;
            }
        }

        return adminActionResult;
    }
}
