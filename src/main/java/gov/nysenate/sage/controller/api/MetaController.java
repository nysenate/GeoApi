package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.meta.MetaProviderResponse;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "meta")
public class MetaController {
    private final SageGeocodeServiceProvider geocodeServiceProvider;

    @Autowired
    public MetaController(SageGeocodeServiceProvider geocodeServiceProvider) {
        this.geocodeServiceProvider = geocodeServiceProvider;
    }

    /**
     * Metadata Api
     * ---------------------------
     * returns a map containing the current active geocoders
     * Usage:
     * (GET)    /api/v2/meta/provider
     */
    // TODO: use this instead of geocodeServiceProvider.geocoders() in frontend
    @GetMapping(value = "/provider")
    public MetaProviderResponse metaProvider() {
        return new MetaProviderResponse(geocodeServiceProvider.geocoders());
    }
}
