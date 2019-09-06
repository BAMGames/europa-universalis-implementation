package com.mkl.eu.client.service.service.board;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;

/**
 * Request for createCounter service.
 *
 * @author MKL.
 */
public class CreateCounterRequest {
    /** Type of the counter to create. */
    private CounterFaceTypeEnum type;
    /** Country of the counter to create. */
    private String country;
    /** Province of the counter to create. */
    private String province;

    /**
     * Constructor for jaxb.
     */
    public CreateCounterRequest() {
    }

    /**
     * Constructor.
     *
     * @param province the province to set.
     * @param type     the type to set.
     * @param country  the country to set.
     */
    public CreateCounterRequest(String province, CounterFaceTypeEnum type, String country) {
        this.province = province;
        this.type = type;
        this.country = country;
    }

    /** @return the type. */
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
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

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }
}
