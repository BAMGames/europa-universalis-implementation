package com.mkl.eu.client.service.service.eco;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;

/**
 * Request for addAdminAction service.
 *
 * @author MKL.
 */
public class AddAdminActionRequest {
    /** Id of the country owner of the administrative action. */
    private Long idCountry;
    /** Type of the administrative action. */
    private AdminActionTypeEnum type;
    /** Eventual if of object subject of the administrative action. */
    private Long idObject;

    /**
     * Constructor for jaxb.
     */
    public AddAdminActionRequest() {
    }

    /**
     * Constructor.
     *
     * @param idCountry the idCountry to set.
     * @param type      the type to set.
     * @param idObject  the idObject to set.
     */
    public AddAdminActionRequest(Long idCountry, AdminActionTypeEnum type, Long idObject) {
        this.idCountry = idCountry;
        this.type = type;
        this.idObject = idObject;
    }


    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    /** @return the type. */
    public AdminActionTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(AdminActionTypeEnum type) {
        this.type = type;
    }

    /** @return the idObject. */
    public Long getIdObject() {
        return idObject;
    }

    /** @param idObject the idObject to set. */
    public void setIdObject(Long idObject) {
        this.idObject = idObject;
    }
}
