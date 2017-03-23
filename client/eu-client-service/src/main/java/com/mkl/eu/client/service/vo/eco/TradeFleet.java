package com.mkl.eu.client.service.vo.eco;

import com.mkl.eu.client.service.vo.EuObject;

/**
 * Trade fleet of a country minor or major.
 *
 * @author MKL
 */
public class TradeFleet extends EuObject {
    /** Owner of the trade fleet (not an entity because it can be a minor country). */
    private String country;
    /** Name of the province where the trade fleet is located. */
    private String province;
    /** Level of the trade fleet. */
    private Integer level;

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

    /** @return the level. */
    public Integer getLevel() {
        return level;
    }

    /** @param level the level to set. */
    public void setLevel(Integer level) {
        this.level = level;
    }
}
