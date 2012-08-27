package gov.nysenate.sage.util;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Jared Williams
 *	This class is the property loader and accessor.  It allows the properties file
 *	to be accessed from both the servlet and non-servlet context
 */
public class Resource {
	private final Properties properties = new Properties();

	public Resource() throws IOException {
	    properties.load(this.getClass().getClassLoader().getResourceAsStream("/app.properties"));
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
