package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ArmyClassEnum;

/**
 * VO for the army classe table.
 *
 * @author MKL.
 */
public class ArmyClasse extends EuObject {
    /** Class of the army. */
    private ArmyClassEnum armyClass;
    /** Period. */
    private String period;
    /** Size of the army. */
    private Integer size;

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
    public Integer getSize() {
        return size;
    }

    /** @param size the size to set. */
    public void setSize(Integer size) {
        this.size = size;
    }
}
