package com.mkl.eu.client.service.service.military;

/**
 * Request for the landRedeploy service.
 *
 * @author MKL.
 */
public class LandRedeployRequest {
    /** Stack that will pillage. */
    private Long idStack;
    /** Type of land looting. */
    private String province;

    /**
     * Constructor for jaxb.
     */
    public LandRedeployRequest() {
    }

    /**
     * Constructor.
     *
     * @param idStack  the id of the stack.
     * @param province the province to redeploy.
     */
    public LandRedeployRequest(Long idStack, String province) {
        this.idStack = idStack;
        this.province = province;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
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
