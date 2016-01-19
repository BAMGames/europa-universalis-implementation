package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.UnitActionEnum;

/**
 * Description of the class.
 *
 * @author MKL.
 */
public interface IUnit {
    /** @return the price. */
    Integer getPrice();

    /** @return the type. */
    ForceTypeEnum getType();

    /** @return the action. */
    UnitActionEnum getAction();

    /** @return the special. */
    boolean isSpecial();
}
