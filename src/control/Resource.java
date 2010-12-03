package control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

/**
 * @author Jared Williams
 *	This class is the property loader and accessor.  It allows the properties file
 *	to be accessed from both the servlet and non-servlet context
 */
public class Resource {
	
	private static String servletResource = "/WEB-INF/app.properties";
	private static String classResource = "WebContent" + servletResource;
//	private static String classResource = "/opt/apache-tomcat-6.0.26/webapps/GeoApi" + servletResource;
	private static InputStream INPUT;
	private static Properties properties;
	private static ServletContext CONTEXT;
	
	/*
	 * If current context is servlet grab resource stream and load props, otherwise
	 * use typical file reader
	 * 
	 */
	private static Properties load() {
		try{
			if(properties == null) {
				properties = new Properties();
				if(CONTEXT == null) {
					init();
				}
				else {
					INPUT = CONTEXT.getResourceAsStream(servletResource);
				}
				properties.load(INPUT);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			properties = null;
		}
		return properties;
	}
	
	public static void init() throws FileNotFoundException {
		INPUT = new FileInputStream(new File(classResource));
	}
	
	/*
	 * Would be called from the servlet, saves Servlet 
	 */
	public static void init(HttpServlet hs) {
		CONTEXT = hs.getServletContext();
	}
	
	public static String get(String key) {
		return load().getProperty(key);
	}
}
