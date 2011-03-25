package gov.nysenate.sage.servlets;

import gov.nysenate.sage.api.exceptions.ApiAuthenticationException;
import gov.nysenate.sage.api.exceptions.ApiCommandException;
import gov.nysenate.sage.api.exceptions.ApiFormatException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.api.methods.MethodLoader;
import gov.nysenate.sage.model.ApiMethod;
import gov.nysenate.sage.model.ApiUser;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.model.abstracts.AbstractApiExecute;
import gov.nysenate.sage.util.ApiController;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ApiServlet extends HttpServlet {
	private Logger logger = Logger.getLogger(ApiServlet.class);
	
	private static final long serialVersionUID = 1L;
	
	HashMap<String,ApiMethod> methods = null;
	ApiController control = null;		
	
	public void init(ServletConfig config) throws ServletException {
		logger.info("Initializing ApiServlet");
		
		super.init(config);
		Resource.init(this);
		methods = MethodLoader.getMethods();
		control = new ApiController();
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Resource.init(this);
		
		PrintWriter out = response.getWriter();

		String uri = java.net.URLDecoder.decode(request.getRequestURI(),"utf-8");
		
		logger.info("Request URI: " + uri);
		
		uri = uri.toLowerCase().replaceAll("(/geoapi/|api/)", "");
		
		StringTokenizer stok = new StringTokenizer(uri,"/");
		
		String key = request.getParameter("key");
		String format = null;
		String command = null;
		String type = null;
				
		try {
			ApiUser user = null;
			if(key == null)
				user = control.getUser(Resource.get("user.default"));
			else
				user = control.getUser(key);
			if(user == null)
				throw new ApiAuthenticationException();
			
			format = stok.nextToken();
			command = stok.nextToken();
			type = stok.nextToken();
			
			ApiMethod method = null;
			if((method = methods.get(command)) != null ) {
				if(!method.validFormat(format))
					throw new ApiFormatException(format);
				
				if(!method.validType(type))
					throw new ApiTypeException(type);
				
				AbstractApiExecute executionClass = method.getInstanceOfExecutionClass();
				Object obj = executionClass.execute(request, response, getMore(format, type, stok));
				
				if(obj == null) {
					throw new ApiInternalException();
				}
				else {
					if(format.equals("xml")) {
						out.print(executionClass.toXml(obj, method.getXstreamClasses()));
					}
					else if(format.equals("json")) {
						out.print(executionClass.toJson(obj));
					}
					else {
						out.print(executionClass.toOther(obj, format));
					}
				}

				
				if(method.getWriteMetric()) {
					logger.info("writing metric");
					
					control.addMetric(user.getId(), constructUrl(uri, request), request.getRemoteAddr());
				}
			}
			else {
				throw new ApiCommandException(command);
			}
		}
		catch (ApiCommandException ace) {
			out.write(getError("error", "Invalid command: " + ace.getMessage() 
					+ ", please view API documentation.", format));
			logger.warn(ace);
		}
		catch (ApiFormatException afe) {
			out.write(getError("error", "Invalid format: " + afe.getMessage()  +
					" for command: " + command + ", please review API documentation.", format));
			logger.warn(afe);
		}
		catch (ApiTypeException ate) {
			out.write(getError("error", "Invalid input " + ate.getMessage() 
					+ " for command: " + command +", please review API documentation.", format));
			logger.warn(ate);
		}
		catch (ApiAuthenticationException aae) {
			out.write(getError("error", "Could not be authorized.", format));
			logger.warn(aae);
		}
		catch (Exception e) {
			out.write(getError("error", "Invalid request " + constructUrl(uri, request) 
					+ ", please check that your input is properly formatted " +
							"and review the API documentation.", format));
			logger.warn(e);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public String getError(String name, String reason, String format) {
		logger.warn("Error! name: " + name + ", reason: " + reason);
		
		ErrorResponse e = new ErrorResponse(reason);
		if(format != null && format.matches("xml|kml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
			return xstream.toXML(e);
		}
		else if(format != null && format.matches("json")) {
			Gson gson = new Gson();
			return gson.toJson(e);
		}
		else {
			return "ERROR," + reason.replaceAll(",", "");
		}
		
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
	
	public ArrayList<String> getMore(String format, String type, StringTokenizer stok) {
		ArrayList<String> strings = new ArrayList<String>();
		strings.add(format);
		strings.add(type);
		while(stok.hasMoreTokens()) {
			strings.add(stok.nextToken());
		}
		return strings;
	}

	
}
