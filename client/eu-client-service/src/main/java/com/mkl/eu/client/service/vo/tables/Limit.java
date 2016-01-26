package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.LimitTypeEnum;

/**
 * VO for the basic forces of a country (tables).
 *
 * @author MKL.
 */
public class Limit extends EuObject {
    /** Country owning these forces. */
    private String country;
    /** Period concerned. */
    private Period period;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private LimitTypeEnum type;

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the period. */
    public Period getPeriod() {
        return period;
    }

    /** @param period the period to set. */
    public void setPeriod(Period period) {
        this.period = period;
    }

    /** @return the number. */
    public Integer getNumber() {
        return number;
    }

    /** @param number the number to set. */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /** @return the type. */
    public LimitTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(LimitTypeEnum type) {
        this.type = type;
    }
}
