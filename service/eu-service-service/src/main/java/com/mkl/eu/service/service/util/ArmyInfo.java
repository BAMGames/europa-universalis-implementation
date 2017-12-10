package com.mkl.eu.service.service.util;

import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

/**
 * Wrapper class for additional information on an army.
 *
 * @author MKL.
 */
public class ArmyInfo {
    /** Type of the army. */
    private CounterFaceTypeEnum type;
    /** Owner of the army. */
    private String country;
    /** Class of the army. */
    private ArmyClassEnum armyClass;

    /** @return the type. */
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the armyClass. */
    public ArmyClassEnum getArmyClass() {
        return armyClass;
    }

    /** @param armyClass the armyClass to set. */
    public void setArmyClass(ArmyClassEnum armyClass) {
        this.armyClass = armyClass;
    }
}
