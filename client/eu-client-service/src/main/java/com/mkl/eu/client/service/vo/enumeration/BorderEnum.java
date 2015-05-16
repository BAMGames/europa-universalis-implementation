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
    PASS("pass"),
    /** Straits. */
    STRAIT("strait"),
    /** . */
    BERING_STRAIT("beringStrait");

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
