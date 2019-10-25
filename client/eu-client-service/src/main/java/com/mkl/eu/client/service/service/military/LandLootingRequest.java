package com.mkl.eu.client.service.service.military;

import com.mkl.eu.client.service.vo.enumeration.LandLootTypeEnum;

/**
 * Request for a service that needs only a province.
 *
 * @author MKL.
 */
public class LandLootingRequest {
    /** Province where the loot will happen. */
    private String province;
    /** Type of land looting. */
    private LandLootTypeEnum type;

    /**
     * Constructor for jaxb.
     */
    public LandLootingRequest() {
    }

    /**
     * Constructor.
     *
     * @param province the province to set.
     */
    public LandLootingRequest(String province, LandLootTypeEnum type) {
        this.province = province;
        this.type = type;
    }

    /** @return the province. */
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the type. */
    public LandLootTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(LandLootTypeEnum type) {
        this.type = type;
    }
}
