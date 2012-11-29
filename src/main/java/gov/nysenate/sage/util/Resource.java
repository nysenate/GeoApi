package gov.nysenate.sage.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jared Williams
 *	This class is the property loader and accessor.  It allows the properties file
 *	to be accessed from both the servlet and non-servlet context
 */
public class Resource {
	private final Properties properties = new Properties();

	public Resource() throws IOException {
	    Map<String,String> env = System.getenv();
	    if (env.containsKey("GEOAPI_CONFIG_FILE")) {
            properties.load(new FileReader(new File(env.get("GEOAPI_CONFIG_FILE"))));
	    } else {
	        properties.load(this.getClass().getClassLoader().getResourceAsStream("app.properties"));
	    }
	}

	public String fetch(String key) {
        return properties.getProperty(key);
	}

	public static String get(String key) {
	    try {
	        return new Resource().fetch(key);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}
