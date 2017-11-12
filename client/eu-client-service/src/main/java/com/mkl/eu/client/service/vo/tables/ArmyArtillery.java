package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;

/**
 * VO for the army artilery table.
 *
 * @author MKL.
 */
public class ArmyArtillery extends EuObject {
    /** Country owning the army. */
    private String country;
    /** Class of the army. */
    private ArmyClassEnum armyClass;
    /** Period. */
    private String period;
    /** Number of artillery of the army. */
    private Integer artillery;

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

    /** @return the period. */
    public String getPeriod() {
        return period;
    }

    /** @param period the period to set. */
    public void setPeriod(String period) {
        this.period = period;
    }

    /** @return the size. */
    public Integer getArtillery() {
        return artillery;
    }

    /** @param artillery the size to set. */
    public void setArtillery(Integer artillery) {
        this.artillery = artillery;
    }
}
