package gov.nysenate.sage.servlets;

import gov.nysenate.sage.util.Config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ResourceLoader implements Filter {

    public void init(FilterConfig fConfig) throws ServletException {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	    Config.refresh();
        chain.doFilter(request, response);
	}

    public void destroy() {

    }
}
