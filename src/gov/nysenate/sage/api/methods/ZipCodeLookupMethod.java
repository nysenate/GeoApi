package gov.nysenate.sage.api.methods;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.connectors.USPSConnect;
import gov.nysenate.sage.model.ApiExecution;

public class ZipCodeLookupMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException {
		
		Object ret = null;		
		String type = more.get(RequestCodes.TYPE.code());
		

		if(type.equals("extended")) {
			ret = USPSConnect.getZipByAddress(request.getParameter("addr1"), 
					request.getParameter("addr2"), 
					request.getParameter("city"), 
					request.getParameter("state"));
		}
		else {
			throw new ApiTypeException(type);
		}
		
		return ret;
	}
}
