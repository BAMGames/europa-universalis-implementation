package com.mkl.eu.front.client.main;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Global configuration.
 *
 * @author MKL.
 */
@Component
public class GlobalConfiguration {
    /** Locale of the application. */
    private Locale locale = Locale.getDefault();

    /** @return the locale. */
    public Locale getLocale() {
        return locale;
    }

    /** @param locale the locale to set. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
