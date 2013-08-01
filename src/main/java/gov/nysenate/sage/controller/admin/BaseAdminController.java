package gov.nysenate.sage.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

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
    private static Logger logger = Logger.getLogger(BaseAdminController.class);

    protected static String AUTH_ATTR = "authenticated";
    protected static String ADMIN_USERNAME_ATTR = "adminUserName";
    protected static String ADMIN_REQUEST_ATTR = "adminRequest";

    public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void init(ServletConfig config) throws ServletException;

    protected static boolean isAuthenticated(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        if (session.getAttribute(ADMIN_USERNAME_ATTR) != null) {
            return true;
        }
        return false;
    }

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
     *
     * @param request
     * @return
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
     *
     * @param request
     * @return
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
