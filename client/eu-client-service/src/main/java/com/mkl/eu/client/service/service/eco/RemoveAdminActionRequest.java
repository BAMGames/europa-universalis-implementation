package com.mkl.eu.client.service.service.eco;

/**
 * Request for removeAdminAction service.
 *
 * @author MKL.
 */
public class RemoveAdminActionRequest {
    /** Id of the administrative action to del. */
    private Long idAdmAct;

    /**
     * Constructor for jaxb.
     */
    public RemoveAdminActionRequest() {
    }

    /**
     * Constructor.
     *
     * @param idAdmAct the idAdmAct to set.
     */
    public RemoveAdminActionRequest(Long idAdmAct) {
        this.idAdmAct = idAdmAct;
    }

    /** @return the idAdmAct. */
    public Long getIdAdmAct() {
        return idAdmAct;
    }

    /** @param idAdmAct the idAdmAct to set. */
    public void setIdAdmAct(Long idAdmAct) {
        this.idAdmAct = idAdmAct;
    }
}
