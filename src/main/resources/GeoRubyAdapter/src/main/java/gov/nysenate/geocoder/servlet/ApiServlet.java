package gov.nysenate.geocoder.servlet;

import gov.nysenate.geocoder.api.exceptions.ApiCommandException;
import gov.nysenate.geocoder.api.exceptions.ApiInternalException;
import gov.nysenate.geocoder.api.methods.MethodLoader;
import gov.nysenate.geocoder.model.AbstractApiExecute;
import gov.nysenate.geocoder.model.ApiMethod;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	HashMap<String,ApiMethod> methods = null;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		methods = MethodLoader.getMethods();
	}
      
	/*
	 *	both methods in this api take advantage of a forked verison of Geocoder::US, "geocode" is a one off API and
	 *  "bulk" is a threaded bulk processing API which offers much better performance 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		PrintWriter out = response.getWriter();

		String uri = URLDecoder.decode(request.getRequestURI(),"utf-8");
		uri = uri.toLowerCase().replaceAll("(/georubyadapter/|api/)", "");
		
		StringTokenizer stok = new StringTokenizer(uri,"/");
		
		String command = null;
		
		try {
			command = stok.nextToken();
			ApiMethod method = null;
			if((method = methods.get(command)) != null ) {
				AbstractApiExecute executionClass = method.getInstanceOfExecutionClass();
				Object obj = executionClass.execute(request, response, null);
				
				if(obj == null) {
					throw new ApiInternalException();
				}
				else {
					out.print(executionClass.toOther(obj, null));
				}
			}
			else {
				throw new ApiCommandException(command);
			}
		}
		catch (ApiCommandException ace) {
			out.write("Invalid command: " + ace.getMessage() 
					+ ", please view API documentation.");
		}
		catch (Exception e) {
			e.printStackTrace();
			out.write("Invalid request " + constructUrl(uri, request) 
					+ ", please check that your input is properly formatted " +
							"and review the API documentation.");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public String constructUrl(String uri, HttpServletRequest req) {
    	String ret = uri + "?";
    	Enumeration<?> params = req.getParameterNames();
    	while(params.hasMoreElements()) {
    		String elem = (String)params.nextElement();
    		ret += elem + "=" + (String)req.getParameter(elem) + "&";
    	}
    	return ret.replaceAll("&$", "");
    }

}
