package com.mkl.eu.front.client.main;

import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
    @Autowired
    /** Internationalisation. */
    private MessageSource message;

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

    /**
     * Proxy of MessageSource#getMessage
     *
     * @param code the code of the message.
     * @param args the arguments of the message.
     * @return the resolved message.
     */
    public String getMessage(String code, Object... args) {
        return message.getMessage(code, args, getLocale());
    }

    /**
     * @param object the enum to translate.
     * @return the enum translated in the client language.
     */
    public String getMessage(Enum object) {
        if (object == null) {
            return null;
        }
        return getMessage(object.getClass().getSimpleName() + "." + object.name());
    }
}
