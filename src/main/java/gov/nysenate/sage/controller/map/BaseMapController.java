package gov.nysenate.sage.controller.map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The base API controller provides various common methods to the map controllers.
 */
public abstract class BaseMapController extends HttpServlet
{
    private static Logger logger = LoggerFactory.getLogger(BaseMapController.class);
    public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    public abstract void init(ServletConfig config) throws ServletException;
}
