package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Counter (A+, MNU, fortress,...).
 *
 * @author MKL
 */
public class Counter extends EuObject<Long> {
    /** Owner of the counter. */
    private Country country;
    /** Stack owning the counter. */
    private Stack owner;
    /** Type of the counter. */
    private CounterTypeEnum type;

    /** @return the country. */
    public Country getCountry() {
        return country;
    }

    /** @param country the country to set. */
    public void setCountry(Country country) {
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
    public CounterTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterTypeEnum type) {
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public void afterUnmarshal(Object target, Object parent) {
        if (this.country != null) {
            this.country.getCounters().add(this);
        }
    }
}
