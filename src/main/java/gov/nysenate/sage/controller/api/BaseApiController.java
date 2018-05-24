package gov.nysenate.sage.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.filter.ApiFilter;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.geo.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The base API controller provides various common methods for the controllers
 * to use.
 */
public abstract class BaseApiController extends HttpServlet
{
    private static Logger logger = LogManager.getLogger(BaseApiController.class);
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


}
