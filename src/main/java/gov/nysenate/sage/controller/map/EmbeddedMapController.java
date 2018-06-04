package gov.nysenate.sage.controller.map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.util.controller.ConstantUtil.MAPS_JSP;

/**
 * Provides a map view of a given district without any wrapper user interface elements.
 */
@Controller
public class EmbeddedMapController
{
    private static Logger logger = LogManager.getLogger(EmbeddedMapController.class);

    @RequestMapping(value = "/map", method = RequestMethod.GET)
    public void map(HttpServletRequest request, HttpServletResponse response,
                    @RequestParam int width, @RequestParam int height,
                    @RequestParam boolean customMapStyle, @RequestParam int saturation, @RequestParam String hue,
                    @RequestParam int lightness, @RequestParam boolean customPolyStyle,
                    @RequestParam String polyHue) throws ServletException, IOException
    {
        if ((width <= 0) || (height <= 0 )) {
            logger.debug("No width and height parameters supplied.");
            width = 0;
            height = 0;
        }
        request.setAttribute("width", width);
        request.setAttribute("height", height);

        setCustomMapStyles( request, customMapStyle, saturation, lightness, hue);

        setCustomPolygonStyles(request, customPolyStyle, polyHue);

        request.getRequestDispatcher(MAPS_JSP).forward(request, response);

    }

    @RequestMapping(value = "/map/{districtType}", method = RequestMethod.GET)
    public void mapDistrictType(HttpServletRequest request, HttpServletResponse response,
                    @RequestParam int width, @RequestParam int height,
                    @RequestParam boolean customMapStyle, @RequestParam int saturation, @RequestParam String hue,
                    @RequestParam int lightness, @RequestParam boolean customPolyStyle,
                    @RequestParam String polyHue, @PathVariable int districtType
                    ) throws ServletException, IOException {

        if ((width <= 0) || (height <= 0 )) {
            logger.debug("No width and height parameters supplied.");
            width = 0;
            height = 0;
        }
        request.setAttribute("width", width);
        request.setAttribute("height", height);

        setCustomMapStyles( request, customMapStyle, saturation, lightness, hue);

        setCustomPolygonStyles(request, customPolyStyle, polyHue);

        request.setAttribute("districtType", districtType);

        request.getRequestDispatcher(MAPS_JSP).forward(request, response);

    }

    @RequestMapping(value = "/map/{districtType}/{districtCode}", method = RequestMethod.GET)
    public void mapDistrictCode(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam int width, @RequestParam int height,
                                @RequestParam boolean customMapStyle, @RequestParam int saturation, @RequestParam String hue,
                                @RequestParam int lightness, @RequestParam boolean customPolyStyle,
                                @RequestParam String polyHue, @PathVariable int districtType,
                                @PathVariable int districtCode) throws ServletException, IOException {

        if ((width <= 0) || (height <= 0 )) {
            logger.debug("No width and height parameters supplied.");
            width = 0;
            height = 0;
        }
        request.setAttribute("width", width);
        request.setAttribute("height", height);

        setCustomMapStyles( request, customMapStyle, saturation, lightness, hue);

        setCustomPolygonStyles(request, customPolyStyle, polyHue);

        request.setAttribute("districtType", districtType);
        request.setAttribute("districtCode", districtCode);

        request.getRequestDispatcher(MAPS_JSP).forward(request, response);
    }

    private void setCustomMapStyles(HttpServletRequest request, boolean customMapStyle, int saturation, int lightness, String hue) {
        // Set custom map styles if requested
        if (customMapStyle) {
            request.setAttribute("customMapStyle", true);
            request.setAttribute("hue", "#" + hue);
            request.setAttribute("saturation", saturation);
            request.setAttribute("lightness", lightness);
        }
        else {
            request.setAttribute("customStyle", false);
        }
    }

    private void setCustomPolygonStyles(HttpServletRequest request, boolean customPolyStyle, String polyHue) {
        // Set custom polygon styles
        if (customPolyStyle) {
            request.setAttribute("customPolyStyle", true);
            request.setAttribute("polyHue", "#" + polyHue);
        }
        else {
            request.setAttribute("customPolyStyle", false);
        }
    }
}
