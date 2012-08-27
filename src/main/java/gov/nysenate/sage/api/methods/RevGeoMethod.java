package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.GeoCode;
import gov.nysenate.sage.model.ApiExecution;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RevGeoMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {

		String type = more.get(RequestCodes.TYPE.code());
		if(!type.equals("latlon"))
		    throw new ApiTypeException(type);

		try {
		    Object ret = null;
		    String service = request.getParameter("service");
			ret = GeoCode.getReverseGeoCodedResponse(more.get(RequestCodes.LATLON.code()), service);
			return ret;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ApiInternalException();
		}
	}
}
