package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.util.SqliteAdapter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StreetLookupMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException {
	    String type = more.get(RequestCodes.TYPE.code());
	    if(!type.equals("zip"))
	        throw new ApiTypeException(type);

		String zip = more.get(RequestCodes.ZIP.code());
		if(zip.length() != 5)
		    throw new ApiTypeException(zip);

		return SqliteAdapter.getInstance().getStreetsForZip(zip);
	}
}
