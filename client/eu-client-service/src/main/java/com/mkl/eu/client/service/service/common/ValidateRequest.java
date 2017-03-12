package com.mkl.eu.client.service.service.common;

/**
 * Sub request for validateAdminActions service.
 *
 * @author MKL.
 */
public class ValidateRequest {
    /** Flag saying that the country is validating or invalidating its administrative actions. */
    private boolean validate;

    /**
     * Constructor for jaxb.
     */
    public ValidateRequest() {
    }

    /**
     * Constructor.
     *
     * @param validate  the validate to set.
     */
    public ValidateRequest(boolean validate) {
        this.validate = validate;
    }

    /** @return the validate. */
    public boolean isValidate() {
        return validate;
    }

    /** @param validate the validate to set. */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
