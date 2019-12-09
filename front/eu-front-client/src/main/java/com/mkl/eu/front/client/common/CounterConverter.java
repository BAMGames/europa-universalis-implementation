package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.util.StringConverter;

/**
 * Counter converter.
 *
 * @author MKL.
 */
public class CounterConverter extends StringConverter<Counter> {

    /** {@inheritDoc} */
    @Override
    public String toString(Counter object) {
        return GlobalConfiguration.getMessage(object.getType()) + " - " + GlobalConfiguration.getMessage(object.getCountry());
    }

    /** {@inheritDoc} */
    @Override
    public Counter fromString(String string) {
        return null;
    }
}
