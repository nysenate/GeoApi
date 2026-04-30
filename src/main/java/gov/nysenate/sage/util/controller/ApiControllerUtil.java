package gov.nysenate.sage.util.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.GenericResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static gov.nysenate.sage.util.controller.ConstantUtil.ADMIN_USERNAME_ATTR;

public final class ApiControllerUtil {
    public static final BaseResponse invalidAuthResponse =
            new GenericResponse(false, "You must be logged in as an administrator to access this API."),
            successResponse = new BaseResponse(ResultStatus.SUCCESS);
    private static final Logger logger = LogManager.getLogger(ApiControllerUtil.class);

    private ApiControllerUtil() {}

    /**
     * Constructs a new Address object using the query parameters of the supplied input.
     * This method exists to provide consistency among the different controllers when retrieving an
     * address from the api.
     * @param addr  complete address in 1 param
     * @return      new Address instance if r was valid
     */
    public static Address getAddressFromParams(String addr, String addr1, String addr2, String postalCity,
                                               String state, String zip5, String zip4) {
        if (addr == null) {
            return new Address(addr1, addr2, postalCity, state, zip5, zip4);
        }
        return Address.getAddress(addr);
    }

    /**
     * Constructs a new Point object using the query parameters of the supplied HttpServletRequest
     * @param lat String
     * @param lon String
     * @return      new Point instance or null
     */
    public static Point getPointFromParams(String lat, String lon) {
        if (lat != null && lon != null) {
            try {
                return new Point(FormatUtil.cleanString(lat), FormatUtil.cleanString(lon));
            }
            catch (Exception ignored) {}
        }
        return null;
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
    public static List<Address> getAddressesFromJsonBody(String json) {
        List<Address> addresses = new ArrayList<>();
        try {
            logger.trace("Batch address json body: {}", json);
            ObjectMapper mapper = new ObjectMapper();
            return List.of(mapper.readValue(json, Address[].class));
        }
        catch(Exception ex) {
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
     * @return a List of Points
     */
    public static List<Point> getPointsFromJsonBody(String json) {
        var points = new ArrayList<Point>();
        try {
            logger.trace("Batch points json body {}", json);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            for (int i = 0; i < node.size(); i++) {
                JsonNode point = node.get(i);
                points.add(new Point(point.get("lat").asText(), point.get("lon").asText()));
            }
        }
        catch(Exception ex) {
            logger.debug("No valid batch point payload detected.");
            logger.trace(ex);
        }
        return points;
    }


    /**
     * Sets the current session as either authenticated or not authenticated. If the user is specified as
     * not authenticated, the entire session will be invalidated.
     * @param request HttpServletRequest
     * @param authenticated Indicate if the user's admin credentials were valid.
     * @param username Indicate the admin username used for login.
     */
    public static void setAuthenticated(HttpServletRequest request, boolean authenticated, String username) {
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
     * Reads the 'from' query parameter and builds a timestamp object representing that date/time.
     * The 'from' parameter will contain a Unix time value (milliseconds since 0 UTC).
     * If the 'from' value is null or invalid, a timestamp of exactly one week ago from the current time
     * will be returned.
     * @param request HttpServletRequest containing the 'from' query parameter.
     * @return Timestamp
     */
    public static Timestamp getBeginTimestamp(HttpServletRequest request) {
        try {
            return new Timestamp(Long.parseLong(request.getParameter("from")));
        }
        catch (Exception ex) {
            logger.debug("Invalid `from` timestamp parameter.", ex);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -7);
            return new Timestamp(c.getTimeInMillis());
        }
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
        try {
            return new Timestamp(Long.parseLong(request.getParameter("to")));
        }
        catch (Exception ex) {
            logger.debug("Invalid `to` timestamp parameter.", ex);
            return new Timestamp(new Date().getTime());
        }
    }

    /**
     * Retrieves the ip address from HttpServletRequest.
     */
    // TODO: same logic used in ApiRequest constructor
    public static String getIpAddress(HttpServletRequest request) {
        String forwardedForIp = request.getHeader("x-forwarded-for");
        return forwardedForIp == null ? request.getRemoteAddr() : forwardedForIp;
    }
}
