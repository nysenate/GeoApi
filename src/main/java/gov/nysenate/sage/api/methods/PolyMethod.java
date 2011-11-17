package gov.nysenate.sage.api.methods;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.sage.api.exceptions.ApiFormatException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.DistrictServices;
import gov.nysenate.sage.connectors.DistrictServices.DistrictType;
import gov.nysenate.sage.model.ApiExecution;

public class PolyMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
		
		Object ret = null;
		String format = more.get(RequestCodes.FORMAT.code());
		String polyType = more.get(RequestCodes.POLY.code());
		Integer district = new Integer(more.get(RequestCodes.DISTRICT.code()));
		
		DistrictType districtType = DistrictType.getDistrictType(polyType);
				
		if(district != null) {
			try {
				ret = DistrictServices.getPolyFromDistrict(districtType, district+"", format);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
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
