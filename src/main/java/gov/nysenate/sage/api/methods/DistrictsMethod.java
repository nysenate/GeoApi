package gov.nysenate.sage.api.methods;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.DistrictServices;
import gov.nysenate.sage.model.ApiExecution;

public class DistrictsMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
				
		Object ret = null;		
		String type = more.get(RequestCodes.TYPE.code());
		String service = request.getParameter("service");
		
		if(type.equals("addr")) {
			try {
				ret = DistrictServices.getDistrictsFromAddress(more.get(RequestCodes.ADDRESS.code()), service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("extended")) {
			try {
				ret = DistrictServices.getDistrictsFromAddress(
							request.getParameter("addr2"), 
							request.getParameter("city"), 
							request.getParameter("state"), 
							request.getParameter("zip4"), 
							request.getParameter("zip5"),
							request.getParameter("validate"),
							request.getParameter("nometa"),
						service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("latlon")) {
			try {
				ret = DistrictServices.getDistrictsFromPoint(more.get(RequestCodes.LATLON.code()), service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else {
			throw new ApiTypeException(type);
		}
		
		return ret;
	}
}
