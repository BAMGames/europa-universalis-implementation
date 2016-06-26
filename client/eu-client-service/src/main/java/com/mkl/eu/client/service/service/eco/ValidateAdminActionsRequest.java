package com.mkl.eu.client.service.service.eco;

/**
 * Sub request for validateAdminActions service.
 *
 * @author MKL.
 */
public class ValidateAdminActionsRequest {
    /** Id of the country whose administrative actions should be validated/invalidated. */
    private Long idCountry;
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
     * @param idCountry the idCountry to set.
     * @param validate  the validate to set.
     */
    public ValidateAdminActionsRequest(Long idCountry, boolean validate) {
        this.idCountry = idCountry;
        this.validate = validate;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
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
