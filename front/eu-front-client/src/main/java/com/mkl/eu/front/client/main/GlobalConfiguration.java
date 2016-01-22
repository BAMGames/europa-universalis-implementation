package com.mkl.eu.front.client.main;

import com.mkl.eu.client.service.vo.tables.Tables;
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
    /** Tables of the game. */
    private Tables tables;

    /** @return the locale. */
    public Locale getLocale() {
        return locale;
    }

    /** @param locale the locale to set. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /** @return the tables. */
    public Tables getTables() {
        return tables;
    }

    /** @param tables the tables to set. */
    public void setTables(Tables tables) {
        this.tables = tables;
    }
}
