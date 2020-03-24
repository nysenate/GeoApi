package gov.nysenate.sage.controller.map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides a map view of a given district without any wrapper user interface elements.
 */
@Controller
public class EmbeddedMapController {
    private static Logger logger = LoggerFactory.getLogger(EmbeddedMapController.class);
    private static String MAPS_JSP = "/WEB-INF/views/maps.jsp";
    private static String COUNTY_COVID_JSP = "/WEB-INF/views/countycovid.jsp";

    /**
     * Embedded Map Api
     * ---------------------
     *
     * Returns an embedded google map with the specified request params
     *
     * Usage:
     * (GET)    /map
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param width int
     * @param height int
     * @param customMapStyle boolean
     * @param saturation int
     * @param customPolyStyle boolean
     * @param hue String
     * @param lightness int
     * @param polyHue String
     * @throws ServletException
     * @throws IOException
     *
     */
    @RequestMapping(value = "/map", method = RequestMethod.GET)
    public void map(HttpServletRequest request, HttpServletResponse response,
                    @RequestParam(required = false, defaultValue = "0") int width,
                    @RequestParam(required = false, defaultValue = "0") int height,
                    @RequestParam(required = false, defaultValue = "false") boolean customMapStyle,
                    @RequestParam(required = false, defaultValue = "0") int saturation,
                    @RequestParam(required = false) String hue,
                    @RequestParam(required = false, defaultValue = "0") int lightness,
                    @RequestParam(required = false, defaultValue = "false") boolean customPolyStyle,
                    @RequestParam(required = false) String polyHue)
            throws ServletException, IOException {

        setCommonRequestAttributes(request, width, height, customMapStyle, saturation, hue, lightness,
                customPolyStyle, polyHue);

        request.getRequestDispatcher(MAPS_JSP).forward(request, response);

    }

    /**
     * Embedded District Type Map Api
     * ------------------------------
     *
     * Returns an embedded google map with the specified district type and request params
     *
     * Usage:
     * (GET)    /map/{districtType}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param width int
     * @param height int
     * @param customMapStyle boolean
     * @param saturation int
     * @param customPolyStyle boolean
     * @param hue String
     * @param lightness int
     * @param polyHue String
     * @throws ServletException
     * @throws IOException
     *
     */
    @RequestMapping(value = "/map/{districtType}", method = RequestMethod.GET)
    public void mapDistrictType(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "0") int width,
                                @RequestParam(required = false, defaultValue = "0") int height,
                                @RequestParam(required = false, defaultValue = "false") boolean customMapStyle,
                                @RequestParam(required = false, defaultValue = "0") int saturation,
                                @RequestParam(required = false) String hue,
                                @RequestParam(required = false, defaultValue = "0") int lightness,
                                @RequestParam(required = false, defaultValue = "false") boolean customPolyStyle,
                                @RequestParam(required = false) String polyHue,
                                @PathVariable String districtType
    ) throws ServletException, IOException {

        setCommonRequestAttributes(request, width, height, customMapStyle, saturation, hue, lightness,
                customPolyStyle, polyHue);

        request.setAttribute("districtType", districtType);

        if (districtType.toLowerCase().equals("county")) {
            request.getRequestDispatcher(COUNTY_COVID_JSP).forward(request, response);
        }
        else {
            request.getRequestDispatcher(MAPS_JSP).forward(request, response);
        }

    }

    /**
     * Embedded District Type, Code Map Api
     * ------------------------------
     *
     * Returns an embedded google map with the specified district type, district code and request params
     *
     * Usage:
     * (GET)    /map/{districtType}/{districtCode}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param width int
     * @param height int
     * @param customMapStyle boolean
     * @param saturation int
     * @param customPolyStyle boolean
     * @param hue String
     * @param lightness int
     * @param polyHue String
     * @throws ServletException
     * @throws IOException
     *
     */
    @RequestMapping(value = "/map/{districtType}/{districtCode}", method = RequestMethod.GET)
    public void mapDistrictCode(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "0") int width,
                                @RequestParam(required = false, defaultValue = "0") int height,
                                @RequestParam(required = false, defaultValue = "false") boolean customMapStyle,
                                @RequestParam(required = false, defaultValue = "0") int saturation,
                                @RequestParam(required = false) String hue,
                                @RequestParam(required = false, defaultValue = "0") int lightness,
                                @RequestParam(required = false, defaultValue = "false") boolean customPolyStyle,
                                @RequestParam(required = false) String polyHue,
                                @PathVariable String districtType,
                                @PathVariable int districtCode)
            throws ServletException, IOException {

        setCommonRequestAttributes(request, width, height, customMapStyle, saturation, hue, lightness,
                customPolyStyle, polyHue);

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
        } else {
            request.setAttribute("customStyle", false);
        }
    }

    private void setCustomPolygonStyles(HttpServletRequest request, boolean customPolyStyle, String polyHue) {
        // Set custom polygon styles
        if (customPolyStyle) {
            request.setAttribute("customPolyStyle", true);
            request.setAttribute("polyHue", "#" + polyHue);
        } else {
            request.setAttribute("customPolyStyle", false);
        }
    }

    private void setCommonRequestAttributes(HttpServletRequest request, int width, int height, boolean customMapStyle, int saturation, String hue,
                                            int lightness, boolean customPolyStyle, String polyHue) {
        if ((width <= 0) || (height <= 0)) {
            logger.debug("No width and height parameters supplied.");
            width = 0;
            height = 0;
        }
        request.setAttribute("width", width);
        request.setAttribute("height", height);

        setCustomMapStyles(request, customMapStyle, saturation, lightness, hue);

        setCustomPolygonStyles(request, customPolyStyle, polyHue);
    }
}
