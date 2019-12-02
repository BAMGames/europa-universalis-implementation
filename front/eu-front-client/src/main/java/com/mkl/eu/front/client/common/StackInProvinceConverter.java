package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.util.StringConverter;

import java.util.stream.Collectors;

/**
 * Stack converter with province display.
 *
 * @author MKL.
 */
public class StackInProvinceConverter extends StringConverter<Stack> {
    /** The global configuration for internationalisation. */
    private GlobalConfiguration globalConfiguration;

    /**
     * Constructor.
     *
     * @param globalConfiguration the global configuration.
     */
    public StackInProvinceConverter(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    /** {@inheritDoc} */
    @Override
    public String toString(Stack object) {
        return object.getCounters().stream()
                .map(counter -> globalConfiguration.getMessage(counter.getType()) + " - " + globalConfiguration.getMessage(counter.getCountry()))
                .collect(Collectors.joining(", "))
                + " -> " + globalConfiguration.getMessage(object.getProvince());
    }

    /** {@inheritDoc} */
    @Override
    public Stack fromString(String string) {
        return null;
    }
}
