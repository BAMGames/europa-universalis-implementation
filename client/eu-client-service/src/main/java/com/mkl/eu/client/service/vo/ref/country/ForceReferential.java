package com.mkl.eu.client.service.vo.ref.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;

/**
 * Referential VO for a force of a country (eg 2 A+ or 3 LD).
 * Can be used for basic forces or reinforcements.
 *
 * @author MKL.
 */
public class ForceReferential extends EuObject {
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private ForceTypeEnum type;
    /** Country owning these forces. */
    private String country;

    /** @return the number. */
    public Integer getNumber() {
        return number;
    }

    /** @param number the number to set. */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /** @return the type. */
    public ForceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(ForceTypeEnum type) {
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
}
