package gov.nysenate.sage.controller.map;

import gov.nysenate.sage.util.FormatUtil;
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

        if (path != null) {
            path = path.replaceFirst("/", "");
            List<String> pathList = new ArrayList(Arrays.asList(path.split("/")));
            if (pathList.size() >= 1) {
                districtType = pathList.get(0);
            }
            if (pathList.size() >= 2) {
                districtCode = pathList.get(1);
            }
        }

        request.setAttribute("districtType", districtType);
        request.setAttribute("districtCode", districtCode);

        request.getRequestDispatcher("/maps.jsp").forward(request, response);
    }
}
