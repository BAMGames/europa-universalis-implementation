package com.mkl.eu.client.service.service.eco;

/**
 * Request for the exchequerRepartition service.
 *
 * @author MKL.
 */
public class ExchequerRepartitionRequest {
    /** Prestige that will be spent for income. */
    private int prestige;

    /**
     * Constructor for jaxb.
     */
    public ExchequerRepartitionRequest() {
    }

    /**
     * Constructor.
     *
     * @param prestige the prestige spent for income.
     */
    public ExchequerRepartitionRequest(int prestige) {
        this.prestige = prestige;
    }

    /** @return the prestige. */
    public int getPrestige() {
        return prestige;
    }

    /** @param prestige the prestige to set. */
    public void setPrestige(int prestige) {
        this.prestige = prestige;
    }
}
