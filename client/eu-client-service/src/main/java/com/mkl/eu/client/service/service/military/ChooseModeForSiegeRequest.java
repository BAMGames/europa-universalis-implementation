package com.mkl.eu.client.service.service.military;

import com.mkl.eu.client.service.vo.enumeration.SiegeModeEnum;

/**
 * Request for chooseMode service.
 *
 * @author MKL.
 */
public class ChooseModeForSiegeRequest {
    /** Mode chosen for the siege (undermine, assault or redeploy). */
    private SiegeModeEnum mode;
    /** Province where an potential redeploy would occur. */
    private String provinceTo;

    /**
     * Constructor for jaxb.
     */
    public ChooseModeForSiegeRequest() {
    }

    /**
     * Constructor.
     *
     * @param mode       the mode to set.
     * @param provinceTo the provinceTo to set.
     */
    public ChooseModeForSiegeRequest(SiegeModeEnum mode, String provinceTo) {
        this.mode = mode;
        this.provinceTo = provinceTo;
    }

    /** @return the mode. */
    public SiegeModeEnum getMode() {
        return mode;
    }

    /** @param mode the mode to set. */
    public void setMode(SiegeModeEnum mode) {
        this.mode = mode;
    }

    /** @return the provinceTo. */
    public String getProvinceTo() {
        return provinceTo;
    }

    /** @param provinceTo the provinceTo to set. */
    public void setProvinceTo(String provinceTo) {
        this.provinceTo = provinceTo;
    }
}
