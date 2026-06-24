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

    /**
     * Embedded Map Api
     * ---------------------
     * Returns an embedded Google map with the specified request params
     * Usage:
     * (GET)    /map
     *
     */
    @GetMapping(value = "/map")
    public void map(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("doh", false);
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
                                @PathVariable String districtType) throws ServletException, IOException {
        request.setAttribute("districtType", districtType);
        request.setAttribute("doh", districtType.equalsIgnoreCase("county") && doh);
        request.getRequestDispatcher(MAPS_JSP).forward(request, response);
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
                                @PathVariable String districtType, @PathVariable int districtCode)
            throws ServletException, IOException {
        request.setAttribute("districtType", districtType);
        request.setAttribute("districtCode", districtCode);
        request.setAttribute("doh", false);
        request.getRequestDispatcher(MAPS_JSP).forward(request, response);
    }
}
