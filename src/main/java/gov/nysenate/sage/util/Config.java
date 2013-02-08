package gov.nysenate.sage.util;

import java.io.File;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nysenate.sage.listener.SageConfigurationListener;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

/**
 * The Config class provides access to the property file configuration values.
 *
 * In order to utilize this class as a single instance configuration for
 * the application, an application factory must build a Config instance. The
 * application factory will provide access to it's Config instance for all the
 * other classes. This design was chosen to allow for the Config class to provide
 * functionality in a testing environment.
 *
 * In the event of property file modifications while the context is deployed
 * the SageConfigurationListener instance supplied will intercept file change events
 * and notify the observers. To observe Config for changes simply call notifyOnChange()
 * which delegates the Observer to SageConfigurationListener. File change modifications
 * are not checked continuously but rather any time the Config object is read from.
 *
 * @see gov.nysenate.sage.factory.ApplicationFactory
 * @see gov.nysenate.sage.listener.SageConfigurationListener
 */

public class Config
{
    private final Logger logger = Logger.getLogger(Config.class);

    /** Pattern to replace {{variables}} used in the property file. */
    private static final Pattern variablePattern = Pattern.compile("\\{\\{(.*?)\\}\\}");

    /** Provides access to the property file */
    private PropertiesConfiguration config;

    /** Reference to listener that acts on configuration changes. */
    private SageConfigurationListener listener;

    /** Load the given property file and set the listener to track file changes. */
    public Config(String propertyFileName, SageConfigurationListener listener) throws ConfigurationException
    {
        this.listener = listener;
        this.config = new PropertiesConfiguration(propertyFileName);
        this.config.setReloadingStrategy(new FileChangedReloadingStrategy());
        this.config.addConfigurationListener(listener);
        logger.debug("Loaded config for " + this.config.getPath());
    }

    /**
     * Reads property file and returns value for given key. If the value contains
     * a {{key}} it will be replaced with the value of that key.
     *
     * @param key - Property key to look up the value for.
     * @return String - Value of property or empty string if not found.
     */
    public String getValue(String key)
    {
        String value = this.config.getString(key, "");
        logger.trace(String.format("Reading config[%s] = %s", key, value));
        String resolvedValue = resolveVariables(value);
        if (value != resolvedValue)
        {
            value = resolvedValue;
            this.config.setProperty(key, value);
        }
        return value;
    }

    /**
     * Proxies to the Observable interface implemented on the listener.
     * @param o - Observer to subscribe to change updates.
     */
    public void notifyOnChange(Observer o)
    {
        this.listener.addObserver(o);
    }

    /**
     * @return File - A File object containing the property file loaded.
     */
    public File getPropertyFile()
    {
        return config.getFile();
    }

    /**
     * Resolves variables in the property value.
     * @param value
     * @return
     */
    private String resolveVariables(String value)
    {
        Matcher variableMatcher = variablePattern.matcher(value);
        while(variableMatcher.find()) {
            String variable = variableMatcher.group(1);
            String replacement = getValue(variable);
            value = value.replace("{{"+variable+"}}", replacement);
            variableMatcher = variablePattern.matcher(value);
        }
        return value;
    }

    @Deprecated
    public static void notify(Observer o) {}

    @Deprecated
    public static String read(String key) {return "";}
}
