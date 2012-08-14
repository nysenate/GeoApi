package gov.nysenate.sage.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Jared Williams
 *	This class is the property loader and accessor.  It allows the properties file
 *	to be accessed from both the servlet and non-servlet context
 */
public class Resource {
	private static Properties properties;

	private static Properties load() {
		try {
			if(properties == null) {
				properties = new Properties();
				properties.load(new FileInputStream(new File("app.properties")));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			properties = null;
		}
		return properties;
	}

	public static String get(String key) {
		return load().getProperty(key);
	}
}
