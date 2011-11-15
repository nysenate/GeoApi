package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.DistrictServices;
import gov.nysenate.sage.model.ApiExecution;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BluebirdMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
				
		Object ret = null;		
		String type = more.get(RequestCodes.TYPE.code());
		String service = request.getParameter("service");
		
		if(type.equals("extended")) {
			try {
				ret = DistrictServices.getDistrictsForBluebird(
							request.getParameter("addr2"), 
							request.getParameter("city"), 
							request.getParameter("state"), 
							request.getParameter("zip4"), 
							request.getParameter("zip5"),
						service);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("latlon")) {
			try {
				ret = DistrictServices.getDistrictsForBlueBird(more.get(RequestCodes.LATLON.code()));
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
