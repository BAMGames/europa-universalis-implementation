package com.mkl.eu.front.client.main;

import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Global configuration.
 *
 * @author MKL.
 */
public class GlobalConfiguration {
    /** Global instance. */
    private static GlobalConfiguration instance = new GlobalConfiguration();
    /** Locale of the application. */
    private Locale locale = Locale.getDefault();
    /** Tables. */
    private Tables tables;
    /** Referential. */
    private Referential referential;
    /** Internationalisation. */
    private MessageSource message;
    /** Configuration of the application. */
    private Properties configuration = new Properties();

    /**
     * Constructor that will retrieve the eu configuration file and parse it.
     * Then it will search for internationalization message sources and load them.
     */
    public GlobalConfiguration() {
        try {
            configuration.load(new FileInputStream("eu.properties"));
        } catch (Exception e) {
            try {
                configuration.load(new FileInputStream("config/eu.properties"));
            } catch (Exception e1) {
                throw new Error("eu.properties file was not found in jar folder or in config folder.", e1);
            }
        }
        System.getProperties().putAll(configuration);

        String language = configuration.getProperty("locale.language");
        if (StringUtils.isNotEmpty(language)) {
            String country = configuration.getProperty("locale.country");
            if (StringUtils.isEmpty(country)) {
                locale = new Locale(language);
            } else {
                locale = new Locale(language, country);
            }
        }

        ReloadableResourceBundleMessageSource message = new ReloadableResourceBundleMessageSource();
        message.setFallbackToSystemLocale(false);
        message.setDefaultEncoding("UTF-8");
        message.setUseCodeAsDefaultMessage(true);
        message.setBasenames("file:" + configuration.getProperty("data.folder") + "/msg/messages",
                "file:" + configuration.getProperty("data.folder") + "/msg/messages-provinces");

        this.message = message;
    }

    /**
     * @return the global instance.
     */
    private static GlobalConfiguration getInstance() {
        return instance;
    }

    /** @return the locale. */
    public static Locale getLocale() {
        return getInstance().locale;
    }

    /** @return the tables. */
    public static Tables getTables() {
        return getInstance().tables;
    }

    /** @param tables the tables to set. */
    public static void setTables(Tables tables) {
        getInstance().tables = tables;
    }

    /** @return the referential. */
    public static Referential getReferential() {
        return getInstance().referential;
    }

    /** @param referential the referential to set. */
    public static void setReferential(Referential referential) {
        getInstance().referential = referential;
    }

    /**
     * Proxy of MessageSource#getMessage
     *
     * @param code the code of the message.
     * @param args the arguments of the message.
     * @return the resolved message.
     */
    public static String getMessage(String code, Object... args) {
        return getInstance().message.getMessage(code, args, getLocale());
    }

    /**
     * @param object the enum to translate.
     * @return the enum translated in the client language.
     */
    public static String getMessage(Enum object) {
        if (object == null) {
            return null;
        }
        return getMessage(object.getClass().getSimpleName() + "." + object.name());
    }

    /**
     * @return the host to call for socket diffs.
     */
    public static String getSocketHost() {
        return getInstance().configuration.getProperty("socket.host");
    }

    /**
     * @return the data folder for all the resources (images, internationalization, etc..).
     */
    public static String getDataFolder() {
        return getInstance().configuration.getProperty("data.folder");
    }

    /**
     * @return the login (temporary).
     */
    public static String getLogin() {
        return getInstance().configuration.getProperty("login");
    }

    /**
     * Initialize the GlobalConfiguration so that the configuration can be used
     * as environment variables.
     */
    public static void init() {
        getInstance();
    }
}
