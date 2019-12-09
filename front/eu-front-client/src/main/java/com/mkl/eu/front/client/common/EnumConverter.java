package com.mkl.eu.front.client.common;

import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.util.StringConverter;

/**
 * Generic class for converter of enum types.
 *
 * @author MKL.
 */
public class EnumConverter<T extends Enum<T>> extends StringConverter<T> {

    /** {@inheritDoc} */
    @Override
    public String toString(T object) {
        return GlobalConfiguration.getMessage(object);
    }

    /** {@inheritDoc} */
    @Override
    public T fromString(String string) {
        return null;
    }
}
