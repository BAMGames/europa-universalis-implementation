package com.mkl.eu.client.service.vo.tables;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;

/**
 * VO for the basic forces of a country (tables).
 *
 * @author MKL.
 */
public class BasicForce extends EuObject implements IBasicForce {
    /** Id. */
    private Long id;
    /** Country owning these forces. */
    private String country;
    /** Period concerned. */
    private String period;
    /** Number of this type of counter. */
    private Integer number;
    /** Type of limit. */
    private ForceTypeEnum type;

    /** @return the id. */
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the period. */
    public String getPeriod() {
        return period;
    }

    /** @param period the period to set. */
    public void setPeriod(String period) {
        this.period = period;
    }

    /** @return the number. */
    @Override
    public Integer getNumber() {
        return number;
    }

    /** @param number the number to set. */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /** @return the type. */
    @Override
    public ForceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(ForceTypeEnum type) {
        this.type = type;
    }
}
