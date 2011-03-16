package gov.nysenate.sage.api.methods;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.util.SqliteAdapter;

public class StreetLookupMethod extends ApiExecution {
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException {
		
		Object ret = null;		
		String type = more.get(RequestCodes.TYPE.code());
		
		if(type.equals("zip")) {
			String zip = more.get(RequestCodes.ZIP.code());
			
			if(zip.length() == 5) {
				ret = SqliteAdapter.getInstance().getStreetsForZip(zip);
			}
			else {
				throw new ApiTypeException(zip);
			}
		}
		else {
			throw new ApiTypeException(type);
		}
		
		return ret;
	}
}
