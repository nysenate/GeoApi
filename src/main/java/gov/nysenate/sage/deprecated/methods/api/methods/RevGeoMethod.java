package gov.nysenate.sage.deprecated.methods.api.methods;

import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiInternalException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RevGeoMethod extends ApiExecution
{
  @Override
  public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException
  {
    String type = more.get(RequestCodes.TYPE.code());
    if (!type.equals("latlon")) {
      throw new ApiTypeException(type);
    }

    try {
      String service = request.getParameter("service");
      //return GeoCode.getReverseGeoCodedResponse(more.get(RequestCodes.LATLON.code()), service);
        return null;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ApiInternalException();
    }
  } // execute()
}
