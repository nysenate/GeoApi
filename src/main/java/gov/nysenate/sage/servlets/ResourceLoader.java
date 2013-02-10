package gov.nysenate.sage.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class ResourceLoader implements Filter {
    private final Logger logger = Logger.getLogger(ResourceLoader.class);
    private File log4jConfigFile;
    private long timeLoaded;
    private long lastChecked;
    private final long interval = 2000;
    public void init(FilterConfig fConfig) throws ServletException {
        timeLoaded = lastChecked = System.currentTimeMillis();
        log4jConfigFile = new File(this.getClass().getClassLoader().getResource("log4j.xml").getPath());
    }

    private synchronized void log4jReload() {
        if (System.currentTimeMillis()-lastChecked > interval) {
            lastChecked = System.currentTimeMillis();
            if (log4jConfigFile.lastModified() > timeLoaded) {
                // This isn't necessarily 100% safe. Log messages from other
                // threads could potentially (I think?) be dropped while reloading.
                //
                // Unfortunately, log4j 1.2 doesn't have a better way for us because
                // configureAndWatch is unsafe for J2EE environments. It opens a
                // FileWatchdog thread that is impossible to close and results in
                // leaking when the context is reloaded.
                logger.info("Reloading logger configuration...");
                DOMConfigurator.configure(log4jConfigFile.getAbsolutePath());
            }
        }
    }

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	    log4jReload();
        chain.doFilter(request, response);
	}

    public void destroy() {}
}
