package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.*;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import connectors.DMVConnect;
import connectors.USPSConnect;
import control.ApiController;
import control.DistrictServices;
import control.GeoCode;
import control.Resource;


/**
 * Servlet implementation class ApiServlet
 */
public class ApiServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static final String API_COMMANDS = "(districts|geocode|revgeo|validate|poly|polysearch|citystatelookup|zipcodelookup)";
	public static final String API_FORMATS = "(xml|json|kml)";
	public static final String API_POLY_TYPES = "(senate|assembly|election|county|congressional)";
	public static final String API_INPUT_TYPE = "(addr|latlon|extended)";
	
    public ApiServlet() {
        super();
    }
    
   /**
    * Main servlet, breaks down URI and processes command, outputting desired response or error message
    * 
    * structure of URI: /GeoApi/api/{format}/{command}/{additional parameters}?{arguments}
    * 
    */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
												throws ServletException, IOException {
		Resource.init(this);
				
		PrintWriter out = response.getWriter();
						
		String uri = java.net.URLDecoder.decode(request.getRequestURI(),"utf-8");
		uri = uri.toLowerCase().replaceAll("(/geoapi/|api/)", "");
		
		String format = null;
		
		String host = request.getRemoteAddr();
		
		String key = request.getParameter("key");
		
		try {
			 /* user authentication
			 */
			
			ApiController control = new ApiController();		
			ApiUser user = null;
			if(key == null) {
				user = control.getUser(Resource.get("user.default"));
			}
			else {
				user = control.getUser(key);
			}
			
			if(user == null) {
				throw new ApiInputException("Invalid API key.");
			}
			control.addMetric(user.getId(), constructUrl(uri, request), host);
			
			
			
			 /* api call validation
			 */ 
			 
			
			StringTokenizer stok = new StringTokenizer(uri,"/");
			
			format = stok.nextToken().toLowerCase();			
			if(format == null || !format.matches(API_FORMATS)) {
				format = (format == null) ? "(none)" : format;
				throw new ApiInputException("Invalid format: " 
						+ format 
						+ ", please review API documentation");
			}
			
			String command = stok.nextToken().toLowerCase();			
			if(command == null || !command.matches(API_COMMANDS)) {
				command = (command == null) ? "(none)" : command;
				throw new ApiInputException("Invalid command: " 
						+ command 
						+ ", please review API documentation");
			}
			
			String type = stok.nextToken().toLowerCase();
			if(type == null || !type.matches(API_INPUT_TYPE)) {
				if(!command.matches("poly(search)?")) {
					type = (type == null) ? "(none)" : type;
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ ", please review API documentation");
				}
			}
			
			
			 /* api call execution
			 */
			 
			
			String service = request.getParameter("service");
			service = (service != null) ? service.toLowerCase() : null;
			
			String addr1 = request.getParameter("addr1");
			String addr2 = request.getParameter("addr2");
			String city = request.getParameter("city");
			String state = request.getParameter("state");
			String zip4 = request.getParameter("zip4");
			String zip5 = request.getParameter("zip5");
			
			if(command.equals("districts")) {
				if(type.equals("addr")){
					out.write(new DistrictServices().getDistrictsFromAddress(
							stok.nextToken(), format, service));
				
				}
				else if(type.equals("extended")){
					out.write(new DistrictServices().getDistrictsFromAddress(
							getExtendedAddress(addr2, city, state, zip4, zip5), format, service));
				
				}
				else if(type.equals("latlon")){
					out.write(new DistrictServices().getDistrictsFromPoint(
							stok.nextToken(), format, service));
				
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				
				}
			}
			else if(command.equals("geocode")) {
				if(type.equals("extended")){
					out.write(GeoCode.getApiGeocodeResponse(
							getExtendedAddress(addr2, city, state, zip4, zip5), format, service));
				
				}
				else if(type.equals("addr")) {
					out.write(GeoCode.getApiGeocodeResponse(
							stok.nextToken(), format, service));
					
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}					
			}
			else if(command.equals("revgeo")) {				
				if(type.equals("latlon")) {
					out.write(GeoCode.getApiReverseGeocodeResponse(
							stok.nextToken(), format, service));
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}					
			}
			else if(command.equals("validate")) {
				service = (service == null ? "usps" : service);
				
				if(type.equals("extended")) {
					
					if(service.equals("usps")) {
						out.write(USPSConnect.validateAddress(
								addr1, addr2, city, state, zip5, zip4, format));

					}
					else if(service.equals("dmv")) {
						out.write(DMVConnect.validateAddress(
								addr2, city, state, zip5, format));
						
					}
					else {
						throw new ApiInputException("Invalid input type: " 
								+ type
								+ " for command " 
								+ command 
								+", please review API documentation");
					}					
				}
				else {
					throw new ApiInputException(type);
					
				}
			}
			else if(command.equals("poly")) {
				if(!type.matches(API_POLY_TYPES)) {
					throw new ApiInputException("Invalid district type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}
				
				String district = "";
				district = stok.nextToken().toLowerCase();
				format = format.equals("xml") ? "kml" : format;				
				if(new Integer(district) != null) {
					new DistrictServices().getPolyFromDistrict(type, district, format, out);
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}
			}
			else if(command.equals("polysearch")) {
				if(!type.matches(API_POLY_TYPES)) {
					throw new ApiInputException("Invalid district type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}
				
				String inputType = "";
				inputType = stok.nextToken().toLowerCase();
				format = format.equals("xml") ? "kml" : format;				
				
				if(inputType.equals("addr")){
					new DistrictServices().getPolyFromAddress(
							stok.nextToken(), format, service, type, out);
				
				}
				else if(inputType.equals("extended")){
					new DistrictServices().getPolyFromAddress(
							getExtendedAddress(addr2, city, state, zip4, zip5), format, service, type, out);
				
				}
				else if (inputType.equals("latlon")){
					new DistrictServices().getPolyFromPoint(
							stok.nextToken(), format, service, type, out);
					
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}
			}
			else if(command.equals("citystatelookup")) {
				
				if(type.equals("extended")) {
					out.write(USPSConnect.getCityStateByZipCode(zip5, format));
				
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
				}
			}
			else if(command.equals("zipcodelookup")) {
				
				if(type.equals("extended")) {
					out.write(USPSConnect.getZipByAddress(addr1,addr2,city,state,format));
				
				}
				else {
					throw new ApiInputException("Invalid input type: " 
							+ type 
							+ " for command " 
							+ command 
							+", please review API documentation");
					
				}
			}
			else {
				throw new ApiInputException("Invalid command: " 
						+ command 
						+ ", please review API documentation");
			}
		}
		catch(ApiInputException bce) {
			out.write(getError("error",bce.getMessage(), format));
		}
		catch(Exception e) {
			if(uri.matches("/GeoApi/?")) {
				
			}
			else {
				e.printStackTrace();
				out.write(getError("error","Invalid request " 
						+ constructUrl(uri, request)
						+ ", please check that your input"
						+ " is properly formatted and review the API documentation", format));
			}
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
	
	public String getExtendedAddress(String addr, String city, String state, String zip4, String zip5) {
		return (addr != null ? addr : "")
			+ ",%20" 
			+ (city  != null ? city : "")
			+ ",%20" 
			+ (state != null ? state : "")
			+ "%20" 
			+ (zip5 != null ? zip5 : "")
			+ (zip4 != null ? "-" + zip4 : ""); 
	}
	
	public String getError(String name, String reason, String format) {
		ErrorResponse e = new ErrorResponse(reason);
		if(format != null && format.matches("xml|kml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
			return xstream.toXML(e);
		}
		Gson gson = new Gson();
		return gson.toJson(e);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
														throws ServletException, IOException {
		doGet(request,response);
	}
	
	public class ApiInputException extends Exception {
		private static final long serialVersionUID = 1L;
		public ApiInputException(String message) {
			super(message);
		}
		public ApiInputException(String message, Throwable t) {
			super(message,t);
		}
	}

}