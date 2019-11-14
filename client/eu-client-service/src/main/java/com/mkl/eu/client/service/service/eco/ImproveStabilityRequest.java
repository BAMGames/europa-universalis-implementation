package com.mkl.eu.client.service.service.eco;

import com.mkl.eu.client.service.vo.enumeration.InvestmentEnum;

/**
 * Request for improveStability service.
 *
 * @author MKL.
 */
public class ImproveStabilityRequest {
    /** Eventual investment of the administrative action. */
    private InvestmentEnum investment;

    /**
     * Constructor for jaxb.
     */
    public ImproveStabilityRequest() {
    }

    /**
     * Constructor for external operation.
     *
     * @param investment the investment to set.
     */
    public ImproveStabilityRequest(InvestmentEnum investment) {
        this.investment = investment;
    }

    /** @return the investment. */
    public InvestmentEnum getInvestment() {
        return investment;
    }

    /** @param investment the investment to set. */
    public void setInvestment(InvestmentEnum investment) {
        this.investment = investment;
    }
}
