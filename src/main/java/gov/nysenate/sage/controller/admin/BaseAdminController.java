package gov.nysenate.sage.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseAdminController extends HttpServlet
{
    private static Logger logger = LogManager.getLogger(BaseAdminController.class);

    protected static String AUTH_ATTR = "authenticated";
    protected static String ADMIN_USERNAME_ATTR = "adminUserName";
    protected static String ADMIN_REQUEST_ATTR = "adminRequest";

    public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void init(ServletConfig config) throws ServletException;

    /**
     * Determine if the current session is authentic by checking to see if an admin auth session key has been set.
     * @param request HttpServletRequest
     * @return True if authenticated (user has logged in as admin), false otherwise.
     */
    protected static boolean isAuthenticated(HttpServletRequest request)
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
    protected static void setAuthenticated(HttpServletRequest request, boolean authenticated, String username)
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
    protected static void setAdminResponse(Object responseObj, HttpServletResponse response)
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
     * Reads the 'from' query parameter and builds a timestamp object representing that date/time.
     * The 'from' parameter will contain a Unix time value (milliseconds since 0 UTC).
     * If the 'from' value is null or invalid, a timestamp of exactly one week ago from the current time
     * will be returned.
     * @param request HttpServletRequest containing the 'from' query parameter.
     * @return Timestamp
     */
    protected static Timestamp getBeginTimestamp(HttpServletRequest request) {
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
    protected static Timestamp getEndTimestamp(HttpServletRequest request) {
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
