package com.mkl.eu.front.client.main;

import com.mkl.eu.client.service.vo.ref.Referential;
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
    /** Tables. */
    private Tables tables;
    /** Referential. */
    private Referential referential;

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

    /** @return the referential. */
    public Referential getReferential() {
        return referential;
    }

    /** @param referential the referential to set. */
    public void setReferential(Referential referential) {
        this.referential = referential;
    }
}
