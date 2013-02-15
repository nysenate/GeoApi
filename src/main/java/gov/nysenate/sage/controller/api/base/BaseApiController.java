package gov.nysenate.sage.controller.api.base;

import gov.nysenate.sage.model.address.Address;
import org.apache.commons.io.IOUtils;

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

    public static void sendResultMap(HttpServletRequest request, Map<String,Object> resultMap)
    {
        request.setAttribute("response", resultMap);
    }

    /*public static ArrayList<Address> getAddressesFromJsonBody(HttpServletRequest r)
    {
        try {
            InputStream inputStream = r.getInputStream();
            String json = IOUtils.toString(r.getInputStream(), "UTF-8");
            System.out.println(json);
        }
        catch(Exception ex){

        }
        return null;
    }*/
}
