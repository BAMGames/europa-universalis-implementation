package com.mkl.eu.client.service.vo;

import javax.xml.bind.annotation.XmlID;
import java.io.Serializable;

/**
 * Mother class of all VOs.
 *
 * @author MKL
 */
public abstract class EuObject implements Serializable {
    /** Id of the object. */
    private Long id;

    /** @return the id. */
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Method added because jaxb stores all the idRef in the same Map,
     * whatever the class is. So if two different classes have the same id,
     * there will be a collision.
     *
     * @return id for jaxb.
     */
    @XmlID
    public String getIdForJaxb() {
        return getClass().toString() + "_" + getId();
    }

    /**
     * So that jaxb works...
     */
    public void setIdForJaxb(String id) {

    }
}
