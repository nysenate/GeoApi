package gov.nysenate.sage.util.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.model.job.JobRequest;
import gov.nysenate.sage.model.job.JobUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ConstantUtil.JOB_REQUEST_ATTR;
import static gov.nysenate.sage.util.controller.ConstantUtil.JOB_USER_ATTR;

public final class JobControllerUtil {
    private static final Logger logger = LoggerFactory.getLogger(JobControllerUtil.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private JobControllerUtil() {}

    public static boolean isAuthenticated(HttpServletRequest request) {
        return request.getSession().getAttribute(JOB_USER_ATTR) != null;
    }

    public static JobUser getJobUser(HttpServletRequest request) {
        if (isAuthenticated(request)) {
            Object jobUser = request.getSession().getAttribute(JOB_USER_ATTR);
            if (jobUser != null) {
                return (JobUser) jobUser;
            }
        }
        return null;
    }

    public static JobRequest getJobRequest(HttpServletRequest request) {
        JobRequest jobRequest = (JobRequest) request.getSession().getAttribute(JOB_REQUEST_ATTR);
        if (jobRequest != null && jobRequest.getRequestor() != null) {
            logger.debug("Getting old job request");
        }
        else {
            logger.debug("Creating new job request");
            jobRequest = new JobRequest(getJobUser(request));
            request.getSession().setAttribute(JOB_REQUEST_ATTR, jobRequest);
        }
        return jobRequest;
    }

    public static void setJobUser(HttpServletRequest request, JobUser user) {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(3600);
        session.setAttribute(JOB_USER_ATTR, user);
    }

    public static void setJobResponse(Object responseObj, HttpServletResponse response) {
        try {
            String json = jsonMapper.writeValueAsString(responseObj);
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
