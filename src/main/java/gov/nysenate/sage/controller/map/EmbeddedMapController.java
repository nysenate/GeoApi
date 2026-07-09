package gov.nysenate.sage.controller.map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Provides a map view of a given district without any wrapper user interface elements.
 * Unlike the pages routed by ReactAppCtrl, embedded maps are public: they are iframed
 * on external sites (e.g. senator pages), so there is no whitelist check here.
 */
@Controller
public class EmbeddedMapController {
    /**
     * Embedded Map Api
     * ---------------------
     * Returns the React app, which renders an embedded Google map of the district(s)
     * identified by the request path (and shows county DoH info when doh=true).
     * Usage:
     * (GET)    /map
     * (GET)    /map/{districtType}
     * (GET)    /map/{districtType}/{districtCode}
     */
    @GetMapping({"/map", "/map/{districtType}", "/map/{districtType}/{districtCode}"})
    public String map() {
        return "forward:/static/dist/index.html";
    }
}
