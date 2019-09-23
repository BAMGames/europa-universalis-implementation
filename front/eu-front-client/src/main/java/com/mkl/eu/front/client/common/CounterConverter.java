package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import javafx.util.StringConverter;

/**
 * Counter converter.
 *
 * @author MKL.
 */
public class CounterConverter extends StringConverter<Counter> {
    @Override
    public String toString(Counter object) {
        return object.getType() + " - " + object.getCountry();
    }

    @Override
    public Counter fromString(String string) {
        return null;
    }
}
