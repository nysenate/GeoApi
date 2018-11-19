package gov.nysenate.sage.filter;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.util.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;

/**
 * ResourceFilter is used to refresh the configuration properties stored in resource files.
 */
@Component
public class ResourceFilter implements Filter
{
    private Config config;
    private final Logger logger = LoggerFactory.getLogger(ResourceFilter.class);
    private String log4jConfigFileName = "log4j2.xml";
    private File log4jConfigFile;
    private long timeLoaded;
    private long lastChecked;
    private final long checkInterval = 2000;
    private BaseDao baseDao;

    @Autowired
    public ResourceFilter(BaseDao baseDao) {
        this.baseDao = baseDao;
        this.config = this.baseDao.getConfig();
    }

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
        chain.doFilter(request, response);
    }
    public void destroy()
    {
        logger.debug("Destroying ResourceFilter.");
    }
}
