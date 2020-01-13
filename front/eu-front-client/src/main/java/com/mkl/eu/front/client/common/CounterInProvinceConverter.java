package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * Counter converter with province display.
 *
 * @author MKL.
 */
public class CounterInProvinceConverter extends StringConverter<Counter> {

    /** {@inheritDoc} */
    @Override
    public String toString(Counter object) {
        if (object == null) {
            return null;
        }
        StringBuilder label = new StringBuilder(GlobalConfiguration.getMessage(object.getType()));
        if (StringUtils.isNotEmpty(object.getCode())) {
            label.append(" '")
                    .append(GlobalConfiguration.getMessage(object.getCode()))
                    .append("'");
        }
        label.append(" - ")
                .append(GlobalConfiguration.getMessage(object.getCountry()))
                .append(" - ")
                .append(GlobalConfiguration.getMessage(object.getOwner().getProvince()));
        return label.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Counter fromString(String string) {
        return null;
    }
}
