package gov.nysenate.sage.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

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

    /**
     * Constructs a new Address object using the query parameters of the supplied HttpServletRequest.
     * This method exists to provide consistency among the different controllers when retrieving an
     * address from the query string.
     * @param r     HttpServletRequest object
     * @return      new Address instance if r was valid
     *              null if r was null
     */
    public static Address getAddressFromParams(HttpServletRequest r)
    {
        Address address = null;
        if (r != null){
            address = new Address(r.getParameter("addr1"), r.getParameter("addr2"), r.getParameter("city"),
                    r.getParameter("state"), r.getParameter("zip5"),  r.getParameter("zip4"));
        }
        return address;
    }

    public static Point getPointFromParams(HttpServletRequest r)
    {
        Point point = null;
        if (r != null){
            try {
                point = new Point(Double.parseDouble(r.getParameter("lat")), Double.parseDouble(r.getParameter("lon")));
                return point;
            }
            catch (Exception ex) {
                logger.debug(ex.getMessage());
            }
        }
        return null;
    }

    public static void sendResultMap(HttpServletRequest request, Map<String,Object> resultMap)
    {
        request.setAttribute("response", resultMap);
    }

    public static ArrayList<Address> getAddressesFromJsonBody(HttpServletRequest r)
    {
        try {
            InputStream inputStream = r.getInputStream();
            String json = IOUtils.toString(r.getInputStream(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
        }
        catch(Exception ex){

        }
        return null;
    }
}
