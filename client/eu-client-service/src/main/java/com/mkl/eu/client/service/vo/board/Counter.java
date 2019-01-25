package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Counter (A+, MNU, fortress,...).
 *
 * @author MKL
 */
public class Counter extends EuObject {
    /** Name of the country owning of the counter. */
    private String country;
    /** Stack owning the counter. */
    private Stack owner;
    /** Type of the counter. */
    private CounterFaceTypeEnum type;
    /** Number of veterans in the counter. */
    private Double veterans;

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return the owner. */
    @XmlTransient
    public Stack getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(Stack owner) {
        this.owner = owner;
    }

    /** @return the type. */
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }

    /** @return the veterans. */
    public Double getVeterans() {
        return veterans;
    }

    /** @param veterans the veterans to set. */
    public void setVeterans(Double veterans) {
        this.veterans = veterans;
    }
}
