package com.mkl.eu.client.service.service.military;

/**
 * Request for withdrawBeforeBattle service.
 *
 * @author MKL.
 */
public class WithdrawBeforeBattleRequest {
    /** Flag saying if a withdraw before battle is wanted. */
    private boolean withdraw;
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
     * @param withdraw the withdraw to set.
     * @param provinceTo the provinceTo to set.
     */
    public WithdrawBeforeBattleRequest(boolean withdraw, String provinceTo) {
        this.withdraw = withdraw;
        this.provinceTo = provinceTo;
    }

    /** @return the withdraw. */
    public boolean isWithdraw() {
        return withdraw;
    }

    /** @param withdraw the withdraw to set. */
    public void setWithdraw(boolean withdraw) {
        this.withdraw = withdraw;
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
