package com.mkl.eu.client.service.vo;

import javax.xml.bind.Unmarshaller;
import java.io.Serializable;

/**
 * Mother class of all VOs.
 *
 * @param <T> Type of the id. Long by default.
 * @author MKL
 */
public abstract class EuObject<T> extends Unmarshaller.Listener implements Serializable {
    /** Id of the object. */
    private T id;

    /** @return the id. */
    public T getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(T id) {
        this.id = id;
    }
}
