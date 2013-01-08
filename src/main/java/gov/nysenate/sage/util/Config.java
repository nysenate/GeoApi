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

public class Config extends Observable{
    private static Logger logger = Logger.getLogger(Config.class);

    private static Config INSTANCE = new Config();
    private Properties properties;
    private File propertyFile;
    private long checkTime;
    private long readTime;

    private final long checkInterval=1000; //Milliseconds
    private static final Pattern variablePattern = Pattern.compile("\\{\\{(.*)\\}\\}");

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

    public static void notify(Observer o) {
        INSTANCE.addObserver(o);
    }

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


    private Config() {
        loadProperties();
    }

    private String getPropertiesPath() {
        Map<String,String> env = System.getenv();
        if (env.containsKey("GEOAPI_CONFIG_FILE")) {
            return env.get("GEOAPI_CONFIG_FILE");
        } else {
            return Config.class.getClassLoader().getResource("app.properties").getPath();
        }
    }

    private synchronized Properties loadProperties() {
        // Check lastModified every X milliseconds for changes
        if (properties == null || System.currentTimeMillis()-checkTime > checkInterval) {
            checkTime = System.currentTimeMillis();
            if (propertyFile.lastModified() > readTime) {
                // Read new properties file...
                properties = new Properties();
                propertyFile = new File(getPropertiesPath());
                readTime = System.currentTimeMillis();
                logger.debug(String.format("(Re)Loading '%s' at: %d",propertyFile.getAbsolutePath(),readTime));
                try {
                    properties.load(new FileReader(propertyFile));
                    setChanged();
                    notifyObservers();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error("Could not load propertyFile: "+propertyFile.getAbsolutePath(),e);
                }
            }
        }
        return properties;
    }

}
