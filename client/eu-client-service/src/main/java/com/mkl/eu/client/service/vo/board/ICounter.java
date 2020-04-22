package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

/**
 * Interface for counter object.
 *
 * @author MKL.
 */
public interface ICounter {
    /** @return the country. */
    String getCountry();

    /** @return the type. */
    CounterFaceTypeEnum getType();

    /** @return the code. */
    String getCode();
}
