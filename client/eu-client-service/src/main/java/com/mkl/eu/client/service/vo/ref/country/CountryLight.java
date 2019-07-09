package com.mkl.eu.client.service.vo.ref.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.client.service.vo.enumeration.ReligionEnum;

/**
 * Light object of a country.
 *
 * @author MKL.
 */
public class CountryLight extends EuObject {
    /** Name of the country. */
    private String name;
    /** Type of the country. */
    private CountryTypeEnum type;
    /** Religion at start of the country. */
    private ReligionEnum religion;
    /** Cultural group of this country. */
    private CultureEnum culture;
    /** Army class of this country. */
    private ArmyClassEnum armyClass;

    /** @return the name. */
    public String getName() {
        return name;
    }

    /** @param name the name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the type. */
    public CountryTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CountryTypeEnum type) {
        this.type = type;
    }

    /** @return the religion. */
    public ReligionEnum getReligion() {
        return religion;
    }

    /** @param religion the religion to set. */
    public void setReligion(ReligionEnum religion) {
        this.religion = religion;
    }

    /** @return the culture. */
    public CultureEnum getCulture() {
        return culture;
    }

    /** @param culture the culture to set. */
    public void setCulture(CultureEnum culture) {
        this.culture = culture;
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
