package gov.nysenate.sage.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseAdminController extends HttpServlet
{
    private static Logger logger = Logger.getLogger(BaseAdminController.class);
    private static ObjectMapper jsonMapper = new ObjectMapper();

    public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void init(ServletConfig config) throws ServletException;

    public static void setAdminResponse(Object responseObj, HttpServletResponse response)
    {
        String json;
        try {
            json = jsonMapper.writeValueAsString(responseObj);
            response.setContentType("application/json");
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
