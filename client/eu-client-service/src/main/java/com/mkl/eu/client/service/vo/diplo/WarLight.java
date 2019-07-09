package com.mkl.eu.client.service.vo.diplo;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.WarTypeEnum;

/**
 * Light VO that describes a War.
 *
 * @author MKL.
 */
public class WarLight extends EuObject {
    /** Name of the war. */
    private String name;
    /** Type of war. */
    private WarTypeEnum type;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the type. */
    public WarTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(WarTypeEnum type) {
        this.type = type;
    }
}
