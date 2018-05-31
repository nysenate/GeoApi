package gov.nysenate.sage.util.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.filter.ApiFilter;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_USERNAME_ATTR;

public class ApiControllerUtil {

    private static Logger logger = LogManager.getLogger(ApiControllerUtil.class);

    /**
     * Constructs a new Address object using the query parameters of the supplied HttpServletRequest.
     * This method exists to provide consistency among the different controllers when retrieving an
     * address from the query string.
     * @param request     HttpServletRequest object
     * @return      new Address instance if r was valid
     *              null if r was null
     */
    public static Address getAddressFromParams(HttpServletRequest request)
    {
        Address address = null;
        if (request != null){
            if (request.getParameter("addr") != null) {
                address = new Address(request.getParameter("addr"));
            }
            else {
                address = new Address(request.getParameter("addr1"), request.getParameter("addr2"), request.getParameter("city"),
                        request.getParameter("state"), request.getParameter("zip5"),  request.getParameter("zip4"));
            }
        }
        return address;
    }

    /**
     * Constructs a new Address object using the query parameters of the supplied input.
     * This method exists to provide consistency among the different controllers when retrieving an
     * address from the api.
     * @param addr  complete address in 1 param
     * @param addr1
     * @param addr2
     * @param city
     * @param state
     * @param zip5
     * @param zip4
     * @return      new Address instance if r was valid
     *              null if r was null
     */
    public static Address getAddressFromParams(String addr, String addr1, String addr2 , String city,
                                               String state, String zip5, String zip4)
    {
        Address address = null;
            if (addr != null) {
                address = new Address(addr);
            }
            else {
                address = new Address(addr1, addr2, city, state, zip5, zip4);
            }
        return address;
    }

    /**
     * Constructs a new Point object using the query parameters of the supplied HttpServletRequest
     * @param r     HttpServletRequest
     * @return      new Point instance or null
     */
    public static Point getPointFromParams(HttpServletRequest r)
    {
        Point point = null;
        if (r != null){
            try {
                point = new Point(Double.parseDouble(r.getParameter("lat")), Double.parseDouble(r.getParameter("lon")));
            }
            catch (Exception ex) { /** Ignored Exception */ }
        }
        return point;
    }

    /** Delegates response to ApiFilter */
    public static void setApiResponse(Object response, HttpServletRequest request)
    {
        ApiFilter.setApiResponse(response, request);
    }

    /**
     * Constructs a collection of Address objects using the JSON payload data in the body of the
     * HttpServletRequest. The root JSON element must be an array containing a collection of
     * address component objects e.g
     * <code>
     *  [{"addr1":"", "addr2":"", "city":"", "state":"","zip5":"", "zip4":""} .. ]
     * </code>
     * @param json Json payload
     * @return ArrayList<Address>
     */
    public static ArrayList<Address> getAddressesFromJsonBody(String json)
    {
        ArrayList<Address> addresses = new ArrayList<>();
        try {
            logger.trace("Batch address json body: " + json);
            ObjectMapper mapper = new ObjectMapper();
            return new ArrayList<>(Arrays.asList(mapper.readValue(json, Address[].class)));
        }
        catch(Exception ex){
            logger.debug("No valid batch address payload detected.");
            logger.trace(ex);
        }
        return addresses;
    }

    /**
     * Constructs a collection of Point objects using the JSON payload data in the body of the
     * HttpServletRequest. The root JSON element must be an array containing a collection of
     * point component objects containing numerical values for "lat" and "lon" e.g
     * <code>
     *     [{"lat":43.123 , "lon":-73.123 }, ..]
     * </code>
     * @param json Json payload
     * @return
     */
    public static ArrayList<Point> getPointsFromJsonBody(String json)
    {
        ArrayList<Point> points = new ArrayList<>();
        try {
            logger.trace("Batch points json body " + json);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            for (int i = 0; i < node.size(); i++) {
                JsonNode point = node.get(i);
                points.add(new Point(point.get("lat").asDouble(), point.get("lon").asDouble()));
            }
        }
        catch(Exception ex){
            logger.debug("No valid batch point payload detected.");
            logger.trace(ex);
        }
        return points;
    }


    /**
     * Determine if the current session is authentic by checking to see if an admin auth session key has been set.
     * @param request HttpServletRequest
     * @return True if authenticated (user has logged in as admin), false otherwise.
     */
    public static boolean isAuthenticated(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        if (session.getAttribute(ADMIN_USERNAME_ATTR) != null) {
            return true;
        }
        return false;
    }

    /**
     * Sets the current session as either authenticated or not authenticated. If the user is specified as
     * not authenticated, the entire session will be invalidated.
     * @param request HttpServletRequest
     * @param authenticated Indicate if the user's admin credentials were valid.
     * @param username Indicate the admin username used for login.
     */
    public static void setAuthenticated(HttpServletRequest request, boolean authenticated, String username)
    {
        HttpSession session = request.getSession();
        if (authenticated) {
            session.setAttribute(ADMIN_USERNAME_ATTR, username);
        }
        else {
            session.setAttribute(ADMIN_USERNAME_ATTR, null);
            session.invalidate();
        }
    }

    /**
     * Since Admin requests do not go through the ApiFilter this method is needed to write result
     * objects as JSON into the servlet response.
     * @param responseObj Object to serialize.
     * @param response The HeepServletResponse to write the result to.
     */
    public static void setAdminResponse(Object responseObj, HttpServletResponse response)
    {
        String json;
        try {
            json = FormatUtil.toJsonString(responseObj);
            response.setContentType("application/json");
            response.setContentLength(json.length());
            response.getWriter().write(json);
        }
        catch(JsonProcessingException ex) {
            logger.error("Failed to json format admin response!", ex);
        }
        catch(IOException ex) {
            logger.error("Failed to write admin response", ex);
        }
    }

    /**
     * Creates an error response message for an api user that is not authenticated as an admin trying to
     * access and admin api
     * @return GenericResponse
     */
    public static GenericResponse invalidAuthResponse() {
        return new GenericResponse(false, "You must be logged in as an administrator to access this API.");
    }

    /**
     * Reads the 'from' query parameter and builds a timestamp object representing that date/time.
     * The 'from' parameter will contain a Unix time value (milliseconds since 0 UTC).
     * If the 'from' value is null or invalid, a timestamp of exactly one week ago from the current time
     * will be returned.
     * @param request HttpServletRequest containing the 'from' query parameter.
     * @return Timestamp
     */
    public static Timestamp getBeginTimestamp(HttpServletRequest request) {
        Timestamp from;
        try {
            from = new Timestamp(Long.parseLong(request.getParameter("from")));
        }
        catch (Exception ex) {
            logger.debug("Invalid `from` timestamp parameter.", ex);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -7);
            from = new Timestamp(c.getTimeInMillis());
        }
        return from;
    }

    /**
     * Reads the 'to' query parameter ands builds a Timestamp object representing the date/time.
     * The 'to' parameter will contain a Unix time value (milliseconds from 0 UTC).
     * If the 'to' parameter is null or invalid, a new Timestamp representing the current time will
     * be returned.
     * @param request HttpServletRequest containing the 'to' query parameter.
     * @return Timestamp
     */
    public static Timestamp getEndTimestamp(HttpServletRequest request) {
        Timestamp to;
        try {
            to = new Timestamp(Long.parseLong(request.getParameter("to")));
        }
        catch (Exception ex) {
            logger.debug("Invalid `to` timestamp parameter.", ex);
            to = new Timestamp(new Date().getTime());
        }
        return to;
    }
}
