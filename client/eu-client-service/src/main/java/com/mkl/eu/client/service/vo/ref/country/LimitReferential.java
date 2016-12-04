package com.mkl.eu.client.service.vo.ref.country;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;

/**
 * Referential VO for the limit force of a country (eg 2 ARMY counters or 3 LDND counters).
 *
 * @author MKL.
 */
public class LimitReferential extends EuObject {
    /** Id. */
    private Long id;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private CounterTypeEnum type;
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
    public CounterTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterTypeEnum type) {
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
