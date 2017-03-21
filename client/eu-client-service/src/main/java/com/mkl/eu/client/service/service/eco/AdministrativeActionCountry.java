package com.mkl.eu.client.service.service.eco;

import com.mkl.eu.client.service.vo.eco.AdministrativeAction;

/**
 * Wrapper of an administrative action and the id of the country it is related.
 *
 * @author MKL.
 */
public class AdministrativeActionCountry {
    /** Id of the country. */
    private Long idCountry;
    /** Economical sheet. */
    private AdministrativeAction action;

    /**
     * Constructor for jaxb.
     */
    public AdministrativeActionCountry() {

    }

    /**
     * Constructor.
     *
     * @param idCountry the idCountry to set.
     * @param action    the action to set.
     */
    public AdministrativeActionCountry(Long idCountry, AdministrativeAction action) {
        this.idCountry = idCountry;
        this.action = action;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the action. */
    public AdministrativeAction getAction() {
        return action;
    }

    /** @param action the action to set. */
    public void setAction(AdministrativeAction action) {
        this.action = action;
    }
}
