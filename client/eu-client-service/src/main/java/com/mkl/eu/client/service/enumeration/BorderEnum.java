package com.mkl.eu.client.service.enumeration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of type of borders.
 *
 * @author MKL
 */
public enum BorderEnum {
    RIVER,
    MOUNTAIN_PASS,
    STRAITS;


    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BorderEnum.class);

    /** Type de roles par code. */
    private static Map<String, BorderEnum> bordersByCode = new HashMap<>();

    static {
            bordersByCode.put("river", RIVER);
        bordersByCode.put("mountain_pass", MOUNTAIN_PASS);
        bordersByCode.put("straits", STRAITS);
    }

    /**
     * Retrieve a border by its code.
     *
     * @param code of the border.
     * @return the border.
     */
    public static BorderEnum getByCode(String code) {
        BorderEnum border = bordersByCode.get(code);
        if (!StringUtils.isEmpty(code) && border == null) {
            LOGGER.error("Fail to retrieve border by its code: {} does not exist.", code);
        }
        return border;
    }
}
