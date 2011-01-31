package gov.nysenate.sage.api.methods;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.USPSConnect;
import gov.nysenate.sage.model.ApiExecution;

public class CityStateLookupMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException {
		
		Object ret = null;		
		String type = more.get(RequestCodes.TYPE.code());
		

		if(type.equals("extended")) {
			ret = USPSConnect.getCityStateByZipCode(request.getParameter("zip5"));
		}
		else {
			throw new ApiTypeException(type);
		}
		
		return ret;
	}
}
