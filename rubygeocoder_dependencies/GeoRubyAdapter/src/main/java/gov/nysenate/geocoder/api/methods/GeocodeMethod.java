package gov.nysenate.geocoder.api.methods;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.geocoder.model.ApiExecution;
import gov.nysenate.geocoder.single.RubySingleRequestWrapper;
import gov.nysenate.geocoder.api.exceptions.ApiInternalException;
import gov.nysenate.geocoder.api.exceptions.ApiTypeException;

public class GeocodeMethod  extends ApiExecution  {
	
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {
//		RubySingleRequest rubySingleRequest = RubyConnectorWrapper.getInstance().getRubySingleRequest();
//		RubyConnector rubyConnector = rubySingleRequest.getRubyConnector();
//		
//		String result = rubyConnector.getResult(request);
//		rubySingleRequest.setActive(false);
//		
//		return result;
		
		return RubySingleRequestWrapper.fillRequest(request);
	}
	
	@Override
	public String toOther(Object obj, String format)  {
		return (String) obj;
	}
}
