package gov.nysenate.sage.api.methods;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.sage.api.exceptions.ApiFormatException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.DistrictServices;
import gov.nysenate.sage.connectors.GeoCode;
import gov.nysenate.sage.connectors.DistrictServices.DistrictType;
import gov.nysenate.sage.model.ApiExecution;

public class PolySearchMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
		
		Object ret = null;
		String format = more.get(RequestCodes.FORMAT.code());
		String polyType = more.get(RequestCodes.POLY.code());
		String type = more.get(RequestCodes.POLY_TYPE.code());
		String service = request.getParameter("service");
		
		DistrictType districtType = DistrictType.getDistrictType(polyType);
		
		if(type.equals("addr")) {
			try {
				ret = DistrictServices.getPolyFromAddress(
						more.get(RequestCodes.POLY_ADDRESS.code()), format, service, districtType);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("latlon")) {
			try {
				ret = DistrictServices.getPolyFromPoint(
						more.get(RequestCodes.POLY_LATLON.code()), format, service, districtType);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("extended")) {
			try {
				ret = DistrictServices.getPolyFromAddress(GeoCode.getExtendedAddress(
						request.getParameter("addr2"), 
						request.getParameter("city"), 
						request.getParameter("state"), 
						request.getParameter("zip4"), 
						request.getParameter("zip5")), format, service, districtType);
			} catch (IOException e) {
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
	public String toXml(Object obj, ArrayList<Class<?>> xstreamClasses) {
		try {
			return toOther(obj, null);
		} catch (ApiFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toJson(Object obj) {
		try {
			return toOther(obj, null);
		} catch (ApiFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toOther(Object obj, String format) throws ApiFormatException {
		if(obj instanceof StringBuffer) {
			return ((StringBuffer)obj).toString();
		}
		else {
			throw new ApiFormatException(format);
		}
	}
}
