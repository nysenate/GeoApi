package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.meta.MetaProviderResponse;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "meta")
public class MetaController {
    private static final Logger logger = LoggerFactory.getLogger(MetaController.class);
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
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    // TODO: use this instead of geocodeServiceProvider.geocoders() in frontend
    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    public void metaProvider(HttpServletRequest request, HttpServletResponse response) {
        ApiRequest apiRequest = getApiRequest(request);
        logMetaRequest(apiRequest);
        Object metaResponse = new MetaProviderResponse(geocodeServiceProvider.geocoders());
        setApiResponse(metaResponse,request);
    }

    private void logMetaRequest(ApiRequest apiRequest) {
        logger.info("Making a meta request with request: {}", apiRequest.getRequest());
    }
}
