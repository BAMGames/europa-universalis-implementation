package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;

/**
 * Counter (A+, MNU, fortress,...).
 *
 * @author MKL
 */
public class Counter extends EuObject {
    /** Owner of the counter. */
    private Country country;
    /** Stack owning the counter. */
    private Stack owner;
    /** Type of the counter. */
    private CounterTypeEnum type;

    /**
     * Constructor.
     */
    public Counter() {

    }

    /** @return the country. */
    public Country getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(Country country) {
        this.country = country;
    }

    /** @return the owner. */
    public Stack getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(Stack owner) {
        this.owner = owner;
    }

    /** @return the type. */
    public CounterTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterTypeEnum type) {
        this.type = type;
    }
}
