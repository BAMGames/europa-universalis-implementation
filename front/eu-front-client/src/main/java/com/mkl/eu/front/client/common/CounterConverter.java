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
    /** The global configuration for internationalisation. */
    private GlobalConfiguration globalConfiguration;

    /**
     * Constructor.
     *
     * @param globalConfiguration the global configuration.
     */
    public CounterConverter(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    /** {@inheritDoc} */
    @Override
    public String toString(Counter object) {
        return globalConfiguration.getMessage(object.getType()) + " - " + globalConfiguration.getMessage(object.getCountry());
    }

    /** {@inheritDoc} */
    @Override
    public Counter fromString(String string) {
        return null;
    }
}
