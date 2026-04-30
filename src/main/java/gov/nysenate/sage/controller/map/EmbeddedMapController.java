package gov.nysenate.sage.controller.map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides a map view of a given district without any wrapper user interface elements.
 */
@Controller
public class EmbeddedMapController {
    private static final String MAPS_JSP = "/WEB-INF/views/maps.jsp";
    private static final String COUNTY_COVID_JSP = "/WEB-INF/views/countydoh.jsp";

    /**
     * Embedded Map Api
     * ---------------------
     * Returns an embedded Google map with the specified request params
     * Usage:
     * (GET)    /map
     *
     */
    @GetMapping(value = "/map")
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
     * Returns an embedded Google map with the specified district type and request params
     * Usage:
     * (GET)    /map/{districtType}
     */
    @GetMapping(value = "/map/{districtType}")
    public void mapDistrictType(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false, defaultValue = "false") boolean doh,
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

        if (districtType.equalsIgnoreCase("county") && doh) {
            request.getRequestDispatcher(COUNTY_COVID_JSP).forward(request, response);
        }
        else {
            request.getRequestDispatcher(MAPS_JSP).forward(request, response);
        }

    }

    /**
     * Embedded District Type, Code Map Api
     * ------------------------------
     * Returns an embedded Google map with the specified district type, district code and request params
     * Usage:
     * (GET)    /map/{districtType}/{districtCode}
     *
     */
    @GetMapping(value = "/map/{districtType}/{districtCode}")
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

    private void setCommonRequestAttributes(HttpServletRequest request, int width, int height, boolean customMapStyle,
                                            int saturation, String hue, int lightness, boolean customPolyStyle,
                                            String polyHue) {
        if (width <= 0 || height <= 0) {
            width = 0;
            height = 0;
        }
        request.setAttribute("width", width);
        request.setAttribute("height", height);

        if (customMapStyle) {
            request.setAttribute("customMapStyle", true);
            request.setAttribute("hue", "#" + hue);
            request.setAttribute("saturation", saturation);
            request.setAttribute("lightness", lightness);
        } else {
            request.setAttribute("customStyle", false);
        }

        if (customPolyStyle) {
            request.setAttribute("customPolyStyle", true);
            request.setAttribute("polyHue", "#" + polyHue);
        } else {
            request.setAttribute("customPolyStyle", false);
        }
    }
}
