package servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jared Williams
 * 
 * filers application requests based on ip address and api requests based on ip
 * and api key
 */
public class SenateFilter implements Filter {
	
	
	//senate ip range
	private final String IP_MATCH = "(10.\\d+.\\d+.\\d+|127.0.0.1|63.118.5[67].\\d+)";

    public SenateFilter() {
    }

	public void destroy() {
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String key = request.getParameter("key");
		String host = request.getRemoteAddr();
		boolean match = false;
		
		if(!host.matches(IP_MATCH)) {
			match = false;
		}
		else {
			match = true;
		}
		
		String uri = ((HttpServletRequest)request).getRequestURI();
		//if it does match we don't care what the credentials are
		if(!match) {
			//if the user is accessing the api, not the GUI
			if(uri.matches("(/GeoApi)?/api/.*")) {
				//if they're outside of the senate ip range and they don't have a key
				//then they're probably not welcome (for now)
				if(!match && key == null) {
					((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
					return;
				}
				//using api, will be turned down if key incorrect
			}
			else {
				//can include exceptions here, anything else will be redirected if not matching
				if(uri.matches("(/GeoApi)?(/kml/sd\\d{1,2}\\.kml|/open\\.jsp)")) {
					
				}
				else {
					((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
					return;
				}
			}
		}
		
		chain.doFilter(request, response);
		
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

}
