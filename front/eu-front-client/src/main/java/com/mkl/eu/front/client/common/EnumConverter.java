package com.mkl.eu.front.client.common;

import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.util.StringConverter;
import org.springframework.context.MessageSource;

/**
 * Generic class for converter of enum types.
 *
 * @author MKL.
 */
public class EnumConverter<T extends Enum<T>> extends StringConverter<T> {
    /** Internationalisation. */
    private MessageSource message;
    /** Configuration of the application. */
    private GlobalConfiguration globalConfiguration;

    public EnumConverter(MessageSource message, GlobalConfiguration globalConfiguration) {
        this.message = message;
        this.globalConfiguration = globalConfiguration;
    }

    /** {@inheritDoc} */
    @Override
    public String toString(T object) {
        return getEnumTranslated(object, message, globalConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public T fromString(String string) {
        return null;
    }

    /**
     * @param object              the enum to translate.
     * @param message             the message source.
     * @param globalConfiguration the configuration containing the locale.
     * @return the enum translated in the client langage.
     */
    public static String getEnumTranslated(Enum object, MessageSource message, GlobalConfiguration globalConfiguration) {
        if (object == null) {
            return null;
        }
        return message.getMessage(object.getClass().getSimpleName() + "." + object.name(), null, globalConfiguration.getLocale());
    }
}
