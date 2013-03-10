package gov.nysenate.sage.deprecated.methods.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiFormatException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiInternalException;
import gov.nysenate.sage.deprecated.methods.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.service.GeoService.GeoException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoCodeMethod extends ApiExecution
{
  private final GeoService geoservice;

  public GeoCodeMethod() throws Exception
  {
    geoservice = new GeoService();
  }

  @Override
  public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiException
  {
    String service = request.getParameter("service");
    service = service == null ? "mapquest" : service;
    String type = more.get(RequestCodes.TYPE.code());
    ArrayList<Address> addresses;

    if (type.equals("addr")) {
      addresses = new ArrayList<Address>(Arrays.asList(new Address(more.get(RequestCodes.ADDRESS.code()))));
    }
    else if (type.equals("extended")) {
      String addr2 = request.getParameter("addr2");
      addr2 = (addr2 == null ? request.getParameter("street") : addr2);
      addr2 = (addr2 == null ? request.getParameter("address") : addr2);
      String number = request.getParameter("number");
      addresses = new ArrayList<Address>(Arrays.asList(new Address(
        request.getParameter("addr1"),
        (number != null ? number + " ":"") + addr2,
        request.getParameter("city"),
        request.getParameter("state"),
        request.getParameter("zip5"),
        request.getParameter("zip4")
      )));
    }
    else if (type.equals("bulk")) {
      addresses = new ArrayList<Address>();

      try {
       // Parse the json here
        JSONArray jsonAddresses;
        jsonAddresses = new JSONArray(request.getParameter("json"));
        for (int i=0; i<jsonAddresses.length(); i++) {
          JSONObject jsonAddress = jsonAddresses.getJSONObject(i);
          String addr2 = jsonAddress.has("addr2") ? jsonAddress.getString("addr2") : "";
          addr2 = (addr2 == "" && jsonAddress.has("street") ? jsonAddress.getString("street") : addr2);
          addr2 = (addr2 == "" && jsonAddress.has("address") ? jsonAddress.getString("address") : addr2);
          String number = jsonAddress.has("number") ? jsonAddress.getString("number"): "";
          Address newAddress = new Address(
            jsonAddress.has("addr1") ? jsonAddress.getString("addr1") : "",
            (number != null ? number + " ":"") + addr2,
            jsonAddress.has("city") ? jsonAddress.getString("city") : "",
            jsonAddress.has("state") ? jsonAddress.getString("state") : "",
            jsonAddress.has("zip5") ? jsonAddress.getString("zip5") : "",
            jsonAddress.has("zip4") ? jsonAddress.getString("zip4") : ""
          );
          addresses.add(newAddress);
        }
      }
      catch (JSONException e) {
        throw new ApiInternalException("Invalid JSON", e);
      }
    }
    else {
      throw new ApiTypeException(type);
    }

    try {
      ArrayList<Object> ret = new ArrayList<Object>();
      ArrayList<Result> results = geoservice.geocode(addresses, service);
      for (Result result : results) {
        if (result == null) {
          ret.add(new ErrorResponse("Internal Geocoding Error."));
        }
        else if (!result.getStatus().equals("0")) {
          ret.add(new ErrorResponse(result.getFirstMessage()));
        }
        else {
          Address bestMatch = result.getFirstAddress();
          ret.add(new Point(bestMatch.latitude, bestMatch.longitude));
        }
      }

      if (type.equals("addr") || type.equals("extended")) {
        return ret.get(0);
      }
      else {
        return ret;
      }

    }
    catch (GeoException e) {
      throw new ApiInternalException("Fatal geocoding Error.", e);
    }
  }


  @Override
  public String toOther(Object obj, String format) throws ApiFormatException
  {
    if (format.equals("csv")) {
      if (obj instanceof Point) {
        return ((Point)obj).toString();
      }
      else if (obj instanceof String) {
        return obj.toString();
      }
      else if (obj instanceof Collection<?>) {
        String ret = "";
        for (Object o : (Collection<?>)obj) {
          if (o instanceof Point) {
            ret += ((Point)o).toString() + "\n";
          }
        }
        return ret;
      }
    }
    else {
      throw new ApiFormatException(format);
    }
    return null;
  }
}
