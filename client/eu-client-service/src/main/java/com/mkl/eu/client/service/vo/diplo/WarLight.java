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

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarLight warLight = (WarLight) o;

        if (!getId().equals(warLight.getId())) return false;
        if (!getName().equals(warLight.getName())) return false;
        return getType() == warLight.getType();

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }
}
