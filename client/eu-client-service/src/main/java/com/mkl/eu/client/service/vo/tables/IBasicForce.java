package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;

/**
 * Common interface between the VO and the entity to have an shared algorithm.
 *
 * @author MKL.
 */
public interface IBasicForce {
    /** @return the number. */
    Integer getNumber();

    /** @return the type. */
    ForceTypeEnum getType();
}
