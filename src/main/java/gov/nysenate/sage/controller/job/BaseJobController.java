package gov.nysenate.sage.controller.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.job.JobRequest;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public abstract class BaseJobController extends HttpServlet
{
    private static Logger logger = Logger.getLogger(BaseJobController.class);
    private static ObjectMapper jsonMapper = new ObjectMapper();
    protected static String AUTH_ATTR = "authenticated";
    protected static String JOB_USER_ATTR = "jobuser";
    protected static String JOB_REQUEST_ATTR = "jobrequest";

    public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void init(ServletConfig config) throws ServletException;

    public static boolean isAuthenticated(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        if (session.getAttribute(JOB_USER_ATTR) != null) {
            return true;
        }
        return false;
    }

    public static JobUser getJobUser(HttpServletRequest request)
    {
        if (isAuthenticated(request)) {
            Object jobUser = request.getSession().getAttribute(JOB_USER_ATTR);
            if (jobUser != null) {
                return (JobUser) jobUser;
            }
        }
        return null;
    }

    public static JobRequest getJobRequest(HttpServletRequest request)
    {
        JobRequest jobRequest = (JobRequest) request.getSession().getAttribute(JOB_REQUEST_ATTR);
        if (jobRequest != null && jobRequest.getRequestor() != null) {
            logger.debug("Getting old job request");
            return jobRequest;
        }
        else {
            logger.debug("Creating new job request");
            jobRequest = new JobRequest(getJobUser(request));
            request.getSession().setAttribute(JOB_REQUEST_ATTR, jobRequest);
            return jobRequest;
        }
    }

    public static void setJobUser(HttpServletRequest request, JobUser user)
    {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(3600);
        session.setAttribute(JOB_USER_ATTR, user);
    }

    public static void unsetJobUser(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        session.invalidate();
    }

    public static void setJobResponse(Object responseObj, HttpServletResponse response)
    {
        String json;
        try {
            json = jsonMapper.writeValueAsString(responseObj);
            response.setContentType("text/plain");
            response.setContentLength(json.length());
            response.getWriter().write(json);
        }
        catch(JsonProcessingException ex) {
            logger.error("Failed to parse job response!", ex);
        }
        catch(IOException ex) {
            logger.error("Failed to write job response", ex);
        }
    }
}
