package com.mkl.eu.client.service.service.military;

/**
 * Request for withdrawBeforeBattle service.
 *
 * @author MKL.
 */
public class WithdrawBeforeBattleRequest {
    /** Province where the withdraw will occur. */
    private String provinceTo;

    /**
     * Constructor for jaxb.
     */
    public WithdrawBeforeBattleRequest() {
    }

    /**
     * Constructor.
     *
     * @param provinceTo the provinceTo to set.
     */
    public WithdrawBeforeBattleRequest(String provinceTo) {
        this.provinceTo = provinceTo;
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
