package gov.nysenate.sage.controller.map;

import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a map view of a given district without any wrapper user interface elements.
 */
public class EmbeddedMapController extends BaseMapController
{
    private static Logger logger = Logger.getLogger(EmbeddedMapController.class);
    private static String MAPS_JSP = "/WEB-INF/views/maps.jsp";

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String path = request.getPathInfo();
        String districtType = "";
        String districtCode = "";
        Integer width = 0, height = 0;
        try {
            width = Integer.parseInt(request.getParameter("width"));
            height = Integer.parseInt(request.getParameter("height"));
        }
        catch (NumberFormatException ex) {
            logger.debug("No width and height parameters supplied.");
        }

        if (path != null) {
            path = path.replaceFirst("/", "");
            List<String> pathList = new ArrayList<String>(Arrays.asList(path.split("/")));
            if (pathList.size() >= 1) {
                districtType = pathList.get(0);
            }
            if (pathList.size() >= 2) {
                districtCode = pathList.get(1);
            }
        }

        request.setAttribute("districtType", districtType);
        request.setAttribute("districtCode", districtCode);
        request.setAttribute("width", width);
        request.setAttribute("height", height);

        request.getRequestDispatcher(MAPS_JSP).forward(request, response);
    }
}
