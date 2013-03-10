package gov.nysenate.sage.deprecated.methods.api.methods;

import gov.nysenate.sage.Response;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.provider.USPS;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiInternalException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.ErrorResponse;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ValidateMethod extends ApiExecution {
	USPS usps;

    public ValidateMethod() throws Exception {
        usps = new USPS();
    }

	@Override
	public Response execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiException {
	    String type = more.get(RequestCodes.TYPE.code());
		if(!type.equals("extended"))
		    throw new ApiTypeException(type);

        Result result = null;
	    /*
        Result result = usps.validate(new Address(
	            request.getParameter("addr1"),
                request.getParameter("addr2"),
                request.getParameter("city"),
                request.getParameter("state"),
                request.getParameter("zip5"),
                request.getParameter("zip4")
        ));
        */

	    if (result==null) {
	        throw new ApiInternalException();
	    } else if (result.getStatus().equals("0")) {
	        //return new ValidateResponse(result.getAddress());
	    } else {
	        String msg = "";
	        for (String m : result.getMessages()) {
	            msg += "\n"+m;
	        }
	        return new ErrorResponse(msg.toString());
	    }
        return null;
	}
}
