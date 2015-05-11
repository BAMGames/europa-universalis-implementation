package com.mkl.eu.client.service.vo.enumeration;

/**
 * Enumeration of type of borders.
 *
 * @author MKL
 */
public enum BorderEnum {
    /** River. */
    RIVER("river"),
    /** Mountain pass. */
    MOUNTAIN_PASS("pass"),
    /** Straits. */
    STRAITS("straits");

    /** Code of the enum. */
    private String code;

    /**
     * Constructor.
     *
     * @param code the code.
     */
    BorderEnum(String code) {
        this.code = code;
    }

    /** @return the code. */
    public String getCode() {
        return code;
    }
}
