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

public class SenateFilter implements Filter {
	
	
	private final String IP_MATCH = "10.\\d+.\\d+.\\d+|127.0.0.1|63.118.5[67].\\d+";

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
		
		if(!match && key == null) {
			((HttpServletResponse)response).sendRedirect("http://www.nysenate.gov");
			return;
		}
		else {
			if(((HttpServletRequest)request).getRequestURI().equals("(/GeoApi(/)?)?(index.jsp)?")) {
				if(!match) {
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
