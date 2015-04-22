package com.mkl.eu.client.service.vo;

import com.mkl.eu.client.common.adapter.JaxbLongAdapter;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
    @XmlID
    @XmlJavaTypeAdapter(JaxbLongAdapter.class)
    @XmlSchemaType(name = "long")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }
}
