package gov.nysenate.sage.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.filter.ApiFilter;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The base API controller provides various common methods for the controllers
 * to use.
 */
public abstract class BaseApiController extends HttpServlet
{
    private static Logger logger = Logger.getLogger(BaseApiController.class);
    public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void init(ServletConfig config) throws ServletException;

    public static ApiRequest getApiRequest(HttpServletRequest r)
    {
        return ApiFilter.getApiRequest(r);
    }

    /** Convenience method to see if request parameter equals a certain value */
    public boolean requestParameterEquals(HttpServletRequest request, String key, String value)
    {
        if (request.getParameter(key) != null && request.getParameter(key).equals(value)) {
            return true;
        }
        return false;
    }

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
     * Constructs a new Point objct using the query parameters of the supplied HttpServletRequest
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
            catch (Exception ex) {
                logger.debug(ex.getMessage());
            }
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
     * @param r
     * @return
     */
    public static ArrayList<Address> getAddressesFromJsonBody(HttpServletRequest r)
    {
        ArrayList<Address> addresses = new ArrayList<>();
        try {
            String json = IOUtils.toString(r.getInputStream(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            for ( int i = 0; i < root.size(); i++ ){
                JsonNode node = root.get(i);
                addresses.add(new Address(node.get("addr1").asText(), node.get("addr2").asText(),
                                          node.get("city").asText(),  node.get("state").asText(),
                                          node.get("zip5").asText(),  node.get("zip4").asText()));
            }
        }
        catch(Exception ex){
            logger.error("Failed to get address from JSON body.", ex);
        }
        return addresses;
    }
}
