package com.mkl.eu.client.service.service.military;

/**
 * Request for a service that needs only a province.
 *
 * @author MKL.
 */
public class ChooseProvinceRequest {
    /** Province of the battle that will be computed. */
    private String province;

    /**
     * Constructor for jaxb.
     */
    public ChooseProvinceRequest() {
    }

    /**
     * Constructor.
     *
     * @param province the province to set.
     */
    public ChooseProvinceRequest(String province) {
        this.province = province;
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
