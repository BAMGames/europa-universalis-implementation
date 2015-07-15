package com.mkl.eu.client.common.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XmlAdapter for ZonedDateTime <-> String.
 *
 * @author MKL.
 */
public class JaxbZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    /** {@inheritDoc} */
    @Override
    public ZonedDateTime unmarshal(String stringValue) throws Exception {
        return stringValue != null ? formatter.parse(stringValue, ZonedDateTime::from) : null;
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(ZonedDateTime value) throws Exception {
        return value != null ? formatter.format(value) : null;
    }
}
