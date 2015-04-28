package com.mkl.eu.client.service.vo.board;

import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;

/**
 * Counter (A+, MNU, fortress,...) used for the service createCounter.
 * Does not extends EuOBject on purpose.
 *
 * @author MKL
 */
public class CounterForCreation {
    /** Type of the counter. */
    private CounterTypeEnum type;
    /** Id of the country (either idCountry of nameCountry must exist). */
    private Long idCountry;
    /** Name of the country (either idCountry of nameCountry must exist). */
    private String nameCountry;

    /** @return the type. */
    public CounterTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterTypeEnum type) {
        this.type = type;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the nameCountry. */
    public String getNameCountry() {
        return nameCountry;
    }

    /** @param nameCountry the nameCountry to set. */
    public void setNameCountry(String nameCountry) {
        this.nameCountry = nameCountry;
    }
}
