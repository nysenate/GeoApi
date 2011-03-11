package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.api.exceptions.ApiFormatException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.GeoCode;
import gov.nysenate.sage.connectors.GeocoderConnect;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.Point;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GeoCodeMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
		
		Object ret = null;		
		String service = request.getParameter("service");
		String type = more.get(RequestCodes.TYPE.code());
		
		if(type.equals("addr")) {
			try {
				ret = GeoCode.getApiGeocodeResponse(more.get(RequestCodes.ADDRESS.code()), service);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("extended")) {
			try {
				String addr2 = request.getParameter("addr2");
				addr2 = (addr2 == null ? request.getParameter("street") : addr2);
				addr2 = (addr2 == null ? request.getParameter("address") : addr2);
				ret = GeoCode.getApiGeocodeResponse(request.getParameter("number"), 
						addr2, 
						request.getParameter("city"), 
						request.getParameter("state"), 
						request.getParameter("zip4"), 
						request.getParameter("zip5"),
						service);
			}
			catch(Exception e) {
				throw new ApiInternalException();
			}
		}
		else if(type.equals("bulk")) {
			try {
				String json = request.getParameter("json");
				return new GeocoderConnect().doBulkParsing(json);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else {
			throw new ApiTypeException(type);
		}
		return ret;
	}
	
	@Override
	public String toOther(Object obj, String format) throws ApiFormatException {
		if(format.equals("csv")) {
			if(obj instanceof Point) {
				return ((Point)obj).lat + "," + ((Point)obj).lon;
			}
			else if(obj instanceof String) {
				return obj.toString();
			}
			else if(obj instanceof Collection<?>) {
				String ret = "";
				for(Object o:(Collection<?>)obj) {
					if(o instanceof Point) {
						ret += ((Point)o).lat + "," + ((Point)o).lon + "\n";
					}
				}
				return ret;
			}
		}
		else {
			throw new ApiFormatException(format);
		}
		return null;
	}
}
