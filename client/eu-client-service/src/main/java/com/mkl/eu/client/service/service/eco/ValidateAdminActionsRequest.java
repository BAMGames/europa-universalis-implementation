package com.mkl.eu.client.service.service.eco;

/**
 * Sub request for validateAdminActions service.
 *
 * @author MKL.
 */
public class ValidateAdminActionsRequest {
    /** Flag saying that the country is validating or invalidating its administrative actions. */
    private boolean validate;

    /**
     * Constructor for jaxb.
     */
    public ValidateAdminActionsRequest() {
    }

    /**
     * Constructor.
     *
     * @param validate  the validate to set.
     */
    public ValidateAdminActionsRequest(boolean validate) {
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
