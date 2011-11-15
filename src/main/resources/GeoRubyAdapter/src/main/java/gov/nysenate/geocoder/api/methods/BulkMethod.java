package gov.nysenate.geocoder.api.methods;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.geocoder.model.ApiExecution;
import gov.nysenate.geocoder.api.exceptions.ApiInternalException;
import gov.nysenate.geocoder.api.exceptions.ApiTypeException;
import gov.nysenate.geocoder.bulk.RequestHandler;

public class BulkMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
		return new RequestHandler(request).execute();
	}
	
	@Override
	public String toOther(Object obj, String format) {
		return (String) obj;
	}
}
