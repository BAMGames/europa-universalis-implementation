package com.mkl.eu.client.common.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XmlAdapter for Long <-> String.
 *
 * @author MKL.
 */
public class JaxbLongAdapter extends XmlAdapter<String, Long> {
    /** {@inheritDoc} */
    @Override
    public Long unmarshal(String v) throws Exception {
        return Long.parseLong(v);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(Long v) throws Exception {
        return Long.toString(v);
    }
}
