package gov.nysenate.sage.filter;

import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * ResourceFilter is used to refresh the configuration properties stored in resource files.
 */
public class ResourceFilter implements Filter
{
    private final Logger logger = Logger.getLogger(ResourceFilter.class);
    private Config config = ApplicationFactory.getConfig();
    private String log4jConfigFileName = "log4j.xml";
    private File log4jConfigFile;
    private long timeLoaded;
    private long lastChecked;
    private final long checkInterval = 2000;

    /**
     * Initialize the log4j config file.
     * @param fConfig
     * @throws ServletException
     */
    public void init(FilterConfig fConfig) throws ServletException
    {
        timeLoaded = lastChecked = System.currentTimeMillis();
        log4jConfigFile = new File(this.getClass().getClassLoader().getResource(log4jConfigFileName).getPath());
    }

    /**
     * Refresh the configurations if needed and relay the request to the next filter.
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        config.refresh();
        log4jRefresh();
        chain.doFilter(request, response);
    }

    /**
     * Reloads the log4j configuration if check interval has elapsed and the file has been modified.
     * This isn't necessarily 100% safe. Log messages from other threads could potentially be dropped
     * while reloading.
     *
     * Unfortunately, log4j 1.2 doesn't have a better way since configureAndWatch is unsafe for
     * J2EE environments. It opens a FileWatchdog thread that is impossible to close and results in
     * leaking when the context is reloaded.
     */
    private synchronized void log4jRefresh()
    {
        if (System.currentTimeMillis() - lastChecked > checkInterval)
        {
            lastChecked = System.currentTimeMillis();
            if (log4jConfigFile.lastModified() > timeLoaded)
            {
                logger.info("Reloading logger configuration...");
                DOMConfigurator.configure(log4jConfigFile.getAbsolutePath());
                timeLoaded = System.currentTimeMillis();
            }
        }
    }

    public void destroy()
    {
        logger.debug("Destroying ResourceFilter.");
    }
}
