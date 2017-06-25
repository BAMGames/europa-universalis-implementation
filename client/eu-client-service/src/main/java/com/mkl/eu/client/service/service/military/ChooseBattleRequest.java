package com.mkl.eu.client.service.service.military;

/**
 * Request for chooseBattle service.
 *
 * @author MKL.
 */
public class ChooseBattleRequest {
    /** Province of the battle that will be computed. */
    private String province;

    /**
     * Constructor for jaxb.
     */
    public ChooseBattleRequest() {
    }

    /**
     * Constructor.
     *
     * @param province the province to set.
     */
    public ChooseBattleRequest(String province) {
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
