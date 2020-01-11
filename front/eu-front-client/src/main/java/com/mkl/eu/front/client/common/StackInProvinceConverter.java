package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

/**
 * Stack converter with province display.
 *
 * @author MKL.
 */
public class StackInProvinceConverter extends StringConverter<Stack> {

    /** {@inheritDoc} */
    @Override
    public String toString(Stack object) {
        return object.getCounters().stream()
                .map(this::getCounterLabel)
                .collect(Collectors.joining(", "))
                + " -> " + GlobalConfiguration.getMessage(object.getProvince());
    }

    /**
     * @param counter the counter to display.
     * @return the text to display for each counter.
     */
    private String getCounterLabel(Counter counter) {
        StringBuilder label = new StringBuilder(GlobalConfiguration.getMessage(counter.getType()));
        if (StringUtils.isNotEmpty(counter.getCode())) {
            label.append(" '")
                    .append(GlobalConfiguration.getMessage(counter.getCode()))
                    .append("'");
        }
        label.append(" - ")
                .append(GlobalConfiguration.getMessage(counter.getCountry()));
        return label.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Stack fromString(String string) {
        return null;
    }
}
