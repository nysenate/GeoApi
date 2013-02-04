package gov.nysenate.sage.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * The Config class provides access to the property file configuration values.
 * Properties are loaded once during initialization and during a fixed interval
 * set by checkInterval every time refresh() is invoked.
 *
 * The ResourceLoader filter will invoke refresh() at the start of every request.
 * If the properties file was modified then all observers will be notified.
 *
 * @see gov.nysenate.sage.filter.ResourceFilter
 */

public class Config extends Observable {

    private static Logger logger = Logger.getLogger(Config.class);
    private static Config INSTANCE = new Config();
    private Properties properties;
    private File propertyFile;
    private long lastCheckedTime;
    private long readTime;
    private final long checkInterval = 1000; // milliseconds
    private static final String propertyFileName = "app.properties";
    private static final Pattern variablePattern = Pattern.compile("\\{\\{(.*)\\}\\}");

    /** Load the properties during initialization */

    private Config() {
        loadProperties();
    }

    /**
     * Reads property file and returns value for given key.
     * @param key
     * @return String   Value of property key or empty string if not found.
     */

    public static String read(String key) {
        if (INSTANCE.properties.containsKey(key)) {
            String value = (String)INSTANCE.properties.get(key);
            logger.debug(String.format("Fetching config[%s]=%s",key, value));
            value = resolveVariables(value);
            INSTANCE.properties.setProperty(key, value);
            return value;
        } else {
            logger.warn("Missing config property: "+key);
            return "";
        }
    }

    /**
     * External classes can subscribe to changes in Config by calling notify().
     * @param o
     */
    public static void notify(Observer o) {
        INSTANCE.addObserver(o);
    }

    /**
     * Refresh the properties.
     */
    public static void refresh() {
        INSTANCE.loadProperties();
    }

    private static String resolveVariables(String value) {
        Matcher variableMatcher = variablePattern.matcher(value);
        while(variableMatcher.find()) {
            String variable = variableMatcher.group(1);
            String replacement = read(variable);
            logger.debug(String.format("Resolving %s to %s",variable, replacement));
            value = value.replace("{{"+variable+"}}", replacement);
        }
        return value;
    }

    /**
     * @return String   String representing the path to the properties file.
     */
    private String getPropertiesPath() {
        Map<String,String> env = System.getenv();
        if (env.containsKey("GEOAPI_CONFIG_FILE")) {
            return env.get("GEOAPI_CONFIG_FILE");
        } else {
            return Config.class.getClassLoader().getResource(propertyFileName).getPath();
        }
    }

    /**
     * Reads the properties file based on the check interval and last modified time of the
     * properties file.
     *
     * @return Properties   Contains the properties.
     */
    private synchronized Properties loadProperties() {
        if (properties == null || System.currentTimeMillis() - lastCheckedTime > checkInterval) {
            lastCheckedTime = System.currentTimeMillis();

            if (propertyFile == null || propertyFile.lastModified() > readTime) {
                properties = new Properties();
                propertyFile = new File(getPropertiesPath());
                readTime = System.currentTimeMillis();
                logger.debug(String.format("(Re)Loading '%s' at: %d", propertyFile.getAbsolutePath(),readTime));

                try {
                    properties.load(new FileReader(propertyFile));
                    setChanged();
                    notifyObservers();
                }
                catch (IOException e) {
                    logger.error("Could not load propertyFile: " + propertyFile.getAbsolutePath(),e);
                }
            }
        }
        return properties;
    }
}
