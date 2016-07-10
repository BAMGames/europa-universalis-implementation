package com.mkl.eu.client.service.vo.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration of the results.
 *
 * @author MKL
 */
public enum ResultEnum {
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
    private ResultEnum(String code) {
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
    public static ResultEnum getByCode(String code) {
        ResultEnum adminActionResult = null;
        for (ResultEnum e : values()) {
            if (StringUtils.equals(code, e.getCode())) {
                adminActionResult = e;
                break;
            }
        }

        return adminActionResult;
    }
}
